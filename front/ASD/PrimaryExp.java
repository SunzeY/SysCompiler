package front.ASD;

import front.Error;

import java.util.ArrayList;

public class PrimaryExp implements ASDNode{
    private Exp exp;
    private LVal lVal;
    private Number number;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public PrimaryExp(Exp exp) {
        this.exp = exp;
        this.lVal = null;
        this.number = null;
        asdNodes.add(exp);
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
        this.exp = null;
        this.number = null;
        asdNodes.add(lVal);
    }

    public PrimaryExp(Number number) {
        this.number = number;
        this.exp = null;
        this.lVal = null;
        asdNodes.add(number);
    }

    @Override
    public void printTestInfo() {
        if (exp != null) {
            System.out.println("LPARENT (");
            exp.printTestInfo();
            System.out.println("RPARENT )");
        } else if (lVal != null) {
            lVal.printTestInfo();
        } else {
            number.printTestInfo();
        }
        System.out.println("<PrimaryExp>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public int getDimension() {
        if (this.lVal != null) {
            return this.lVal.getDimension();
        } else if (this.exp != null){
            return this.exp.getDimension();
        }
        return 0;
    }

    public String getName() {
        if (this.lVal != null) {
            return this.lVal.getName();
        } else if (this.exp != null){
            return this.exp.getName();
        }
        return null;
    }

    public int getValue() throws Error {
        if (this.number == null) {
            throw new Error(Error.Type.other_error, -1);
        }
        return this.number.getValue();
    }
}
