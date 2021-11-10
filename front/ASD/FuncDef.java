package front.ASD;

import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class FuncDef implements ASDNode{
    private FuncType funcType;
    private Indent indent;
    private FuncFParams funcFParams;
    private Block block;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();

    public FuncDef(FuncType funcType, Indent indent, FuncFParams funcFParams, Block block, ASDNode blockEnd) {
        this.funcType = funcType;
        this.indent = indent;
        this.funcFParams = funcFParams;
        this.block = block;
        asdNodes.add(funcType);
        asdNodes.add(indent);
        asdNodes.add(funcFParams);
        asdNodes.add(block);
        asdNodes.add(blockEnd);
    }

    @Override
    public void printTestInfo() {
        this.funcType.printTestInfo();
        this.indent.printTestInfo();
        System.out.println("LPARENT (");
        this.funcFParams.printTestInfo();
        System.out.println("RPARENT )");
        block.printTestInfo();
        System.out.println("<FuncDef>");
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
        midCodeList.add(MidCode.Op.FUNC, this.funcType.getType().equals(FuncType.Type.Void) ? "void" : "int",
                indent.getName(), "#VACANT");
        this.block.gen_mid(midCodeList);
        midCodeList.add(MidCode.Op.RETURN, "#VACANT", "#VACANT", "#VACANT");
        midCodeList.add(MidCode.Op.END_FUNC, this.funcType.getType().equals(FuncType.Type.Void) ? "void" : "int",
                indent.getName(), "#VACANT");
        return "";
    }

    public Integer getArgc() {
        return this.funcFParams.getArgc();
    }

    public FuncType.Type getType() {
        return this.funcType.getType();
    }

    public Indent getIndent() {
        return this.indent;
    }
}
