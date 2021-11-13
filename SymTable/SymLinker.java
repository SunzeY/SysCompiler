package SymTable;

import front.ASD.ASDNode;
import front.ASD.Block;
import front.ASD.ConstDef;
import front.ASD.ConstInitVal;
import front.ASD.Exp;
import front.ASD.FormatString;
import front.ASD.FuncDef;
import front.ASD.FuncFParam;
import front.ASD.FuncFParams;
import front.ASD.FuncType;
import front.ASD.Indent;
import front.ASD.InitVal;
import front.ASD.ErrorRepresent;
import front.ASD.MainFuncDef;
import front.ASD.PrimaryExp;
import front.ASD.Stmt;
import front.ASD.UnaryExp;
import front.ASD.VarDef;
import front.Error;
import front.ErrorRecorder;
import front.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SymLinker {
    private final Stack<SymItem> stack = new Stack<>();
    private final HashMap<String, SymbolTable> blockLoc2table = new HashMap<>();
    public final HashMap<ASDNode, SymItem> node2tableItem = new HashMap<>();
    private final ASDNode root;
    private SymbolTable currentTable;
    private final HashMap<String, ArrayList<SymbolTable>> funcSymbolsTables = new HashMap<>();
    private ArrayList<SymbolTable> funcTable = new ArrayList<>();
    private final ArrayList<SymbolTable> symbolTables = new ArrayList<>();
    private final Stack<SymbolTable> tableStack = new Stack<>();
    private final int[] depths = new int[100];
    private int currentDepth;
    private final HashMap<String, SymbolTable> name2Table = new HashMap<>();
    private String currentFucName;
    private boolean inAssign = false;

    public SymLinker(ASDNode root) {
        this.root = root;
        this.currentDepth = 0;
        for (int i = 0; i < 100; i += 1) {
            depths[i] = 0;
        }
    }

    String get_loc() {
        return "<" + currentDepth + "," + (depths[currentDepth] > 0 ? depths[currentDepth] - 1 : 0) + ">";
    }

    public HashMap<String, SymbolTable> getBlockLoc2table() {
        return this.blockLoc2table;
    }

    public HashMap<String, ArrayList<SymItem>> getFuncTable() {
        HashMap<String, ArrayList<SymItem>> res = new HashMap<>();
        for (Map.Entry<String, ArrayList<SymbolTable>> pair : funcSymbolsTables.entrySet()) {
            String funcName = pair.getKey();
            ArrayList<SymItem> funcTable = new ArrayList<>();
            int addr = 4;
            for (SymbolTable table : pair.getValue()) {
                for (SymItem item: table.symItems) {
                    addr = item.set_addr(addr);
                    funcTable.add(item);
                }
            }
            res.put(funcName, funcTable);
        }
        return res;
    }

    private void travel(ASDNode node, ASDNode funcFormalArgs) throws Error {
        if (node == null) {
            return;
        }
        if (node instanceof MainFuncDef) {
            funcTable = new ArrayList<>();
        }
         if (node instanceof Block) {
            currentDepth += 1;
            currentTable = new SymbolTable(new int[]{currentDepth, depths[currentDepth]});
            blockLoc2table.put("<" + currentDepth + "," + depths[currentDepth] + ">", currentTable);
            tableStack.push(currentTable);
            depths[currentDepth] += 1;
            if (funcFormalArgs != null) { //函数块， 将形参加入符号表
                addFormalArgs(funcFormalArgs);
            }
        } else if (node instanceof VarDef) {
            VarDef varDef = (VarDef) node;
            checkTable(varDef.getName(), varDef.getIndent());
            String name = varDef.getName();
            InitVal initVal = varDef.getInitVal();
            SymItem item = new Var(name, false, initVal, varDef.getDimension(), varDef.getArrayShape(), get_loc());
            currentTable.symItems.add(item);
            stack.add(item);
            node2tableItem.put(((VarDef) node).indent, item);
        } else if (node instanceof ConstDef) {
            ConstDef constDef = (ConstDef) node;
            checkTable(constDef.getName(), constDef.getIndent());
            String name = constDef.getName();
            ConstInitVal constInitVal = constDef.getInitVal();
            SymItem item = new Var(name, true, constInitVal, constDef.getDimension(), constDef.getArrayShape(), get_loc());
            currentTable.symItems.add(item);
            stack.add(item);
            node2tableItem.put(((ConstDef) node).indent, item);
        } else if (node instanceof FuncDef) {
            checkTable(((FuncDef) node).getName(), ((FuncDef) node).getIndent());
            String name = ((FuncDef) node).getName();
            Integer argc = ((FuncDef) node).getArgc();
            Func.Type type = ((FuncDef) node).getType().equals(FuncType.Type.Int) ? Func.Type.intFunc : Func.Type.voidFunc;
            SymItem item = new Func(name, type, argc, get_loc());
            currentFucName = ((FuncDef) node).getName();
            currentTable.symItems.add(item);
            stack.add(item);
            node2tableItem.put(((FuncDef) node).getIndent(), item);
            funcTable = new ArrayList<>();
            travel(node.getChild().get(node.getChild().size() - 2), node.getChild().get(node.getChild().size() - 3)); //函数形参需要加入符号表
            funcSymbolsTables.put(name, (ArrayList<SymbolTable>) funcTable.clone());
            funcTable = new ArrayList<>();
            return;
        } else if (node instanceof UnaryExp && ((UnaryExp) node).type.equals(UnaryExp.Type.FuncCall)) {
            Indent indent = (Indent) node.getChild().get(0);
            SymItem item = findInStack(((UnaryExp) node).getFuncCallName(), indent, "Func", true);
            int size = 0;
            if (node.getChild().size() > 1) {
                size = node.getChild().get(1).getChild().size();
            }
            if (item != null && ((Func) item).getType().equals(Func.Type.voidFunc) && this.inAssign) {
                //TODO
            }
            if (item != null && !((Func) item).checkForm(size)) {
                ErrorRecorder.recordError(new Error(Error.Type.func_arg_cnt_mismatch, indent.getLineNum()));
                node2tableItem.put(indent, item);
            } else {
                if (node.getChild().size() > 1) {
                    String funcName = ((Indent) node.getChild().get(0)).getName();
                    SymbolTable table = name2Table.get(funcName);
                    if (!(table == null)) {  // happen when no name find!
                        int index = 0;
                        ArrayList<Integer> realShape = null;
                        for (ASDNode nod : node.getChild().get(1).getChild()) {
                            Exp exp = (Exp) nod;
                            int formDimension = 0;
                            if (exp.getName() != null) {
                                SymItem funcCall = findInStack(exp.getName(), indent, "Func", false);
                                if (funcCall != null) {
                                    if (((Func) funcCall).getType().equals(Func.Type.voidFunc)) {
                                        ErrorRecorder.recordError(new Error(Error.Type.func_arg_type_mismatch, indent.getLineNum()));
                                    }
                                    continue;
                                }
                                SymItem symItem = findInStack(exp.getName(), null, "Var", true);
                                if (symItem == null) {
                                    break;
                                }
                                int var_dimension = 0;
                                if (symItem instanceof Var) {
                                    var_dimension = ((Var) symItem).getDimension();
                                    realShape = ((Var) symItem).getShape();
                                } else {
                                    var_dimension = ((FuncFormVar) symItem).getDimension();
                                    realShape = ((FuncFormVar) symItem).getShape();
                                }
                                formDimension = var_dimension - exp.getDimension();
                            }
                            if (((FuncFormVar) table.getTable().get(index)).getDimension() != formDimension) {
                                ErrorRecorder.recordError(new Error(Error.Type.func_arg_type_mismatch, indent.getLineNum()));
//                            } else if (formDimension > 0) {
//                                ArrayList<Integer> formalShape = ((FuncFormVar) table.getTable().get(index)).getShape();
//                                for (int i=1; i <formDimension; i++) {
//                                    if (!realShape.get(i + exp.getDimension() ).equals(formalShape.get(i))) {
//                                        ErrorRecorder.recordError(new Error(Error.Type.func_arg_type_mismatch, indent.getLineNum()));
//                                    }
//                                }
//                            }
                            }
                            index += 1;
                        }
                    }
                }
            }
            node2tableItem.put(node, item);
        } else if (node instanceof Stmt && (((Stmt) node).getType().equals(Stmt.Type.Assign) ||
                ((Stmt) node).getType().equals(Stmt.Type.input))) {
            this.inAssign = true;
            Indent indent = (Indent) node.getChild().get(0).getChild().get(0);
            SymItem item = findInStack(indent.getName(), indent, "Var", true);
            if (item != null && item.isConst()) {
                ErrorRecorder.recordError(new Error(Error.Type.changeConst, indent.getLineNum()));
            }
            node2tableItem.put(indent, item);
        } else if (node instanceof Stmt && ((Stmt) node).getType().equals(Stmt.Type.output)) {
            Token token = ((ErrorRepresent) node.getChild().get(0)).getToken();
            FormatString formatString = ((FormatString) node.getChild().get(1));
            if (formatString.getFormatCharNum() != node.getChild().size() - 2) {
                ErrorRecorder.recordError(new Error(Error.Type.printf_num_miss_match, token.getLineNum()));
            }
        } else if (node instanceof PrimaryExp &&((PrimaryExp) node).lVal != null) {
            Indent indent = (Indent) ((PrimaryExp) node).lVal.getChild().get(0);
            SymItem item = findInStack(indent.getName(), indent, "Var", true);
            node2tableItem.put(indent, item);
        }
        for (ASDNode childNode : node.getChild()) {
            travel(childNode, null);
        }

        if (node instanceof Block) {
            if (funcFormalArgs != null) {
                name2Table.put(currentFucName, currentTable);
            }
            currentDepth -= 1;
            popFromStack();
        } else if (node instanceof Stmt && (((Stmt) node).getType().equals(Stmt.Type.Assign) ||
                ((Stmt) node).getType().equals(Stmt.Type.input))) {
            this.inAssign = false;
        }
    }

    private void addFormalArgs(ASDNode funcFormalArgs) throws Error {
        FuncFParams params = (FuncFParams) funcFormalArgs;
        for (FuncFParam param: params.getFuncFParams()) {
            String name = param.getName();
            checkTable(name, param.getIndent());
            int dimension = param.getDimension();
            FuncFormVar formVar = new FuncFormVar(name, dimension, param.getShape(), "<" + currentDepth + "," + depths[currentDepth+1] + ">");
            currentTable.symItems.add(formVar);
            stack.add(formVar);
            node2tableItem.put(param.indent, formVar);
        }
    }

    private SymItem findInStack(String name, Indent indent, String type, boolean checkError) {
        ArrayList<SymItem> items = new ArrayList<>(stack);
        for (int i = items.size() - 1; i >= 0; i -= 1) {
            SymItem item = items.get(i);
            if (item.getName().equals(name)) {
                if (item instanceof Func && type.equals("Func")) {
                    return item;
                }
                else if ((item instanceof Var || item instanceof FuncFormVar) && type.equals("Var")) {
                    return item;
                }
            }
        }
        if (checkError) ErrorRecorder.recordError(new Error(Error.Type.undefined_name, indent.getLineNum()));
        return null; //返回null一定要检查！
    }

    private void checkTable(String name, Indent indent) {
        for (SymItem item : currentTable.symItems) {
            if (item.getName().equals(name)) {
                ErrorRecorder.recordError(new Error(Error.Type.name_redefine, indent.getLineNum()));
            }
        }
    }

    private void popFromStack() {
        stack.removeAll(currentTable.symItems);
        symbolTables.add(currentTable);
        funcTable.add(currentTable);
        tableStack.pop();
        currentTable = tableStack.peek();
    }

    public void link() {
        currentTable = new SymbolTable(new int[]{0, 0});
        tableStack.push(currentTable);
        symbolTables.add(currentTable);
        try {
            travel(root, null);
            travelForCheck(root);
            funcSymbolsTables.put("main", (ArrayList<SymbolTable>) funcTable.clone());
        } catch (Error ignored) {
        }
        blockLoc2table.put("<0,0>", currentTable);
    }

    private enum BlockType {
        whileBlock, voidFuncBlock, intFuncBlock
    }

    private final Stack<BlockType> blockStack = new Stack<>();
    private int returnNum;
    private void travelForCheck(ASDNode node) {
        if (node == null) {
            return;
        }
        if (node instanceof FuncDef && ((FuncDef) node).getType().equals(FuncType.Type.Void)) {
            blockStack.add(BlockType.voidFuncBlock);
            returnNum = 0;
        } else if (node instanceof FuncDef && ((FuncDef) node).getType().equals(FuncType.Type.Int)) {
            blockStack.add(BlockType.intFuncBlock);
            returnNum = 0;
            Block block = (Block) node.getChild().get(node.getChild().size()-2);
            if (block.getChild().size() != 0) {
                ASDNode stmt = block.getChild().get(block.getChild().size() - 1).getChild().get(0);
                if (!(stmt instanceof Stmt
                        && ((Stmt) stmt).getType().equals(Stmt.Type.returnStmt))) {
                    ErrorRecorder.recordError(new Error(Error.Type.non_void_non_return,
                            ((ErrorRepresent) node.getChild().get(node.getChild().size() - 1)).getToken().getLineNum()));
                }
            }
            else {
                ErrorRecorder.recordError(new Error(Error.Type.non_void_non_return,
                        ((ErrorRepresent) node.getChild().get(node.getChild().size() - 1)).getToken().getLineNum()));
            }
        } else if (node instanceof MainFuncDef) {
            blockStack.add(BlockType.intFuncBlock);
            Block block = (Block) node.getChild().get(node.getChild().size()-2);
            boolean noReturn = false;
            if (block.getChild().size() != 0) {
                ASDNode stmt = block.getChild().get(block.getChild().size() - 1).getChild().get(0);
                if (!(stmt instanceof Stmt
                        && ((Stmt) stmt).getType().equals(Stmt.Type.returnStmt))) {
                    ErrorRecorder.recordError(new Error(Error.Type.non_void_non_return,
                            ((ErrorRepresent) node.getChild().get(node.getChild().size() - 1)).getToken().getLineNum()));
                }
            }
            else {
                ErrorRecorder.recordError(new Error(Error.Type.non_void_non_return,
                        ((ErrorRepresent) node.getChild().get(node.getChild().size() - 1)).getToken().getLineNum()));
            }
        } else if (node instanceof Stmt) {
            Stmt.Type type = ((Stmt) node).getType();
            if (type.equals(Stmt.Type.whileBranch)) {
                blockStack.add(BlockType.whileBlock);
            } else if (type.equals(Stmt.Type.returnStmt)) {
                if (node.getChild().size() > 1 && blockStack.peek().equals(BlockType.voidFuncBlock)) {
                    ErrorRecorder.recordError(new Error(Error.Type.void_return,
                            ((ErrorRepresent) node.getChild().get(0)).getToken().getLineNum()));
                    returnNum += 1;
                } else if (blockStack.peek().equals(BlockType.intFuncBlock)) {
                    returnNum += 1;
                }
            } else if (type.equals(Stmt.Type.breakStmt) || type.equals(Stmt.Type.continueStmt)) {
                if (!blockStack.peek().equals(BlockType.whileBlock)) {
                    ErrorRecorder.recordError(new Error(Error.Type.misused_BC,
                            ((ErrorRepresent) node.getChild().get(0)).getToken().getLineNum()));
                }
            }
        }
        for (ASDNode childNode : node.getChild()) {
            travelForCheck(childNode);
        }
        if (node instanceof FuncDef && ((FuncDef) node).getType().equals(FuncType.Type.Int)
                || node instanceof MainFuncDef) {
            if (!blockStack.isEmpty() && blockStack.peek().equals(BlockType.intFuncBlock) && returnNum == 0) {
                ErrorRecorder.recordError(new Error(Error.Type.non_void_non_return,
                        ((ErrorRepresent) node.getChild().get(node.getChild().size() - 1)).getToken().getLineNum()));
                blockStack.pop();
            }
        } else if (node instanceof FuncDef && ((FuncDef) node).getType().equals(FuncType.Type.Void)) {
            if (blockStack.peek().equals(BlockType.voidFuncBlock)) {
                blockStack.pop();
            }
        } else if (node instanceof Stmt && ((Stmt) node).getType().equals(Stmt.Type.whileBranch)) {
            blockStack.pop();
        }
    }
}
