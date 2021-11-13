package front.ASD;

import front.Error;
import mid.MidCodeList;

import java.util.ArrayList;

public class FuncFParam implements ASDNode{
    private BType bType;
    public Indent indent;
    private ArrayList<ConstExp> constExps;
    private boolean isArray;

    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    private int dimension;

    public FuncFParam(BType bType, Indent indent, ArrayList<ConstExp> constExps, int dimension) {
        this.bType = bType;
        this.indent = indent;
        this.constExps = constExps;
        this.dimension = dimension;
        asdNodes.add(bType);
        asdNodes.add(indent);
        asdNodes.addAll(constExps);
    }

    @Override
    public void printTestInfo() {
        bType.printTestInfo();
        indent.printTestInfo();
        if (isArray) {
            System.out.println("LBRACK [");
            System.out.println("RBRACK ]");
            for (ConstExp constExp: constExps) {
                System.out.println("LBRACK [");
                constExp.printTestInfo();
                System.out.println("RBRACK ]");
            }
        }
        System.out.println("<FuncFParam>");
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
        return this.indent.getName();
    }

    public boolean getIsArray() {
        return this.isArray;
    }

    public Indent getIndent() {
        return this.indent;
    }

    public int getDimension() {
        return this.dimension;
    }

    public ArrayList<Integer> getShape() throws Error {
        ArrayList<Integer> shape = new ArrayList<>();
        if (this.dimension != 1) {
            shape.add(-1);
        }
        for (ConstExp exp: constExps) {
            shape.add(exp.getValue());
        }
        return shape;
    }
}
