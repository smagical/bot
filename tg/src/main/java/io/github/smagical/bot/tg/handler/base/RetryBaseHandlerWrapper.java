package io.github.smagical.bot.tg.handler.base;

import io.github.smagical.bot.tg.Bot;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public abstract class RetryBaseHandlerWrapper extends BaseHandlerWrapper  {
    protected Runnable lastRunnable;
    protected int retryCount = 3;
    protected int retryAlready = 1;

    public RetryBaseHandlerWrapper(Bot bot) {
        super(bot);
    }


    @Override
    protected void onError(TdApi.Error error) {
        if (lastRunnable != null && retryAlready < retryCount) {
            retryAlready++;
            lastRunnable.run();
        }else {
            retryAlready = -1;
            lastRunnable = null;
            error(error);
        }
    }

    protected void error(TdApi.Error error) {}
    protected void ok(TdApi.Ok ok) {}

    @Override
    protected void onOk(TdApi.Ok object) {
        retryAlready = -1;
        lastRunnable = null;
        ok(object);
    }

    @Override
    public void onHandle(TdApi.Object object) {
        lastRunnable = () -> {
            handle(object);
        };
        lastRunnable.run();
    }
}
