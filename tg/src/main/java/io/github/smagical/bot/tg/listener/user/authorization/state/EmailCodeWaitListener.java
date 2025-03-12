package io.github.smagical.bot.tg.listener.user.authorization.state;

import io.github.smagical.bot.tg.handler.user.authorization.state.WaitEmailCodeHandler;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

import static io.github.smagical.bot.tg.util.Utils.promptString;

@Slf4j
public class EmailCodeWaitListener implements Listener<WaitEmailCodeHandler.EmailCodeWaitEvent> {

    private MessageDispatch dispatch;

    public EmailCodeWaitListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public boolean support(Event event) {
        return WaitEmailCodeHandler.EmailCodeWaitEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(WaitEmailCodeHandler.EmailCodeWaitEvent emailCodeWaitEvent) {
        dispatch.send(new WaitEmailCodeHandler.EmailCodeEvent(promptString("请输入邮箱验证码:")));
    }
}

