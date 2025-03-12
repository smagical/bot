package io.github.smagical.bot.tg.handler.base;

import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.event.EventData;

public  class RetryBaseEvent<T> extends BaseEvent<T> {
    private final int retry ;

    public RetryBaseEvent(int retry, EventData<T> code) {
        super(code);
        this.retry = retry;

    }
    public RetryBaseEvent(int retry, T code) {
        super(code);
        this.retry = retry;

    }

    public int getRetry() {
        return retry;
    }
}
