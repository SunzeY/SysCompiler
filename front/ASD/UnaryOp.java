package front.ASD;

import front.Token;

import java.util.ArrayList;

public class UnaryOp implements ASDNode{
    private Token token;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public UnaryOp(Token token) {
        this.token = token;
    }

    @Override
    public void printTestInfo() {
        System.out.println(token.toString());
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
    public String toString() {
        return this.token.toString();
    }
}
