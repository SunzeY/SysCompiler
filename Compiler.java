import SymTable.SymItem;
import SymTable.SymbolTable;
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
        if (debugging == 1) {
            System.out.print(analyser.result());
        }

        // parsing the source code
        Parser parser = new Parser(analyser.getTokenList());
        if (!parser.analyze()){
            System.out.println("Error parsing your testfile");
            ErrorRecorder.PrintErrorRecord();
            return;
        }
        CompUnit unit = parser.getASDTree();
        if (debugging == 1) {
            unit.printTestInfo();
        }

        // linking
        SymLinker symLinker = new SymLinker(unit);
        symLinker.link();
        if (!ErrorRecorder.withoutError()) {
            ErrorRecorder.PrintErrorRecord();
        }

        //generate mid_code
        MidCodeList midCodeList = new MidCodeList(symLinker.node2tableItem);
        unit.gen_mid(midCodeList);
        midCodeList.printCode();
        HashMap<String, ArrayList<SymItem>> funcTables = symLinker.getFuncTable();
        SymbolTable global_table = symLinker.getBlockLoc2table().get("<0,0>");
        midCodeList.addTmp(funcTables, global_table);

        // generate mips_code
        MipsGenerator mips = new MipsGenerator(midCodeList.midCodes, midCodeList.strCons, funcTables, global_table);
        mips.translate();
        mips.toFile();
    }
}
