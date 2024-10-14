package io.github.smagical.bot.bot.handler.chat.update;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatMessageAutoDeleteTimeHandler extends BaseHandlerWrapper {
    public ChatMessageAutoDeleteTimeHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatMessageAutoDeleteTime chatMessageAutoDeleteTime = (TdApi.UpdateChatMessageAutoDeleteTime) object;
        log.debug("chatMessageAutoDeleteTime: \n {}", chatMessageAutoDeleteTime);
        TdApi.Chat chat = getBot().getChat(chatMessageAutoDeleteTime.chatId);
        synchronized (chat) {
            chat.messageAutoDeleteTime = chatMessageAutoDeleteTime.messageAutoDeleteTime;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatMessageAutoDeleteTime.CONSTRUCTOR
        };
    }
}