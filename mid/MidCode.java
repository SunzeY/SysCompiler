package mid;

import com.sun.org.apache.bcel.internal.generic.CASTORE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

public class MidCode {

    public enum Op {
        ASSIGN, ADD, SUB, MUL, DIV, MOD, PRINT, NOT,
        GETINT, FUNC, END_FUNC, PREPARE_CALL, CALL,
        PUSH_PARA, RETURN, VAR_DEF, CONST_DEF,
        ARR_SAVE, ARR_LOAD,  PUSH_PARA_ARR,
        JUMP_IF, JUMP, LABEL, SET,

        // help_type
        SIGNAL_ARR_ADDR, NEW_BLOCK, EXIT_BLOCK, WHILE_BIND, EMPTY, EMPTY_INPUT, ENTER_WHILE,
        EXIT_WHILE, FUNC_FORM_VAR_DEF
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
        put(Op.PUSH_PARA_ARR, "PUSH_PARA_ARR");
        put(Op.JUMP_IF, "JUMP_IF");
        put(Op.JUMP, "JUMP");
        put(Op.LABEL, "LABEL");
        put(Op.SET, "SET");
    }};

    public Op instr;

    public String operand1;

    public String operand2;

    public String result;

    /* for arrive_data_flow */
    public Integer d_index = null;

    public String define;

    public ArrayList<String> use;


    public MidCode(Op instr, String operand1, String operand2, String result) {
        this.instr = instr;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.result = result;
    }

    @Override
    public String toString() {
        if (instr == Op.EMPTY) {
            return "_E_M_P_T_Y_";
        }
        if (instr == Op.FUNC_FORM_VAR_DEF) {
            return "FUNC_FORM_VAR_DEF " + this.operand1;
        }
        if (instr == Op.EMPTY_INPUT) {
            return "EMPTY_INPUT";
        }
        if (instr == Op.LABEL) {
            return result + ":";
        }
        if (instr == Op.JUMP) {
            return "JUMP" + " " + result;
        }
        if (instr == Op.JUMP_IF) {
            return "JUMP_IF" + " " + operand1.split(" ")[0]  + " " + operand2 + " " + operand1.split(" ")[1]  +  " " + result;
        }
        if (instr == Op.SET) {
            return "SET " + result + " := " + operand1.split(" ")[0]  + " " + operand2 + " " + operand1.split(" ")[1];
        }
        if (instr == Op.GETINT) {
            return operand1 + " = " + "input()";
        }
        if (instr == Op.VAR_DEF || instr == Op.CONST_DEF) {
            return "var " + operand1 + (operand2.equals("#VACANT") ? "" : "= " + operand2);
        }
        if (instr == Op.FUNC || instr == Op.END_FUNC) {
            return "#############" + toString.get(instr) + " " + operand1 + " " + operand2 + "############";
        }
        if (instr == Op.ASSIGN || instr == Op.ARR_SAVE || instr == Op.ARR_LOAD) {
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

    public static boolean is_arith(MidCode.Op op) {
        return op == Op.ADD || op == Op.SUB || op == Op.MUL || op == Op.DIV;
    }

    public String get_def() {
        switch (this.instr) {
            case ASSIGN:
            case VAR_DEF:
            case GETINT:
            case ARR_LOAD:
            case FUNC_FORM_VAR_DEF:
                this.define =  (!isVar(operand1)) ? null : this.operand1;
                return this.define;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case SET:
                this.define = (!isVar(result)) ? null : this.result;
                return this.define;
            default:
                return null;
        }
    }

    public ArrayList<String> get_use() {
        ArrayList<String> used_vars = new ArrayList<>();
        switch (this.instr) {
            case ASSIGN:
            case VAR_DEF:
                if (isVar(operand2)) {
                    used_vars.add(operand2);
                }
                break;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
                if (isVar(operand1)) {
                    used_vars.add(operand1);
                }
                if (isVar(operand2)) {
                    used_vars.add(operand2);
                }
                break;
            case PRINT:
            case PUSH_PARA:
            case RETURN:
                if (isVar(operand1)) {
                    used_vars.add(operand1);
                }
                break;
            case ARR_SAVE:
                String rank_r = this.operand1.split("\\[")[1].substring(0, operand1.split("\\[")[1].length() - 1);
                if (isVar(rank_r)) {
                    used_vars.add(rank_r);
                }
                if (isVar(operand2)) {
                    used_vars.add(operand2);
                }
                break;
            case ARR_LOAD:
                String rank_l = this.operand2.split("\\[")[1].substring(0, operand2.split("\\[")[1].length() - 1);
                if (isVar(rank_l)) {
                    used_vars.add(rank_l);
                }
                break;
            case SET:
            case JUMP_IF:
                String num1 = this.operand1.split(" ")[0];
                String num2 = this.operand1.split(" ")[1];
                if (isVar(num1))  {
                    used_vars.add(num1);
                }
                if (isVar(num2)) {
                    used_vars.add(num2);
                }
                break;
        }
        this.use = used_vars;
        return used_vars;
    }

    public void refact(HashMap<String, String> var2const) {
        switch (this.instr) {
            case SET:
            case JUMP_IF:
                String num1 = this.operand1.split(" ")[0];
                String num2 = this.operand1.split(" ")[1];
                for (HashMap.Entry<String, String> pair: var2const.entrySet()) {
                    if (num1.equals(pair.getKey())) {
                        num1 = pair.getValue();
                    }
                    if (num2.equals(pair.getKey())) {
                        num2 = pair.getValue();
                    }
                }
                this.operand1 = num1 + " " + num2;
                return;
            case ARR_SAVE:
                String rank_r = this.operand1.split("\\[")[1].substring(0, operand1.split("\\[")[1].length() - 1);
                for (HashMap.Entry<String, String> pair: var2const.entrySet()) {
                    if (rank_r.equals(pair.getKey())) {
                        this.operand1 = this.operand1.split("\\[")[0] + "[" + pair.getValue() + "]";
                    }
                    if (operand2.equals(pair.getKey())) {
                        operand2 = pair.getValue();
                    }
                }
                return;
            case ARR_LOAD:
                String rank_l = this.operand2.split("\\[")[1].substring(0, operand2.split("\\[")[1].length() - 1);
                for (HashMap.Entry<String, String> pair: var2const.entrySet()) {
                    if (rank_l.equals(pair.getKey())) {
                        this.operand2 = this.operand2.split("\\[")[0] + "[" + pair.getValue() + "]";
                    }
                }
                return;
            default:
                for (HashMap.Entry<String, String> pair: var2const.entrySet()) {
                    if (operand1.equals(pair.getKey())) {
                        operand1 = pair.getValue();
                    }
                    if (operand2.equals(pair.getKey())) {
                        operand2 = pair.getValue();
                    }
                }
        }
    }

    public HashSet<Integer> get_gen() {
        HashSet<Integer> gen = new HashSet<>();
        gen.add(this.d_index);
        return gen;
    }

    public HashSet<Integer> get_kill() {
        if (this.get_def() != null) {
            HashSet<Integer> kill = new HashSet<>(DataFlower.var_define_points.get(this.get_def()));
            kill.remove(this.d_index);
            return kill;
        }
        return new HashSet<>();
    }

    public static final Pattern IS_DIGIT = Pattern.compile("[0-9]*");
    public static boolean begins_num(String operand) {
        return IS_DIGIT.matcher(operand).matches() || operand.charAt(0) == '+' || operand.charAt(0) == '-';
    }

    public static boolean isVar(String operand) {
        if (operand.split("@").length == 2 && operand.split("@")[1].equals("<0,0>")) { // global_var
            return false;
        }
        return ((operand.charAt(0) != '#' || operand.startsWith("#T")) && !begins_num(operand) && !operand.equals("%RTX"));
    }
}
