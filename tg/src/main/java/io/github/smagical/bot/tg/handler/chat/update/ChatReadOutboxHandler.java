package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatReadOutboxHandler extends BaseHandlerWrapper {
    public ChatReadOutboxHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatReadOutbox chatReadInbox = (TdApi.UpdateChatReadOutbox) object;
        log.debug("chatReadInbox:\n {}", chatReadInbox);
        TdApi.Chat chat = getBot().getChat(chatReadInbox.chatId);
        if (chat != null) {
            synchronized (chat){
                chat.lastReadOutboxMessageId = chatReadInbox.lastReadOutboxMessageId;
            }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatReadOutbox.CONSTRUCTOR
        };
    }
}