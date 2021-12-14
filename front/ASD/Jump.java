package front.ASD;

import mid.MidCode;
import mid.MidCodeList;

import java.util.ArrayList;

public class Jump implements ASDNode{
    public String label;

    public Jump(String label) {
        this.label = label;
    }

    @Override
    public void printTestInfo() {
    }

    @Override
    public void linkWithSymbolTable() {
    }

    @Override
    public ArrayList<ASDNode> getChild() {
        return null;
    }

    @Override
    public String gen_mid(MidCodeList midCodeList) {
        return midCodeList.add(MidCode.Op.JUMP, "#VACANT", "#VACANT", label);
    }
}
