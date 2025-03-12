package io.github.smagical.bot.tg.handler.base;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.HandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public abstract class BaseHandlerWrapper implements HandlerWrapper {



    private Bot bot;

    public BaseHandlerWrapper(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()){
            case TdApi.Error.CONSTRUCTOR:
                onError((TdApi.Error)object);
                break;
            case TdApi.Ok.CONSTRUCTOR:
                onOk((TdApi.Ok)object);
                break;
            default:
                log.error("Receive wrong response from TDLib:\t" + object);

        }
    }

    protected void onError(TdApi.Error error) {
        log.error(error.toString());
    }
    protected void onOk(TdApi.Ok object) {}

    @Override
    public void onHandle(TdApi.Object object) {
          handle(object);
    }
    protected abstract void  handle(TdApi.Object object);

    @Override
    public Bot getBot() {
        return bot;
    }
}
