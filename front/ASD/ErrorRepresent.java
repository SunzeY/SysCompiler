package front.ASD;

import front.ASD.ASDNode;
import front.Token;
import mid.MidCodeList;

import java.util.ArrayList;

public class ErrorRepresent implements ASDNode {
    private Token token;

    public ErrorRepresent(Token sym) {
        token = sym;
    }

    @Override
    public void printTestInfo() {
    }

    @Override
    public void linkWithSymbolTable() {

    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return new ArrayList<>();
    }

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        return null;
    }

    public Token getToken() {
        return token;
    }
}
