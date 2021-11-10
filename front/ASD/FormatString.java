package front.ASD;

import front.Token;
import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        return null;
    }

    public int getFormatCharNum() {
        return this.token.getFormatCharNum();
    }

    @Override
    public String toString() {
        return this.token.toString();
    }

    public String getString() {
        return this.token.getString();
    }
}
