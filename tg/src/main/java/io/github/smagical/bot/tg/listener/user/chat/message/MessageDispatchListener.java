package io.github.smagical.bot.tg.listener.user.chat.message;

import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;

import java.util.ArrayList;
import java.util.List;

public class MessageDispatchListener implements Listener {
    private List<Listener> listeners;
    private MessageDispatch dispatch;

    public MessageDispatchListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
       listeners = new ArrayList<Listener>();
       listeners.add(new MessageListener());
    }

    @Override
    public void onListener(Event event) {
        for (Listener listener : listeners) {
            if (listener.support(event)) {
                listener.onListener(event);
                if (!listener.next()) {
                    return;
                }
            }
        }
    }

    @Override
    public boolean support(Event event) {
        return listeners.stream().anyMatch(listener -> listener.support(event));
    }

}
