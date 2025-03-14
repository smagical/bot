package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatVideoChatHandler extends BaseHandlerWrapper {
    public ChatVideoChatHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatVideoChat chatVideoChat = (TdApi.UpdateChatVideoChat) object;
        log.debug("Chat video chat:\n {}", chatVideoChat);
        TdApi.Chat chat = getBot().getChat(chatVideoChat.chatId);

        if (chat != null) {
            synchronized (chat) {
                chat.videoChat = chatVideoChat.videoChat;
            }
        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatVideoChat.CONSTRUCTOR
        };
    }
}