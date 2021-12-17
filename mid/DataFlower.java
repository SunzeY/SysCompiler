package mid;

import SymTable.FuncFormVar;
import SymTable.SymItem;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static mid.MidCode.Op.ENTER_WHILE;
import static mid.MidCode.Op.EXIT_BLOCK;
import static mid.MidCode.Op.EXIT_WHILE;
import static mid.MidCode.Op.FUNC;
import static mid.MidCode.Op.NEW_BLOCK;
import static mid.MidCode.Op.SIGNAL_ARR_ADDR;

public class DataFlower {

    public static ArrayList<Block> blocks = new ArrayList<>();

    public static final int START = 0;
    public static final int EXIT = -1;

    public static HashMap<Integer, Block> bno2block = new HashMap<>();

    public static HashMap<String, HashSet<Integer>> var_define_points = new HashMap<>();

    public static HashMap<Integer, MidCode> d_index2code = new HashMap<>();

    public static void define_point_ranking(MidCodeList midCodeList) {
        int d_index = 0;
        for (MidCode midCode : midCodeList.midCodes) {
            String def = midCode.get_def();
            if (def != null && def.charAt(0) != '#') {
                midCode.d_index = d_index;
                d_index2code.put(d_index, midCode);
                d_index += 1;
                if (var_define_points.containsKey(def)) {
                    var_define_points.get(def).add(midCode.d_index);
                } else {
                    var_define_points.put(def, new HashSet<>());
                }
            }
        }
    }

    public static final HashSet<MidCode.Op> HELP_INSTR = new HashSet<MidCode.Op>() {{
        add(SIGNAL_ARR_ADDR);
        add(NEW_BLOCK);
        add(EXIT_BLOCK);
        add(ENTER_WHILE);
        add(EXIT_WHILE);
    }};

    public static void divide_base_block(MidCodeList midCodeList) {
        remove_redundant_label(midCodeList);
        HashMap<MidCode, Integer> allocated_bno = new HashMap<>();
        int cur_bno = 0;
        for (MidCode midCode : midCodeList.midCodes) {
            if (!HELP_INSTR.contains(midCode.instr)) {
                if (midCode.instr == MidCode.Op.JUMP || midCode.instr == MidCode.Op.JUMP_IF || midCode.instr == MidCode.Op.RETURN) {
                    allocated_bno.put(midCode, cur_bno);
                    cur_bno += 1;
                } else if (midCode.instr == MidCode.Op.LABEL || midCode.instr == MidCode.Op.FUNC) {
                    cur_bno += 1;
                    allocated_bno.put(midCode, cur_bno);
                } else {
                    allocated_bno.put(midCode, cur_bno);

                }
            }
        }

        int bno = 0;
        ArrayList<MidCode> block_seq = new ArrayList<>();
        for (MidCode midCode : midCodeList.midCodes) {
            if (allocated_bno.containsKey(midCode)) {
                if (allocated_bno.get(midCode) != bno) {
                    bno = allocated_bno.get(midCode);
                    Block block = new Block(allocated_bno.get(midCode), block_seq);
                    blocks.add(block);
                    bno2block.put(bno, block);
                    block_seq = new ArrayList<>();
                }
                block_seq.add(midCode);
            }
        }

        regulate_block();
        link_base_block();
        compute_in_out();
        compute_in_out_arrive();
    }

    public static void link_base_block() {
        HashMap<String, Block> label2block = new HashMap<>();
        for (Block block : blocks) {
            if (!block.midCodes.isEmpty() && (block.midCodes.get(0).instr == MidCode.Op.LABEL || block.midCodes.get(0).instr == MidCode.Op.FUNC)) {
                if (block.midCodes.get(0).instr == MidCode.Op.LABEL) {
                    label2block.put(block.midCodes.get(0).result, block);
                } else {
                    label2block.put(block.midCodes.get(0).operand2, block);
                }
            }
        }
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);
            Block nextBlock = null;
            if (i < blocks.size() - 1) {
                nextBlock = blocks.get(i + 1);
            }
            if (!block.midCodes.isEmpty()) {
                MidCode lastCode = block.midCodes.get(block.midCodes.size() - 1);
                if (lastCode.instr == MidCode.Op.JUMP) {
                    block.successors.add(label2block.get(lastCode.result).bno);
                    label2block.get(lastCode.result).precursors.add(block.bno);
                } else if (lastCode.instr == MidCode.Op.JUMP_IF) {
                    block.successors.add(label2block.get(lastCode.result).bno);
                    label2block.get(lastCode.result).precursors.add(block.bno);
                    if (nextBlock != null) {
                        block.successors.add(nextBlock.bno);
                        nextBlock.precursors.add(block.bno);
                    }
                } else if (lastCode.instr == MidCode.Op.CALL) {
                    block.successors.add(EXIT);
                    label2block.get(lastCode.operand1).precursors.add(0);
                } else if (lastCode.instr == MidCode.Op.RETURN || nextBlock == null) {
                    block.successors.add(EXIT);
                } else {
                    block.successors.add(nextBlock.bno);
                    nextBlock.precursors.add(block.bno);
                }

            }
        }
    }

    public static void regulate_block() {
        ArrayList<Block> new_blocks = new ArrayList<>();
        for (Block block : blocks) {
            if (!block.midCodes.isEmpty()) {
                new_blocks.add(block);
            }
        }
        if (new_blocks.get(0).midCodes.get(0).instr != MidCode.Op.FUNC) { // global_var_define
            new_blocks.remove(0);
        }
        blocks = new_blocks;
    }

    private static void remove_redundant_label(MidCodeList midCodeList) {
        ArrayList<MidCode> new_midCodes = new ArrayList<>();
        ArrayList<String> exist_jump_labels = new ArrayList<>();
        for (MidCode midCode : midCodeList.midCodes) {
            if (midCode.instr == MidCode.Op.JUMP || midCode.instr == MidCode.Op.JUMP_IF) {
                exist_jump_labels.add(midCode.result);
            }
        }

        for (MidCode midCode : midCodeList.midCodes) {
            if (midCode.instr == MidCode.Op.LABEL && !exist_jump_labels.contains(midCode.result)) {
                continue;
            }
            new_midCodes.add(midCode);
        }
        midCodeList.midCodes = new_midCodes;
    }

    public static void compute_in_out() {
        boolean modified;
        do {
            modified = false;
            for (int i = blocks.size() - 1; i >= 0; i--) {
                Block block = blocks.get(i);
                for (int successor_bno : block.successors) {
                    if (successor_bno != EXIT) block.out.addAll(bno2block.get(successor_bno).in);
                }
                HashSet<String> new_in = new HashSet<>(block.use);
                HashSet<String> out_minus_def = new HashSet<>(block.out);
                out_minus_def.removeAll(block.def);
                new_in.addAll(out_minus_def);
                if (modified_s(block.in, new_in)) {
                    modified = true;
                    block.in = new_in;
                }
            }
        } while (modified);
    }

    public static void compute_in_out_arrive() {
        boolean modified;
        do {
            modified = false;
            for (Block block : blocks) {
                for (int precursor_bno : block.precursors) {
                    if (precursor_bno != START) block.in_arrive.addAll(bno2block.get(precursor_bno).out_arrive);
                }
                HashSet<Integer> new_out = new HashSet<Integer>(block.gen);
                HashSet<Integer> in_minus_kill = new HashSet<Integer>(block.in_arrive);
                in_minus_kill.removeAll(block.kill);
                new_out.addAll(in_minus_kill);
                if (modified_i(block.out_arrive, new_out)) {
                    modified = true;
                    block.out_arrive = new_out;
                }
            }
        } while (modified);
    }

    private static boolean modified_s(HashSet<String> l1, HashSet<String> l2) {
        HashSet<String> remains1 = new HashSet<>(l1);
        remains1.removeAll(l2);
        HashSet<String> remains2 = new HashSet<>(l2);
        remains2.removeAll(l1);
        return !(remains1.isEmpty() && remains2.isEmpty());
    }

    private static boolean modified_i(HashSet<Integer> l1, HashSet<Integer> l2) {
        HashSet<Integer> remains1 = new HashSet<>(l1);
        remains1.removeAll(l2);
        HashSet<Integer> remains2 = new HashSet<>(l2);
        remains2.removeAll(l1);
        return !(remains1.isEmpty() && remains2.isEmpty());
    }


    public static void remove_redundant() {
        for (Block block : blocks) {
            block.remove_redundant_code();
        }
    }

    public static void const_broadcast() {
        for (Block block : blocks) {
            block.const_broad_cast();
            block.const_broad_cast_by_arrival();
        }
    }


    public static void printBlockInfo(String filename) {
        PrintStream out = System.out;
        try {
            PrintStream os = new PrintStream(filename);
            System.setOut(os);
        } catch (IOException ignored) {
        }
        for (Block block : blocks) {
            System.out.println("=================================================");
            System.out.println("current_bno: " + block.bno);
            System.out.println("precursor_bnos: " + block.precursors);
            System.out.println("successor_bnos: " + block.successors);
            System.out.println("use: " + block.get_use());
            System.out.println("def: " + block.get_def());
            System.out.println("in: " + block.in);
            System.out.println("out: " + block.out);
            System.out.println("#################################################");
            for (MidCode midCode : block.midCodes) {
                System.out.println(midCode);
            }
            System.out.println("=================================================");
            System.out.println();
        }
        System.setOut(out);
    }

    public static void refresh(MidCodeList midCodeList) {
        blocks = new ArrayList<>();
        bno2block = new HashMap<>();
        var_define_points = new HashMap<>();
        d_index2code = new HashMap<>();
        for (MidCode midCode: midCodeList.midCodes) {
            midCode.d_index = null;
        }
    }
}
