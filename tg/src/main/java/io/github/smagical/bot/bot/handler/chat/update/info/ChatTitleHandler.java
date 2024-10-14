package io.github.smagical.bot.bot.handler.chat.update.info;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatTitleHandler extends BaseHandlerWrapper {
    public ChatTitleHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatTitle updateChatTitle = (TdApi.UpdateChatTitle) object;
        log.debug("\n{}",updateChatTitle);
        TdApi.Chat chat = getBot().getChat(updateChatTitle.chatId);
        synchronized (chat) {
            chat.title = updateChatTitle.title;
        }

    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatTitle.CONSTRUCTOR
        };
    }
}