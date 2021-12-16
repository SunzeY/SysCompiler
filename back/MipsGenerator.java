package back;

import SymTable.FuncFormVar;
import SymTable.SymItem;
import SymTable.SymbolTable;
import SymTable.Var;
import mid.MidCode;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class MipsGenerator {
    public boolean optimize_mul = true;
    public boolean optimize_assign_reg = true;

    public boolean div_opt_hard = true;

    public static final int LOCAL_ADDR_INIT = 104;
    public static final int STACK_T_BEGIN = 60;
    public static final int STACK_S_BEGIN = 24;
    public static final String STACK_RA = "0($sp)";
    public static final int _data_start = 0x10010000;
    public int call_func_sp_offset = 0;

    public final Stack<Integer> prepare_cnt = new Stack<>();

    public HashMap<String, Integer> globalArrayAddr = new HashMap<>();
    public int globalsize = 0;
    public int tmp_label_idx = 1;
    public ArrayList<mid.MidCode> midCodes;
    public final ArrayList<String> mipsCodes = new ArrayList<>();
    public ArrayList<String> strCons;
    public HashMap<String, ArrayList<SymItem>> funcTables;
    public SymbolTable globalTable;
    public final HashMap<String, Integer> funcStackSize = new HashMap<>();
    public final ArrayList<Integer> spSize = new ArrayList<Integer>() {{
        add(0);
    }};
    public ArrayList<String> sRegTable = new ArrayList<String>() {{
        for (int i = 0; i < 9; i += 1) {
            add("#VACANT");
        }
    }};
    public ArrayList<String> tRegTable = new ArrayList<String>() {{
        for (int i = 0; i < 10; i += 1) {
            add("#VACANT");
        }
    }};

    public String assignSReg(String name) {
        for (int i = 0; i < sRegTable.size(); i++) {
            if (sRegTable.get(i).equals("#VACANT")) {
                sRegTable.set(i, name);
                return (i == sRegTable.size() - 1) ? "$fp" : ("$s" + i);
            }
        }
        return "#INVALID";
    }

    public String assignTReg(String name) {
        for (int i = 0; i < 10; i++) {
            if (tRegTable.get(i).equals("#VACANT")) {
                tRegTable.set(i, name);
                return "$t" + i;
            }
        }
        return "#INVALID";
    }

    public HashMap<MidCode.Op, String> mipsInstr = new HashMap<MidCode.Op, String>() {{
        put(MidCode.Op.ADD, "addu");
        put(MidCode.Op.SUB, "sub");
        put(MidCode.Op.MUL, "mul");
        put(MidCode.Op.DIV, "div");
        put(MidCode.Op.MOD, "mod");
    }};

    public final String[] reg = new String[]{
            "$zero", "$at", "$v0", "$v1",
            "$a0", "$a1", "$a2", "$a3",
            "$t0", "$t1", "$t2", "$t3",
            "$t4", "$t5", "$t6", "$t7",
            "$s0", "$s1", "$s2", "$s3",
            "$s4", "$s5", "$s6", "$s7",
            "$t8", "$t9", "$k0", "$k1",
            "$gp", "$sp", "$fp", "$ra"
    };

    public final HashMap<String, String> b_instr = new HashMap<String, String>() {{
        put("!=", "bne");
        put("==", "beq");
        put(">=", "bge");
        put("<=", "ble");
        put("<", "blt");
        put(">", "bgt");
    }};

    public final HashMap<String, String> set_instr = new HashMap<String, String>() {{
        put("!=", "sne");
        put("==", "seq");
        put(">=", "sge");
        put("<=", "sle");
        put("<", "slt");
        put(">", "sgt");
    }};

    public final HashMap<String, String> set_instr_i = new HashMap<String, String>() {{
        put("<", "slti");
        // put(">", "sgti");
    }};

    public MipsGenerator(ArrayList<mid.MidCode> midCodes, ArrayList<String> strCons, HashMap<String, ArrayList<SymItem>> funcTables, SymbolTable globalTable) {
        this.midCodes = midCodes;
        this.strCons = strCons;
        this.funcTables = funcTables;
        this.globalTable = globalTable;
        for (Map.Entry<String, ArrayList<SymItem>> pair : funcTables.entrySet()) {
            String name = pair.getKey();
            if (pair.getValue().isEmpty()) {
                funcStackSize.put(name, 0);
                continue;
            }
            SymItem lastItem = pair.getValue().get(pair.getValue().size() - 1);
            funcStackSize.put(name, lastItem.getAddr() + lastItem.getSize());
        }

        for (SymItem item : globalTable.symItems) {
            globalsize = item.set_addr(globalsize);
        }
    }

    String currentFunc = "";

    public void generate(String code) {
        mipsCodes.add(code);
    }

    public void generate(String instr, String operand1) {
        mipsCodes.add(instr + " " + operand1);
    }

    public void generate(String instr, String operand1, String operand2) {
        mipsCodes.add(instr + " " + operand1 + ", " + operand2);
        release(operand2);
        if (instr.equals("sw") || instr.equals("bltz") || instr.equals("blez") || instr.equals("bgez") || instr.equals("bgtz")) {
            release(operand1);
        }
    }

    public void generate(String instr, String num1, String num2, String num3) {
        generate(instr, num1, num2, num3, false);
    }

    public void generate(String instr, String num1, String num2, String num3, boolean without_release) {
        if (instr.equals("mul") && is_const(num3) && optimize_mul && utils.is_2_power(Math.abs(Integer.parseInt(num3)))) {
            int multiplier = Integer.parseInt(num3);
            if (multiplier > 0) {
                generate("sll", num1, num2, Integer.toString(utils.log2(multiplier)), without_release);
            } else {
                generate("sll", num1, num2, Integer.toString(utils.log2(Math.abs(multiplier))), without_release);
                generate("subu", num1, "$zero", num1);
            }
            return;
        }
        generate(instr + " " + num1 + ", " + num2 + ", " + num3);
        if (without_release) {
            return;
        }
        if (instr.equals("addu") || instr.equals("subu") || instr.equals("mul") || instr.equals("div") || instr.equals("sll") || instr.equals("sra")) {
            if (!num1.equals(num2)) {
                release(num2);
            }
            if (!num1.equals(num3)) {
                release(num3);
            }
        } else if (is_con_jump(instr)) {
            release(num1);
            release(num2);
        }
    }

    private boolean is_con_jump(String instr) {
        return b_instr.containsValue(instr);
    }

    private void release(String addr) {
        if (addr.charAt(0) == '$' && addr.charAt(1) == 't') {
            generate("# RELEASE " + addr + " bind var " + tRegTable.get(addr.charAt(2) - '0'));
            tRegTable.set(addr.charAt(2) - '0', "#VACANT");
        }
    }

    int para_number = 0;

    public void translate() {
        generate(".data");
        int addr = 0;
        for (SymItem item : globalTable.symItems) {
            if (item instanceof Var && !((Var) item).getShape().isEmpty()) {
                generate("arr_" + item.getName() + "_: .space " + item.getSize());
                globalArrayAddr.put(item.getUniqueName(), addr);
                addr += item.getSize();
            }
        }
        for (int i = 0; i < strCons.size(); i += 1) {
            mipsCodes.add("str" + i + ": .asciiz" + " \"" + strCons.get(i) + "\"");
        }
        generate(".text");
        boolean init = true;
        generate("addi $gp, $gp, " + this.globalsize);
        for (MidCode code : midCodes) {
            mipsCodes.add("# ====" + code + "====");
            MidCode.Op instr = code.instr;
            String operand1 = code.operand1;
            String operand2 = code.operand2;
            String result = code.result;
            if (instr.equals(MidCode.Op.FUNC)) { //new Function
                if (init) {
                    generate("addi $sp, $sp, -" + (funcStackSize.get("main") + LOCAL_ADDR_INIT));
                    generate("j main");
                    init = false;
                }

                currentFunc = operand2;
                mipsCodes.add(operand2 + ":");
                call_func_sp_offset = 0;
                for (int i = 0; i < sRegTable.size(); i++) {
                    sRegTable.set(i, "#VACANT");
                }
                for (SymItem item : funcTables.get(currentFunc)) {
                    if (item instanceof FuncFormVar) {
                        String sReg = assignSReg(item.getUniqueName());
                        if (!sReg.equals("#INVALID")) {
                            generate("lw", sReg, (item.getAddr() + call_func_sp_offset) + "($sp)");
                        } else {
                            break;
                        }
                    }
                }

            } else if (instr.equals(MidCode.Op.RETURN)) {
                if (!operand1.equals("#VACANT")) {
                    load_value(operand1, "$v0");
                }
                if (currentFunc.equals("main")) {
                    // end of mips - code
                    generate("addi $gp, $gp, -" + this.globalsize);
                    generate("addi $sp, $sp, " + (funcStackSize.get("main") + LOCAL_ADDR_INIT));
                    mipsCodes.add("li $v0, 10");
                    mipsCodes.add("syscall");
                } else {
                    mipsCodes.add("jr $ra");
                }

            } else if (instr.equals(MidCode.Op.PREPARE_CALL)) {
                prepare_cnt.push(0);
                spSize.add(LOCAL_ADDR_INIT + funcStackSize.get(operand1));
                call_func_sp_offset = sum(spSize);
                generate("addi $sp, $sp, -" + spSize.get(spSize.size() - 1));
            } else if (instr.equals(MidCode.Op.PUSH_PARA)) {
                para_number = prepare_cnt.peek();
                String para_addr = funcTables.get(operand2).get(para_number).getAddr().toString() + "($sp)";
                prepare_cnt.set(prepare_cnt.size() - 1, para_number + 1);
                String reg = "$a1";

                boolean b_in_reg = in_reg(operand1) || assign_reg(operand1, true);
                String b = symbol_to_addr(operand1);

                if (b_in_reg) {
                    generate("sw", b, para_addr);
                } else if (is_const(operand1)) {
                    generate("li", reg, b);
                    generate("sw", reg, para_addr);
                } else {
                    generate("lw", reg, b);
                    generate("sw", reg, para_addr);
                }

            } else if (instr.equals(MidCode.Op.PUSH_PARA_ARR)) {
                para_number = prepare_cnt.peek();
                String para_addr = funcTables.get(operand2).get(para_number).getAddr().toString() + "($sp)";
                prepare_cnt.set(prepare_cnt.size() - 1, para_number + 1);
                String rank_s = "0";
                if (operand1.split("\\[").length > 1) {
                    rank_s = operand1.split("\\[")[1].substring(0, operand1.split("\\[")[1].length() - 1);
                    operand1 = operand1.split("\\[")[0];
                }
                String push_reg = symbol_to_addr_array(operand1, rank_s);
                generate("sw", push_reg, para_addr);

            } else if (instr.equals(MidCode.Op.CALL)) {
                prepare_cnt.pop();
                ArrayList<Integer> saved_s = new ArrayList<>(), saved_t = new ArrayList<>();
                ArrayList<String> t_old = (ArrayList<String>) tRegTable.clone();
                for (int i = 0; i < tRegTable.size(); i += 1) {
                    if (!tRegTable.get(i).equals("#VACANT")) {
                        saved_t.add(i);
                        generate("sw", "$t" + i, (STACK_T_BEGIN + 4 * i + funcStackSize.get(operand1)) + "($sp)");
                        tRegTable.set(i, "#VACANT");
                    }
                }
                ArrayList<String> s_old = (ArrayList<String>) sRegTable.clone();
                for (int i = 0; i < sRegTable.size(); i += 1) {
                    if (!sRegTable.get(i).equals("#VACANT")) {
                        saved_s.add(i);
                        generate("sw", ((i == sRegTable.size() - 1) ? "$fp" : ("$s" + i)), (STACK_S_BEGIN + 4 * i + funcStackSize.get(operand1)) + "($sp)");
                        sRegTable.set(i, "#VACANT");
                    }
                }

                if (!currentFunc.equals("main")) {
                    generate("sw", "$ra", STACK_RA);
                }
                generate("jal", operand1);
                if (!currentFunc.equals("main")) {
                    generate("lw", "$ra", STACK_RA);
                }
                for (int i : saved_s) {
                    generate("lw", ((i == sRegTable.size() - 1) ? "$fp" : ("$s" + i)), (STACK_S_BEGIN + 4 * i + funcStackSize.get(operand1)) + "($sp)");
                }
                sRegTable = s_old;

                //caller recover tRegs
                for (int i : saved_t) {
                    generate("lw", "$t" + i, (STACK_T_BEGIN + 4 * i + funcStackSize.get(operand1)) + "($sp)");
                }
                tRegTable = t_old;
                generate("addi $sp, $sp, " + spSize.get(spSize.size() - 1));
                spSize.remove(spSize.size() - 1);
                call_func_sp_offset = sum(spSize);


            } else if (instr.equals(MidCode.Op.PRINT)) {
                if (operand1.charAt(0) == '#' && operand1.charAt(1) != 'T') {
                    generate("la $a0, " + operand1.substring(1));
                    generate("li $v0, 4");
                } else {
                    load_value(operand1, "$a0");
                    generate("li $v0, 1");
                }
                generate("syscall");

            } else if (instr.equals(MidCode.Op.GETINT) || instr.equals(MidCode.Op.EMPTY_INPUT)) {
                generate("li $v0, 5");
                generate("syscall");
                if (instr.equals(MidCode.Op.GETINT)) {
                    save_value("$v0", operand1);
                }
            } else if (instr.equals(MidCode.Op.ASSIGN) || instr.equals(MidCode.Op.VAR_DEF) || instr.equals(MidCode.Op.CONST_DEF)) {
                if (!operand2.equals("#VACANT")) {
                    gen_assign(operand1, operand2);
                }
            } else if (is_arithmatic(instr)) {
                String mips_instr = mipsInstr.get(instr);
                String reg1 = "$a1";
                String reg2 = "$a2";
                boolean a_in_reg = in_reg(result) || assign_reg(result, false);
                boolean b_in_reg_or_const = is_const(operand1) || in_reg(operand1) || assign_reg(operand1, true);
                boolean c_in_reg_or_const = is_const(operand2) || in_reg(operand2) || assign_reg(operand2, true);

                String a = symbol_to_addr(result);
                String b = symbol_to_addr(operand1);
                String c = symbol_to_addr(operand2);
//                boolean is_2_pow_1 = is_const(code.operand1) && utils.is_2_power(Integer.getInteger(code.operand1));
//                boolean is_2_pow_2 = is_const(code.operand2) && utils.is_2_power(Integer.getInteger(code.operand2));

                boolean sra = false;
                if (is_const(b) && is_const(c)) {
                    int ans = 0;
                    if (mips_instr.equals("addu")) {
                        ans = Integer.parseInt(b) + Integer.parseInt(c);
                    } else if (mips_instr.equals("sub")) {
                        ans = Integer.parseInt(b) - Integer.parseInt(c);
                    } else if (mips_instr.equals("mul")) {
                        ans = Integer.parseInt(b) * Integer.parseInt(c);
                    } else if (mips_instr.equals("div")) {
                        ans = Integer.parseInt(b) / Integer.parseInt(c);
                    } else if (mips_instr.equals("mod")) {
                        ans = Integer.parseInt(b) % Integer.parseInt(c);
                    }
                    if (a_in_reg) {
                        generate("li", a, Integer.toString(ans));
                    } else {
                        generate("li", reg1, Integer.toString(ans));
                        generate("sw", reg1, a);
                    }
                    continue;
                }
                if ((instr.equals(MidCode.Op.DIV) || instr.equals(MidCode.Op.MOD)) && is_const(operand2) && div_opt_hard) {
                    if (!in_reg(operand1)) {
                        generate("lw", reg2, b);
                        b = reg2;
                    }
                    String reg3 = "$a3";
                    String reg4 = "$a0";
                    if (a_in_reg && !b.equals(a)) { // !(a = a / 4)
                        reg4 = a;
                    }
                    int d = Integer.parseInt(c);
                    long[] multiplier = utils.choose_multiplier(Math.abs(d), utils.N-1);
                    long m = multiplier[0];
                    long sh_post = multiplier[1];
                    long l = multiplier[2];
                    int q = 0;
                    if (d == 1 || d == -1) {
                        if (d == 1) {
                            generate("move", reg4, b);
                        } else {
                            generate("subu", reg4, "$zero", b);
                        }
                    } else if (Math.abs(d) == 1 << l) {
                        generate("sra", reg3, b, Integer.toString((int) (l - 1)));
                        generate("srl", reg4, reg3, Integer.toString((int) (utils.N - l)));
                        generate("addu", reg3, b, reg4, true);
                        generate("sra", reg4, reg3, Integer.toString((int) l));
                    } else if (m < (1L << (utils.N - 1))) {
                        generate("li", reg3, Integer.toString((int) m));
                        generate("mult", b, reg3);
                        generate("mfhi", reg3);
                        generate("sra", reg4, reg3, Integer.toString((int) sh_post));
                        generate("sra", reg3, b, String.valueOf(31));
                        generate("subu", reg4, reg4, reg3, true);
                    } else {
                        generate("li", reg3, Integer.toString((int) (m - Math.pow(2, utils.N))));
                        generate("mult", b, reg3);
                        generate("mfhi", reg3);
                        generate("addu", reg3, b, reg3);
                        generate("sra", reg4, reg3, Integer.toString((int) sh_post));
                        generate("sra", reg3, b, String.valueOf(31));
                        generate("subu", reg4, reg4, reg3, true);
                    }
                    if (d < 0) {
                        generate("subu", reg4, "$zero", reg4);
                    }

                    if (instr.equals(MidCode.Op.MOD)) {
                        generate("mul", reg3, reg4, Integer.toString(d), true);
                        generate("subu", reg4, b, reg3);
                    }

                    if (!a_in_reg) {
                        generate("sw", reg4, a);
                    } else if (b.equals(a)) { // a = a / 4;
                        generate("move", a, reg4);
                    }
                    continue;
                }
                if (a_in_reg) {
                    if (b_in_reg_or_const && c_in_reg_or_const) {
                        if (is_const(b)) {
                            generate("li", reg2, b);
                            b = reg2;
                        }
                        gen_arithmetic(mips_instr, a, b, c); // c can be a const
                    } else if (b_in_reg_or_const) {
                        generate("lw", reg1, c);
                        if (is_const(b)) {
                            generate("li", reg2, b);
                            b = reg2;
                        }
                        gen_arithmetic(mips_instr, a, b, reg1);
                    } else if (c_in_reg_or_const) {
                        generate("lw", reg1, b);
                        gen_arithmetic(mips_instr, a, reg1, c);
                    } else {
                        generate("lw", reg1, b);
                        generate("lw", reg2, c);
                        gen_arithmetic(mips_instr, a, reg1, reg2);
                    }
                } else {
                    if (b_in_reg_or_const && c_in_reg_or_const) {
                        if (is_const(b)) {
                            generate("li", reg2, b);
                            b = reg2;
                        }
                        gen_arithmetic(mips_instr, reg1, b, c);
                        generate("sw", reg1, a);
                    } else if (b_in_reg_or_const) {
                        generate("lw", reg1, c);
                        if (is_const(b)) {
                            generate("li", reg2, b);
                            b = reg2;
                        }
                        gen_arithmetic(mips_instr, reg1, b, reg1);
                        generate("sw", reg1, a);
                    } else if (c_in_reg_or_const) {
                        generate("lw", reg1, b);
                        gen_arithmetic(mips_instr, reg1, reg1, c);
                        generate("sw", reg1, a);
                    } else {
                        generate("lw", reg1, b);
                        generate("lw", reg2, c);
                        gen_arithmetic(mips_instr, reg1, reg1, reg2);
                        generate("sw", reg1, a);
                    }
                }

            } else if (instr.equals(MidCode.Op.ARR_LOAD) || instr.equals(MidCode.Op.ARR_SAVE)) {
                // a = b[c] or b[c] = a
                String array_op;
                boolean a_in_reg;
                String a;
                if (instr.equals(MidCode.Op.ARR_SAVE)) {
                    array_op = operand1;
                    a_in_reg = in_reg(operand2) || assign_reg(result, true);
                    a = symbol_to_addr(operand2);
                } else {
                    array_op = operand2;
                    a_in_reg = in_reg(operand1) || assign_reg(result, false);
                    a = symbol_to_addr(operand1);
                }
                String item_addr;
                String reg0 = "$a0";
                String reg = "$a1";
                String reg2 = "$a2";

                // symbol[rank]
                String symbol = array_op.split("\\[")[0];
                String rank = array_op.split("\\[")[1].substring(0, array_op.split("\\[")[1].length() - 1);
                boolean global = in_global(symbol);
                boolean isPointer = isPointer(symbol);
                if (isPointer) { // pointer on stack
                    if (in_reg(symbol)) {
                        reg0 = symbol_to_addr(symbol);
                    } else {
                        generate("lw", reg0, symbol_to_addr(symbol));
                    }
                    if (utils.begins_num(rank)) {
                        int offset = 4 * Integer.parseInt(rank);
                        item_addr = offset + "(" + reg0 + ")";
                    } else {
                        boolean rank_in_reg = in_reg(rank);
                        rank = symbol_to_addr(rank);
                        if (rank_in_reg) {
                            generate("sll", reg, rank, "2");
                        } else {
                            generate("lw", reg, rank);
                            generate("sll", reg, reg, "2");
                        }
                        gen_arithmetic("addu", reg, reg0, reg);
                        item_addr = "0(" + reg + ")";
                    }
                } else {
                    if (utils.begins_num(rank)) { // const rank
                        int offset = 4 * Integer.parseInt(rank);
                        if (global) { // symbol with no "<"
                            item_addr = "arr_" + symbol.split("@")[0] + "_+" + offset + "($zero)";
                        } else { //on stack
                            offset += Objects.requireNonNull(search_Item(symbol)).getAddr() + call_func_sp_offset;
                            item_addr = offset + "($sp)";
                        }
                    } else { //rank is in memory or register 4*rank + sp + call_func_sp_offset
                        boolean rank_in_reg = in_reg(rank);
                        rank = symbol_to_addr(rank);
                        if (rank_in_reg) {
                            generate("sll", reg, rank, "2");
                        } else {
                            generate("lw", reg, rank);
                            generate("sll", reg, reg, "2");
                        }

                        if (global) { // symbol with no "<"
                            item_addr = "arr_" + symbol.split("@")[0] + "_(" + reg + ")";
                        } else {
                            generate("addu", reg, reg, String.valueOf((search_Item(symbol).getAddr() + call_func_sp_offset)));
                            generate("addu", reg, reg, "$sp");
                            item_addr = "0(" + reg + ")";
                        }
                    }
                }

                if (instr.equals(MidCode.Op.ARR_LOAD)) {
                    if (a_in_reg) {
                        generate("lw", a, item_addr);
                    } else {
                        generate("lw", reg2, item_addr);
                        generate("sw", reg2, a);
                    }
                } else { // ARR_SAVE
                    if (a_in_reg) {
                        generate("sw", a, item_addr);
                    } else if (is_const(operand2)) {
                        generate("li", reg2, a);
                        generate("sw", reg2, item_addr);
                    } else {
                        generate("lw", reg2, a);
                        generate("sw", reg2, item_addr);
                    }
                }
            } else if (instr.equals(MidCode.Op.JUMP)) {
                generate("j", result);

            } else if (instr.equals(MidCode.Op.JUMP_IF)) {
                String num1 = operand1.split(" ")[0];
                String num2 = operand1.split(" ")[1];
                boolean a_in_reg = in_reg(num1) || assign_reg(num1, true);
                boolean b_in_reg = in_reg(num2) || assign_reg(num2, true);
                String a = symbol_to_addr(num1);
                String b = symbol_to_addr(num2);
                String reg1 = "$a1";
                String reg2 = "$a2";

                if (is_const(num1)) {
                    generate("li", reg1, a);
                } else if (a_in_reg) {
                    reg1 = a;
                } else {
                    generate("lw", reg1, a);
                }

                if (is_const(num2)) {
                    generate("li", reg2, b);
                } else if (b_in_reg) {
                    reg2 = b;
                } else {
                    generate("lw", reg2, b);
                }

                generate(b_instr.get(operand2), reg1, reg2, result);

            } else if (instr.equals(MidCode.Op.LABEL)) {
                generate(result + ":");

            } else if (instr.equals(MidCode.Op.SET)) {
                String num1 = operand1.split(" ")[0];
                String num2 = operand1.split(" ")[1];
                boolean a_in_reg = in_reg(result) || assign_reg(result, false);
                boolean b_in_reg_or_const = is_const(num1) || in_reg(num1) || assign_reg(num1, true);
                boolean c_in_reg = in_reg(num2) || assign_reg(num2, true);

                String reg0 = "$a0";
                String reg1 = "$a1";
                String reg2 = "$a2";

                String a = symbol_to_addr(result);
                if (a_in_reg) {
                    reg0 = a;
                }
                String b = symbol_to_addr(num1);
                String c = symbol_to_addr(num2);
                if (b_in_reg_or_const) {
                    if (is_const(b)) {
                        generate("li", reg1, b);
                    } else {
                        reg1 = b;
                    }
                } else {
                    generate("lw", reg1, b);
                }

                if (is_const(c)) {
                    if (set_instr_i.containsKey(operand2)) {
                        generate(set_instr_i.get(operand2), reg0, reg1, c);
                    } else {
                        generate("li", reg2, c);
                        generate(set_instr.get(operand2), reg0, reg1, c);
                    }
                } else {
                    if (!c_in_reg) {
                        generate("lw", reg2, c);
                    } else {
                        reg2 = c;
                    }
                    generate(set_instr.get(operand2), reg0, reg1, reg2);
                }
                if (!a_in_reg) {
                    generate("sw", reg0, a);
                }
            }
        }

    }

    private boolean isPointer(String symbol) {
        SymItem curItem = null;
        if (!currentFunc.equals("")) {
            for (SymItem item : funcTables.get(currentFunc)) {
                if (item.getUniqueName().equals(symbol)) {
                    curItem = item;
                    break;
                }
            }
        }
        if (curItem == null) {
            for (SymItem item : globalTable.symItems) {
                if (item.getUniqueName().equals(symbol)) {
                    curItem = item;
                    break;
                }
            }
        }
        assert curItem != null;
        return curItem instanceof FuncFormVar;
    }

    private String symbol_to_addr_array(String operand, String rank) {
        SymItem curItem = null;
        boolean in_global = false;
        String reg = "$a0";
        String reg1 = "$a1";
        if (!currentFunc.equals("")) {
            for (SymItem item : funcTables.get(currentFunc)) {
                if (item.getUniqueName().equals(operand)) {
                    curItem = item;
                    break;
                }
            }
        }
        if (curItem == null) {
            in_global = true;
            for (SymItem item : globalTable.symItems) {
                if (item.getUniqueName().equals(operand)) {
                    curItem = item;
                    break;
                }
            }
        }
        assert curItem != null;

        if (curItem instanceof Var) {
            ArrayList<Integer> shape = ((Var) curItem).getShape();
            if (Character.isDigit(rank.charAt(0))) {
                int rank_v = Integer.parseInt(rank);
                if (rank_v != 0) { // 2-dimension array with a[index] to find addr
                    rank_v = rank_v * shape.get(1);
                }

                int offset = curItem.getAddr();
                offset = offset + rank_v * 4 + call_func_sp_offset;
                if (in_global) {
                    generate("li " + reg + ", " + "0x" + Integer.toHexString(globalArrayAddr.get(curItem.getUniqueName()) + rank_v * 4 + _data_start));
                } else {
                    generate("move", reg, "$sp");
                    generate("addiu", reg, reg, Integer.toString(offset));
                }
            } else {
                String offset = symbol_to_addr(rank);
                if (in_reg(rank)) {
                    generate("sll", reg1, offset, "2");
                } else {
                    generate("lw", reg1, offset);
                    generate("sll", reg1, reg1, "2");
                }
                if (in_global) {
                    if (shape.size() > 1) {
                        generate("mul", reg1, reg1, shape.get(1).toString());
                    }
                    generate("addiu", reg, reg1, "0x" + Integer.toHexString(globalArrayAddr.get(curItem.getUniqueName()) + _data_start));
                } else {
                    if (shape.size() > 1) {
                        generate("mul", reg1, reg1, shape.get(1).toString());
                    }
                    generate("addiu", reg, "$sp", Integer.toString(call_func_sp_offset + search_Item(operand).getAddr()));
                    generate("add", reg, reg, reg1);
                }
            }
        } else { // already in funcFormVar(addr)
            ArrayList<Integer> shape = ((FuncFormVar) curItem).getShape();
            if (Character.isDigit(rank.charAt(0))) {
                int rank_v = Integer.parseInt(rank);
                if (rank_v != 0) { // 2-dimension array with a[index] to find addr
                    rank_v = rank_v * shape.get(1);
                }
                if (in_reg(operand)) {
                    generate("addiu", reg, symbol_to_addr(operand), Integer.toString(rank_v * 4));
                } else {
                    generate("lw", reg, symbol_to_addr(operand));
                    generate("addiu", reg, reg, Integer.toString(rank_v * 4));
                }
            } else {
                String offset = symbol_to_addr(rank);
                if (in_reg(rank)) {
                    generate("sll", reg1, offset, "2");
                } else {
                    generate("lw", reg1, offset);
                    generate("sll", reg1, reg1, "2");
                }
                if (shape.size() > 1) {
                    generate("mul", reg1, reg1, shape.get(1).toString());
                }
                if (in_reg(operand)) {
                    generate("add", reg, symbol_to_addr(operand), reg1);
                } else {
                    generate("lw", reg, symbol_to_addr(operand));
                    generate("add", reg, reg, reg1);
                }
            }
        }

        return reg;
    }

    private SymItem search_Item(String name) {
        for (SymItem item : funcTables.get(currentFunc)) {
            if (item.getUniqueName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    private boolean in_global(String symbol) {
        for (SymItem item : globalTable.symItems) {
            if (item.getUniqueName().equals(symbol)) {
                return true;
            }
        }
        return false;
    }

    private void gen_arithmetic(String mips_instr, String num1, String num2, String num3) {
        String reg3 = "$a3";
        if (is_const(num2)) {
            if (mips_instr.equals("addu")) {
                generate("addiu", num1, num3, num2);
            } else if (mips_instr.equals("mul")) {
                generate(mips_instr, num1, num3, num2);
            } else if (mips_instr.equals("div") || mips_instr.equals("subu") || mips_instr.equals("mod")) {
                generate("li", reg3, num2);
                if (mips_instr.equals("div") || mips_instr.equals("mod")) {
                    generate("div", reg3, num3);
                    generate(mips_instr.equals("mod") ? "mfhi" : "mflo", num1);
                    if (!num1.equals(num3)) {
                        release(num3);
                    }
                } else {
                    generate("li", reg3, num2);
                    generate(mips_instr, num1, reg3, num3);
                }
            } else {
                generate(mips_instr, num1, num2, num3);
            }
        } else if ((mips_instr.equals("div") || mips_instr.equals("mod")) && !is_const(num3)) {
            generate("div", num2, num3);
            generate(mips_instr.equals("mod") ? "mfhi" : "mflo", num1);
            if (!num1.equals(num2)) {
                release(num2);
            }
            if (!num1.equals(num3)) {
                release(num3);
            }
        } else if (mips_instr.equals("sra")) {
            String label = assign_label();
            String label2 = assign_label();
            generate(mips_instr, num1, reg3, num3);
            generate("subu", num1, "$zero", num1);
            generate("j", label2);
            generate(label + ":");
            generate(mips_instr, num1, num2, num3);
            generate(label2 + ":");
        } else if (mips_instr.equals("addu") && is_const(num3)) {
            if (num3.equals("1073741824")) {
                generate("lui", reg3, "0x4000");
                generate("addu", num1, num2, reg3);
            } else {
                generate("addiu", num1, num2, num3);
            }
        } else if (mips_instr.equals("subu") && is_const(num3)) {
            if (num3.equals("1073741824")) {
                generate("lui", reg3, "0xc000");
                generate("addu", num1, num2, reg3);
            } else {
                String neg = num3.charAt(0) == '+' ? '-' + num3.substring(1)
                        : num3.charAt(0) == '-' ? num3.substring(1) : '-' + num3;
                generate("addiu", num1, num2, neg);
            }
        } else {
            if (mips_instr.equals("mod")) {
                generate("div", num1, num2, num3);
                generate("mfhi", num1);
            } else {
                generate(mips_instr, num1, num2, num3);
            }
        }
    }


    private String assign_label() {
        String ret = "tmp_label_" + tmp_label_idx;
        tmp_label_idx += 1;
        return ret;
    }

    private void show_reg_status() {
        System.out.println("^^^^^^^^^^REG_TABLE^^^^^^^^^^");
        for (int i = 0; i < tRegTable.size(); i += 1) {
            System.out.println("$t" + i + ": " + tRegTable.get(i));
        }
        for (int i = 0; i < sRegTable.size(); i += 1) {
            System.out.println(((i == 8) ? "$fp" : ("$s" + i)) + ": " + sRegTable.get(i));
        }
        System.out.println("^^^^^^^^^^REG_TABLE^^^^^^^^^^");
    }

    private String symbol_to_addr(String operand) {
        if (utils.begins_num(operand)) {
            if (operand.equals("0")) {
                return "$zero";
            }
            return operand;
        }

        if (operand.equals("%RTX")) {
            return "$v0";
        }

        for (int i = 0; i < tRegTable.size(); i++) {
            if (tRegTable.get(i).equals(operand)) {
                return "$t" + i;
            }
        }

        for (int i = 0; i < sRegTable.size(); i++) {
            if (sRegTable.get(i).equals(operand)) {
                return (i == sRegTable.size() - 1) ? "$fp" : ("$s" + i);
            }
        }
        SymItem curItem = null;
        if (!currentFunc.equals("")) {
            for (SymItem item : funcTables.get(currentFunc)) {
                if (item.getUniqueName().equals(operand)) {
                    curItem = item;
                    break;
                }
            }
        }
        if (curItem == null) {
            for (SymItem item : globalTable.symItems) {
                if (item.getUniqueName().equals(operand)) {
                    return (item.getAddr() - globalsize) + "($gp)";
                }
            }
        }
        assert curItem != null;
        return (call_func_sp_offset + curItem.getAddr()) + "($sp)";
    }


    private boolean in_reg(String operand) {
        if (operand.equals("0") || operand.equals("%RTX")) {
            return true;
        }
        if (is_const(operand)) {
            return false;
        }
        for (String s : tRegTable) {
            if (s.equals(operand)) {
                return true;
            }
        }
        for (String s : sRegTable) {
            if (s.equals(operand)) {
                return true;
            }
        }
        return false;
    }

    private boolean is_const(String operand) {
        return utils.begins_num(operand) && !operand.equals("0") && !(operand).endsWith("($sp)") && !(operand).endsWith("($gp)");
    }

    private int sum(ArrayList<Integer> spSize) {
        int sum = 0;
        for (Integer i : spSize) {
            sum += i;
        }
        return sum;
    }

    private boolean is_arithmatic(MidCode.Op instr) {
        return instr.equals(MidCode.Op.ADD) || instr.equals(MidCode.Op.MOD) ||
                instr.equals(MidCode.Op.MUL) || instr.equals(MidCode.Op.DIV) ||
                instr.equals(MidCode.Op.SUB);
    }

    private void gen_assign(String operand1, String operand2) {
        boolean a_in_reg = in_reg(operand1) || assign_reg(operand1, false);
        String a = symbol_to_addr(operand1);

        String reg = "$a1";

        if (a_in_reg) {
            load_value(operand2, a);
        } else {
            boolean b_in_reg = in_reg(operand2) || assign_reg(operand2, false);
            String b = symbol_to_addr(operand2);
            if (b_in_reg) {
                generate("sw", b, a);
            } else if (is_const(operand2)) {
                generate("li", reg, b);
                generate("sw", reg, a);
            } else {
                generate("lw", reg, b);
                generate("sw", reg, a);
            }
        }
    }

    private void save_value(String reg, String operand1) {
        boolean in_reg = in_reg(operand1) || assign_reg(operand1, false);
        String addr = symbol_to_addr(operand1);
        if (in_reg) {
            generate("move " + addr + ", " + reg);
        } else if (!is_const(operand1)) {
            generate("sw", reg, addr);
        } else {
            assert false;
        }
    }

    private void load_value(String operand, String reg) {
        boolean in_reg = in_reg(operand) || assign_reg(operand, true);
        String addr = symbol_to_addr(operand);
        if (in_reg) {
            generate("move", reg, addr);
        } else if (is_const(operand)) {
            generate("li", reg, addr);
        } else {
            generate("lw", reg, addr);
        }
    }

    public void toFile() {
        PrintStream out = System.out;
        try {
            PrintStream os = new PrintStream("mips.txt");
            System.setOut(os);
        } catch (IOException ignored) {
        }
        for (String mipsCode : mipsCodes) {
            System.out.println(mipsCode);
        }

        System.setOut(out);
    }

    public boolean assign_reg(String symbol, boolean only_para) {
        if (symbol.charAt(0) != '#' && !in_global(symbol) && optimize_assign_reg && !is_const(symbol)) {
            SymItem item = findItem(symbol);
            if (item instanceof Var && !only_para) {
                String s_reg = assignSReg(symbol);
                return !s_reg.equals("#INVALID");
            }
        } else if (symbol.charAt(0) == '#' && !only_para && optimize_assign_reg) {
            String t_reg = assignTReg(symbol);
            return !t_reg.equals("#INVALID");
        }
        return false;
    }

    public SymItem findItem(String operand) {
        SymItem curItem = null;
        if (!currentFunc.equals("")) {
            for (SymItem item : funcTables.get(currentFunc)) {
                if (item.getUniqueName().equals(operand)) {
                    curItem = item;
                    break;
                }
            }
        }
        if (curItem == null) {
            for (SymItem item : globalTable.symItems) {
                if (item.getUniqueName().equals(operand)) {
                    curItem = item;
                }
            }
        }
        assert curItem != null;
        return curItem;
    }
}
