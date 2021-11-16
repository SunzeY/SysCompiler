package SymTable;

import front.ASD.ConstInitVal;
import front.ASD.InitVal;

import java.util.ArrayList;

public class Var implements SymItem{
    private String name;
    private boolean isConst;
    private InitVal initVal;
    public ConstInitVal constInitVal;
    public int addr;
    private int dimension;
    private ArrayList<Integer> shape;
    public String loc;

    public Var(String name, boolean isConst, InitVal initVal, int dimension, ArrayList<Integer> shape, String loc) {
        this.name = name;
        this.isConst = isConst;
        this.initVal = initVal;
        this.constInitVal = null;
        this.dimension = dimension;
        this.shape = shape;
        this.loc = loc;
    }

    public Var(String name, boolean isConst, ConstInitVal constInitVal, int dimension, ArrayList<Integer> shape, String loc) {
        this.name = name;
        this.isConst = isConst;
        this.constInitVal = constInitVal;
        this.dimension = dimension;
        this.shape = shape;
        this.loc = loc;
    }

    public String getName() {
        return this.name;
    }
    public boolean isConst() {
        return this.isConst;
    }

    @Override
    public String get_loc() {
        return this.loc;
    }

    @Override
    public int set_addr(int addr) {
        int size = 1;
        for (Integer dimensionSize: shape) {
            size *= dimensionSize;
        }
        this.addr = addr;
        return this.addr + size * 4;
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
        if (this.name.charAt(0) == '#') {
            return this.name;
        }
        return this.name + "@" + this.loc;
    }

    public int getDimension() {
        return this.dimension;
    }

    public ArrayList<Integer> getShape() {
        return this.shape;
    }
}
