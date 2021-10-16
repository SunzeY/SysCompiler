package front.ASD;

import front.Error;

import java.util.ArrayList;

public class ConstDef implements ASDNode{
    private Indent indent;
    private ArrayList<ConstExp> constExps;
    private ConstInitVal constInitVal;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public ConstDef(Indent indent, ArrayList<ConstExp> constExps, ConstInitVal constInitVal) {
        this.indent = indent;
        this.constExps = constExps;
        this.constInitVal = constInitVal;
        asdNodes.add(indent);
        asdNodes.addAll(constExps);
        asdNodes.add(constInitVal);
    }

    @Override
    public void printTestInfo() {
        indent.printTestInfo();
        for (ConstExp constExp: constExps) {
            System.out.println("LBRACK [");
            constExp.printTestInfo();
            System.out.println("RBRACK ]");
        }
        System.out.println("ASSIGN =");
        constInitVal.printTestInfo();
        System.out.println("<ConstDef>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public String getName() {
        return this.indent.getName();
    }

    public ConstInitVal getInitVal() {
        return this.constInitVal;
    }

    public Indent getIndent() {
        return this.indent;
    }

    public int getDimension() {
        return this.constExps.size();
    }

    public ArrayList<Integer> getArrayShape() throws Error {
        ArrayList<Integer> shape = new ArrayList<>();
        for (ConstExp exp : constExps) {
            shape.add(exp.getValue());
        }
        return shape;
    }
}
