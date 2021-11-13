package mid;

import SymTable.SymItem;
import SymTable.SymbolTable;
import SymTable.Var;
import front.ASD.ASDNode;
import front.ASD.ConstInitVal;
import front.ASD.InitVal;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

public class MidCodeList {
    public ArrayList<MidCode> midCodes;
    public int tmpIndex;
    public int label_cnt;
    public ArrayList<String> strCons;
    public int[] block_location;
    public final int[] block_num = new int[100];
    public HashMap<ASDNode, SymItem> node2symItem;

    public final Stack<String> begin_tables = new Stack<>();
    public final Stack<String> end_tables = new Stack<>();

    public MidCodeList(HashMap<ASDNode, SymItem> node2symItem) {
        this.node2symItem = node2symItem;
        this.midCodes = new ArrayList<>();
        this.tmpIndex = 0;
        this.strCons = new ArrayList<>();
        this.label_cnt = 0;
        block_location = new int[]{0, 0};
        for (int i = 0; i < 100; i += 1) {
            block_num[i] = 0;
        }
    }

    public String add(MidCode.Op instr, String operand1, String operand2, String r) {
        if (instr.equals(MidCode.Op.WHILE_BIND)) {
            begin_tables.push(operand1);
            end_tables.push(operand2);
            return null;
        }
        if (instr.equals(MidCode.Op.PUSH_PARA) && midCodes.get(midCodes.size()-1).instr.equals(MidCode.Op.SIGNAL_ARR_ADDR)) {
            midCodes.remove(midCodes.size()-1);
            instr = MidCode.Op.PUSH_PARA_ARR;
        } else if (!instr.equals(MidCode.Op.PRINT)) {
            if (operand2.contains("[") && !instr.equals(MidCode.Op.ARR_LOAD)) { //array var must be load into temple var.
                operand2 = this.add(MidCode.Op.ARR_LOAD, "#AUTO", operand2, "#VACANT");
            }
            if (operand1.contains("[") && !instr.equals(MidCode.Op.ARR_SAVE)) {
                if (instr.equals(MidCode.Op.JUMP_IF)) {
                    String op1 = operand1.split(" ")[0];
                    String op2 = operand1.split(" ")[1];
                    if (op1.contains("[")) {
                        op1 = this.add(MidCode.Op.ARR_LOAD, "#AUTO", op1, "#VACANT");
                    }
                    if (op2.contains("[")) {
                        op2 = this.add(MidCode.Op.ARR_LOAD, "#AUTO", op2, "#VACANT");
                    }
                    operand1 = op1 + " " + op2;
                }
                else {
                    operand1 = this.add(MidCode.Op.ASSIGN, "#AUTO", operand1, "#VACANT");
                    if (instr.equals(MidCode.Op.ASSIGN) || instr.equals(MidCode.Op.GETINT)) {
                        instr = MidCode.Op.ARR_SAVE;
                    }
                }
            }
        }
        else {
            if (operand1.contains("[") && !operand2.equals("#STRCONS")) { //array var must be load into temple var.
                operand1 = this.add(MidCode.Op.ARR_LOAD, "#AUTO", operand1, "#VACANT");
            }
        }
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
            result = "#T" + tmpIndex;
            tmpIndex += 1;
        }
        if (result.equals("#AUTO_LABEL")) {
            result = "label_" + label_cnt;
            label_cnt += 1;
        }
        if (operand1.equals("#AUTO")) {
            operand1 = "#T" + tmpIndex;
            result = operand1;
            tmpIndex += 1;
        }
        if (operand2.equals("#STRCONS")) {
            strCons.add(operand1);
            operand2 = "#VACANT";
            operand1 = "#str" + Integer.toString(strCons.size() - 1);
        }
        midCodes.add(new MidCode(instr, operand1, operand2, result));
        if (result.equals("#T14")) {
            System.out.println("aa");
        }
        return result;
    }

    public void printCode() {
        PrintStream out = System.out;
        try {
            PrintStream os = new PrintStream("19375341_孙泽一_优化前中间代码.txt");
            System.setOut(os);
        } catch (IOException ignored) {
        }
        int i = 0;
        for (String strCon: strCons) {
            System.out.println("str" + i + ": " +  "\""+ strCon + "\"");
            i += 1;
        }
        for (MidCode midCode: midCodes) {
            System.out.println(midCode.instr + " " + midCode.toString());
        }
        System.setOut(out);
    }

    public void addTmp(HashMap<String, ArrayList<SymItem>> funcTable, SymbolTable global_table) {
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
                Var var = new Var(midCode.result, false, (InitVal) null, 0,  new ArrayList<Integer>(), "#inFunc");
                end_addr = var.set_addr(end_addr);
                if (currentFuncTable != null) {
                    currentFuncTable.add(var);
                } else {
                    global_table.symItems.add(var);
                }
            }
        }
    }

    public String alloc_label() {
        String result = "label_" + label_cnt;
        label_cnt += 1;
        return result;
    }
}
