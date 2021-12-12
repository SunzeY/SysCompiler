package front.ASD;

import mid.MidCode;
import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        String true_label = "";
        String bool_var = "";
        if (lAndExps.size() == 1) {
            return lAndExps.get(0).gen_mid(midCodeList);
        }
        bool_var = midCodeList.add(MidCode.Op.ASSIGN, "#AUTO", "1", "#VACANT");
        for (LAndExp eqExp : lAndExps) {
            String ans = eqExp.gen_mid(midCodeList);
            if (true_label.equals("")) {
                true_label = midCodeList.add(MidCode.Op.JUMP_IF, ans + " " + "0", "!=", "#AUTO_LABEL");
            } else {
                midCodeList.add(MidCode.Op.JUMP_IF, ans + " " + "0", "!=", true_label);
            }
        }
        midCodeList.add(MidCode.Op.ASSIGN, bool_var, "0", "#VACANT");
        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", true_label);
        assert !bool_var.equals("");
        return bool_var;
    }
}
