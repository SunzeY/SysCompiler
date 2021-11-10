package front.ASD;

import mid.MidCodeList;

import java.util.ArrayList;

public class Decl implements ASDNode {
    public ConstDecl constDecl;
    public VarDecl varDecl;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Decl(ConstDecl constDecl) {
        this.constDecl = constDecl;
        this.varDecl = null;
        asdNodes.add(constDecl);
    }

    public Decl(VarDecl varDecl) {
        this.constDecl = null;
        this.varDecl = varDecl;
        asdNodes.add(varDecl);
    }

    @Override
    public void printTestInfo() {
        if (constDecl != null) {
            constDecl.printTestInfo();
        }  else {
            varDecl.printTestInfo();
        }
        // System.out.println("<Decl>");
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
        if (constDecl != null) {
            constDecl.gen_mid(midCodeList);
        }  else {
            varDecl.gen_mid(midCodeList);
        }
        return "";
    }
}
