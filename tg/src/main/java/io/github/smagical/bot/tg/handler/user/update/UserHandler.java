package io.github.smagical.bot.tg.handler.user.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class UserHandler extends BaseHandlerWrapper {
    public UserHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateUser user = (TdApi.UpdateUser) object;
        getBot().send(new UpdateUserEvent(user.user));
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateUser.CONSTRUCTOR};
    }
    public class UpdateUserEvent extends BaseEvent<TdApi.User> implements UpdateUserDispatchHandler.UpdateUserEvent<TdApi.User> {
        private UpdateUserEvent(TdApi.User code) {
            super(code);
        }
    }
}
