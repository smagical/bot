package io.github.smagical.bot.tg.handler.chat.update.ui;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatThemeHandler extends BaseHandlerWrapper {
    public ChatThemeHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatTheme chatTheme = (TdApi.UpdateChatTheme) object;
        log.debug("chat theme: \n{}", chatTheme);
        TdApi.Chat chat = getBot().getChat(chatTheme.chatId);
        synchronized (chat) {
            chat.themeName = chatTheme.themeName;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatTheme.CONSTRUCTOR
        };
    }
}