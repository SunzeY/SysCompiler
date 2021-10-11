package front.ASD;

import front.Token;

import java.util.ArrayList;

public class AddExp implements ASDNode{
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public ArrayList<Token> Ops;
    public ArrayList<MulExp> mulExps;

    public AddExp(ArrayList<Token> Ops, ArrayList<MulExp> mulExps) {
        this.Ops = Ops;
        this.mulExps = mulExps;
        asdNodes.addAll(mulExps);
    }

    @Override
    public void printTestInfo() {
        int i = 0;
        for (MulExp mulExp: mulExps) {
            mulExp.printTestInfo();
            System.out.println("<AddExp>");
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
}
