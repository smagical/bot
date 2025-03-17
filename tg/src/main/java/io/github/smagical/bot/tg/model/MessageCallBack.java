package io.github.smagical.bot.tg.model;

import org.drinkless.tdlib.TdApi;

public  interface MessageCallBack<T>{
    public void accept(T message);
    public void error(TdApi.Error error);
}