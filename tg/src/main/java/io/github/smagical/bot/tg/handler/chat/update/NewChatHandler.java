package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class NewChatHandler  extends BaseHandlerWrapper {
    public NewChatHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateNewChat newChat = (TdApi.UpdateNewChat) object;
        log.debug("New chat:\n {}", newChat);
        getBot().addChat(newChat.chat);
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateNewChat.CONSTRUCTOR
        };
    }
}