package io.github.smagical.bot.bot.handler.chat.update;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
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