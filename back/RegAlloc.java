package back;

import SymTable.SymItem;

import javax.print.attribute.HashAttributeSet;
import java.util.ArrayList;
import java.util.HashMap;

public class RegAlloc {
    public static final Integer TMP_VAR_INIT_CNT = 0;
    public static final Integer VAR_INIT_CNT = 1;
    public ArrayList<String> regs = new ArrayList<String>(){{
        for (int i=0; i<10; i+=1) {
            add("$t" + i);
        }
        for (int i=0; i<9; i+=1) {
            add("$s" + i);
        }
    }};

    public int[] ref_cnt = new int[regs.size()];

    public HashMap<String, String> indent2reg = new HashMap<>();
    public HashMap<String, String> reg2Indent = new HashMap<>();

    public MipsGenerator generator;

    public int pointer = 0;

    private RegAlloc(MipsGenerator generator) {
        this.generator = generator;
        for (int i=0; i<regs.size(); i+=1) {
            ref_cnt[i] = 0;
        }
    }

    public String find(String indent) {
        if (indent2reg.containsKey(indent)) { // already cached
            return indent2reg.get(indent);
            // TODO: modify pointer
        }
        int i = 0;
        while(true) {
            if (ref_cnt[(i + pointer) % regs.size()] != 0) {
                ref_cnt[(i + pointer) % regs.size()] -= 1;
                i += 1;
            } else {
                // write back
                String allocated_reg = regs.get((i + pointer) % regs.size());
                String pre_indent = reg2Indent.get(allocated_reg);
                allocReg(pre_indent, (i+pointer)%regs.size(), false);

                // fill new
                indent2reg.put(indent, allocated_reg);
                reg2Indent.put(allocated_reg, indent);
                allocReg(indent, (i+pointer)%regs.size(), true);

                // modify ref_cnt
                if (indent.charAt(0) == '#') {
                    ref_cnt[(i + pointer) % regs.size()] = TMP_VAR_INIT_CNT;
                } else {
                    ref_cnt[(i + pointer) % regs.size()] = VAR_INIT_CNT;
                }
                return allocated_reg;
            }
        }
    }


    public void allocReg(String indent, int index, boolean fill) {
        SymItem curItem = null;
        String reg = regs.get(index);
        if (!generator.currentFunc.equals("")) {
            for (SymItem item : generator.funcTables.get(generator.currentFunc)) {
                if (item.getUniqueName().equals(indent)) {
                    curItem = item;
                    break;
                }
            }
        }
        if (curItem == null) {
            for (SymItem item : generator.globalTable.symItems) {
                if (item.getUniqueName().equals(indent)) {
                    generator.generate((fill ? "lw " : "sw ") + reg + ", " + (item.getAddr() - generator.globalsize) + "($gp)");
                    return;
                }
            }
        }
        assert curItem != null;
        generator.generate((fill ? "lw " : "sw ") + reg + ", " + (generator.call_func_sp_offset + curItem.getAddr()) + "($sp)");
    }


}
