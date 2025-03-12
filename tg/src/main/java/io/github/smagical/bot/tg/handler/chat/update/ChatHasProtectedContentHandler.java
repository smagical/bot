package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatHasProtectedContentHandler extends BaseHandlerWrapper {
    public ChatHasProtectedContentHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatHasProtectedContent chatHasProtectedContent = (TdApi.UpdateChatHasProtectedContent) object;
        log.debug("chat has protected content:\n {}", chatHasProtectedContent);
        TdApi.Chat chat = getBot().getChat(chatHasProtectedContent.chatId);
        synchronized (chat) {
            chat.hasProtectedContent = chatHasProtectedContent.hasProtectedContent;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatHasProtectedContent.CONSTRUCTOR
        };
    }
}