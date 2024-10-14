package io.github.smagical.bot.bot.handler.chat.update;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class MessageMentionReadHandler extends BaseHandlerWrapper {
    public MessageMentionReadHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateMessageMentionRead messageMentionRead = (TdApi.UpdateMessageMentionRead) object;
        log.debug("Message mention read:\n {}", messageMentionRead);
        TdApi.Chat chat = getBot().getChat(messageMentionRead.chatId);
        if (chat != null) {
           synchronized (chat){
               chat.unreadMentionCount = messageMentionRead.unreadMentionCount;
           }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateMessageMentionRead.CONSTRUCTOR
        };
    }
}