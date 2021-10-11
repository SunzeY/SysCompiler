package front.SymTable;

import front.ASD.ASDNode;
import front.ASD.Block;
import front.ASD.ConstDef;
import front.ASD.ConstInitVal;
import front.ASD.FormatString;
import front.ASD.FuncDef;
import front.ASD.FuncFParam;
import front.ASD.FuncFParams;
import front.ASD.FuncType;
import front.ASD.Indent;
import front.ASD.InitVal;
import front.ASD.ErrorRepresent;
import front.ASD.MainFuncDef;
import front.ASD.Stmt;
import front.ASD.UnaryExp;
import front.ASD.VarDef;
import front.Error;
import front.ErrorRecorder;
import front.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class SymLinker {
    private final Stack<SymItem> stack = new Stack<>();
    private final HashMap<Integer[], SymbolTable> indent2table = new HashMap<>();
    private final HashMap<ASDNode, SymItem> node2tableItem = new HashMap<>();
    private final ASDNode root;
    private static SymbolTable currentTable;
    private final ArrayList<SymbolTable> symbolTables = new ArrayList<>();
    private final Stack<SymbolTable> tableStack = new Stack<>();
    private final int[] depths = new int[100];
    private Integer currentDepth;

    public SymLinker(ASDNode root) {
        this.root = root;
        this.currentDepth = 0;
        for (int i = 0; i < 100; i += 1) {
            depths[i] = 0;
        }
    }

    private void travel(ASDNode node, ASDNode funcFormalArgs) throws Error {
        if (node == null) {
            return;
        }
        if (node instanceof Block) {
            currentDepth += 1;
            currentTable = new SymbolTable(new int[]{currentDepth, depths[currentDepth]});
            indent2table.put(new Integer[]{currentDepth, depths[currentDepth]}, currentTable);
            tableStack.push(currentTable);
            depths[currentDepth] += 1;
            if (funcFormalArgs != null) { //函数块， 将形参加入符号表
                addFormalArgs(funcFormalArgs);
            }
        } else if (node instanceof VarDef) {
            checkTable(((VarDef) node).getName(), ((VarDef) node).getIndent());
            String name = ((VarDef) node).getName();
            InitVal initVal = ((VarDef) node).getInitVal();
            SymItem item = new Var(name, false, initVal);
            currentTable.symItems.add(item);
            stack.add(item);
            node2tableItem.put(node, item);
            return;
        } else if (node instanceof ConstDef) {
            checkTable(((ConstDef) node).getName(), ((ConstDef) node).getIndent());
            String name = ((ConstDef) node).getName();
            ConstInitVal constInitVal = ((ConstDef) node).getInitVal();
            SymItem item = new Var(name, true, constInitVal);
            currentTable.symItems.add(item);
            stack.add(item);
            node2tableItem.put(node, item);
            return;
        } else if (node instanceof FuncDef) {
            checkTable(((FuncDef) node).getName(), ((FuncDef) node).getIndent());
            String name = ((FuncDef) node).getName();
            Integer argc = ((FuncDef) node).getArgc();
            Func.Type type = ((FuncDef) node).getType().equals(FuncType.Type.Int) ? Func.Type.intFunc : Func.Type.voidFunc;
            SymItem item = new Func(name, type, argc);
            currentTable.symItems.add(item);
            stack.add(item);
            node2tableItem.put(node, item);
            travel(node.getChild().get(node.getChild().size() - 2), node.getChild().get(node.getChild().size() - 3)); //函数形参需要加入符号表
            return;
        } else if (node instanceof Indent) {
            SymItem item = findInStack(((Indent) node).getName(), (Indent) node);
            node2tableItem.put(node, item);
            return;
        } else if (node instanceof UnaryExp && ((UnaryExp) node).type.equals(UnaryExp.Type.FuncCall)) {
            Indent indent = (Indent) node.getChild().get(0);
            SymItem item = findInStack(((UnaryExp) node).getFuncCallName(), indent);
            // only need to check number.
            int size = 0;
            if (node.getChild().size() > 1) {
                size = node.getChild().get(1).getChild().size();
            }
            if (item != null && !((Func) item).checkForm(size)) {
                ErrorRecorder.recordError(new Error(Error.Type.func_arg_cnt_mismatch, indent.getLineNum()));
            }
            node2tableItem.put(node, item);
        } else if (node instanceof Stmt && (((Stmt) node).getType().equals(Stmt.Type.Assign) ||
                ((Stmt) node).getType().equals(Stmt.Type.input))) {
            Indent indent = (Indent) node.getChild().get(0).getChild().get(0);
            SymItem item = findInStack(indent.getName(), indent);
            if (item != null && item.isConst()) {
                ErrorRecorder.recordError(new Error(Error.Type.changeConst, indent.getLineNum()));
            }
        } else if (node instanceof Stmt && ((Stmt) node).getType().equals(Stmt.Type.output)) {
            Token token = ((ErrorRepresent) node.getChild().get(0)).getToken();
            FormatString formatString = ((FormatString) node.getChild().get(1));
            if (formatString.getFormatCharNum() != node.getChild().size() - 2) {
                ErrorRecorder.recordError(new Error(Error.Type.printf_num_miss_match, token.getLineNum()));
            }
        }
        for (ASDNode childNode : node.getChild()) {
            travel(childNode, null);
        }

        if (node instanceof Block) {
            currentDepth -= 1;
            popFromStack();
        }
    }

    private void addFormalArgs(ASDNode funcFormalArgs) throws Error {
        FuncFParams params = (FuncFParams) funcFormalArgs;
        for (FuncFParam param: params.getFuncFParams()) {
            String name = param.getName();
            checkTable(name, param.getIndent());
            boolean isArray = param.getIsArray();
            FuncFormVar formVar = new FuncFormVar(name, isArray);
            currentTable.symItems.add(formVar);
            stack.add(formVar);
            node2tableItem.put(param, formVar);
        }
    }

    private SymItem findInStack(String name, Indent indent) throws Error {
        ArrayList<SymItem> items = new ArrayList<>(stack);
        for (int i = items.size() - 1; i >= 0; i -= 1) {
            SymItem item = items.get(i);
            if (item.getName().equals(name)) {
                return item;
            }
        }
        ErrorRecorder.recordError(new Error(Error.Type.undefined_name, indent.getLineNum()));
        return null; //返回null一定要检查！
    }

    private void checkTable(String name, Indent indent) throws Error {
        for (SymItem item : currentTable.symItems) {
            if (item.getName().equals(name)) {
                ErrorRecorder.recordError(new Error(Error.Type.name_redefine, indent.getLineNum()));
            }
        }
    }

    private void popFromStack() {
        stack.removeAll(currentTable.symItems);
        symbolTables.add(currentTable);
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
        } catch (Error e) {
            e.printStackTrace();
        }
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
        } else if (node instanceof MainFuncDef) {
            blockStack.add(BlockType.intFuncBlock);
        } else if (node instanceof Stmt) {
            Stmt.Type type = ((Stmt) node).getType();
            if (type.equals(Stmt.Type.whileBranch)) {
                blockStack.add(BlockType.whileBlock);
            } else if (type.equals(Stmt.Type.returnStmt)) {
                //System.out.println(((ErrorRepresent) node.getChild().get(0)).getToken().getLineNum());
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
