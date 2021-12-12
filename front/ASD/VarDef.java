package front.ASD;

import front.Error;
import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class VarDef implements ASDNode {
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
        for (ConstExp constExp : constExps) {
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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        // 变量名@<depth, 序号>
        String name = indent.getName() + "@" + midCodeList.node2symItem.get(indent).get_loc();
        if (constExps.size() == 0) { // not-Array
            if (initVal != null) {
                String value = initVal.gen_mid(midCodeList);
                midCodeList.add(MidCode.Op.VAR_DEF, name, value, "#VACANT");
            } else {
                midCodeList.add(MidCode.Op.VAR_DEF, name, "#VACANT", "#VACANT");
            }
        } else {
            if (initVal != null) {
                String value = initVal.gen_mid(midCodeList);
                assert value.equals("#ARRAY");
                ArrayList<String> initValues = new ArrayList<>();
                initVal.getInitValue(initValues, midCodeList);
                int index = 0;
                for (String res : initValues) {
                    midCodeList.add(MidCode.Op.ARR_SAVE, name + "[" + index + "]", res, "#VACANT");
                    index += 1;
                }
            } else {
                midCodeList.add(MidCode.Op.VAR_DEF, name, "#VACANT", "#VACANT");
            }
        }
        return "";
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
