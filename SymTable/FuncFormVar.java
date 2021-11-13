package SymTable;

import java.util.ArrayList;

public class FuncFormVar implements SymItem{
    public String name;
    private int dimension;
    private ArrayList<Integer> shape;
    public int addr;
    public String loc;

    public FuncFormVar(String name, int dimension, ArrayList<Integer> shape, String loc) {
        this.name = name;
        this.dimension = dimension;
        this.shape = shape;
        this.loc = loc;
    }

    public String getName() {
        return this.name;
    }

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
        return this.addr + 4;
    }

    @Override
    public Integer getAddr() {
        return this.addr;
    }

    @Override
    public Integer getSize() {
        int size = 1;
        for (Integer dimensionSize: shape) {
            size *= dimensionSize;
        }
        return size * 4;
    }

    @Override
    public String getUniqueName() {
        return this.name + "@" + loc;
    }

    public int getDimension() {
        return this.dimension;
    }
    public ArrayList<Integer> getShape() {
        return this.shape;
    }
}
