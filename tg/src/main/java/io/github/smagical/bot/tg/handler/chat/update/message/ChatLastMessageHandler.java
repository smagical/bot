package io.github.smagical.bot.tg.handler.chat.update.message;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.tg.handler.chat.update.ui.ChatPositionHandler;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatLastMessageHandler extends BaseHandlerWrapper {
    public ChatLastMessageHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatLastMessage lastMessage = (TdApi.UpdateChatLastMessage) object;
        log.debug("lastMessage:\n {}", lastMessage);
        TdApi.Chat chat = getBot().getChat(lastMessage.chatId);
        if (chat == null) {
            return;
        }
        synchronized (chat) {
            chat.lastMessage = lastMessage.lastMessage;
            getBot().send(
                    new ChatPositionHandler.ChatPositionUpdateEvent(
                            new ChatPositionHandler.ChatPositionUpdateEvent.Data(
                                    lastMessage.chatId,
                                    lastMessage.positions
                            )
                    )
            );
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatLastMessage.CONSTRUCTOR
        };
    }

    public class LastMessageEvent extends NewMessageHandler.MessageEvent {
        private LastMessageEvent(TdApi.Message code) {
            super(code);
        }
    }
}