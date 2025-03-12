package io.github.smagical.bot.tg.handler;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.HandlerWrapper;
import org.drinkless.tdlib.TdApi;

public class UpdateOptionHandler implements HandlerWrapper {
    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateOption.CONSTRUCTOR};
    }


    @Override
    public Bot getBot() {
        return null;
    }

    @Override
    public void onResult(TdApi.Object object) {

    }
}
