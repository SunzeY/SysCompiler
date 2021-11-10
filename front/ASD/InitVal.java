package front.ASD;

import front.Error;
import mid.MidCodeList;

import java.util.ArrayList;
import java.util.zip.InflaterInputStream;

public class InitVal implements ASDNode{
    public enum Type{
        mulInitVal,
        Exp
    }
    private ArrayList<ASDNode> asdNodes;
    private Type type;

    public InitVal(Type type, ArrayList<ASDNode> asdNodes) {
        this.asdNodes = asdNodes;
        this.type = type;
    }

    @Override
    public void printTestInfo() {
        if (this.type.equals(Type.Exp)) {
            this.asdNodes.get(0).printTestInfo();
        } else {
            System.out.println("LBRACE {");
            boolean tag = false;
            for (ASDNode asdNode: asdNodes) {
                if (tag) {
                    System.out.println("COMMA ,");
                }
                asdNode.printTestInfo();
                tag = true;
            }
            System.out.println("RBRACE }");
        }
        System.out.println("<InitVal>");
    }

    @Override
    public void linkWithSymbolTable() {
    }
    @Override
    public ArrayList<ASDNode> getChild() {
        if (asdNodes == null) {
            return new ArrayList<>();
        }
        return asdNodes;
    }

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        String value = "";
        if (this.type.equals(InitVal.Type.Exp)) {
            value = (asdNodes.get(0)).gen_mid(midCodeList);
        } else {
            // TODO array
        }
        return value;
    }
}
