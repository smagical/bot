package io.github.smagical.bot.tg.listener.plugin;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.user.authorization.state.AuthorizationStateDispatchHandler;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PluginInitListener  implements Listener<AuthorizationStateDispatchHandler.LoginSuccessEvent> {

    private Bot bot;
    private PluginDispatch dispatch;

    public PluginInitListener(Bot bot,PluginDispatch dispatch) {
        this.bot = bot;
        this.dispatch = dispatch;
    }

    @Override
    public void onListener(AuthorizationStateDispatchHandler.LoginSuccessEvent event) {
        bot.removeListener(this);
        log.info("Plugin has been initialized");
        dispatch.initPlugin();
        bot.addListener(dispatch);
        log.info("Plugin has been initialized successfully");
    }
}
