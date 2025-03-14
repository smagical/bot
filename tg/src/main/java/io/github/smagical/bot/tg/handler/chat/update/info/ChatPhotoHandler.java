package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatPhotoHandler extends BaseHandlerWrapper {
    public ChatPhotoHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatPhoto chatPhoto = (TdApi.UpdateChatPhoto) object;
        log.debug("chat photo: \n{}", chatPhoto);
        TdApi.Chat chat = getBot().getChat(chatPhoto.chatId);
        if (chat == null) {
            return;
        }
        synchronized (chat) {
            chat.photo = chatPhoto.photo;
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatPhoto.CONSTRUCTOR
        };
    }
}