package front;

import java.util.ArrayList;
import java.util.HashMap;

public class LexicalAnalyser {

    private final ArrayList<Token> tokens;
    private StringBuilder token;
    private String programCode;
    private int index;
    private int formatCharNum;
    private int lineNum;
    public static HashMap<String, String> reserveMap = new HashMap<String, String>() {{
        put("main", "MAINTK");
        put("const", "CONSTTK");
        put("int", "INTTK");
        put("break", "BREAKTK");
        put("continue", "CONTINUETK");
        put("if", "IFTK");
        put("else", "ELSETK");
        put("while", "WHILETK");
        put("getint", "GETINTTK");
        put("printf", "PRINTFTK");
        put("return", "RETURNTK");
        put("void", "VOIDTK");
    }};

    public LexicalAnalyser() {
        tokens = new ArrayList<>();
        formatCharNum = 0;
    }


    public void error() {
        if (Compiler.debugging == 2) System.out.println("error when doing lexical analysis at line Number" + lineNum);
    }

    public ArrayList<Token> getTokenList() {
        return tokens;
    }

    public void analyze(String programCode) {
        index = 0;
        lineNum = 1;
        this.programCode = programCode;
        while (index < programCode.length()) {
            token = new StringBuilder();
            String tokenClass = getToken();
            if (Token.decode.containsValue(tokenClass)) {
                if (tokenClass.equals("STRCON")) {
                    tokens.add(new Token(tokenClass, token.toString(), lineNum, formatCharNum));
                    this.formatCharNum = 0;
                } else {
                    tokens.add(new Token(tokenClass, token.toString(), lineNum));
                }
            }
        }
    }

    public String getToken() {
        String tokenClass = "";
        while (isSpace() || isNewLine() || isTab()) {
            index += 1;
        }
        if (index >= programCode.length()) {
            return "false";
        }
        if (isLetter()) {
            while (isLetter() || isDigit()) {              // words Branch
                token.append(programCode.charAt(index));
                index += 1;
            }
            tokenClass = reserveMap.getOrDefault(token.toString(), "IDENFR");

        } else if (isDigit()) {                            // digit Branch
            while (isDigit()) {
                token.append(programCode.charAt(index));
                index += 1;
            }
            if (token.toString().length() > 1 && token.toString().charAt(0) == '0') {
                error();
            }
            tokenClass = "INTCON";
        } else if (isEqs()) {                              // op_div Branch
            token.append(programCode.charAt(index));       // two character analysis.
            index += 1;
            if (isEqs()) {
                token.append(programCode.charAt(index));
                index += 1;
                tokenClass = "EQL";
            } else {
                tokenClass = "ASSIGN";
            }
        } else if (isVLine()) {
            token.append(programCode.charAt(index));
            index += 1;
            if (isVLine()) {
                token.append(programCode.charAt(index));
                index += 1;
                tokenClass = "OR";
            } else {
                error();
            }
        } else if (isAnd()) {
            token.append(programCode.charAt(index));
            index += 1;
            if (isAnd()) {
                token.append(programCode.charAt(index));
                index += 1;
                tokenClass = "AND";
            } else {
                error();
            }
        } else if (isLABrace()) {
            token.append(programCode.charAt(index));
            index += 1;
            if (isEqs()) {
                token.append(programCode.charAt(index));
                index += 1;
                tokenClass = "LEQ";
            } else {
                tokenClass = "LSS";
            }
        } else if (isRABrace()) {
            token.append(programCode.charAt(index));
            index += 1;
            if (isEqs()) {
                token.append(programCode.charAt(index));
                index += 1;
                tokenClass = "GEQ";
            } else {
                tokenClass = "GRE";
            }
        } else if (isExc()) {
            token.append(programCode.charAt(index));
            index += 1;
            if (isEqs()) {
                token.append(programCode.charAt(index));
                index += 1;
                tokenClass = "NEQ";
            } else {
                tokenClass = "NOT";
            }
        } else if (isAdd()) {
            tokenClass = "PLUS";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isMinus()) {
            tokenClass = "MINU";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isStar()) {
            tokenClass = "MULT";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isPercent()) {
            tokenClass = "MOD";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isSemi()) {
            tokenClass = "SEMICN";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isComma()) {
            tokenClass = "COMMA";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isLParent()) {
            tokenClass = "LPARENT";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isRParent()) {
            tokenClass = "RPARENT";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isLBrack()) {
            tokenClass = "LBRACK";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isRBrack()) {
            tokenClass = "RBRACK";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isLBrace()) {
            tokenClass = "LBRACE";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isRBrace()) {
            tokenClass = "RBRACE";
            token.append(programCode.charAt(index));
            index += 1;
        } else if (isDQmark()) {
            token.append(programCode.charAt(index));
            index += 1;
            this.formatCharNum = 0;
            while (!isDQmark()) {
                char a = programCode.charAt(index);
                if (a == '%' && programCode.charAt(index + 1) == 'd') {
                    this.formatCharNum += 1;
                } else if (!(a==32 || a==33 || (40<=a && a<=126))) {
                    ErrorRecorder.recordError(new Error(Error.Type.illegal_sym, lineNum));
                }
                token.append(programCode.charAt(index));
                index += 1;
            }
            token.append(programCode.charAt(index));
            index += 1;
            tokenClass = "STRCON";
        } else if (isDiv()) {
            index += 1;
            if (isDiv()) {
                while (!isNewLine()) {
                    index += 1;
                }
                index += 1;
                return "annotation";
            } else if (isStar()) {
                do {
                    do {
                        index += 1;
                        isNewLine();
                        if (index >= programCode.length()) {
                            error();
                            return "failed";
                        }
                    } while (anoStar());
                    do {
                        index += 1;
                        isNewLine();
                        if (isDiv()) {
                            index += 1;
                            return "annotation";
                        }
                    } while (isStar());
                    if (index >= programCode.length()) {
                        error();
                        return "failed";
                    }
                } while (anoStar());
                if (index == programCode.length()) { //reach the end without `*/` match the end of annotation.
                    error();
                }
            } else {
                token.append(programCode.charAt(index-1));
                tokenClass = "DIV";
            }
        } else {
            error();
            return "failed";
        }
        return tokenClass;
    }


    public String result() {
        StringBuilder ans = new StringBuilder();
        for (Token token : tokens) {
            ans.append(token.toString()).append("\n");
        }
        return ans.toString();
    }

    public boolean isSpace() {
        return index < programCode.length() && programCode.charAt(index) == ' ';
    }

    public boolean isNewLine() {
        if (index < programCode.length() && programCode.charAt(index) == '\n'){
            lineNum += 1;
            return true;
        }
        return false;
    }

    public boolean isTab() {
        return index < programCode.length() && programCode.charAt(index) == '\t';
    }

    public boolean isLetter() {
        return index < programCode.length() &&
                (Character.isLowerCase(programCode.charAt(index))
                        || Character.isUpperCase(programCode.charAt(index))
                        || programCode.charAt(index) == '_');
    }

    public boolean isDigit() {
        return index < programCode.length() && Character.isDigit(programCode.charAt(index));
    }

    public boolean isSemi() {
        return index < programCode.length() && programCode.charAt(index) == ';';
    }

    public boolean isComma() {
        return index < programCode.length() && programCode.charAt(index) == ',';
    }

    public boolean isLBrace() {
        return index < programCode.length() && programCode.charAt(index) == '{';
    }

    public boolean isRBrace() {
        return index < programCode.length() && programCode.charAt(index) == '}';
    }

    public boolean isLBrack() {
        return index < programCode.length() && programCode.charAt(index) == '[';
    }

    public boolean isRBrack() {
        return index < programCode.length() && programCode.charAt(index) == ']';
    }

    public boolean isLParent() {
        return index < programCode.length() && programCode.charAt(index) == '(';
    }

    public boolean isRParent() {
        return index < programCode.length() && programCode.charAt(index) == ')';
    }

    public boolean isEqs() {
        return index < programCode.length() && programCode.charAt(index) == '=';
    }

    public boolean isAdd() {
        return index < programCode.length() && programCode.charAt(index) == '+';
    }

    public boolean isMinus() {
        return index < programCode.length() && programCode.charAt(index) == '-';
    }

    public boolean isStar() {
        return index < programCode.length() && programCode.charAt(index) == '*';
    }

    public boolean anoStar() {
        return programCode.charAt(index) != '*';
    }

    public boolean isPercent() {
        return index < programCode.length() && programCode.charAt(index) == '%';
    }

    public boolean isLABrace() {
        return index < programCode.length() && programCode.charAt(index) == '<';
    }

    public boolean isRABrace() {
        return index < programCode.length() && programCode.charAt(index) == '>';
    }

    public boolean isVLine() {
        return index < programCode.length() && programCode.charAt(index) == '|';
    }

    public boolean isAnd() {return index < programCode.length() && programCode.charAt(index) == '&'; }

    public boolean isExc() {
        return index < programCode.length() && programCode.charAt(index) == '!';
    }

    public boolean isDQmark() {
        return index < programCode.length() && programCode.charAt(index) == '"';
    }

    public boolean isDiv() {
        return index < programCode.length() && programCode.charAt(index) == '/';
    }
}
