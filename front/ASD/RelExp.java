package front.ASD;

import front.Token;
import mid.MidCode;
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
        AddExp left = addExps.get(0);
        String true_label = "";
        String bool_var = "";
        if (addExps.size() == 1) {
            return addExps.get(0).gen_mid(midCodeList);
        }
        String left_ans = left.gen_mid(midCodeList);
        for (int i = 1; i < addExps.size(); i++) {
            bool_var = midCodeList.add(MidCode.Op.ASSIGN, "#AUTO", "1", "#VACANT");
            String op = Ops.get(i-1).getString();
            AddExp right = addExps.get(i);
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
