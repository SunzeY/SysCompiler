package front.ASD;

import front.Token;
import mid.MidCode;
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
        RelExp left = relExps.get(0);
        String true_label = "";
        String bool_var = "";
        if (relExps.size() == 1) {
            return relExps.get(0).gen_mid(midCodeList);
        }
        String left_ans = left.gen_mid(midCodeList);
        for (int i = 1; i < relExps.size(); i++) {
            bool_var = midCodeList.add(MidCode.Op.ASSIGN, "#AUTO", "1", "#VACANT");
            String op = Ops.get(i-1).getString();
            RelExp right = relExps.get(i);
            String right_ans = right.gen_mid(midCodeList);
            true_label = midCodeList.add(MidCode.Op.JUMP_IF, left_ans + " " + right_ans, op, "#AUTO_LABEL");
            midCodeList.add(MidCode.Op.ASSIGN, bool_var, "0", "#VACANT");
            midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", true_label);
            left_ans = bool_var;
        }
        assert !bool_var.equals("");
        return bool_var;
    }
}
