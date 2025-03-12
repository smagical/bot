package io.github.smagical.bot.tg.handler.chat.update;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class SecretChatHandler  extends BaseHandlerWrapper {
    public SecretChatHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateSecretChat secretChat = (TdApi.UpdateSecretChat) object;
        log.debug("\n{}",secretChat);
        getBot().addSecretChat(secretChat.secretChat);
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateSecretChat.CONSTRUCTOR
        };
    }
}