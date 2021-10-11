package front.SymTable;

public class Func implements SymItem{

    public enum Type{
        voidFunc, intFunc;
    }
    private Integer argc;
    private String name;
    private Type type;

    public Func(String name, Type type, Integer argc) {
        this.name = name;
        this.argc = argc;
        this.type = type;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isConst() {
        return false;
    }

    public boolean checkForm(int number) {
        return argc == number;
    }
}
