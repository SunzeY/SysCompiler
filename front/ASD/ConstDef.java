package front.ASD;

import front.Error;
import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class ConstDef implements ASDNode{
    public Indent indent;
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

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        String name =  indent.getName() + "@" + midCodeList.node2symItem.get(indent).get_loc();
        if (constExps.size() == 0) { // not-Array
            String value = constInitVal.gen_mid(midCodeList);
            midCodeList.add(MidCode.Op.CONST_DEF, name, value, "#VACANT");
        } else {
            String value = constInitVal.gen_mid(midCodeList);
            assert value.equals("#ARRAY");
            ArrayList<Integer> initValues = new ArrayList<>();
            constInitVal.getInitValue(initValues);
            int index = 0;
            for (Integer res: initValues) {
                midCodeList.add(MidCode.Op.ARR_SAVE, name + "[" + index + "]", res.toString(), "#VACANT");
                index += 1;
            }
        }
        return "";
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
