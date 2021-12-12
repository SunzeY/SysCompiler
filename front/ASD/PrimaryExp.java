package front.ASD;

import front.Error;
import mid.MidCodeList;

import java.util.ArrayList;

public class PrimaryExp implements ASDNode{
    public Exp exp;
    public LVal lVal;
    public Number number;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public String value;

    public PrimaryExp(Exp exp) {
        this.exp = exp;
        this.lVal = null;
        this.number = null;
        this.value = null;
        asdNodes.add(exp);
    }

    public PrimaryExp(LVal lVal) {
        this.lVal = lVal;
        this.exp = null;
        this.number = null;
        this.value = null;
        asdNodes.add(lVal);
    }

    public PrimaryExp(Number number) {
        this.number = number;
        this.exp = null;
        this.lVal = null;
        this.value = null;
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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        try {
            return Integer.toString(this.getValue());
        } catch (Error ignored) {
        }
        if (this.exp != null) {
            return exp.gen_mid(midCodeList);
        } else if (this.lVal != null) {
            return this.lVal.gen_mid(midCodeList);
        } else {
            return Integer.toString(this.number.getValue());
        }
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
        if (this.value != null) {
            return Integer.parseInt(value);
        }
        if (this.number != null) {
            return this.number.getValue();
        }
        if (this.exp != null) {
            Integer value = this.exp.getValue();
            if (value != null) {
                return this.exp.getValue();
            }
        }
        throw new Error(Error.Type.other_error, -1);
    }
}
