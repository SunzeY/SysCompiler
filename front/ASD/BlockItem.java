package front.ASD;

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
}
