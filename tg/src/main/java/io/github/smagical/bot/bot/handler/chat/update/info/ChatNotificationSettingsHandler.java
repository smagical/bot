package io.github.smagical.bot.bot.handler.chat.update.info;

import io.github.smagical.bot.bot.Bot;
import io.github.smagical.bot.bot.handler.base.BaseHandlerWrapper;
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