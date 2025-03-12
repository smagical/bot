package io.github.smagical.bot.tg.listener.user.authorization.state;

import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.event.user.LogoutEvent;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogoutListener implements Listener<LogoutEvent> {

    private MessageDispatch dispatch;

    public LogoutListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public boolean support(Event event) {
        return LogoutEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(LogoutEvent logoutEvent) {
       log.info("Logged out");
    }
}