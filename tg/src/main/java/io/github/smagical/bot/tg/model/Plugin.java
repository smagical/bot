package io.github.smagical.bot.tg.model;


import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.tg.Bot;

public interface Plugin {
     void onHandle(Event event);
     void init(Bot bot);
     void destroy();
}
