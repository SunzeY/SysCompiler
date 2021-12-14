package front.ASD;

import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class LAndExp implements ASDNode{
    private ArrayList<EqExp> eqExps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public LAndExp(ArrayList<EqExp> eqExps) {
        this.eqExps = eqExps;
        asdNodes.addAll(eqExps);
    }

    @Override
    public void printTestInfo() {
        eqExps.get(0).printTestInfo();
        System.out.println("<LAndExp>");
        for (int i = 0; i < eqExps.size()-1; i++) {
            System.out.println("AND &&");
            eqExps.get(i+1).printTestInfo();
            System.out.println("<LAndExp>");
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
        String false_label = "";
        String bool_var = "";
        if (eqExps.size() == 1) {
            return eqExps.get(0).gen_mid(midCodeList);
        }
        bool_var = midCodeList.add(MidCode.Op.ASSIGN, "#AUTO", "0", "#VACANT");
        for (EqExp eqExp : eqExps) {
            String ans = eqExp.gen_mid(midCodeList);
            if (false_label.equals("")) {
                false_label = midCodeList.add(MidCode.Op.JUMP_IF, ans + " " + "0", "==", "#AUTO_LABEL");
            } else {
                midCodeList.add(MidCode.Op.JUMP_IF, ans + " " + "0", "==", false_label);
            }
        }
        midCodeList.add(MidCode.Op.ASSIGN, bool_var, "1", "#VACANT");
        midCodeList.add(MidCode.Op.LABEL, "#VACANT", "#VACANT", false_label);
        assert !bool_var.equals("");
        return bool_var;
    }

    public void gen_mid_opt(MidCodeList midCodeList, String next_or, ASDNode stmt, String end_if) {
        if (eqExps.size() == 1) {
            eqExps.get(0).gen_mid_opt(midCodeList, next_or, stmt, end_if, true);
            return;
        }
        for (EqExp eqExp : eqExps) {
            eqExp.gen_mid_opt(midCodeList, next_or, stmt, end_if, false);
        }
        stmt.gen_mid(midCodeList);
        midCodeList.add(MidCode.Op.JUMP, "#VACANT", "#VACANT", end_if);
    }
}
