package io.github.smagical.bot.plugin.util;

public class ParamsUtils {
    public static void CheckParamsByLen(String[] args, int len) {
         if (args.length < len) {
             throw new RuntimeException(String.format("The parameter length is not enough to 4 %s",len));
         }
    }
    public static Long StringToLong(String str) {
        return Long.parseLong(str);
    }
}
