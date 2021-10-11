package front.ASD;

import java.util.ArrayList;

public class Number implements ASDNode{
    private IntConst intConst;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Number(IntConst intConst) {
        this.intConst = intConst;
        this.asdNodes.add(intConst);
    }

    @Override
    public void printTestInfo() {
        intConst.printTestInfo();
        System.out.println("<Number>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }
}
