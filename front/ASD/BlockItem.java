package front.ASD;

import mid.MidCodeList;

import java.util.ArrayList;

public class BlockItem implements ASDNode{
    private Decl decl;
    private Stmt stmt;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public BlockItem(Decl decl) {
        this.decl = decl;
        this.stmt = null;
        asdNodes.add(decl);
    }

    public BlockItem(Stmt stmt) {
        this.stmt = stmt;
        this.decl = null;
        asdNodes.add(stmt);
    }

    @Override
    public void printTestInfo() {
        if (this.decl == null) {
            this.stmt.printTestInfo();
        } else {
            this.decl.printTestInfo();
        }
        //System.out.println("<BlockItem>");
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
        if (this.decl == null) {
            this.stmt.gen_mid(midCodeList);
        } else {
            this.decl.gen_mid(midCodeList);
        }
        return "";
    }
}
