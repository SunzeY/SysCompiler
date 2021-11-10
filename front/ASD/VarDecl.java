package front.ASD;

import mid.MidCodeList;

import java.util.ArrayList;

public class VarDecl implements ASDNode{
    private BType bType;
    private ArrayList<VarDef> varDefs;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public VarDecl(BType bType, ArrayList<VarDef> varDefs) {
        this.bType = bType;
        this.varDefs = varDefs;
        asdNodes.addAll(varDefs);
    }
    @Override
    public void printTestInfo() {
        System.out.println("INTTK int");
        boolean tag = false;
        for (VarDef varDef: varDefs) {
            if (tag) {
                System.out.println("COMMA ,");
            }
            varDef.printTestInfo();
            tag = true;
        }
        System.out.println("SEMICN ;");
        System.out.println("<VarDecl>");
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
        for (VarDef varDef: varDefs) {
            varDef.gen_mid(midCodeList);
        }
        return "";
    }
}
