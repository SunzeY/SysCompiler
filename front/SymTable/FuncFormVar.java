package front.SymTable;

import front.ASD.ASDNode;
import front.ASD.ConstInitVal;
import front.ASD.InitVal;

public class FuncFormVar implements SymItem{
    private String name;
    private boolean isConst;

    public FuncFormVar(String name, boolean isArray) {
        this.name = name;
        this.isConst = isArray;
    }

    public String getName() {
        return this.name;
    }

    public boolean isConst() {
        return false;
    }
}
