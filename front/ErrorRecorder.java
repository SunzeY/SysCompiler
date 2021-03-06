package front;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

public class ErrorRecorder {
    private static ArrayList<Error> errors = new ArrayList<>();

    public static void recordError(Error error) {
        errors.add(error);
    }

    public static void PrintErrorRecord() {
        PrintStream out = System.out;
        try {
            PrintStream os = new PrintStream("error.txt");
            System.setOut(os);
        } catch (IOException ignored) {
        }
        errors.sort(Comparator.comparingInt(o -> o.lineNumber));
        ArrayList<Error> printedErrors = new ArrayList<>();
        for (Error error: errors) {
            boolean tag = true;
            for (Error printedError: printedErrors) {
                if (printedError.equals(error)) {
                    tag = false;
                    break;
                }
            }
            if(tag && !error.toString().equals("-1 null")) System.out.println(error.toString());
            printedErrors.add(error);

        }
        System.setOut(out);
    }

    public static boolean withoutError() {
        return errors.isEmpty();
    }
}
