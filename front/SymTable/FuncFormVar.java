package front.SymTable;

import front.ASD.ASDNode;
import front.ASD.ConstInitVal;
import front.ASD.InitVal;

import java.util.ArrayList;

public class FuncFormVar implements SymItem{
    public String name;
    private int dimension;
    private ArrayList<Integer> shape;

    public FuncFormVar(String name, int dimension, ArrayList<Integer> shape) {
        this.name = name;
        this.dimension = dimension;
        this.shape = shape;
    }

    public String getName() {
        return this.name;
    }

    public boolean isConst() {
        return false;
    }

    public int getDimension() {
        return this.dimension;
    }

    public ArrayList<Integer> getShape() {
        return this.shape;
    }
}
