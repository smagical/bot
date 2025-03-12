package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class BasicGroupFullInfoHandler extends BaseHandlerWrapper {
    public BasicGroupFullInfoHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateBasicGroupFullInfo group = (TdApi.UpdateBasicGroupFullInfo) object;
        log.debug("Update group full info: \n  {}", group);
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateBasicGroupFullInfo.CONSTRUCTOR
        };
    }
}