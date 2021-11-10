package front.ASD;

import front.Error;
import front.Token;
import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        String op1 = mulExps.get(0).gen_mid(midCodeList);
        String result = op1;
        for (int i =0; i < Ops.size(); i += 1) {
            String op2 = mulExps.get(i+1).gen_mid(midCodeList);
            mid.MidCode.Op op = Ops.get(i).getTokenClass().equals("PLUS") ? mid.MidCode.Op.ADD : mid.MidCode.Op.SUB;
            result = midCodeList.add(op, op1, op2, "#AUTO");
            op1 = result;
        }
        return result;
    }

    public int getDimension() {
        if (!Ops.isEmpty()) {
            return 0;
        }
        return this.mulExps.get(0).getDimension();
    }

    public String getName() {
        if (!Ops.isEmpty()) {
            return null;
        }
        return this.mulExps.get(0).getName();
    }

    public int getValue() throws Error {
        int value = mulExps.get(0).getValue();
        for (int index = 1; index < mulExps.size(); index += 1) {
            if (Ops.get(index-1).toString().equals("PLUS +")) {
                value = value + mulExps.get(index).getValue();
            } else {
                value = value - mulExps.get(index).getValue();
            }
        }
        return value;
    }
}
