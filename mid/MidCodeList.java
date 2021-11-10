package mid;

import SymTable.SymItem;
import SymTable.Var;
import front.ASD.ASDNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MidCodeList {
    public ArrayList<MidCode> midCodes;
    public int tmpIndex;
    public ArrayList<String> strCons;
    public int[] block_location;
    public final int[] block_num = new int[100];
    public HashMap<ASDNode, SymItem> node2symItem;

    public MidCodeList(HashMap<ASDNode, SymItem> node2symItem) {
        this.node2symItem = node2symItem;
        this.midCodes = new ArrayList<>();
        this.tmpIndex = 0;
        this.strCons = new ArrayList<>();
        block_location = new int[]{0, 0};
        for (int i = 0; i < 100; i += 1) {
            block_num[i] = 0;
        }
    }

    public String add(MidCode.Op instr, String operand1, String operand2, String r) {
        if (instr.equals(MidCode.Op.NEW_BLOCK)) {
            block_location[0] += 1;
            block_num[block_location[0]] += 1;
            block_location[1] = block_num[block_location[0]] - 1;
            operand1 = Arrays.toString(block_location);
        } else if (instr.equals((MidCode.Op.EXIT_BLOCK))) {
            operand1 = Arrays.toString(block_location);
            block_location[0] = block_location[0] - 1;
            block_location[1] = block_num[block_location[0]] - 1;
        }
        String result = r;
        if (result.equals("#AUTO")) {
            result = "#T" + Integer.toString(tmpIndex);
            tmpIndex += 1;
        }
        if (operand2.equals("#STRCONS")) {
            strCons.add(operand1);
            operand2 = "#VACANT";
            operand1 = "#str" + Integer.toString(strCons.size() - 1);
        }
        midCodes.add(new MidCode(instr, operand1, operand2, result));
        return result;
    }

    public void printCode() {
        int i = 0;
        for (String strCon: strCons) {
            System.out.println("str" + i + ": " +  "\""+ strCon + "\"");
            i += 1;
        }
        for (MidCode midCode: midCodes) {
            System.out.println(midCode.toString());
        }
    }

    public void addTmp(HashMap<String, ArrayList<SymItem>> funcTable) {
        String name = "";
        int end_addr = 0;
        ArrayList<SymItem> currentFuncTable = null;
        for (MidCode midCode: midCodes) {
            if (midCode.instr.equals(MidCode.Op.FUNC)) {
                name = midCode.operand2;
                currentFuncTable = funcTable.get(name);
                if (currentFuncTable.isEmpty()) {
                    end_addr = 0;
                } else {
                    end_addr = currentFuncTable.get(currentFuncTable.size() - 1).getAddr();
                }
            } else if (midCode.instr.equals(MidCode.Op.END_FUNC)) {
                name = "";
            } else if (midCode.result.charAt(0) == '#' && midCode.result.charAt(1) == 'T') {
                Var var = new Var(midCode.result, false, null, 0,  new ArrayList<Integer>(), "#inFunc");
                end_addr = var.set_addr(end_addr);
                assert currentFuncTable != null;
                currentFuncTable.add(var);
            }
        }
    }
}
