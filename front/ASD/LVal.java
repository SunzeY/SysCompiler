package front.ASD;

import SymTable.FuncFormVar;
import SymTable.SymItem;
import SymTable.Var;
import mid.MidCode;
import mid.MidCodeList;

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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        // 变量名@<depth,序号>
        String name = indent.getName() + "@" + midCodeList.node2symItem.get(indent).get_loc();
        SymItem item = midCodeList.node2symItem.get(indent);
        ArrayList<Integer> shape;
        if (item instanceof Var) {
            shape = ((Var) item).getShape();
        } else {
            shape = ((FuncFormVar) item).getShape();
        }
        if (!exps.isEmpty()) {
            if (shape.size() > 1 && shape.size() == exps.size()) {
                String x = exps.get(0).gen_mid(midCodeList);
                String y = exps.get(1).gen_mid(midCodeList);
                String base = midCodeList.add(MidCode.Op.MUL, x, shape.get(1).toString(), "#AUTO");
                name += "[" + midCodeList.add(MidCode.Op.ADD, y, base, "#AUTO") + "]";
            } else {
                String rank = exps.get(0).gen_mid(midCodeList);
                if (rank.contains("[")) {
                    rank = midCodeList.add(MidCode.Op.ARR_LOAD, "#AUTO", rank, "#VACANT");
                }
                if (shape.size() != exps.size()) {
                    midCodeList.add(MidCode.Op.SIGNAL_ARR_ADDR, "#VACANT", "#VACANT", "#VACANT");
                }
                name += "[" + rank + "]";
            }
        } else if (!shape.isEmpty() && shape.get(0) != -1) {
            midCodeList.add(MidCode.Op.SIGNAL_ARR_ADDR, "#VACANT", "#VACANT", "#VACANT");
        }
        return name;
    }

    public int getDimension() {
        return this.exps.size();
    }

    public String getName() {
        return this.indent.getName();
    }

}
