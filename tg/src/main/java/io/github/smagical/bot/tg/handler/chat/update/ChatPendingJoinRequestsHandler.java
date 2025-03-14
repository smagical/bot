package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatPendingJoinRequestsHandler extends BaseHandlerWrapper {
    public ChatPendingJoinRequestsHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatPendingJoinRequests chatPendingJoinRequests = (TdApi.UpdateChatPendingJoinRequests) object;
        log.debug("Chat pending join requests received:\n {}", chatPendingJoinRequests);
        TdApi.Chat chat = getBot().getChat(chatPendingJoinRequests.chatId);

        if (chat != null) {
            synchronized (chat){
                chat.pendingJoinRequests = chat.pendingJoinRequests;
            }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatPendingJoinRequests.CONSTRUCTOR
        };
    }
}