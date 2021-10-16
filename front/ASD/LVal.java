package front.ASD;

import java.util.ArrayList;

public class LVal implements ASDNode{
    public Indent indent;
    public ArrayList<Exp> exps;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public LVal(Indent indent, ArrayList<Exp> exps) {
        this.indent = indent;
        this.exps = exps;
        asdNodes.add(indent);
        asdNodes.addAll(exps);
    }

    @Override
    public void printTestInfo() {
        indent.printTestInfo();
        for (Exp exp: exps) {
            System.out.println("LBRACK [");
            exp.printTestInfo();
            System.out.println("RBRACK ]");
        }
        System.out.println("<LVal>");
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return asdNodes;
    }

    public int getDimension() {
        return this.exps.size();
    }

    public String getName() {
        return this.indent.getName();
    }
}
