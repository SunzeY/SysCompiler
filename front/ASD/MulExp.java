package front.ASD;

import front.Error;
import front.Token;
import mid.MidCode;
import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        try {
            return Integer.toString(this.getValue());
        } catch (Error ignored) {
        }
        String op1 = unaryExps.get(0).gen_mid(midCodeList);
        String result = op1;
        for (int i =0; i < Ops.size(); i += 1) {
            String op2 = unaryExps.get(i+1).gen_mid(midCodeList);
            mid.MidCode.Op op = Ops.get(i).getTokenClass().equals("MULT") ? mid.MidCode.Op.MUL :
                                Ops.get(i).getTokenClass().equals("DIV") ? mid.MidCode.Op.DIV : mid.MidCode.Op.MOD;
            result = midCodeList.add(op, op1, op2, "#AUTO");
            op1 = result;
        }
        return result;
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
                value = value * unaryExps.get(index).getValue();
            } else if (Ops.get(index-1).toString().equals("DIV /")){
                value = value / unaryExps.get(index).getValue();
            } else {
                value = value % unaryExps.get(index).getValue();
            }
        }
        return value;
    }
}
