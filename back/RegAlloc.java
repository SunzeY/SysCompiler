package back;

import SymTable.SymItem;
import mid.Block;
import mid.MidCode;
import mid.VarConfliction;

import javax.print.attribute.HashAttributeSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RegAlloc {
    public static final HashMap<String, HashSet<String>> funcVars = VarConfliction.funcVars;
    public static final HashMap<String, Integer> var_weight = VarConfliction.var_weight;
    public static final HashMap<String, ArrayList<String>> funcVarByWeight = new HashMap<String, ArrayList<String>>() {
        private int compare(String x1, String x2) {
            return (var_weight.get(x1) - var_weight.get(x2));
        }

        {
        for (Map.Entry<String, HashSet<String>> funcVar: funcVars.entrySet()) {
            ArrayList<String> vars = new ArrayList<>(funcVar.getValue());
            vars.sort(this::compare);
            put(funcVar.getKey(), vars);
        }
    }};

    public static final ArrayList<Block> blocks = VarConfliction.blocks;

    public static final HashMap<MidCode, Integer> midCode2Bno = VarConfliction.midCode2Bno;

    public HashMap<Integer, Block> bno2block = new HashMap<Integer, Block>(){{
        for (Block block: VarConfliction.blocks) {
            bno2block.put(block.bno, block);
        }
    }};


    public static ArrayList<String> reg_pool = new ArrayList<String>(){{
        for (int i = 0; i < 8; i++) {
            add("$s" + i);
        }
        add("$fp");
    }};

    public static String alloc_reg(String VarName, String funcName) {
        ArrayList<String> funcTable = funcVarByWeight.get(funcName);
        int i = funcTable.indexOf(VarName);
        if (i > 8) {
            return null;
        }
        return reg_pool.get(i);
    }

    public static final HashMap<MidCode, HashSet<String>> alive_vars = new HashMap<MidCode, HashSet<String>>(){{
        for (Block block: blocks) {
            for (int i = 0; i < block.midCodes.size(); i++) {
                HashSet<String> alive_var = new HashSet<>(block.out);
                alive_var.addAll(block.in);
                for (int j = i + 1; j < block.midCodes.size(); j++) {
                    alive_var.addAll(block.midCodes.get(j).get_use());
                }
                put(block.midCodes.get(i), alive_var);
            }
        }
    }};

    public static void try_release_s_reg(ArrayList<String> sRegTable, MidCode midCode, ArrayList<String> mipsCodes) {
        for (String s_reg: sRegTable) {
            if (s_reg.equals("#VACANT")) {
                return;
            }
        }
        if (alive_vars.containsKey(midCode)) {
            for (int i = 0; i < sRegTable.size(); i++) {
                String var = sRegTable.get(i);
                if (midCode.get_use().contains(sRegTable.get(i)) || (midCode.get_def() != null) && midCode.get_def().equals(sRegTable.get(i))) {
                    continue;
                }
                if (!var.startsWith("#") && var_weight.containsKey(var) && var_weight.get(var) < 30 && !alive_vars.get(midCode).contains(var)) {
                    mipsCodes.add("# release s_reg["+i+"] bind " + sRegTable.get(i));
                    sRegTable.set(i, "#VACANT");
                    return;
                }
            }
        }
    }
}
