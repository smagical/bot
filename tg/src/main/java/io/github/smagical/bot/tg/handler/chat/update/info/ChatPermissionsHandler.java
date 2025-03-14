package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatPermissionsHandler extends BaseHandlerWrapper {
    public ChatPermissionsHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatPermissions chatPermissions = (TdApi.UpdateChatPermissions) object;
        log.debug("Chat permissions: \n{}", chatPermissions);
        TdApi.Chat chat = getBot().getChat(chatPermissions.chatId);
        if (chat == null) {
            return;
        }
        synchronized (chat) {
            chat.permissions = chatPermissions.permissions;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatPermissions.CONSTRUCTOR
        };
    }
}