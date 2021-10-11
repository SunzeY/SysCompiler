package front.ASD;

import java.util.ArrayList;

public class FuncType implements ASDNode{



    public enum Type {
        Int, Void
    }

    private Type type;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public FuncType(Type type) {
        this.type = type;
    }

    @Override
    public void printTestInfo() {
        if (type.equals(Type.Int)) {
            System.out.println("INTTK int");
        } else {
            System.out.println("VOIDTK void");
        }
        System.out.println("<FuncType>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Type getType() {
        return this.type;
    }
}
