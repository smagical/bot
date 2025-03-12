package io.github.smagical.bot.tg.listener.user.authorization.state;

import io.github.smagical.bot.tg.handler.user.authorization.state.AuthorizationStateDispatchHandler;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;

import java.util.ArrayList;
import java.util.List;

public class AuthorizationStateListener implements Listener<AuthorizationStateDispatchHandler.AuthorizationStateEvent> {
    private MessageDispatch dispatch;

    private static List<Listener> listeners = new ArrayList<>();

    public AuthorizationStateListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
        listeners.add(new CodeWaitListener(dispatch));
        listeners.add(new EmailAddressWaitListener(dispatch));
        listeners.add(new EmailCodeWaitListener(dispatch));
        listeners.add(new LoginListener(dispatch));
        listeners.add(new LogoutListener(dispatch));
        listeners.add(new OtherDeviceConfirmationWaitListener(dispatch));
    }

    @Override
    public boolean support(Event event) {
        return AuthorizationStateDispatchHandler.AuthorizationStateEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(AuthorizationStateDispatchHandler.AuthorizationStateEvent event) {
        for (Listener listener : listeners) {
            if (listener.support(event)) {
                listener.onListener(event);
                if (!listener.next()) return;
            }
        }
    }
}
