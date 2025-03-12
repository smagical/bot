package io.github.smagical.bot.tg.listener.user;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.GetBot;
import io.github.smagical.bot.tg.handler.user.update.UpdateUserDispatchHandler;
import io.github.smagical.bot.tg.handler.user.update.UserFullInfoHandler;
import io.github.smagical.bot.tg.handler.user.update.UserHandler;
import io.github.smagical.bot.tg.handler.user.update.UserStatusHandler;
import io.github.smagical.bot.tg.util.Format;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class UserListener<T,V> implements Listener<UpdateUserDispatchHandler.UpdateUserEvent>, GetBot {

    private Bot bot;
    public UserListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onListener(UpdateUserDispatchHandler.UpdateUserEvent event) {
        log.trace("UserListener received update user event:\n {}", event.getOriginalData());
        if (event instanceof UserFullInfoHandler.UpdateUserFullInfoEvenvt){
            onListner((UserFullInfoHandler.UpdateUserFullInfoEvenvt)event);
        }else if (event instanceof UserHandler.UpdateUserEvent){
            onListner((UserHandler.UpdateUserEvent)event);
        }else if (event instanceof UserStatusHandler.UserStatusEvent){
            onListner((UserStatusHandler.UserStatusEvent)event);
        }

    }

    private void onListner(UserFullInfoHandler.UpdateUserFullInfoEvenvt event) {
       getBot().addUserFullInfo(event.getData().getData().userId,
               event.getData().getData().userFullInfo);
    }

    private void onListner(UserHandler.UpdateUserEvent event) {
        getBot().addUser(event.getData().getData());
        log.debug("UserListener received update user event:\n {}", Format.format(event.getData().getData()));
    }
    private void onListner(UserStatusHandler.UserStatusEvent event) {
        TdApi.User user = getBot().getUser(event.getData().getData().userId);
        if (user != null) {
            synchronized (user){
                user.status = event.getData().getData().status;
            }
        }
    }



    @Override
    public Bot getBot() {
        return this.bot;
    }
}
