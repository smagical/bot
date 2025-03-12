package io.github.smagical.bot.tg.listener.user.authorization.state;

import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.event.user.LoginEvent;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginListener implements Listener<LoginEvent> {

    private MessageDispatch dispatch;

    public LoginListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public boolean support(Event event) {
        return LoginEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(LoginEvent loginEvent) {
       if (loginEvent instanceof LoginEvent.LoginSuccessEvent) {
           log.info("Login success");
       }else {
           log.info("Login failed {}", ((LoginEvent.LoginFailureEvent)loginEvent).getOriginalData());
       }
    }
}