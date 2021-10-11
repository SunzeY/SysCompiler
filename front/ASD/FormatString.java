package front.ASD;

import front.Token;

import java.util.ArrayList;

public class FormatString implements ASDNode {
    private Token token;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FormatString(Token token) {
        this.token = token;
    }

    @Override
    public void printTestInfo() {
        System.out.println(token.toString());
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public int getFormatCharNum() {
        return this.token.getFormatCharNum();
    }

}
