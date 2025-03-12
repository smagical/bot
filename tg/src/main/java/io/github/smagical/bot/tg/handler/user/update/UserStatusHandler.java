package io.github.smagical.bot.tg.handler.user.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class UserStatusHandler extends BaseHandlerWrapper {
    public UserStatusHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateUserStatus status = (TdApi.UpdateUserStatus) object;
        getBot().send(new UserStatusEvent(status));
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateUserStatus.CONSTRUCTOR};
    }

    public class UserStatusEvent extends BaseEvent<TdApi.UpdateUserStatus>implements UpdateUserDispatchHandler.UpdateUserEvent<TdApi.UpdateUserStatus> {
        private UserStatusEvent(TdApi.UpdateUserStatus code) {
            super(code);
        }
    }
}
