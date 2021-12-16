package mid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class Block {
    public int bno;
    public ArrayList<MidCode> midCodes;
    public ArrayList<Integer> precursors = new ArrayList<>();
    public ArrayList<Integer> successors = new ArrayList<>();
    public HashSet<String> use;
    public HashSet<String> def;
    public HashSet<String> in = new HashSet<>();
    public HashSet<String> out = new HashSet<>();
    public HashSet<Integer> gen;
    public HashSet<Integer> kill;
    public HashSet<Integer> in_arrive = new HashSet<>();
    public HashSet<Integer> out_arrive = new HashSet<>();


    public Block(int bno, ArrayList<MidCode> midCodes) {
        this.bno = bno;
        this.midCodes = midCodes;
        compute_use_def();
        compute_gen_kill();
    }

    private void compute_use_def() {
        use = new HashSet<>();
        def = new HashSet<>();
        for (MidCode midCode : midCodes) {
            String cur_def = midCode.get_def();
            ArrayList<String> cur_use = midCode.get_use();
            for (String use : cur_use) {
                if (!def.contains(use) && use != null) {
                    this.use.add(use);
                }
            }
            if (!use.contains(cur_def) && cur_def != null) {
                def.add(cur_def);
            }
        }
    }

    public void compute_gen_kill() {
        kill = new HashSet<>();
        gen = new HashSet<>();
        for (int i = midCodes.size() - 1; i >= 0; i--) {
            MidCode midCode = midCodes.get(i);
            if (midCode.d_index != null) {
                HashSet<Integer> tmp = new HashSet<>(midCode.get_gen());
                tmp.removeAll(kill);
                gen.addAll(tmp);
                kill.addAll(midCode.get_kill());
            }
        }
    }

    public HashSet<String> get_use() {
        return this.use;
    }

    public HashSet<String> get_def() {
        return this.def;
    }

    public void remove_redundant_code() {
        HashSet<String> un_defined_var = new HashSet<>(out);
        for (int i = midCodes.size() - 1; i >= 0; i--) {
            MidCode cur_code = midCodes.get(i);
            if (cur_code.get_def() != null && !un_defined_var.contains(cur_code.get_def())) {
                System.out.println("========remove_redundant_code===========");
                System.out.println(cur_code.toString());
                System.out.println("========================================");
                if (cur_code.instr == MidCode.Op.GETINT) {
                    cur_code.instr = MidCode.Op.EMPTY_INPUT;
                } else {
                    cur_code.instr = MidCode.Op.EMPTY;
                }
            } else {
                if (cur_code.get_def() != null) {
                    un_defined_var.remove(cur_code.get_def());
                }
                un_defined_var.addAll(cur_code.get_use());
            }
        }
    }

    public void const_broad_cast() {
        HashMap<String, String> const_val_table = new HashMap<>();
        for (MidCode cur_code : midCodes) {
            boolean can_broadcast = false;
            HashMap<String, String> var2const = new HashMap<>();
            for (String use_var : cur_code.get_use()) {
                if (const_val_table.containsKey(use_var)) {
                    System.out.println("===============const_broadcast=============");
                    System.out.println(cur_code + "   " + use_var + "->" + const_val_table.get(use_var));
                    System.out.println("===========================================");
                    can_broadcast = true;
                    var2const.put(use_var, const_val_table.get(use_var));
                }
            }
            if (can_broadcast) {
                cur_code.refact(var2const);
            }
            String def_var = cur_code.get_def();
            if (def_var != null && (cur_code.instr == MidCode.Op.VAR_DEF || cur_code.instr == MidCode.Op.ASSIGN) && begins_num(cur_code.operand2)) {
                const_val_table.put(def_var, cur_code.operand2);
            } else if (def_var != null) {
                const_val_table.remove(def_var);
            }
        }
    }

    public void const_broad_cast_by_arrival() {
        for (MidCode cur_code : midCodes) {
            HashMap<String, String> name2value = new HashMap<>();
            for (String use_var : cur_code.get_use()) {
                ArrayList<Integer> define_point = new ArrayList<>();
                for (Integer def_index : in_arrive) {
                    if (def_index.equals(5)) {
                        System.out.println(DataFlower.d_index2code);
                    }
                    if (DataFlower.d_index2code.get(def_index).get_def() != null && DataFlower.d_index2code.get(def_index).get_def().equals(use_var)) {
                        define_point.add(def_index);
                    }
                }
                if (define_point.size() == 1) { // only when define point - (only across block)
                    HashSet<String> block_in_def = new HashSet<>();
                    boolean define_in_block = false;
                    for (MidCode cur_bloc_code : midCodes) {
                        if (cur_bloc_code.equals(cur_code)) {
                            if (block_in_def.contains(use_var)) {
                                define_in_block = true;
                            }
                            break;
                        } else {
                            if (cur_bloc_code.d_index != null) {
                                block_in_def.add(cur_bloc_code.get_def());
                            }
                        }
                    }
                    if (!define_in_block) {
                        MidCode define_code = DataFlower.d_index2code.get(define_point.get(0));
                        System.out.println(define_code);
                        if ((define_code.instr == MidCode.Op.VAR_DEF || define_code.instr == MidCode.Op.ASSIGN) && begins_num(define_code.operand2)) {
                            name2value.put(use_var, define_code.operand2);
                            System.out.println("===============const_broadcast_across_base_block=============");
                            System.out.println(cur_code + "   " + use_var + "->" + define_code.operand2);
                            System.out.println("===========================================");
                        }
                    }
                }
            }
            if (!name2value.isEmpty()) {
                cur_code.refact(name2value);
            }
        }
    }

    public static final Pattern IS_DIGIT = Pattern.compile("[0-9]*");

    public static boolean begins_num(String operand) {
        return IS_DIGIT.matcher(operand).matches() || operand.charAt(0) == '+' || operand.charAt(0) == '-';
    }
}
