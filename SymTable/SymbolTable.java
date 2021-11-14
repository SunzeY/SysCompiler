package SymTable;

import java.util.ArrayList;

public class SymbolTable {
    public final ArrayList<SymItem> symItems = new ArrayList<>();
    public String loc;
    public Integer[] int_loc;

    public SymbolTable(int[] indent)  {
        this.loc = "<" + indent[0] + "," + indent[1] + ">";
        int_loc = new Integer[]{indent[0], indent[1]};
    }

    public ArrayList<SymItem> getTable() {
        return this.symItems;
    }
}
