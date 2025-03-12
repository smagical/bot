package io.github.smagical.bot.tg;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public interface HandlerWrapper extends Client.ResultHandler ,GetBot{
     default int[] support(){
          return new int[0];
     }
     default void onHandle(TdApi.Object object) {
          onResult(object);
     }

     default int getOrder(){
          return Integer.MAX_VALUE;
     }
}
