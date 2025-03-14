package io.github.smagical.bot.bus;

import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class MessageDispatch {

    private Object object = new Object();
    private final  HashSet< Listener> set = new HashSet<>();
    private volatile List<Listener> listeners = new ArrayList<>();


    public void send(Event event) {
        for (Listener listener : listeners) {
            if (listener.support(event)) {
                try {
                    listener.onListener(event);
                }catch (Exception e){
                    e.printStackTrace();
                }
                if(!listener.next()) break;
            }
        }
    }

    public void addListener(Listener listener) {
       synchronized (set) {
           set.add(listener);
           List<Listener> list = set.stream().sorted((a,b)->Integer.compare(a.getOrder(),b.getOrder())).toList();
           this.listeners = list;
       }

    }

    public void removeListener(Listener listener) {
        synchronized (set) {
            set.remove(listener);
            List<Listener> list = set.stream().sorted((a,b)->Integer.compare(a.getOrder(),b.getOrder())).toList();
            this.listeners = list;
        }
    }

    public void removeAllListeners() {
        synchronized (set){
            set.clear();
            listeners = new ArrayList<>();
        }
    }
}
