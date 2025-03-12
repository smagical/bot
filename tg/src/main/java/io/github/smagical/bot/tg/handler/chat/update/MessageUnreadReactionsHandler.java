package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class MessageUnreadReactionsHandler extends BaseHandlerWrapper {
    public MessageUnreadReactionsHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateMessageUnreadReactions messageUnreadReactions = (TdApi.UpdateMessageUnreadReactions) object;
        log.debug("Message unread reactions:\n {}", messageUnreadReactions);
         TdApi.Chat chat = getBot().getChat(messageUnreadReactions.chatId);
         if (chat != null) {
             synchronized (chat) {
                 chat.unreadReactionCount = messageUnreadReactions.unreadReactionCount;
             }
         }


    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateMessageUnreadReactions.CONSTRUCTOR
        };
    }
}