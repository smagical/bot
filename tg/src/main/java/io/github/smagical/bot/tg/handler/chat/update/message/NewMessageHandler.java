package io.github.smagical.bot.tg.handler.chat.update.message;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class NewMessageHandler extends BaseHandlerWrapper {
    public NewMessageHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateNewMessage message = (TdApi.UpdateNewMessage) object;
        log.debug("lastMessage:\n {}", message);
        getBot().send(
                new MessageEvent(message.message)
        );

    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateNewMessage.CONSTRUCTOR
        };
    }

    public static class MessageEvent extends io.github.smagical.bot.event.message.MessageEvent<TdApi.Message> {
        public MessageEvent(TdApi.Message code) {
            super(code);
        }
    }
}
