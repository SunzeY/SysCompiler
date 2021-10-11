package front;

import java.util.HashMap;
import java.util.Map;

public class Token {
    private final String tokenClass;  // TokenClass Symbol
    //private final int codeVal;        // encode value 0-37
    private final String originStr;   // original string
    private final int intValue;       // value(only `INTCON` have)
    private final int lineNum;
    private int formatCharNum;

    public static Map<Integer, String> decode = new HashMap<Integer, String>() {{
        put(0, "IDENFR");
        put(1, "MAINTK");
        put(2, "CONSTTK");
        put(3, "INTTK");
        put(4, "BREAKTK");
        put(5, "CONTINUETK");
        put(6, "IFTK");
        put(7, "ELSETK");
        put(8, "WHILETK");
        put(9, "GETINTTK");
        put(10, "PRINTFTK");
        put(11, "RETURNTK");
        put(12, "VOIDTK");
        put(13, "NOT");
        put(14, "AND");
        put(15, "OR");
        put(16, "PLUS");
        put(17, "MINU");
        put(18, "MULT");
        put(19, "DIV");
        put(20, "MOD");
        put(21, "LSS");
        put(22, "LEQ");
        put(23, "GRE");
        put(24, "GEQ");
        put(25, "EQL");
        put(26, "NEQ");
        put(27, "ASSIGN");
        put(28, "SEMICN");
        put(29, "COMMA");
        put(30, "LPARENT");
        put(31, "RPARENT");
        put(32, "LBRACK");
        put(33, "RBRACK");
        put(34, "LBRACE");
        put(35, "RBRACE");
        put(36, "INTCON");
        put(37, "STRCON");
    }};

    public Token(String tokenClass, String originStr, int lineNum) {
        this.tokenClass = tokenClass;
        this.originStr = originStr;
        this.lineNum = lineNum;
        if (tokenClass.equals("INTCON")) {
            this.intValue = Integer.parseInt(originStr);
        } else {
            this.intValue = 0;
        }
        this.formatCharNum = 0;
    }

    public Token(String tokenClass, String originStr, int lineNum, int formatCharNum) {
        this.tokenClass = tokenClass;
        this.originStr = originStr;
        this.lineNum = lineNum;
        if (tokenClass.equals("INTCON")) {
            this.intValue = Integer.parseInt(originStr);
        } else {
            this.intValue = 0;
        }
        this.formatCharNum = formatCharNum;
    }

    @Override
    public String toString() {
        return this.tokenClass + " " + this.originStr;
    }


    public String getTokenClass() {
        return this.tokenClass;
    }

    public String getName() {
        return this.originStr;
    }

    public int getLineNum() {
        return this.lineNum;
    }

    public int getFormatCharNum() {
        return this.getLineNum();
    }
}
