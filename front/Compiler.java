package front;

import front.ASD.CompUnit;
import front.LexicalAnalyser;
import front.Parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Compiler {
    public static int debugging = 0;
    private static final String inputFilePath = "testfile.txt";
    private static final String output1FilePath = "output1.txt";
    private static final String outputFilePath = "output.txt";
    public static boolean branch_opt = false;

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
}
