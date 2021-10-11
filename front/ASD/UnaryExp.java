package front.ASD;

import front.SymTable.Func;

import java.util.ArrayList;

public class UnaryExp implements ASDNode{

    public enum Type {
        PrimaryExp, FuncCall, mulUnaryExp
    }
    public Type type;
    public ArrayList<ASDNode> asdNodes;

    public UnaryExp(Type type, ArrayList<ASDNode> asdNodes) {
        this.type = type;
        this.asdNodes = asdNodes;
    }

    @Override
    public void printTestInfo() {
        if (type.equals(Type.PrimaryExp)) {
            asdNodes.get(0).printTestInfo();
        } else if (type.equals(Type.FuncCall)) {
            asdNodes.get(0).printTestInfo();
            System.out.println("LPARENT (");
            if (asdNodes.size()> 1) {
                asdNodes.get(1).printTestInfo();
            }
            System.out.println("RPARENT )");
        } else {
            asdNodes.get(0).printTestInfo();
            asdNodes.get(1).printTestInfo();
        }
        System.out.println("<UnaryExp>");
    }

    public String getFuncCallName() {
        assert (type.equals(Type.FuncCall));
        return ((Indent)asdNodes.get(0)).getName();
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
