package io.github.smagical.bot.tg.handler.chat.update.message;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.tg.handler.chat.update.ui.ChatPositionHandler;
import io.github.smagical.bot.event.message.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

/**
 * 草稿信息
 */
@Slf4j
public class ChatDraftMessageHandler extends BaseHandlerWrapper {
    public ChatDraftMessageHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatDraftMessage chatDraftMessage = (TdApi.UpdateChatDraftMessage) object;
        log.debug("chatDraftMessage: \n{}", chatDraftMessage);
        TdApi.Chat chat = getBot().getChat(chatDraftMessage.chatId);
        synchronized (chat) {
            chat.draftMessage = chatDraftMessage.draftMessage;
            getBot().send(new ChatPositionHandler.ChatPositionUpdateEvent(
                    new ChatPositionHandler.ChatPositionUpdateEvent.Data(chatDraftMessage.chatId,chatDraftMessage.positions)
            ));
            getBot().send(
                    new DraftMessageEvent(chatDraftMessage)
            );
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatDraftMessage.CONSTRUCTOR
        };
    }

    public class DraftMessageEvent extends MessageEvent<TdApi.UpdateChatDraftMessage> {
        private DraftMessageEvent(TdApi.UpdateChatDraftMessage code) {
            super(code);
        }
    }

}