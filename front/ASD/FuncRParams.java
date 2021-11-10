package front.ASD;

import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class FuncRParams implements ASDNode{
    private ArrayList<Exp> exps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FuncRParams(ArrayList<Exp> exps) {
        this.exps = exps;
        asdNodes.addAll(exps);
    }

    @Override
    public void printTestInfo() {
        if (exps.size() == 0) {
            return;
        }
        boolean flag = false;
        for (Exp exp: exps) {
            if (flag) {
                System.out.println("COMMA ,");
            }
            exp.printTestInfo();
            flag = true;
        }
        System.out.println("<FuncRParams>");
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
        for (Exp exp: exps) {
            String name = exp.gen_mid(midCodeList);
            midCodeList.add(MidCode.Op.PUSH_PARA, name, "#VACANT", "#VACANT");
        }
        return "";
    }
}
