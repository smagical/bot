package io.github.smagical.bot.tg.handler.base;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import org.drinkless.tdlib.TdApi;

public abstract class RetryBaseHandlerAndListenerWrapper<T extends Event> extends RetryBaseHandlerWrapper implements Listener<T> {
    public RetryBaseHandlerAndListenerWrapper(Bot bot) {
        super(bot);
    }

    @Override
    protected void ok(TdApi.Ok ok) {
        getBot().removeListener(this);
    }

    @Override
    protected void error(TdApi.Error error) {
        getBot().removeListener(this);
    }

    @Override
    public void onHandle(TdApi.Object object) {
        getBot().addListener(this);
        super.onHandle(object);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
