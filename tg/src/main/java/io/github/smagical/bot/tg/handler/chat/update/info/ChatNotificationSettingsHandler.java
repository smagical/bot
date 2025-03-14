package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatNotificationSettingsHandler extends BaseHandlerWrapper {
    public ChatNotificationSettingsHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatNotificationSettings chatNotificationSettings = (TdApi.UpdateChatNotificationSettings) object;
        log.debug("Chat notification settings: \n{}", chatNotificationSettings);
        TdApi.Chat chat = getBot().getChat(chatNotificationSettings.chatId);
        if (chat == null) {
            return;
        }
        synchronized (chat) {
            chat.notificationSettings = chatNotificationSettings.notificationSettings;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatNotificationSettings.CONSTRUCTOR
        };
    }
}