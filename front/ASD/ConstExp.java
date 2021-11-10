package front.ASD;

import front.Error;
import mid.MidCodeList;

import java.util.ArrayList;

public class ConstExp implements ASDNode{
    private AddExp addExp;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
        asdNodes.add(addExp);
    }

    @Override
    public void printTestInfo() {
        addExp.printTestInfo();
        System.out.println("<ConstExp>");
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
        return null;
    }

    public int getValue() throws Error {
        return this.addExp.getValue();
    }
}
