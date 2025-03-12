package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatUnreadMentionCountHandler extends BaseHandlerWrapper {
    public ChatUnreadMentionCountHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatUnreadMentionCount chatUnreadMentionCount = (TdApi.UpdateChatUnreadMentionCount) object;
        log.debug("Chat unread mention count:\n {}", chatUnreadMentionCount);
        TdApi.Chat chat = getBot().getChat(chatUnreadMentionCount.chatId);
        if (chat != null) {
           synchronized (chat){
               chat.unreadMentionCount = chatUnreadMentionCount.unreadMentionCount;
           }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatUnreadMentionCount.CONSTRUCTOR
        };
    }
}