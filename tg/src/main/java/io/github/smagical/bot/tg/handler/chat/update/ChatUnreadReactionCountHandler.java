package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatUnreadReactionCountHandler extends BaseHandlerWrapper {
    public ChatUnreadReactionCountHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatUnreadReactionCount chatUnreadReactionCount = (TdApi.UpdateChatUnreadReactionCount) object;
        log.debug("Chat unread reaction count:\n {}", chatUnreadReactionCount);
        TdApi.Chat chat = getBot().getChat(chatUnreadReactionCount.chatId);
        if (chat != null) {
           synchronized (chat){
               chat.unreadReactionCount = chatUnreadReactionCount.unreadReactionCount;
           }
        }

    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatUnreadReactionCount.CONSTRUCTOR
        };
    }
}