package front.ASD;

import java.util.ArrayList;

public class LOrExp implements ASDNode{
    private ArrayList<LAndExp> lAndExps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public LOrExp(ArrayList<LAndExp> lAndExps) {
        this.lAndExps = lAndExps;
        asdNodes.addAll(lAndExps);
    }

    @Override
    public void printTestInfo() {
        lAndExps.get(0).printTestInfo();
        System.out.println("<LOrExp>");
        for (int i = 0; i < lAndExps.size()-1; i++) {
            System.out.println("OR ||");
            lAndExps.get(i+1).printTestInfo();
            System.out.println("<LOrExp>");
        }
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }
}
