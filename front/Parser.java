package front;

import front.ASD.*;
import front.ASD.Number;

import java.util.ArrayList;

public class Parser {
    private ASDNode ASDRoot;
    private final ArrayList<Token> tokens;
    private int tokenIndex;

    public Parser(ArrayList<Token> tokens) {
        ASDRoot = null;
        this.tokens = tokens;
        tokenIndex = -1;
    }

    public void getSym() throws Error {
        if (this.tokenIndex > tokens.size() - 1) {
            throw new Error(Error.Type.other_error, -1);
        }
        this.tokenIndex += 1;
    }

    private Token sym() {
        return this.tokens.get(tokenIndex);
    }

    private Token sym(int bias) {
        return this.tokens.get(tokenIndex + bias);
    }

    public String symType() {
        return tokens.get(tokenIndex).getTokenClass();
    }

    public String symType(int bios) {
        return tokens.get(tokenIndex + bios).getTokenClass();
    }

    public boolean analyze() {
        try {
            getSym();
            this.ASDRoot = CompUnit();
        } catch (Error error) {
            return false;
        }
        return tokenIndex == tokens.size();
    }

    public CompUnit getASDTree() {
        return (CompUnit) this.ASDRoot;
    }

    public CompUnit CompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        try {
            if (symType(1).equals("MAINTK")) {
                mainFuncDef = MainFuncDef();
                if (Compiler.debugging == 2) System.out.println("<CompUnit>");
                return new CompUnit(decls, funcDefs, mainFuncDef);
            } else {
                while (!symType(2).equals("LPARENT")) {
                    if (symType().equals("CONSTTK")) {
                        decls.add(new Decl(ConstDecl()));
                    } else {
                        decls.add(new Decl(VarDecl()));
                    }
                }
            }
            if (symType(1).equals("MAINTK")) {
                mainFuncDef = MainFuncDef();
                if (Compiler.debugging == 2) System.out.println("<CompUnit>");
                return new CompUnit(decls, funcDefs, mainFuncDef);
            } else {
                while (!symType(1).equals("MAINTK") && symType(2).equals("LPARENT")) {
                    funcDefs.add(FuncDef());
                }
            }
            mainFuncDef = MainFuncDef();
        } catch (Error error) {
            //...
        }
        if (Compiler.debugging == 2) System.out.println("<CompUnit>");
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    private ConstDecl ConstDecl() throws Error {
        BType btype;
        ArrayList<ConstDef> constDefs = new ArrayList<>();

        if (!symType().equals("CONSTTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("INTTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        btype = new BType(BType.Type.Int);
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        constDefs.add(ConstDef());
        while (symType().equals("COMMA")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            constDefs.add(ConstDef());
        }
        if (!symType().equals("SEMICN")) {
            ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
            tokenIndex -= 1;
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();

        if (Compiler.debugging == 2) System.out.println("<ConstDecl>");

        return new ConstDecl(btype, constDefs);
    }


    private VarDecl VarDecl() throws Error {
        BType btype = new BType(BType.Type.Int);
        ArrayList<VarDef> varDefs = new ArrayList<>();
        if (!symType().equals("INTTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        varDefs.add(VarDef());
        while (symType().equals("COMMA")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            varDefs.add(VarDef());
        }
        if (!symType().equals("SEMICN")) {
            ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
            tokenIndex -= 1;
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();

        if (Compiler.debugging == 2) System.out.println("<VarDecl>");
        return new VarDecl(btype, varDefs);
    }


    private MainFuncDef MainFuncDef() throws Error {
        Block block;
        if (!symType().equals("INTTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("MAINTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("LPARENT")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("RPARENT")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        block = Block();

        if (Compiler.debugging == 2) System.out.println("<MainFuncDef>");
        ErrorRepresent blockEnd = new ErrorRepresent(sym(-1));
        return new MainFuncDef(block, blockEnd);
    }

    private ConstDef ConstDef() throws Error {
        if (!symType().equals("IDENFR")) {
            throw new Error(Error.Type.other_error, -1);
        }
        Indent indent = new Indent(sym());
        ArrayList<ConstExp> constExps = new ArrayList<>();
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        while (symType().equals("LBRACK")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            constExps.add(ConstExp());
            if (!symType().equals("RBRACK")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_brace, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        }
        if (!symType().equals("ASSIGN")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        ConstInitVal constInitVal = ConstInitVal();

        if (Compiler.debugging == 2) System.out.println("<ConstDef>");
        return new ConstDef(indent, constExps, constInitVal);
    }

    private ConstInitVal ConstInitVal() throws Error {
        ArrayList<ASDNode> asdNodes = new ArrayList<>();
        ConstInitVal.Type type = ConstInitVal.Type.mulInitVal;
        if (symType().equals("LBRACE")) { //`{`
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (symType().equals("RBRACE")) {
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                if (Compiler.debugging == 2) System.out.println("<ConstInitVal>");
                return new ConstInitVal(type, asdNodes);
            }
            asdNodes.add(ConstInitVal());
            while (symType().equals("COMMA")) {
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                asdNodes.add(ConstInitVal());
            }
            if (!symType().equals("RBRACE")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else {
            type = ConstInitVal.Type.Exp;
            asdNodes.add(ConstExp());
        }

        if (Compiler.debugging == 2) System.out.println("<ConstInitVal>");
        return new ConstInitVal(type, asdNodes);
    }

    private InitVal InitVal() throws Error {
        InitVal.Type type = InitVal.Type.mulInitVal;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();
        if (symType().equals("LBRACE")) { //`{`
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (symType().equals("RBRACE")) {
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                if (Compiler.debugging == 2) System.out.println("<InitVal>");
                return new InitVal(InitVal.Type.mulInitVal, asdNodes);
            }
            asdNodes.add(InitVal());
            while (symType().equals("COMMA")) {
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                asdNodes.add(InitVal());
            }
            if (!symType().equals("RBRACE")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else {
            type = InitVal.Type.Exp;
            asdNodes.add(Exp());
        }
        if (Compiler.debugging == 2) System.out.println("<InitVal>");
        return new InitVal(type, asdNodes);
    }

    private VarDef VarDef() throws Error {
        Indent indent;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        InitVal initVal = null;
        if (!symType().equals("IDENFR")) {
            throw new Error(Error.Type.other_error, -1);
        }
        indent = new Indent(sym());
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        while (symType().equals("LBRACK")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            constExps.add(ConstExp());
            if (!symType().equals("RBRACK")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_brace, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        }
        if (symType().equals("ASSIGN")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            initVal = InitVal();
        }

        if (Compiler.debugging == 2) System.out.println("<VarDef>");
        return new VarDef(indent, constExps, initVal);
    }

    private FuncDef FuncDef() throws Error {
        FuncType funcType;
        Indent indent;
        FuncFParams funcFParams = new FuncFParams(new ArrayList<>());
        Block block;
        if (!symType().equals("INTTK") && !symType().equals("VOIDTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (symType().equals("INTTK")) {
            funcType = new FuncType(FuncType.Type.Int);
        } else {
            funcType = new FuncType(FuncType.Type.Void);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        if (Compiler.debugging == 2) System.out.println("<FuncType>");
        getSym();
        if (!symType().equals("IDENFR")) {
            throw new Error(Error.Type.other_error, -1);
        }
        indent = new Indent(sym());
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("LPARENT")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("RPARENT")) {
            funcFParams = FuncFParams();
        }
        if (!symType().equals("RPARENT")) {
            ErrorRecorder.recordError(new Error(Error.Type.miss_parent, sym(-1).getLineNum()));
            this.tokenIndex -= 1;
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        block = Block();
        if (Compiler.debugging == 2) System.out.println("<FuncDef>");
        ErrorRepresent blockEnd = new ErrorRepresent(sym(-1));
        return new FuncDef(funcType, indent, funcFParams, block, blockEnd);
    }

    private FuncFParams FuncFParams() throws Error {
        ArrayList<FuncFParam> funcFParams = new ArrayList<>();
        funcFParams.add(FuncFParam());
        while (symType().equals("COMMA")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            funcFParams.add(FuncFParam());
        }
        if (Compiler.debugging == 2) System.out.println("<FuncFParams>");
        return new FuncFParams(funcFParams);
    }


    private FuncFParam FuncFParam() throws Error {
        BType btype = new BType(BType.Type.Int);
        Indent indent;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        if (!symType().equals("INTTK")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("IDENFR")) {
            throw new Error(Error.Type.other_error, -1);
        }
        indent = new Indent(sym());
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("LBRACK")) {
            if (Compiler.debugging == 2) System.out.println("<FuncFParam>");
            return new FuncFParam(btype, indent, constExps, false);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        if (!symType().equals("RBRACK")) {
            ErrorRecorder.recordError(new Error(Error.Type.miss_brace, sym(-1).getLineNum()));
            this.tokenIndex -= 1;
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        while (symType().equals("LBRACK")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            constExps.add(ConstExp());
            if (!symType().equals("RBRACK")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_brace, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        }
        if (Compiler.debugging == 2) System.out.println("<FuncFParam>");
        return new FuncFParam(btype, indent, constExps, true);
    }

    private Block Block() throws Error {
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        if (!symType().equals("LBRACE")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        while (!symType().equals("RBRACE")) {
            blockItems.add(BlockItem());
        }
        if (!symType().equals("RBRACE")) {
            throw new Error(Error.Type.other_error, -1);
        }
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();

        if (Compiler.debugging == 2) System.out.println("<Block>");
        return new Block(blockItems);
    }

    private BlockItem BlockItem() throws Error {
        if (symType().equals("CONSTTK")) {
            return new BlockItem(new Decl(ConstDecl()));
        } else if (symType().equals("INTTK")) {
            return new BlockItem(new Decl(VarDecl()));
        } else {
            return new BlockItem(Stmt());
        }
    }

    private Stmt Stmt() throws Error {
        ArrayList<ASDNode> asdNodes = new ArrayList<>();
        Stmt.Type type;
        if (symType().equals("SEMICN")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (Compiler.debugging == 2) System.out.println("<Stmt>");
            return new Stmt(Stmt.Type.None, asdNodes);
        }
        if (symType().equals("LBRACE")) {
            asdNodes.add(Block());
            if (Compiler.debugging == 2) System.out.println("<Stmt>");
            return new Stmt(Stmt.Type.Block, asdNodes);
        }

        if (symType().equals("IFTK")) {
            type = Stmt.Type.ifBranch;
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("LPARENT")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            asdNodes.add(Cond());
            if (!symType().equals("RPARENT")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_parent, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            asdNodes.add(Stmt());
            if (symType().equals("ELSETK")) {
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                asdNodes.add(Stmt());
            }
        } else if (symType().equals("WHILETK")) {
            type = Stmt.Type.whileBranch;
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("LPARENT")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            asdNodes.add(Cond());
            if (!symType().equals("RPARENT")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_parent, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            asdNodes.add(Stmt());
        } else if (symType().equals("BREAKTK")) {
            type = Stmt.Type.breakStmt;
            asdNodes.add(new ErrorRepresent(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("SEMICN")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else if (symType().equals("CONTINUETK")) {
            type = Stmt.Type.continueStmt;
            asdNodes.add(new ErrorRepresent(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("SEMICN")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else if (symType().equals("RETURNTK")) {
            type = Stmt.Type.returnStmt;
            asdNodes.add(new ErrorRepresent(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("SEMICN")) {
                asdNodes.add(Exp());
            }
            if (!symType().equals("SEMICN")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else if (symType().equals("PRINTFTK")) {
            type = Stmt.Type.output;
            asdNodes.add(new ErrorRepresent(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("LPARENT")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("STRCON")) {
                throw new Error(Error.Type.other_error, -1);
            }
            asdNodes.add(new FormatString(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            while (symType().equals("COMMA")) {
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                asdNodes.add(Exp());
            }
            if (!symType().equals("RPARENT")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_parent, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();

            if (!symType().equals("SEMICN")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else {
            boolean isExp = true;
            int savedTokenIndex = this.tokenIndex;
            boolean tag = Compiler.debugging == 2;
            Compiler.debugging = 0;
            try {
                LVal();
            } catch (Error e) {
                //..
            }
            if (symType().equals("ASSIGN")) {
                isExp = false;
            }
            this.tokenIndex = savedTokenIndex;
            if (tag) Compiler.debugging = 2;
            if (isExp) { //Exp
                type = Stmt.Type.Exp;
                asdNodes.add(Exp());
                if (!symType().equals("SEMICN")) {
                    ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                    this.tokenIndex -= 1;
                }
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                if (Compiler.debugging == 2) System.out.println("<Stmt>");
                return new Stmt(type, asdNodes);
            }
            // LVal = ...
            asdNodes.add(LVal());
            if (!symType().equals("ASSIGN")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (symType().equals("GETINTTK")) {
                type = Stmt.Type.input;
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                if (!symType().equals("LPARENT")) {
                    throw new Error(Error.Type.other_error, -1);
                }
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                if (!symType().equals("RPARENT")) {
                    ErrorRecorder.recordError(new Error(Error.Type.miss_parent, sym(-1).getLineNum()));
                    this.tokenIndex -= 1;
                }
                if (Compiler.debugging == 2) System.out.println(sym().toString());
                getSym();
                if (!symType().equals("SEMICN")) {
                    ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                    this.tokenIndex -= 1;
                }
            } else {
                type = Stmt.Type.Assign;
                asdNodes.add(Exp());
            }
            if (!symType().equals("SEMICN")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_semi, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        }

        if (Compiler.debugging == 2) System.out.println("<Stmt>");
        return new Stmt(type, asdNodes);
    }

    private Exp Exp() throws Error {
        AddExp addExp = AddExp();
        if (Compiler.debugging == 2) System.out.println("<Exp>");
        return new Exp(addExp);
    }

    private Cond Cond() throws Error {
        LOrExp lOrExp = LOrExp();
        if (Compiler.debugging == 2) System.out.println("<Cond>");
        return new Cond(lOrExp);
    }

    private LVal LVal() throws Error {
        Indent indent;
        ArrayList<Exp> exps = new ArrayList<>();
        if (!symType().equals("IDENFR")) {
            throw new Error(Error.Type.other_error, -1);
        }
        indent = new Indent(sym());
        if (Compiler.debugging == 2) System.out.println(sym().toString());
        getSym();
        while (symType().equals("LBRACK")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            exps.add(Exp());
            if (!symType().equals("RBRACK")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        }

        if (Compiler.debugging == 2) System.out.println("<LVal>");
        return new LVal(indent, exps);
    }

    private PrimaryExp PrimaryExp() throws Error {
        PrimaryExp primaryExp;
        if (symType().equals("LPARENT")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            primaryExp = new PrimaryExp(Exp());
            if (!symType().equals("RPARENT")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else if (symType().equals("INTCON")) {
            primaryExp = new PrimaryExp(new Number(new IntConst(sym())));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            if (Compiler.debugging == 2) System.out.println("<Number>");
            getSym();
        } else {
            primaryExp = new PrimaryExp(LVal());
        }

        if (Compiler.debugging == 2) System.out.println("<PrimaryExp>");
        return primaryExp;
    }

    private UnaryExp UnaryExp() throws Error {
        UnaryExp.Type type;
        ArrayList<ASDNode> asdNodes = new ArrayList<>();
        if (symType().equals("IDENFR") && symType(1).equals("LPARENT")) {
            type = UnaryExp.Type.FuncCall;
            if (!symType().equals("IDENFR")) {
                throw new Error(Error.Type.other_error, -1);
            }
            asdNodes.add(new Indent(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("LPARENT")) {
                throw new Error(Error.Type.other_error, -1);
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (!symType().equals("RPARENT")) {
                asdNodes.add(FuncRParams());
            }
            if (!symType().equals("RPARENT")) {
                ErrorRecorder.recordError(new Error(Error.Type.miss_parent, sym(-1).getLineNum()));
                this.tokenIndex -= 1;
            }
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
        } else if (symType().equals("PLUS") || symType().equals("MINU") || symType().equals("NOT")) {
            type = UnaryExp.Type.mulUnaryExp;
            asdNodes.add(new UnaryOp(sym()));
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            if (Compiler.debugging == 2) System.out.println("<UnaryOp>");
            asdNodes.add(UnaryExp());
        } else {
            type = UnaryExp.Type.PrimaryExp;
            asdNodes.add(PrimaryExp());
        }
        if (Compiler.debugging == 2) System.out.println("<UnaryExp>");
        return new UnaryExp(type, asdNodes);
    }

    private FuncRParams FuncRParams() throws Error {
        ArrayList<Exp> exps = new ArrayList<>();
        exps.add(Exp());
        while (symType().equals("COMMA")) {
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            exps.add(Exp());
        }
        if (Compiler.debugging == 2) System.out.println("<FuncRParams>");
        return new FuncRParams(exps);
    }

    private MulExp MulExp() throws Error {
        ArrayList<UnaryExp> unaryExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();
        unaryExps.add(UnaryExp());
        while (symType().equals("MULT") || symType().equals("DIV") || symType().equals("MOD")) {
            Ops.add(sym());
            if (Compiler.debugging == 2) System.out.println("<MulExp>");
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            unaryExps.add(UnaryExp());
        }
        if (Compiler.debugging == 2) System.out.println("<MulExp>");
        return new MulExp(Ops, unaryExps);
    }

    private AddExp AddExp() throws Error {
        ArrayList<MulExp> mulExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();
        mulExps.add(MulExp());
        while (symType().equals("PLUS") || symType().equals("MINU")) {
            Ops.add(sym());
            if (Compiler.debugging == 2) System.out.println("<AddExp>");
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            mulExps.add(MulExp());
        }
        if (Compiler.debugging == 2) System.out.println("<AddExp>");
        return new AddExp(Ops, mulExps);
    }

    private RelExp RelExp() throws Error {
        ArrayList<AddExp> addExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();
        addExps.add(AddExp());
        while (symType().equals("LSS") || symType().equals("LEQ") || symType().equals("GRE") || symType().equals("GEQ")) {
            Ops.add(sym());
            if (Compiler.debugging == 2) System.out.println("<RelExp>");
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            addExps.add(AddExp());
        }
        if (Compiler.debugging == 2) System.out.println("<RelExp>");
        return new RelExp(Ops, addExps);
    }

    private EqExp EqExp() throws Error {
        ArrayList<RelExp> relExps = new ArrayList<>();
        ArrayList<Token> Ops = new ArrayList<>();
        relExps.add(RelExp());
        while (symType().equals("EQL") || symType().equals("NEQ")) {
            Ops.add(sym());
            if (Compiler.debugging == 2) System.out.println("<EqExp>");
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            relExps.add(RelExp());
        }
        if (Compiler.debugging == 2) System.out.println("<EqExp>");
        return new EqExp(Ops, relExps);
    }

    private LAndExp LAndExp() throws Error {
        ArrayList<EqExp> eqExps = new ArrayList<>();
        eqExps.add(EqExp());
        while (symType().equals("AND")) {
            if (Compiler.debugging == 2) System.out.println("<LAndExp>");
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            eqExps.add(EqExp());
        }
        if (Compiler.debugging == 2) System.out.println("<LAndExp>");
        return new LAndExp(eqExps);
    }

    private LOrExp LOrExp() throws Error {
        ArrayList<LAndExp> lAndExps = new ArrayList<>();
        lAndExps.add(LAndExp());
        while (symType().equals("OR")) {
            if (Compiler.debugging == 2) System.out.println("<LOrExp>");
            if (Compiler.debugging == 2) System.out.println(sym().toString());
            getSym();
            lAndExps.add(LAndExp());
        }
        if (Compiler.debugging == 2) System.out.println("<LOrExp>");
        return new LOrExp(lAndExps);
    }


    private ConstExp ConstExp() throws Error {
        AddExp addExp = AddExp();
        if (Compiler.debugging == 2) System.out.println("<ConstExp>");
        return new ConstExp(addExp);
    }
}
