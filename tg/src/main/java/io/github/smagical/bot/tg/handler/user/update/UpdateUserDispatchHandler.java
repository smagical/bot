package io.github.smagical.bot.tg.handler.user.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.DispatchHandler;
import io.github.smagical.bot.event.Event;

public class UpdateUserDispatchHandler extends DispatchHandler {
    public UpdateUserDispatchHandler(Bot bot) {
        super(bot);
        addHandler(new UserHandler(bot));
        addHandler(new UserStatusHandler(bot));
        addHandler(new UserFullInfoHandler(bot));
    }
    public static interface UpdateUserEvent<T> extends Event<T> {}
}
