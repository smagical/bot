package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatHasScheduledMessagesHandler extends BaseHandlerWrapper {
    public ChatHasScheduledMessagesHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatHasScheduledMessages chatHasScheduledMessages = (TdApi.UpdateChatHasScheduledMessages) object;
        log.debug("Chat has scheduled messages: \n {}", chatHasScheduledMessages);
        TdApi.Chat chat = getBot().getChat(chatHasScheduledMessages.chatId);
        if (chat ==  null) {
            return;
        }
        synchronized (chat) {
            chat.hasScheduledMessages = chatHasScheduledMessages.hasScheduledMessages;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatHasScheduledMessages.CONSTRUCTOR
        };
    }
}