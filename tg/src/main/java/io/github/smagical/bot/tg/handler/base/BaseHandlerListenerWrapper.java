package io.github.smagical.bot.tg.handler.base;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import org.drinkless.tdlib.TdApi;

public abstract class BaseHandlerListenerWrapper<T extends Event> extends BaseHandlerWrapper implements Listener<T> {
    public BaseHandlerListenerWrapper(Bot bot) {
        super(bot);
    }

    @Override
    protected void onOk(TdApi.Ok object) {
        super.onOk(object);
        getBot().removeListener(this);
    }

    @Override
    protected void onError(TdApi.Error error) {
        super.onError(error);
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
