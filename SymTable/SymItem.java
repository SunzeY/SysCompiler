package SymTable;

public interface SymItem {
    public String getName();
    boolean isConst();
    public String get_loc();
    public int set_addr(int addr);
    public Integer getAddr();
    public Integer getSize();

    String getUniqueName();
}
