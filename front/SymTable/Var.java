package front.SymTable;

import front.ASD.ConstInitVal;
import front.ASD.InitVal;

public class Var implements SymItem{
    private String name;
    private boolean isConst;
    private InitVal initVal;
    private ConstInitVal constInitVal;

    public Var(String name, boolean isConst, InitVal initVal) {
        this.name = name;
        this.isConst = isConst;
        this.initVal = initVal;
        this.constInitVal = null;
    }

    public Var(String name, boolean isConst, ConstInitVal constInitVal) {
        this.name = name;
        this.isConst = isConst;
        this.constInitVal = constInitVal;
    }

    public String getName() {
        return this.name;
    }
    public boolean isConst() {
        return this.isConst;
    }
}
