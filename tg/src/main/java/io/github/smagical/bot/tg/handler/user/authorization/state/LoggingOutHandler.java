package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import org.drinkless.tdlib.TdApi;

public class LoggingOutHandler extends BaseHandlerWrapper {
    public LoggingOutHandler(Bot bot) {
        super(bot);
    }

    public class LogoutEvent extends io.github.smagical.bot.event.user.LogoutEvent implements AuthorizationStateDispatchHandler.AuthorizationStateEvent{

        private LogoutEvent(Object code) {
            super(code);
        }
    }

    @Override
    protected void handle(TdApi.Object object) {
        getBot().send(
                new LogoutEvent(null)
        );
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR};
    }
}
