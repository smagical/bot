package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.RetryBaseEvent;
import io.github.smagical.bot.tg.handler.base.RetryBaseHandlerAndListenerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.event.Event;
import org.drinkless.tdlib.TdApi;

public class WaitPassword extends RetryBaseHandlerAndListenerWrapper<WaitPassword.PasswordEvent> {

    public WaitPassword(Bot bot) {
        super(bot);
    }

    public class PasswordWaitEvent extends RetryBaseEvent<TdApi.AuthorizationStateWaitCode> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<TdApi.AuthorizationStateWaitCode> {
        private PasswordWaitEvent(TdApi.AuthorizationStateWaitCode code) {
            super(retryAlready, code);
        }
    }
    public static class PasswordEvent extends BaseEvent<String> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<String> {
        public PasswordEvent(String code) {
            super(code);
        }
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitPassword.CONSTRUCTOR};
    }

    @Override
    protected void error(TdApi.Error error) {
        super.error(error);
        getBot().send(
                new AuthorizationStateDispatchHandler.LoginFailureEvent(error)
        );
    }

    @Override
    protected void handle(TdApi.Object object) {
        getBot().send(
                new WaitPassword.PasswordWaitEvent((TdApi.AuthorizationStateWaitCode) object)
        );
    }

    @Override
    public boolean support(Event event) {
        return WaitCodeHandler.CodeEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(WaitPassword.PasswordEvent event) {
        TdApi.CheckAuthenticationCode code =
                new TdApi.CheckAuthenticationCode(
                        event.getData().getData()
                );
        getBot().getClient().send(code,this);
    }


}
