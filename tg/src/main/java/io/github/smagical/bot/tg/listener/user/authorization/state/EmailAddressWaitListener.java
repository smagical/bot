package io.github.smagical.bot.tg.listener.user.authorization.state;

import io.github.smagical.bot.tg.handler.user.authorization.state.WaitEmailAddressHandler;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

import static io.github.smagical.bot.tg.util.Utils.promptString;


@Slf4j
public class EmailAddressWaitListener implements Listener< WaitEmailAddressHandler.EmailAddressWaitEvent> {

    private MessageDispatch dispatch;

    public EmailAddressWaitListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public boolean support(Event event) {
        return  WaitEmailAddressHandler.EmailAddressWaitEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener( WaitEmailAddressHandler.EmailAddressWaitEvent codeWaitEvent) {
        dispatch.send(new WaitEmailAddressHandler.EmailAddressEvent(promptString("请输入邮箱地址:")));
    }
}
