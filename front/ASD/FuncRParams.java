package front.ASD;

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
}
