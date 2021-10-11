package front.ASD;

import java.util.ArrayList;

public class Stmt implements ASDNode{

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
                for (ASDNode asdNode: asdNodes.subList(1, asdNodes.size())) {
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

    public Type getType() {
        return this.type;
    }
}
