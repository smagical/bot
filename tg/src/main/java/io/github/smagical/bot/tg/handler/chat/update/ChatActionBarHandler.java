package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatActionBarHandler extends BaseHandlerWrapper {
    public ChatActionBarHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatActionBar chatActionBar = (TdApi.UpdateChatActionBar) object;
        log.debug("chatActionBar: \n{}", chatActionBar);
        TdApi.Chat chat = getBot().getChat(chatActionBar.chatId);
        synchronized (chat) {
            chat.actionBar = chatActionBar.actionBar;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatActionBar.CONSTRUCTOR
        };
    }
}