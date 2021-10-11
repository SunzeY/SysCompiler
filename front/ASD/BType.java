package front.ASD;

import java.util.ArrayList;

public class BType implements ASDNode{

    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public enum Type{
        Int
    }
    private Type type;

    public BType(Type type) {
        this.type = type;
    }

    @Override
    public void printTestInfo() {
        System.out.println("INTTK int");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }
}
