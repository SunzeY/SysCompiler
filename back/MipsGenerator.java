package back;

import SymTable.SymItem;
import SymTable.SymbolTable;
import mid.MidCode;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MipsGenerator {
    public boolean optimize_mul_div = false;
    public boolean optimize_assign_reg = false;

    public static final int LOCAL_ADDR_INIT = 100;
    public static final int STACK_T_BEGIN = 56;
    public static final int STACK_S_BEGIN = 24;
    public static final String STACK_RA = "0($sp)";

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
        for (int i = 0; i < 8; i += 1) {
            add("#VACANT");
        }
    }};
    public ArrayList<String> tRegTable = new ArrayList<String>() {{
        for (int i = 0; i < 10; i += 1) {
            add("#VACANT");
        }
    }};

    public String assignSReg(String name) {
        for (int i = 0; i < 8; i++) {
            if (sRegTable.get(i).equals("#VACANT")) {
                sRegTable.set(i, name);
                return "$s" + i;
            }
        }
        return "#INCALID";
    }

    public HashMap<MidCode.Op, String> mipsInstr = new HashMap<MidCode.Op, String>() {{
        put(MidCode.Op.ADD, "addu");
        put(MidCode.Op.SUB, "sub");
        put(MidCode.Op.MUL, "mul");
        put(MidCode.Op.DIV, "div");
        put(MidCode.Op.MOD, ""); //TODO
    }};

    int call_func_sp_offset = 0;

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
        generate(instr + " " + num1 + ", " + num2 + ", " + num3);
        if (instr.equals("addu") || instr.equals("subu") || instr.equals("mul") || instr.equals("div") || instr.equals("sll") || instr.equals("sra")) {
            if (!num1.equals(num2)) {
                release(num2);
            }
            if (!num1.equals(num3)) {
                release(num3);
            }
        } else if (instr.equals("beq") || instr.equals("bne")) {
            release(num1);
        }
    }

    private void release(String addr) {
        if (addr.charAt(0) == '$' && addr.charAt(1) == 't') {
            tRegTable.set(addr.charAt(2) - '0', "#VACANT");
            generate("# RELEASE" + addr);
        }
    }

    int para_number = 0;

    public void translate() {
        generate(".data");
//        for (front.SymTable.SymItem item: block2table.get(new int[]{0, 0}).symItems) {
//            //TODO array
//        }
        for (int i = 0; i < strCons.size(); i += 1) {
            mipsCodes.add("str" + i + ": .asciiz" + " \"" + strCons.get(i) + "\"");
        }
        generate(".text");
        boolean init = true;

        for (MidCode code : midCodes) {
            mipsCodes.add("# ====" + code + "====");
            MidCode.Op instr = code.instr;
            String operand1 = code.operand1;
            String operand2 = code.operand2;
            String result = code.result;
            if (instr.equals(MidCode.Op.FUNC)) { //new Function
                if (init) {
                    mipsCodes.add("addi $sp, $sp, -" + funcStackSize.get("main") + LOCAL_ADDR_INIT);
                    mipsCodes.add("j main");
                    init = false;
                }

                currentFunc = operand2;
                mipsCodes.add(operand2 + ":");
                call_func_sp_offset = 0;
                for (int i = 0; i < 8; i++) {
                    sRegTable.set(i, "#VACANT");
                }
                for (SymItem item : funcTables.get(currentFunc)) {
                    String sReg = assignSReg(item.getUniqueName());
                    if (!sReg.equals("#INVALID")) {
                        generate("lw", sReg, (item.getAddr() + call_func_sp_offset) + "($sp)");
                    } else {
                        break;
                    }
                }

            } else if (instr.equals(MidCode.Op.RETURN)) {
                if (!operand1.equals("#VACANT")) {
                    load_value(operand1, "$v0");
                }
                if (currentFunc.equals("main")) {
                    mipsCodes.add("li $v0, 10");
                    mipsCodes.add("syscall");
                } else {
                    mipsCodes.add("jr $ra");
                }

            } else if (instr.equals(MidCode.Op.PREPARE_CALL)) {
                para_number = 0;
                spSize.add(LOCAL_ADDR_INIT + funcStackSize.get(operand1));
                call_func_sp_offset = sum(spSize);
                generate("addi $sp, $sp, -" + spSize.get(spSize.size() - 1));

            } else if (instr.equals(MidCode.Op.PUSH_PARA)) {
                String para_addr = funcTables.get(currentFunc).get(para_number).getAddr().toString();
                String reg = "$a1";

                boolean b_in_reg = in_reg(operand1);
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

            } else if (instr.equals(MidCode.Op.CALL)) {
                //protect tRegs
                show_reg_status();
                ArrayList<Integer> saved_s = new ArrayList<>(), saved_t = new ArrayList<>();
                ArrayList<String> t_old = (ArrayList<String>) tRegTable.clone();
                for (int i = 0; i < 10; i += 1) {
                    if (!tRegTable.get(i).equals("#VACANT")) {
                        saved_t.add(i);
                        generate("sw", "$t" + i, (STACK_T_BEGIN + 4 * i) + "($sp)");
                        tRegTable.set(i, "#VACANT");
                    }
                }
                ArrayList<String> s_old = (ArrayList<String>) sRegTable.clone();
                for (int i = 0; i < 8; i += 1) {
                    if (!sRegTable.get(i).equals("#VACANT")) {
                        saved_s.add(i);
                        generate("sw", "$s" + i, (STACK_S_BEGIN + 4 * i) + "($sp)");
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
                    generate("lw", "$s" + i, (STACK_S_BEGIN + 4 * i) + "($sp)");
                }
                sRegTable = s_old;

                //caller recover tRegs
                for (int i : saved_t) {
                    generate("lw", "$t" + i, STACK_T_BEGIN + 4 * i + "(sp)");
                }
                tRegTable = t_old;
                if (spSize.get(spSize.size() - 1) != 0) {
                    generate("addi $sp, $sp, " + spSize.get(spSize.size() - 1));
                    spSize.remove(spSize.size() - 1);
                }
                call_func_sp_offset = sum(spSize);

            } else if (instr.equals(MidCode.Op.PRINT)) {
                if (operand1.charAt(0) == '#') {
                    generate("la $a0, " + operand1.substring(1));
                    generate("li $v0, 4");
                } else {
                    load_value(operand1, "$a0");
                    generate("li $v0, 1");
                }
                generate("syscall");

            } else if (instr.equals(MidCode.Op.GETINT)) {
                generate("li $v0, 5");
                generate("syscall");
                save_value("$v0", operand1);
            } else if (instr.equals(MidCode.Op.ASSIGN) || instr.equals(MidCode.Op.VAR_DEF) || instr.equals(MidCode.Op.CONST_DEF)) {
                if (!operand2.equals("#VACANT")) {
                    gen_assign(operand1, operand2);
                }
            } else if (is_arithmatic(instr)) {
                String mips_instr = mipsInstr.get(instr);
                String reg1 = "$a1";
                String reg2 = "$a2";
                boolean a_in_reg = in_reg(result);
                boolean b_in_reg_or_const = is_const(operand1) || in_reg(operand1);
                boolean c_in_reg_or_const = is_const(operand2) || in_reg(operand2);

                String a = symbol_to_addr(result);
                String b = symbol_to_addr(operand1);
                String c = symbol_to_addr(operand2);

//                boolean is_2_pow_1 = is_const(code.operand1) && utils.is_2_power(Integer.getInteger(code.operand1));
//                boolean is_2_pow_2 = is_const(code.operand2) && utils.is_2_power(Integer.getInteger(code.operand2));

                boolean sra = false;

                if (a_in_reg) {
                    if (b_in_reg_or_const && c_in_reg_or_const) {
                        gen_arithmetic(mips_instr, a, b, c);
                    } else if (b_in_reg_or_const) {
                        generate("lw", reg1, c);
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
                        gen_arithmetic(mips_instr, reg1, b, c);
                        generate("sw", reg1, a);
                    } else if (b_in_reg_or_const) {
                        generate("lw", reg1, c);
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
            }

        }
    }

    private void gen_arithmetic(String mips_instr, String num1, String num2, String num3) {
        String reg3 = "$a3";
        if (is_const(num2)) {
            if (mips_instr.equals("addu")) {
                generate("addiu", num1, num3, num2);
            }
        } else if (is_const(num3)) {
            generate("addiu", num1, num2, num3);
        } else {
            generate("addu", num1, num2, num3);
        }
    }

    private void show_reg_status() {
        System.out.println("^^^^^^^^^^REG_TABLE^^^^^^^^^^");
        for (int i = 0; i < 10; i += 1) {
            System.out.println("$t" + i + ": " + tRegTable.get(i));
        }
        for (int i = 0; i < 8; i += 1) {
            System.out.println("$s" + i + ": " + sRegTable.get(i));
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

        for (int i = 0; i < 10; i++) {
            if (tRegTable.get(i).equals(operand)) {
                return "$t" + i;
            }
        }

        for (int i = 0; i < 8; i++) {
            if (sRegTable.get(i).equals(operand)) {
                return "$s" + i;
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
                    return item.getAddr() - LOCAL_ADDR_INIT + "($gp)";
                }
            }
        }
        assert curItem != null;
        return (call_func_sp_offset - curItem.getAddr()) + "($sp)";
    }


    private boolean in_reg(String operand) {
        if (operand.equals("0") || operand.equals("%RTX")) {
            return true;
        }
        if (is_const(operand)) {
            return false;
        }
        for (int i = 0; i < 10; i++) {
            if (tRegTable.get(i).equals(operand)) {
                return true;
            }
        }
        for (int i = 0; i < 8; i++) {
            if (sRegTable.get(i).equals(operand)) {
                return true;
            }
        }
        return false;
    }

    private boolean is_const(String operand) {
        return utils.begins_num(operand) && !operand.equals("0");
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
                instr.equals(MidCode.Op.MUL) || instr.equals(MidCode.Op.DIV);
    }

    private void gen_assign(String operand1, String operand2) {
        boolean a_in_reg = in_reg(operand1);
        String a = symbol_to_addr(operand1);

        String reg = "$a1";

        if (a_in_reg) {
            load_value(operand2, a);
        } else {
            boolean b_in_reg = in_reg(operand2);
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
        boolean in_reg = in_reg(operand1);
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
        boolean in_reg = in_reg(operand);
        String addr = symbol_to_addr(operand);
        if (in_reg) {
            generate("move", reg, addr);
        } else if (is_const(operand)) {
            generate("li", reg, addr);
        } else {
            generate("lw", reg, addr);
        }
    }

    public void toFile() throws FileNotFoundException {
        System.setOut(new PrintStream("mips.txt"));
        for (String mipsCode : mipsCodes) {
            System.out.println(mipsCode);
        }
    }
}
