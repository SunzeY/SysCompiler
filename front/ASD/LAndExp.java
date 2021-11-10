package front.ASD;

import mid.MidCodeList;

import java.util.ArrayList;

public class LAndExp implements ASDNode{
    private ArrayList<EqExp> eqExps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public LAndExp(ArrayList<EqExp> eqExps) {
        this.eqExps = eqExps;
        asdNodes.addAll(eqExps);
    }

    @Override
    public void printTestInfo() {
        eqExps.get(0).printTestInfo();
        System.out.println("<LAndExp>");
        for (int i = 0; i < eqExps.size()-1; i++) {
            System.out.println("AND &&");
            eqExps.get(i+1).printTestInfo();
            System.out.println("<LAndExp>");
        }
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
}
