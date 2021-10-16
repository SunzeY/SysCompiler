import front.ASD.CompUnit;
import front.ErrorRecorder;
import front.LexicalAnalyser;
import front.Parser;
import front.SymTable.SymLinker;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.ParameterizedType;

public class Compiler {
    public static int debugging = 2;
    private static final String inputFilePath = "testfile.txt";
    private static final String outputFilePath = "output.txt";
    private static final String errorFilePath = "error.txt";

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
        parser.analyze();
        CompUnit unit = parser.getASDTree();
        System.setOut(os);
        // unit.printTestInfo();
        System.setOut(error);
        SymLinker symLinker = new SymLinker(unit);
        symLinker.link();
        if (!ErrorRecorder.withoutError()) {
            ErrorRecorder.PrintErrorRecord();
        }
    }
}
