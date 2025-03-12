package io.github.smagical.bot.tg.handler;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.HandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DispatchHandler implements HandlerWrapper {

    private ConcurrentHashMap<Integer,HandlerWrapper> handlers = new ConcurrentHashMap<>();
    private Bot bot;

    public DispatchHandler(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onResult(TdApi.Object object) {
        try {
            handlers.getOrDefault(object.getConstructor(), NoopHandler.getInstance()).onHandle(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void addHandler(HandlerWrapper handler) {
        if (handler == null) throw new NullPointerException("handler is null");
        for (int index : handler.support()) {
            handlers.merge(index, handler,(a,b)->{
                if (a.getOrder() > b.getOrder()) return b;
                return a;
            });
        }
    }

    public boolean removeHandler(HandlerWrapper handler) {
        if (handler == null) throw new NullPointerException("handler is null");
        for (int index : handler.support()) {
            handlers.remove(index, handler);
        }
        return true;
    }


    @Override
    public Bot getBot() {
        return this.bot;
    }

    @Override
    public int[] support() {
        return handlers.keySet().stream().mapToInt(i -> i).toArray();
    }
}
