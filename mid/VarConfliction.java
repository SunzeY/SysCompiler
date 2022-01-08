package mid;

import com.sun.org.apache.bcel.internal.generic.NEW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class VarConfliction {
    
    public static final int LOOP_FACTOR = 30;
    public static final HashMap<String, HashSet<String>> funcVars = new HashMap<>();
    public static final HashMap<String, Integer> var_weight = new HashMap<>();
    public static final HashMap<Integer, Block> d_index2Block = new HashMap<>();
    public static final HashMap<MidCode, Integer> midCode2Bno = new HashMap<>();

    public static final ArrayList<Block> blocks = DataFlower.blocks;

    public static final HashMap<String, HashSet<Integer>> var_define_points = DataFlower.var_define_points;

    public static final HashMap<Integer, HashSet<String>> block_used_vars = new HashMap<Integer, HashSet<String>>(){{
        for (Block block: blocks) {
            HashSet<String> used_vars = new HashSet<>();
            for (MidCode midCode: block.midCodes) {
                used_vars.addAll(midCode.get_use());
                if (midCode.d_index != null) {
                    d_index2Block.put(midCode.d_index, block);
                }
                midCode2Bno.put(midCode, block.bno);
            }
            put(block.bno, used_vars);
        }
    }};

    public static void get_func_vars(MidCodeList midCodeList) {
        int weight = 1;
        String currentFuncName = null;
        HashSet<String> currentFuncVars = new HashSet<>();
        for (MidCode midCode: midCodeList.midCodes) {
            if (midCode.instr == MidCode.Op.ENTER_WHILE) {
                weight = weight * LOOP_FACTOR;
            } else if (midCode.instr == MidCode.Op.EXIT_WHILE) {
                weight = weight / LOOP_FACTOR;
            } else if (midCode.instr == MidCode.Op.FUNC) {
                currentFuncName = midCode.operand2;
                currentFuncVars = new HashSet<>();
            } else if (midCode.instr == MidCode.Op.END_FUNC) {
                currentFuncVars.removeIf(x->x.startsWith("#T"));
                funcVars.put(currentFuncName, currentFuncVars);
            }
            if (midCode.get_def() != null) {
                currentFuncVars.add(midCode.get_def());
                if (var_weight.containsKey(midCode.get_def())) {
                    var_weight.replace(midCode.get_def(), var_weight.get(midCode.get_def()) + weight);
                } else {
                    var_weight.put(midCode.get_def(), weight);
                }
            }
            for (String use_var: midCode.get_use()) {
                currentFuncVars.add(use_var);
                if (var_weight.containsKey(use_var)) {
                    var_weight.replace(use_var, var_weight.get(use_var) + weight);
                } else {
                    var_weight.put(use_var, weight);
                }
            }
        }
    }

    public static HashSet<Integer> use_chain(Integer d_index, String VarName) {
        HashSet<Integer> use_chain = new HashSet<>();
        use_chain.add(d_index2Block.get(d_index).bno);
        for (Block block: blocks) {
            if (block.in_arrive.contains(d_index) && block_used_vars.get(block.bno).contains(VarName)) {
                use_chain.add(block.bno);
            }
        }
        return use_chain;
    }

    public static void gen_conflict_() {
        for (Map.Entry<String, HashSet<String>> pair: funcVars.entrySet()) {
            for (String var_name: pair.getValue()) {
                ArrayList<HashSet<Integer>> use_chains = new ArrayList<>();
                for (Integer d_index: var_define_points.get(var_name)) {
                    use_chains.add(use_chain(d_index, var_name));
                }
                HashSet<HashSet<Integer>> net = new HashSet<>();
                for (int i = 0; i < use_chains.size(); i++) {
                    HashSet<Integer> use_chain = use_chains.get(i);
                    if (i == 0) {
                        net.add(use_chain);
                    }
                    for (HashSet<Integer> net_sub_chain: net) {
                        if (have_common(net_sub_chain, use_chain)) {
                            net_sub_chain.addAll(use_chain);
                        }
                    }
                }
            }
        }
    }

    public static boolean have_common(HashSet<Integer> s1, HashSet<Integer> s2) {
        for (Integer i: s1) {
            if (s2.contains(i)) {
                return true;
            }
        }
        return false;
    }
}
