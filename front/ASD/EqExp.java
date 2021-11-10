package front.ASD;

import front.Token;
import mid.MidCodeList;

import java.util.ArrayList;

public class EqExp implements ASDNode{

    public ArrayList<Token> Ops;
    public ArrayList<RelExp> relExps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public EqExp(ArrayList<Token> Ops, ArrayList<RelExp> relExps) {
        this.Ops = Ops;
        this.relExps = relExps;
        asdNodes.addAll(relExps);
    }

    @Override
    public void printTestInfo() {
        relExps.get(0).printTestInfo();
        System.out.println("<EqExp>");
        for (int i = 0; i < Ops.size(); i++) {
            System.out.println(Ops.get(i).toString());
            relExps.get(i+1).printTestInfo();
            System.out.println("<EqExp>");
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
