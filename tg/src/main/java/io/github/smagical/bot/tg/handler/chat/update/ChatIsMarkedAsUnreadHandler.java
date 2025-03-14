package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatIsMarkedAsUnreadHandler extends BaseHandlerWrapper {
    public ChatIsMarkedAsUnreadHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatIsMarkedAsUnread chatIsMarkedAsUnread = (TdApi.UpdateChatIsMarkedAsUnread) object;
        log.debug("\n{}",chatIsMarkedAsUnread);
        TdApi.Chat chat = getBot().getChat(chatIsMarkedAsUnread.chatId);
        if (chat ==  null) {
            return;
        }
        synchronized (chat) {
            chat.isMarkedAsUnread = chatIsMarkedAsUnread.isMarkedAsUnread;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatIsMarkedAsUnread.CONSTRUCTOR
        };
    }
}