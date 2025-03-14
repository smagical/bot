package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

/**
 * 点击的类型 比如👍👎 🎉
 */
@Slf4j
public class ChatAvailableReactionsHandler extends BaseHandlerWrapper {
    public ChatAvailableReactionsHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatAvailableReactions chatAvailableReactions = (TdApi.UpdateChatAvailableReactions) object;
        log.debug("Chat available reactions: \n{}", chatAvailableReactions);
        TdApi.Chat chat = getBot().getChat(chatAvailableReactions.chatId);
        if (chat ==  null) {
            return;
        }
        synchronized (chat) {
            chat.availableReactions = chatAvailableReactions.availableReactions;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatAvailableReactions.CONSTRUCTOR
        };
    }
}