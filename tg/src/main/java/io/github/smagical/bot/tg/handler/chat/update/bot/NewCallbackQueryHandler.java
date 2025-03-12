package io.github.smagical.bot.tg.handler.chat.update.bot;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import org.drinkless.tdlib.TdApi;

public class NewCallbackQueryHandler extends BaseHandlerWrapper {

    public NewCallbackQueryHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateNewCallbackQuery newCallbackQuery = (TdApi.UpdateNewCallbackQuery) object;
        getBot().send(
                new NewCallbackQueryEvent(newCallbackQuery)
        );
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateNewCallbackQuery.CONSTRUCTOR};
    }

    public class NewCallbackQueryEvent extends BaseEvent<TdApi.UpdateNewCallbackQuery>{

        public NewCallbackQueryEvent(TdApi.UpdateNewCallbackQuery code) {
            super(code);
        }
    }
}
