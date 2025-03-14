package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatBlockListHandler extends BaseHandlerWrapper {
    public ChatBlockListHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateChatBlockList chatBlockList = (TdApi.UpdateChatBlockList) object;
        log.debug("ChatBlockList: \n{}", chatBlockList);
        TdApi.Chat chat = getBot().getChat(chatBlockList.chatId);
        if (chat ==  null) {
            return;
        }
        synchronized (chat) {
            chat.blockList = chatBlockList.blockList;

        }
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateChatBlockList.CONSTRUCTOR
        };
    }
}