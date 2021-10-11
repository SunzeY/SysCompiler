package front.ASD;

import java.util.ArrayList;

public class FuncFParams implements ASDNode{
    private ArrayList<FuncFParam> funcFParams;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FuncFParams(ArrayList<FuncFParam> funcFParams) {
        this.funcFParams = funcFParams;
        asdNodes.addAll(funcFParams);
    }

    @Override
    public void printTestInfo() {
        if (funcFParams.size() == 0) {
            return;
        }
        boolean flag = false;
        for (FuncFParam funcFParam: funcFParams) {
            if (flag) {
                System.out.println("COMMA ,");
            }
            funcFParam.printTestInfo();
            flag = true;
        }
        System.out.println("<FuncFParams>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Integer getArgc() {
        return this.funcFParams.size();
    }

    public ArrayList<FuncFParam> getFuncFParams() {
        return this.funcFParams;
    }
}
