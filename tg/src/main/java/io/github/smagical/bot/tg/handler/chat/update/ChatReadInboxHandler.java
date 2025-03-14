package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatReadInboxHandler extends BaseHandlerWrapper {
    public ChatReadInboxHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatReadInbox chatReadInbox = (TdApi.UpdateChatReadInbox) object;
        log.debug("Chat read inbox: \n{}", chatReadInbox);
        TdApi.Chat chat = getBot().getChat(chatReadInbox.chatId);

        if (chat != null) {
            synchronized (chat){
                chat.lastReadInboxMessageId = chatReadInbox.lastReadInboxMessageId;
            }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatReadInbox.CONSTRUCTOR
        };
    }
}