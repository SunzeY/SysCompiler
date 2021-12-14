package front.ASD;

import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        return this.asdNodes.get(0).gen_mid(midCodeList);
    }


    public void gen_mid_opt(MidCodeList midCodeList, String else_label, String endIf, ASDNode stmt) {
        this.lOrExp.gen_mid_opt(midCodeList, else_label, endIf, stmt);
    }
}
