package mid;

import java.util.HashMap;

public class MidCode {
    public enum Op {
        ASSIGN, ADD, SUB, MUL, DIV, MOD, PRINT, NOT,
        GETINT, FUNC, END_FUNC, PREPARE_CALL, CALL,
        PUSH_PARA, RETURN, VAR_DEF, CONST_DEF, NEW_BLOCK, EXIT_BLOCK,
    }

    public final HashMap<Op, String> toString = new HashMap<Op, String>() {{
        put(Op.ASSIGN, "ASSIGN");
        put(Op.ADD, "ADD");
        put(Op.SUB, "SUB");
        put(Op.MUL, "MUL");
        put(Op.DIV, "DIV");
        put(Op.MOD, "MOD");
        put(Op.PRINT, "PRINT");
        put(Op.NOT, "NOT");
        put(Op.GETINT, "GETINT");
        put(Op.FUNC, "FUNC");
        put(Op.END_FUNC, "END_FUNC");
        put(Op.PREPARE_CALL, "PREPARE_CALL");
        put(Op.CALL, "CALL");
        put(Op.PUSH_PARA, "PUSH_PARA");
        put(Op.RETURN, "RETURN");
        put(Op.VAR_DEF, "VAR_DEF");
        put(Op.CONST_DEF, "CONST_DEF");
        put(Op.NEW_BLOCK, "NEW_BLOCK");
        put(Op.EXIT_BLOCK, "EXIT_BLOCK");
    }};

    public Op instr;

    public String operand1;

    public String operand2;

    public String result;

    public MidCode(Op instr, String operand1, String operand2, String result) {
        this.instr = instr;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
    }

    @Override
    public String toString() {
        if (instr == Op.GETINT) {
            return operand1 + " = " + "input()";
        }
        if (instr == Op.VAR_DEF || instr == Op.CONST_DEF) {
            return "var " + operand1 + (operand2.equals("#VACANT") ? "" : "= " + operand2);
        }
        if (instr == Op.FUNC || instr == Op.END_FUNC) {
            return "#############" + toString.get(instr) + " " + operand1 + " " + operand2 + "############";
        }
        if (instr == Op.ASSIGN) {
            return operand1 + " = " + operand2;
        }
        if (!result.equals("#VACANT")) {
            return result + " = " + operand1 + " " + toString.get(instr) + " " + operand2;
        }
        if (operand1.equals("#VACANT") && operand2.equals("#VACANT")) {
            return toString.get(instr);
        }
        if (operand2.equals("#VACANT")) {
            return toString.get(instr) + " " + operand1;
        }
        return toString.get(instr) + " " + operand1 + " " + operand2;
    }
}
