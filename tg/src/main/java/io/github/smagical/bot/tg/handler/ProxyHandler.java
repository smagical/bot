package io.github.smagical.bot.tg.handler;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.HandlerWrapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProxyHandler implements HandlerWrapper {
    private String host ="127.0.0.1";
    private int port = 7890;
    private boolean enable = true;
    private TdApi.ProxyType proxyType = new TdApi.ProxyTypeHttp();



    public TdApi.AddProxy  getProxy() {
        TdApi.AddProxy proxy = new TdApi.AddProxy();
        proxy.enable = enable;
        proxy.port = port;
        proxy.type = proxyType;
        proxy.server = host;
        return proxy;
    }




    @Override
    public void onResult(TdApi.Object object) {
        log.info(object.toString());
    }

    @Override
    public int[] support() {
        return new int[]{0};
    }

    @Override
    public Bot getBot() {
        return null;
    }
}
