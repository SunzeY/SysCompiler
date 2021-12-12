package front.ASD;

import front.Token;
import mid.MidCodeList;

import java.util.ArrayList;

public class UnaryOp implements ASDNode{
    private Token token;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public UnaryOp(Token token) {
        this.token = token;
    }

    @Override
    public void printTestInfo() {
        System.out.println("<UnaryOp>");
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

    @Override
    public String toString() {
        return this.token.toString();
    }

    public String getType() {
        return this.token.getTokenClass();
    }
}
