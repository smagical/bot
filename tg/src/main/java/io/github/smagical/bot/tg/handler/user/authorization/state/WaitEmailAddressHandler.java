package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.RetryBaseEvent;
import io.github.smagical.bot.tg.handler.base.RetryBaseHandlerAndListenerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.event.Event;
import org.drinkless.tdlib.TdApi;

public class WaitEmailAddressHandler extends RetryBaseHandlerAndListenerWrapper<WaitEmailAddressHandler.EmailAddressEvent> {

    public WaitEmailAddressHandler(Bot bot) {
        super(bot);
    }

    @Override
    public boolean support(Event event) {
        return EmailAddressEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(EmailAddressEvent event) {
        getBot().getClient().send(
                new TdApi.SetAuthenticationEmailAddress(event.getData().getData()),
                this
        );
    }

    @Override
    protected void error(TdApi.Error error) {
        super.error(error);
        getBot().send(
                new AuthorizationStateDispatchHandler.LoginFailureEvent(error)
        );
    }

    public class EmailAddressWaitEvent extends RetryBaseEvent<TdApi.AuthorizationStateWaitEmailAddress> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<TdApi.AuthorizationStateWaitEmailAddress> {
        private EmailAddressWaitEvent(TdApi.AuthorizationStateWaitEmailAddress code) {
            super(retryAlready, code);
        }
    }
    public static class EmailAddressEvent extends BaseEvent<String> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<String> {

        public EmailAddressEvent(String code) {
            super(code);
        }
    }


    @Override
    protected void handle(TdApi.Object object) {
        getBot().send(
                new EmailAddressWaitEvent(
                        (TdApi.AuthorizationStateWaitEmailAddress)object
                )
        );
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitEmailAddress.CONSTRUCTOR};
    }
}
