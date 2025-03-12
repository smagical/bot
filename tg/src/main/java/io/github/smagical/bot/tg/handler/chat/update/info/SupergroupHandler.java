package io.github.smagical.bot.tg.handler.chat.update.info;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;


@Slf4j
public class SupergroupHandler extends BaseHandlerWrapper {
    public SupergroupHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.UpdateSupergroup supergroup = (TdApi.UpdateSupergroup) object;
        log.debug("Supergroup: \n{}", supergroup);
    }

    @Override
    public int[] support() {
        return new int[] {
                TdApi.UpdateSupergroup.CONSTRUCTOR
        };
    }
}