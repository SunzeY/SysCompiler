package front.ASD;

import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        return this.addExp.gen_mid(midCodeList);
    }

    public int getDimension() {
        return addExp.getDimension();
    }

    public String getName() {
        return this.addExp.getName();
    }
}
