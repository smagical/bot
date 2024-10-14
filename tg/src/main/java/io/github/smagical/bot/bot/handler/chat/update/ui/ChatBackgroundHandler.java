package io.github.smagical.bot.bot.handler.chat.update.ui;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatBackgroundHandler extends BaseHandlerWrapper {
    public ChatBackgroundHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatBackground chatBackground = (TdApi.UpdateChatBackground) object;
        log.debug("\n{}",chatBackground);
        TdApi.Chat chat = getBot().getChat(chatBackground.chatId);
        synchronized (chat) {
            chat.background = chatBackground.background;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatBackground.CONSTRUCTOR
        };
    }
}