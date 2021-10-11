package front.ASD;

import java.util.ArrayList;

public interface ASDNode {
    public void printTestInfo();
    public void linkWithSymbolTable();
    ArrayList<ASDNode> getChild();
}
