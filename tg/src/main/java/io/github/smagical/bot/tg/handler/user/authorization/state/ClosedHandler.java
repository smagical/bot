package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class ClosedHandler extends BaseHandlerWrapper {
    public ClosedHandler(Bot bot) {
        super(bot);
    }

    public  class ClosedEvent extends BaseEvent{
        private ClosedEvent() {
            super(null);
        }
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.AuthorizationStateClosed state = (TdApi.AuthorizationStateClosed) object;
        getBot().send(new ClosedEvent());
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateClosed.CONSTRUCTOR};
    }
}
