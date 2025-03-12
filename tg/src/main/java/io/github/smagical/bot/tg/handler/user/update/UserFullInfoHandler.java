package io.github.smagical.bot.tg.handler.user.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class UserFullInfoHandler extends BaseHandlerWrapper {
    public UserFullInfoHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateUserFullInfo userFullInfo = (TdApi.UpdateUserFullInfo) object;
        getBot().send(new UpdateUserFullInfoEvenvt(userFullInfo));
    }


    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateUserFullInfo.CONSTRUCTOR};
    }

    public class UpdateUserFullInfoEvenvt extends BaseEvent<TdApi.UpdateUserFullInfo> implements UpdateUserDispatchHandler.UpdateUserEvent<TdApi.UpdateUserFullInfo> {
        private UpdateUserFullInfoEvenvt(TdApi.UpdateUserFullInfo code) {
            super(code);
        }
    }
}
