package front.ASD;

import front.Token;
import mid.MidCodeList;

import java.util.ArrayList;

public class Indent implements ASDNode{
    private Token token;
    private String name;

    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public Indent(Token token) {
        this.token = token;
        this.name = token.getName();
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

    public String getName() {
        return this.name;
    }

    public int getLineNum() {
        return this.token.getLineNum();
    }
}
