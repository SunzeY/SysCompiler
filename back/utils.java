package back;

import java.util.regex.Pattern;

public class utils {
    public static final Pattern IS_DIGIT = Pattern.compile("[0-9]*");
    public static boolean begins_num(String operand) {
        return IS_DIGIT.matcher(operand).matches() || operand.charAt(0) == '+' || operand.charAt(0) == '-';
    }

    public static boolean is_2_power(Integer integer) {
        int x = integer;
        return (x & (x - 1)) == 0;
    }
}
