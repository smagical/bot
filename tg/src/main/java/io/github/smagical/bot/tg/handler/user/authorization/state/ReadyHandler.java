package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import org.drinkless.tdlib.TdApi;

public class ReadyHandler extends BaseHandlerWrapper {
    public ReadyHandler(Bot bot) {
        super(bot);
    }


    @Override
    protected void handle(TdApi.Object object) {
        getBot()
                .send(new AuthorizationStateDispatchHandler.LoginSuccessEvent(null));
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateReady.CONSTRUCTOR};
    }
}
