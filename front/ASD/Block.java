package front.ASD;

import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class Block implements ASDNode{
    private ArrayList<BlockItem> blockItems;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public Block(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
        asdNodes.addAll(blockItems);
    }


    @Override
    public void printTestInfo() {
        System.out.println("LBRACE {");
        for (BlockItem blockItem: blockItems) {
            blockItem.printTestInfo();
        }
        System.out.println("RBRACE }");
        System.out.println("<Block>");
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
        midCodeList.add(MidCode.Op.NEW_BLOCK, "#VACANT", "#VACANT", "#VACANT");
        for (BlockItem blockItem: blockItems) {
            blockItem.gen_mid(midCodeList);
        }
        midCodeList.add(MidCode.Op.EXIT_BLOCK, "#VACANT", "#VACANT", "#VACANT");
        return "";
    }
}
