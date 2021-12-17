package front.ASD;

import front.Compiler;
import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class Stmt implements ASDNode {

    public enum Type {
        Assign, Exp, ifBranch, whileBranch, breakStmt, continueStmt,
        returnStmt, input, output, None, Block
    }

    private Type type;
    private ArrayList<ASDNode> asdNodes;


    public Stmt(Type type, ArrayList<ASDNode> asdNodes) {
        this.type = type;
        this.asdNodes = asdNodes;
    }

    @Override
    public void printTestInfo() {
        switch (type) {
            case Assign:
                asdNodes.get(0).printTestInfo();
                System.out.println("ASSIGN =");
                asdNodes.get(1).printTestInfo();
                System.out.println("SEMICN ;");
                break;
            case Exp:
                asdNodes.get(0).printTestInfo();
                System.out.println("SEMICN ;");
                break;
            case Block:
                asdNodes.get(0).printTestInfo();
                break;
            case ifBranch:
                System.out.println("IFTK if");
                System.out.println("LPARENT (");
                asdNodes.get(0).printTestInfo();
                System.out.println("RPARENT )");
                asdNodes.get(1).printTestInfo();
                if (asdNodes.size() > 2) {
                    System.out.println("ELSETK else");
                    asdNodes.get(2).printTestInfo();
                }
                break;
            case whileBranch:
                System.out.println("WHILETK while");
                System.out.println("LPARENT (");
                asdNodes.get(0).printTestInfo();
                System.out.println("RPARENT )");
                asdNodes.get(1).printTestInfo();
                break;
            case breakStmt:
                System.out.println("BREAKTK break");
                System.out.println("SEMICN ;");
                break;
            case continueStmt:
                System.out.println("CONTINUETK continue");
                System.out.println("SEMICN ;");
                break;
            case returnStmt:
                System.out.println("RETURNTK return");
                if (asdNodes.size() == 2) {
                    asdNodes.get(1).printTestInfo();
                }
                System.out.println("SEMICN ;");
                break;
            case input:
                asdNodes.get(0).printTestInfo();
                System.out.println("ASSIGN =");
                System.out.println("GETINTTK getint");
                System.out.println("LPARENT (");
                System.out.println("RPARENT )");
                System.out.println("SEMICN ;");
                break;
            case output:
                System.out.println("PRINTFTK printf");
                System.out.println("LPARENT (");
                boolean flag = false;
                for (ASDNode asdNode : asdNodes.subList(1, asdNodes.size())) {
                    if (flag) {
                        System.out.println("COMMA ,");
                    }
                    asdNode.printTestInfo();
                    flag = true;
                }
                System.out.println("RPARENT )");
                System.out.println("SEMICN ;");
                break;
            case None:
                System.out.println("SEMICN ;");
            default:
                break;
        }
        System.out.println("<Stmt>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        if (asdNodes == null) {
            return new ArrayList<>();
        }
        return asdNodes;
    }

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        String result = "";
        switch (type) {
            case Assign:
                String name = this.asdNodes.get(0).gen_mid(midCodeList);
                if (name.contains("[")) {
                    String tmp = midCodeList.add(MidCode.Op.ASSIGN,
                                "#AUTO",
                                this.asdNodes.get(1).gen_mid(midCodeList), "#VACANT");
                    midCodeList.add(MidCode.Op.ARR_SAVE, name, tmp, "#VACANT");
                } else {
                    result = midCodeList.add(MidCode.Op.ASSIGN,
                            this.asdNodes.get(0).gen_mid(midCodeList),
                            this.asdNodes.get(1).gen_mid(midCodeList), "#VACANT");
                }
                break;
            case Exp:
            case Block:
                result = this.asdNodes.get(0).gen_mid(midCodeList);
                break;
            case ifBranch:
                if (!Compiler.branch_opt) {
                    String bool_value = asdNodes.get(0).gen_mid(midCodeList);
                    if (asdNodes.size() == 2) { //no else
                        String endIf = midCodeList.add(MidCode.Op.JUMP_IF, bool_value + " " + 0, "==", "#AUTO_LABEL");
                        asdNodes.get(1).gen_mid(midCodeList);
                        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACNAT", endIf);
                    } else {
                        String else_label = midCodeList.add(MidCode.Op.JUMP_IF, bool_value + " " + 0, "==", "#AUTO_LABEL");
                        asdNodes.get(1).gen_mid(midCodeList);
                        String endIf = midCodeList.add(MidCode.Op.JUMP, "#VACANT", "#VACANT", "#AUTO_LABEL");
                        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACNAT", else_label);
                        asdNodes.get(2).gen_mid(midCodeList);
                        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACNAT", endIf);
                    }
                } else { // branch_opt
                    if (asdNodes.size() == 2) {
                        String endIf = midCodeList.alloc_label();
                        ((Cond) asdNodes.get(0)).gen_mid_opt(midCodeList, endIf, endIf, (Stmt) asdNodes.get(1));
                        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACNAT", endIf);
                    } else {
                        String else_label = midCodeList.alloc_label();
                        String endIf = midCodeList.alloc_label();
                        ((Cond) asdNodes.get(0)).gen_mid_opt(midCodeList, else_label, endIf, (Stmt) asdNodes.get(1));
                        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACNAT", else_label);
                        asdNodes.get(2).gen_mid(midCodeList);
                        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACNAT", endIf);
                    }
                }
                break;
            case whileBranch:
                // convert into if(cond)(do{block}while(cond))
                if (!Compiler.branch_opt) {
                    String judge_value = midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", "#AUTO_LABEL");
                    String cond_bool_value = asdNodes.get(0).gen_mid(midCodeList);
                    String end_loop = midCodeList.add(MidCode.Op.JUMP_IF, cond_bool_value + " " + 0, "==", "#AUTO_LABEL");
                    String begin_loop = midCodeList.alloc_label();
                    midCodeList.add(MidCode.Op.WHILE_BIND, judge_value, end_loop, "#VACANT");
                    midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", begin_loop);
                    asdNodes.get(1).gen_mid(midCodeList);
                    midCodeList.begin_tables.pop();
                    midCodeList.end_tables.pop();
                    String re_cond_bool_value = asdNodes.get(0).gen_mid(midCodeList);
                    midCodeList.add(MidCode.Op.JUMP_IF, re_cond_bool_value + " " + 0, "!=", begin_loop);
                    midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", end_loop);
                } else {
                    String judge_value = midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", "#AUTO_LABEL");
                    String begin_loop = midCodeList.alloc_label();
                    ASDNode jump = new Jump(begin_loop);
                    String endJudgeIf = midCodeList.alloc_label();
                    ((Cond) asdNodes.get(0)).gen_mid_opt(midCodeList, endJudgeIf, endJudgeIf, jump);
                    midCodeList.add(MidCode.Op.WHILE_BIND, judge_value, endJudgeIf, "#VACANT");
                    midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", begin_loop);
                    midCodeList.add(MidCode.Op.ENTER_WHILE, "#VACANT", "#VACANT", "#VACANT");
                    asdNodes.get(1).gen_mid(midCodeList);
                    midCodeList.begin_tables.pop();
                    midCodeList.end_tables.pop();
                    ((Cond) asdNodes.get(0)).gen_mid_opt(midCodeList, endJudgeIf, endJudgeIf, jump);
                    midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", endJudgeIf);
                    midCodeList.add(MidCode.Op.EXIT_WHILE, "#VACANT", "#VACANT", "#VACANT");
                }
                break;
            case breakStmt:
                String end = midCodeList.end_tables.peek();
                assert end != null;
                midCodeList.add(MidCode.Op.JUMP, "#VACANT", "#VACANT", end);
                break;
            case continueStmt:
                String begin = midCodeList.begin_tables.peek();
                assert begin != null;
                midCodeList.add(MidCode.Op.JUMP, "#VACANT", "#VACANT", begin);
                break;
            case returnStmt:
                result = midCodeList.add(MidCode.Op.RETURN,
                        (asdNodes.size() == 2) ? asdNodes.get(1).gen_mid(midCodeList) : "#VACANT",
                        "#VACANT",
                        "#VACANT");
                break;
            case input:
                result = midCodeList.add(MidCode.Op.GETINT,
                        this.asdNodes.get(0).gen_mid(midCodeList),
                        "#VACANT",
                        "#VACANT");
                break;
            case output:
                // midCodeList.add(MidCode.Op.PRINT, ((FormatString)asdNodes.get(1)).toString(), "#VACANT", "#VACANT");
                String[] cutFStrings = ((FormatString) asdNodes.get(1)).getString().split("%d");
                int i = 0;
                for (ASDNode asdNode : asdNodes.subList(2, asdNodes.size())) {
                    if (i < cutFStrings.length && !cutFStrings[i].equals("")) {
                        midCodeList.add(MidCode.Op.PRINT, cutFStrings[i], "#STRCONS", "#VACANT");
                    }
                    midCodeList.add(MidCode.Op.PRINT, asdNode.gen_mid(midCodeList), "#VACANT", "#VACANT");
                    i += 1;
                }
                if (i < cutFStrings.length && !cutFStrings[i].equals("")) {
                    midCodeList.add(MidCode.Op.PRINT, cutFStrings[i], "#STRCONS", "#VACANT");
                }
                break;
            case None:
            default:
                break;
        }
        return result;
    }

    public Type getType() {
        return this.type;
    }
}
