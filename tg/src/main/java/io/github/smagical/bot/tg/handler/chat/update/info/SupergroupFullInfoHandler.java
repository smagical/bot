package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class SupergroupFullInfoHandler extends BaseHandlerWrapper {
    public SupergroupFullInfoHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateSupergroupFullInfo supergroupFullInfo = (TdApi.UpdateSupergroupFullInfo) object;
        log.debug("\n{}",supergroupFullInfo);
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateSupergroupFullInfo.CONSTRUCTOR
        };
    }
}