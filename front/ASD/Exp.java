package front.ASD;

import java.util.ArrayList;

public class Exp implements ASDNode{
    private AddExp addExp;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Exp(AddExp addExp) {
        this.addExp = addExp;
        asdNodes.add(addExp);
    }

    @Override
    public void printTestInfo() {
        addExp.printTestInfo();
        System.out.println("<Exp>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }
}
