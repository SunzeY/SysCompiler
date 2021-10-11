package front.ASD;

import java.util.ArrayList;

public class VarDef implements ASDNode{
    public Indent indent;
    private ArrayList<ConstExp> constExps;
    private InitVal initVal;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public VarDef(Indent indent, ArrayList<ConstExp> constExps) {
        this.indent = indent;
        this.constExps = constExps;
        this.initVal = null;
        asdNodes.add(indent);
        asdNodes.addAll(constExps);
    }

    public VarDef(Indent indent, ArrayList<ConstExp> constExps, InitVal initVal) {
        this.indent = indent;
        this.constExps = constExps;
        this.initVal = initVal;
        asdNodes.add(indent);
        asdNodes.addAll(constExps);
        asdNodes.add(initVal);
    }

    @Override
    public void printTestInfo() {
        indent.printTestInfo();
        for (ConstExp constExp: constExps) {
            System.out.println("LBRACK [");
            constExp.printTestInfo();
            System.out.println("RBRACK ]");
        }
        if (initVal != null) {
            System.out.println("ASSIGN =");
            initVal.printTestInfo();
        }
        System.out.println("<VarDef>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    public String getName() {
        return this.indent.getName();
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public Integer[] getType() {
        return new Integer[]{1, 2};
    }

    public InitVal getInitVal() {
        return this.initVal;
    }

    public Indent getIndent() {
        return this.indent;
    }
}
