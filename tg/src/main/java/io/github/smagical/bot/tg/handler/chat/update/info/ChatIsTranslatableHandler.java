package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatIsTranslatableHandler extends BaseHandlerWrapper {
    public ChatIsTranslatableHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatIsTranslatable chatIsTranslatable = (TdApi.UpdateChatIsTranslatable) object;
        log.debug("Chat isTranslatable:\n {}", chatIsTranslatable);
        TdApi.Chat chat = getBot().getChat(chatIsTranslatable.chatId);
        synchronized (chat) {
            chat.isTranslatable = chatIsTranslatable.isTranslatable;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatIsTranslatable.CONSTRUCTOR
        };
    }
}