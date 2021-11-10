package front.ASD;

import mid.MidCodeList;

import java.util.ArrayList;

public class ConstDecl implements ASDNode{
    private BType bType;
    private ArrayList<ConstDef> constDefs;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public ConstDecl(BType bType, ArrayList<ConstDef> constDefs) {
        this.bType = bType;
        this.constDefs = constDefs;
        asdNodes.add(bType);
        asdNodes.addAll(constDefs);
    }

    @Override
    public void printTestInfo() {
        System.out.println("CONSTTK const");
        System.out.println("INTTK int");
        boolean tag = false;
        for (ConstDef constDef: constDefs) {
            if (tag) {
                System.out.println("COMMA ,");
            }
            constDef.printTestInfo();
            tag = true;
        }
        System.out.println("SEMICN ;");
        System.out.println("<ConstDecl>");
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
        for (ConstDef constDef: constDefs) {
            constDef.gen_mid(midCodeList);
        }
        return "";
    }
}
