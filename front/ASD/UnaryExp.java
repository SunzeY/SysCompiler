package front.ASD;

import front.Error;
import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class UnaryExp implements ASDNode{

    public enum Type {
        PrimaryExp, FuncCall, mulUnaryExp
    }
    public Type type;
    public ArrayList<ASDNode> asdNodes;

    public UnaryExp(Type type, ArrayList<ASDNode> asdNodes) {
        this.type = type;
        this.asdNodes = asdNodes;
    }

    @Override
    public void printTestInfo() {
        if (type.equals(Type.PrimaryExp)) {
            asdNodes.get(0).printTestInfo();
        } else if (type.equals(Type.FuncCall)) {
            asdNodes.get(0).printTestInfo();
            System.out.println("LPARENT (");
            if (asdNodes.size()> 1) {
                asdNodes.get(1).printTestInfo();
            }
            System.out.println("RPARENT )");
        } else {
            asdNodes.get(0).printTestInfo();
            asdNodes.get(1).printTestInfo();
        }
        System.out.println("<UnaryExp>");
    }

    public String getFuncCallName() {
        assert (type.equals(Type.FuncCall));
        return ((Indent)asdNodes.get(0)).getName();
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        if (asdNodes == null) {
            return new ArrayList<>();
        }
        return asdNodes;
    }

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        if (this.type.equals(Type.mulUnaryExp)) {
            String type = ((UnaryOp) asdNodes.get(0)).getType();
            MidCode.Op op = type.equals("SUB") ? MidCode.Op.SUB :
                            type.equals("ADD") ? MidCode.Op.ADD : MidCode.Op.NOT;
            if (op.equals(MidCode.Op.NOT)) {
                return midCodeList.add(op, this.asdNodes.get(1).gen_mid(midCodeList), "#VACANT", "#AUTO");
            } else {
                return midCodeList.add(op, "0", this.asdNodes.get(1).gen_mid(midCodeList), "#AUTO");
            }
        } else if (this.type.equals(Type.FuncCall)) {
            String funcName = ((Indent) asdNodes.get(0)).getName();
            midCodeList.add(MidCode.Op.PREPARE_CALL, funcName, "#VACANT", "#VACANT");
            if (asdNodes.size()> 1) {
                asdNodes.get(1).gen_mid(midCodeList);
            }
            midCodeList.add(MidCode.Op.CALL, funcName, "#VACANT", "#VACANT");
            // TODO return value pass by %RET or void
            return midCodeList.add(MidCode.Op.ADD, "%RTX", "0", "#AUTO");
        }
        return this.asdNodes.get(0).gen_mid(midCodeList);
    }

    public int getDimension() {
        if (this.type != Type.PrimaryExp) {
            return 0;
        }
        return ((PrimaryExp) this.asdNodes.get(0)).getDimension();
    }

    public String getName() {
        if (this.type == Type.PrimaryExp) {
            return ((PrimaryExp) this.asdNodes.get(0)).getName();
        } else if (this.type == Type.FuncCall) {
            return ((Indent) this.asdNodes.get(0)).getName();
        }
        return null;
    }

    public int getValue() throws Error {
        if (this.type.equals(Type.PrimaryExp)) {
            return ((PrimaryExp) this.asdNodes.get(0)).getValue();
        }
        else if(this.type.equals(Type.mulUnaryExp)) {
            if (((UnaryOp) asdNodes.get(0)).toString().equals("MINUS -")) {
                return -((PrimaryExp) this.asdNodes.get(0)).getValue();
            } else if (((UnaryOp) asdNodes.get(0)).toString().equals("Not")) {
                if (((PrimaryExp) this.asdNodes.get(0)).getValue() == 0) {
                    return 0;
                }
                else {
                    return 1;
                }
            }
        }
        throw new Error(Error.Type.other_error, -1);
    }
}

