package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatMessageSenderHandler extends BaseHandlerWrapper {
    public ChatMessageSenderHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatMessageSender chatMessageSender = (TdApi.UpdateChatMessageSender) object;
        log.debug("chat permissions: \n {}", chatMessageSender);
        TdApi.Chat chat = getBot().getChat(chatMessageSender.chatId);
        if (chat ==  null) {
            return;
        }
        synchronized (chat) {
            chat.messageSenderId = chatMessageSender.messageSenderId;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatMessageSender.CONSTRUCTOR
        };
    }
}