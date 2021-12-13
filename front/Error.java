package front;

import java.util.HashMap;

public class Error extends Exception{
    public Type errorType;
    public int lineNumber;
    public enum Type {
        other_error,
        illegal_sym, name_redefine, undefined_name, func_arg_cnt_mismatch,
        func_arg_type_mismatch, void_return, non_void_non_return, changeConst,
        miss_semi, miss_parent, miss_brace, printf_num_miss_match, misused_BC,
    }

    public final HashMap<Type, String> encode = new HashMap<Type, String>(){{
        put(Type.illegal_sym, "a");
        put(Type.name_redefine, "b");
        put(Type.undefined_name, "c");
        put(Type.func_arg_cnt_mismatch, "d");
        put(Type.func_arg_type_mismatch, "e");
        put(Type.void_return, "f");
        put(Type.non_void_non_return, "g");
        put(Type.changeConst, "h");
        put(Type.miss_semi, "i");
        put(Type.miss_parent, "j");
        put(Type.miss_brace, "k");
        put(Type.printf_num_miss_match, "l");
        put(Type.misused_BC, "m");
    }};

    public Error(Type type, int lineNumber) {
        this.errorType = type;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        return  lineNumber + " " + encode.get(errorType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Error)) {
            return false;
        }
        Error error = (Error) obj;
        return error == this || (error.toString().equals(this.toString()));
    }
}
