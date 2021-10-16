package front.SymTable;

import java.util.ArrayList;

public class SymbolTable {
    public final ArrayList<SymItem> symItems = new ArrayList<>();
    public int[] indent;

    public SymbolTable(int[] indent)  {
        this.indent = indent;
    }

    public ArrayList<SymItem> getTable() {
        return this.symItems;
    }
}
