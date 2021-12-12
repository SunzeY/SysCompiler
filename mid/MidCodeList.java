package mid;

import SymTable.SymItem;
import SymTable.SymbolTable;
import SymTable.Var;
import front.ASD.ASDNode;
import front.ASD.ConstInitVal;
import front.ASD.InitVal;
import sun.rmi.transport.proxy.RMIHttpToPortSocketFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Pattern;

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
                else if (instr.equals(MidCode.Op.ASSIGN) || instr.equals(MidCode.Op.GETINT)){
                    if (instr.equals(MidCode.Op.ASSIGN)) {
                        operand2 = this.add(MidCode.Op.ASSIGN, "#AUTO", operand1, "#VACANT");
                    } else {
                        operand2 = this.add(MidCode.Op.GETINT, "#AUTO", "#VACANT", "#VACANT");
                    }
                    instr = MidCode.Op.ARR_SAVE;
                } else {
                    operand1 = this.add(MidCode.Op.ARR_LOAD, "#AUTO", operand1, "#VACANT");
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
        return result;
    }

    public void printCode(String fileName) {
        PrintStream out = System.out;
        try {
            PrintStream os = new PrintStream(fileName);
            System.setOut(os);
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        int i = 0;
        for (String strCon: strCons) {
            System.out.println("str" + i + ": " +  "\""+ strCon + "\"");
            i += 1;
        }
        for (MidCode midCode: midCodes) {
            System.out.println(midCode.toString());
        }
        System.setOut(out);
    }

    public void addTmp(HashMap<String, ArrayList<SymItem>> funcTable, SymbolTable global_table) {
        String name = "";
        int end_addr = 4;
        ArrayList<SymItem> currentFuncTable = null;
        for (MidCode midCode: midCodes) {
            if (midCode.instr.equals(MidCode.Op.FUNC)) {
                name = midCode.operand2;
                currentFuncTable = funcTable.get(name);
                if (currentFuncTable.isEmpty()) {
                    end_addr = 4;
                } else {
                    end_addr = currentFuncTable.get(currentFuncTable.size() - 1).getAddr() + currentFuncTable.get(currentFuncTable.size() - 1).getSize();
                    if (end_addr == 0) { // special debug--funcFormVar[-1][...]
                        end_addr = 4;
                    }
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

    public void remove_redundant_assign() {
        ArrayList<MidCode> new_midCodes = new ArrayList<>();
        for (int i = 0; i < midCodes.size()-1; i+=1) {
            MidCode c1 = midCodes.get(i);
            MidCode c2 = midCodes.get(i+1);
            if (c2.instr.equals(MidCode.Op.ASSIGN)  &&
                    c1.result.charAt(0) == '#' && c1.result.equals(c2.operand2) && MidCode.is_arith(c1.instr)) {
                new_midCodes.add(new MidCode(c1.instr, c1.operand1, c1.operand2, c2.operand1));
                i += 1;
            } else if (c1.instr == MidCode.Op.ASSIGN && c1.operand1.charAt(0) == '#' && c1.operand1.equals(c2.operand1)
                    && MidCode.is_arith(c2.instr)) {
                new_midCodes.add(new MidCode(c2.instr, c1.operand2, c2.operand2, c2.result));
                i += 1;
            } else if (c1.instr == MidCode.Op.ASSIGN && c1.operand1.charAt(0) == '#' && c1.operand1.equals(c2.operand2) &&
                    MidCode.is_arith(c2.instr)) {
                new_midCodes.add(new MidCode(c2.instr, c2.operand1, c1.operand2, c2.result));
                i += 1;
            } else if (i == midCodes.size()-2) {
                new_midCodes.add(c1);
                new_midCodes.add(c2);
            } else {
                new_midCodes.add(c1);
            }
        }
        midCodes = new_midCodes;
    }

    public void swap_facter() {
        ArrayList<MidCode> new_midCode = new ArrayList<>();
        for (MidCode midCode: midCodes) {
            if (MidCode.is_arith(midCode.instr) && midCode.instr != MidCode.Op.DIV && begins_num(midCode.operand1) && !begins_num(midCode.operand2)) {
                new_midCode.add(new MidCode(midCode.instr, midCode.operand2, midCode.operand1, midCode.result));
            } else {
                new_midCode.add(midCode);
            }
        }
        midCodes = new_midCode;
    }

    public void arith_to_assign() {
        ArrayList<MidCode> new_midCode = new ArrayList<>();
        for (MidCode midCode: midCodes) {
            if (midCode.instr == MidCode.Op.MUL && midCode.operand1.equals("1")) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, midCode.operand2, "#VACANT"));
            } else if (midCode.instr == MidCode.Op.MUL && midCode.operand2.equals("1")) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, midCode.operand1, "#VACANT"));
            } else if (midCode.instr == MidCode.Op.DIV && midCode.operand2.equals("1")) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, midCode.operand1, "#VACANT"));
            } else if (midCode.instr == MidCode.Op.MOD && (midCode.operand1.equals("1") || midCode.operand2.equals("1"))) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, "0", "#VACANT"));
            } else if (midCode.instr == MidCode.Op.ADD && midCode.operand1.equals("0")) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, midCode.operand2, "#VACANT"));
            } else if (midCode.instr == MidCode.Op.ADD && midCode.operand2.equals("0")) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, midCode.operand1, "#VACANT"));
            } else if (midCode.instr == MidCode.Op.SUB && midCode.operand2.equals("0")) {
                new_midCode.add(new MidCode(MidCode.Op.ASSIGN, midCode.result, midCode.operand1, "#VACANT"));
            } else {
                new_midCode.add(midCode);
            }
        }
        midCodes = new_midCode;
    }

    public static final Pattern IS_DIGIT = Pattern.compile("[0-9]*");
    public static boolean begins_num(String operand) {
        return IS_DIGIT.matcher(operand).matches() || operand.charAt(0) == '+' || operand.charAt(0) == '-';
    }
}
