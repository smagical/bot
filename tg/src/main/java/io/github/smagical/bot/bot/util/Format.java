package io.github.smagical.bot.bot.util;

import org.drinkless.tdlib.TdApi;

public class Format {
    public static String format(TdApi.User user) {
        return user.firstName+user.lastName +":"+user.id;
    }

}
