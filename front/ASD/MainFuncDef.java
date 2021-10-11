package front.ASD;

import java.util.ArrayList;

public class MainFuncDef implements ASDNode{
    private Block block;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public MainFuncDef(Block block, ASDNode blockEnd) {
        this.block = block;
        asdNodes.add(block);
        asdNodes.add(blockEnd);
    }

    @Override
    public void printTestInfo() {
        System.out.println("INTTK int");
        System.out.println("MAINTK main");
        System.out.println("LPARENT (");
        System.out.println("RPARENT )");
        block.printTestInfo();
        System.out.println("<MainFuncDef>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }
}
