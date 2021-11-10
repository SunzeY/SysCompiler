import SymTable.SymItem;
import back.MipsGenerator;
import front.ASD.CompUnit;
import front.ErrorRecorder;
import front.LexicalAnalyser;
import front.Parser;
import SymTable.SymLinker;
import mid.MidCodeList;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

public class Compiler {
    public static int debugging = 2;
    private static final String inputFilePath = "testfile.txt";
    private static final String outputFilePath = "output.txt";
    private static final String errorFilePath = "error.txt";
    private static final String midCodeFilePath = "mid_code.txt";

    private static String readFile() throws IOException {
        InputStream is = new FileInputStream(inputFilePath);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder buffer = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            buffer.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();
        is.close();
        return buffer.toString();
    }


    public static void main(String[] args) throws IOException {
        LexicalAnalyser analyser = new LexicalAnalyser();
        analyser.analyze(readFile());
        PrintStream out = System.out;
        PrintStream os = new PrintStream(outputFilePath);
        PrintStream error = new PrintStream(errorFilePath);
//        PrintStream os1 = new PrintStream(output1FilePath);
//        System.setOut(os1);
        if (debugging == 1) {
            System.out.print(analyser.result());
        }
        Parser parser = new Parser(analyser.getTokenList());
        if (!parser.analyze()){
            return;
        }
        CompUnit unit = parser.getASDTree();
        System.setOut(os);
        // unit.printTestInfo();
        System.setOut(error);
        SymLinker symLinker = new SymLinker(unit);
        symLinker.link();
        if (!ErrorRecorder.withoutError()) {
            ErrorRecorder.PrintErrorRecord();
        }
        MidCodeList midCodeList = new MidCodeList(symLinker.node2tableItem);
        unit.gen_mid(midCodeList);
        System.setOut(new PrintStream(midCodeFilePath));
        midCodeList.printCode();
        HashMap<String, ArrayList<SymItem>> funcTables = symLinker.getFuncTable();
        midCodeList.addTmp(funcTables);
        MipsGenerator mips = new MipsGenerator(midCodeList.midCodes, midCodeList.strCons, funcTables, symLinker.getBlockLoc2table().get("<0,0>"));
        mips.translate();
        mips.toFile();
    }
}
