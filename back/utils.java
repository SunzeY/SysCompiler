package back;

import java.util.regex.Pattern;

import static java.lang.Math.log;

public class utils {
    public static final int N = 32;
    public static final Pattern IS_DIGIT = Pattern.compile("[0-9]*");
    public static boolean begins_num(String operand) {
        return IS_DIGIT.matcher(operand).matches() || operand.charAt(0) == '+' || operand.charAt(0) == '-';
    }

    public static boolean is_2_power(Integer integer) {
        int x = integer;
        return (x & (x - 1)) == 0;
    }

    public static long[] choose_multiplier(int d, int prec) {
        long l = (long) Math.ceil((Math.log(d) / Math.log(2)));
        long sh_post = l;
        long m_low = (long) Math.floor(Math.pow(2, N+l)/d);
        long m_high = (long) Math.floor((Math.pow(2, N+l) + Math.pow(2, N+l-prec))/d);
        while ((Math.floor(m_low >> 1) < Math.floor(m_high >> 1)) && sh_post>0) {
            m_low = (long) Math.floor(m_low >> 1);
            m_high = (long) Math.floor(m_high >> 1);
            sh_post = sh_post - 1;
        }
        return new long[]{m_high, sh_post, l};
    }

    public static int log2(Integer integer) {
        return (int) Math.floor(Math.log(integer) / Math.log(2));
    }
}
