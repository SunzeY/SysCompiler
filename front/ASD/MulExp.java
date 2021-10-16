package front.ASD;

import front.Error;
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

    public int getDimension() {
        if (!Ops.isEmpty()) {
            return 0;
        }
        return this.unaryExps.get(0).getDimension();
    }

    public String getName() {
        if (!Ops.isEmpty()) {
            return null;
        }

        return this.unaryExps.get(0).getName();
    }

    public int getValue() throws Error {
        int value = unaryExps.get(0).getValue();
        for (int index = 1; index < unaryExps.size(); index += 1) {
            if (Ops.get(index-1).toString().equals("MULT *")) {
                value = value + unaryExps.get(index).getValue();
            } else {
                value = value / unaryExps.get(index).getValue();
            }
        }
        return value;
    }
}
