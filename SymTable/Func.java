package SymTable;

public class Func implements SymItem{

    public enum Type{
        voidFunc, intFunc;
    }
    private Integer argc;
    private String name;
    private Type type;
    public String loc;
    public int addr;

    public Func(String name, Type type, Integer argc, String loc) {
        this.name = name;
        this.argc = argc;
        this.type = type;
        this.loc = loc;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isConst() {
        return false;
    }

    @Override
    public String get_loc() {
        return this.loc;
    }

    @Override
    public int set_addr(int addr) {
        this.addr = addr;
        return this.addr;
    }

    @Override
    public Integer getAddr() {
        return this.addr;
    }

    @Override
    public Integer getSize() {
        return 0;
    }

    @Override
    public String getUniqueName() {
        return this.name;
    }

    public boolean checkForm(int number) {
        return argc == number;
    }

    public Type getType() {
        return this.type;
    }
}
