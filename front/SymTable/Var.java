package front.SymTable;

import front.ASD.ConstInitVal;
import front.ASD.InitVal;

import java.util.ArrayList;

public class Var implements SymItem{
    private String name;
    private boolean isConst;
    private InitVal initVal;
    private ConstInitVal constInitVal;
    private int dimension;
    private ArrayList<Integer> shape;

    public Var(String name, boolean isConst, InitVal initVal, int dimension, ArrayList<Integer> shape) {
        this.name = name;
        this.isConst = isConst;
        this.initVal = initVal;
        this.constInitVal = null;
        this.dimension = dimension;
        this.shape = shape;
    }

    public Var(String name, boolean isConst, ConstInitVal constInitVal, int dimension, ArrayList<Integer> shape) {
        this.name = name;
        this.isConst = isConst;
        this.constInitVal = constInitVal;
        this.dimension = dimension;
        this.shape = shape;
    }

    public String getName() {
        return this.name;
    }
    public boolean isConst() {
        return this.isConst;
    }

    public int getDimension() {
        return this.dimension;
    }

    public ArrayList<Integer> getShape() {
        return this.shape;
    }
}
