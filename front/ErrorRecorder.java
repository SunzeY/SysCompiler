package front;

import java.util.ArrayList;
import java.util.Comparator;

public class ErrorRecorder {
    private static final ArrayList<Error> errors = new ArrayList<>();

    public static void recordError(Error error) {
        errors.add(error);
    }

    public static void PrintErrorRecord() {
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
            if(tag) System.out.println(error.toString());
            printedErrors.add(error);
        }
    }

    public static boolean withoutError() {
        return errors.isEmpty();
    }
}
