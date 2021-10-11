package front.ASD;

import java.util.ArrayList;

public class Cond implements ASDNode{

    private LOrExp lOrExp;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
        asdNodes.add(lOrExp);
    }

    @Override
    public void printTestInfo() {
        lOrExp.printTestInfo();
        System.out.println("<Cond>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }
}
