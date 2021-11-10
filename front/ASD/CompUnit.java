package front.ASD;

import mid.MidCodeList;

import java.util.ArrayList;

public class CompUnit implements ASDNode {

    private ArrayList<Decl> decls;
    private ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainDuncDef;
    private final ArrayList<ASDNode> asdNodes = new ArrayList<>();
    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainDuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainDuncDef = mainDuncDef;
        asdNodes.addAll(decls);
        asdNodes.addAll(funcDefs);
        asdNodes.add(mainDuncDef);
    }

    @Override
    public void printTestInfo() {
        for (Decl decl: decls) {
            decl.printTestInfo();
        }
        for (FuncDef funcDef: funcDefs) {
            funcDef.printTestInfo();
        }
        mainDuncDef.printTestInfo();
        System.out.println("<CompUnit>");
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
        for (ASDNode asdNode: asdNodes) {
            asdNode.gen_mid(midCodeList);
        }
        return "";
    }
}
