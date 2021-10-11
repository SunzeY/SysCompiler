package front.ASD;

import front.Token;

import java.util.ArrayList;

public class MulExp implements ASDNode{

    public ArrayList<Token> Ops;
    public ArrayList<UnaryExp> unaryExps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public MulExp(ArrayList<Token> Ops, ArrayList<UnaryExp> unaryExps) {
        this.Ops = Ops;
        this.unaryExps = unaryExps;
        asdNodes.addAll(unaryExps);
    }

    @Override
    public void printTestInfo() {
        int i = 0;
        for (UnaryExp unaryExp: unaryExps) {
            unaryExp.printTestInfo();
            System.out.println("<MulExp>");
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
