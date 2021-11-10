package front.ASD;

import front.Token;
import mid.MidCodeList;

import java.util.ArrayList;

public class RelExp implements ASDNode{

    public ArrayList<Token> Ops;
    public ArrayList<AddExp> addExps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public RelExp(ArrayList<Token> Ops, ArrayList<AddExp> addExps) {
        this.Ops = Ops;
        this.addExps = addExps;
        asdNodes.addAll(addExps);
    }

    @Override
    public void printTestInfo() {
        int i = 0;
        for (AddExp addExp: addExps) {
            addExp.printTestInfo();
            System.out.println("<RelExp>");
            if (i < Ops.size()) {
                System.out.println(Ops.get(i).toString());
            }
            i += 1;
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
