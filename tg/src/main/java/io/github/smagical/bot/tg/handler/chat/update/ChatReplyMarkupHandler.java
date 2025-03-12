package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatReplyMarkupHandler extends BaseHandlerWrapper {
    public ChatReplyMarkupHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatReplyMarkup chatReplyMarkup = (TdApi.UpdateChatReplyMarkup) object;
        log.debug("chat reply markup: \n {}", chatReplyMarkup);
        TdApi.Chat chat = getBot().getChat(chatReplyMarkup.chatId);
        if (chat != null) {
            synchronized (chat){
                chat.replyMarkupMessageId = chatReplyMarkup.replyMarkupMessageId;
            }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatReplyMarkup.CONSTRUCTOR
        };
    }
}