package io.github.smagical.bot.tg.handler.chat.update.ui;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * chat在列表的位置
 *
 */
@Slf4j
public class ChatPositionHandler extends BaseHandlerWrapper {
    public ChatPositionHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatPosition chatPosition = (TdApi.UpdateChatPosition) object;
        log.debug("\n更新chat在列表的位置\n{}",chatPosition.toString());
        getBot().send(new ChatPositionUpdateEvent(
                new ChatPositionUpdateEvent.Data(
                        chatPosition.chatId,chatPosition.position
                )
        ));
    }

    public static class ChatPositionUpdateEvent extends BaseEvent<ChatPositionUpdateEvent.Data>{
        public final static class Data{
            private final long chatId;
            private final List<TdApi.ChatPosition> chatPosition;

            public Data(long chatId, List<TdApi.ChatPosition> chatPosition) {
                this.chatId = chatId;
                this.chatPosition = Collections.unmodifiableList(chatPosition);
            }

            public Data( long chatId,TdApi.ChatPosition... chatPositions) {
                this.chatPosition = Arrays.asList(chatPositions);
                this.chatId = chatId;
            }
            public long getChatId() {
                return chatId;
            }
            public List<TdApi.ChatPosition> getChatPosition() {
                return chatPosition;
            }
        }
        public ChatPositionUpdateEvent(ChatPositionUpdateEvent.Data code) {
            super(code);
        }
    }


    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatPosition.CONSTRUCTOR
        };
    }
}