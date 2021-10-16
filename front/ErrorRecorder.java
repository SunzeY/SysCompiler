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
        for (Error error: errors) {
            System.out.println(error.toString());
        }
    }

    public static boolean withoutError() {
        return errors.isEmpty();
    }
}
