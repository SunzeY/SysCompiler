package front.ASD;

import java.util.ArrayList;

public class ConstInitVal implements ASDNode{

    public enum Type{
        mulInitVal,
        Exp
    }
    private ArrayList<ASDNode> asdNodes;
    private ConstInitVal.Type type;

    public ConstInitVal(ConstInitVal.Type type, ArrayList<ASDNode> asdNodes) {
        this.asdNodes = asdNodes;
        this.type = type;
    }

    @Override
    public void printTestInfo() {
        if (this.type.equals(ConstInitVal.Type.Exp)) {
            this.asdNodes.get(0).printTestInfo();
        } else {
            System.out.println("LBRACE {");
            boolean tag = false;
            for (ASDNode asdNode: asdNodes) {
                if (tag) {
                    System.out.println("COMMA ,");
                }
                asdNode.printTestInfo();
                tag = true;
            }
            System.out.println("RBRACE }");
        }
        System.out.println("<ConstInitVal>");
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
}
