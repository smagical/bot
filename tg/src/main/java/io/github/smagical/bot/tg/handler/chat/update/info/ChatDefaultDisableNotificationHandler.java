package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;


@Slf4j
public class ChatDefaultDisableNotificationHandler extends BaseHandlerWrapper {
    public ChatDefaultDisableNotificationHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatDefaultDisableNotification chatDefaultDisableNotification = (TdApi.UpdateChatDefaultDisableNotification) object;
        log.debug("chatDefaultDisableNotification : \n{}", chatDefaultDisableNotification);
        TdApi.Chat chat = getBot().getChat(chatDefaultDisableNotification.chatId);
        if (chat == null) {
            return;
        }
        synchronized (chat) {
            chat.defaultDisableNotification = chatDefaultDisableNotification.defaultDisableNotification;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatDefaultDisableNotification.CONSTRUCTOR
        };
    }
}