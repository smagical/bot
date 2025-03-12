package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.RetryBaseEvent;
import io.github.smagical.bot.tg.handler.base.RetryBaseHandlerAndListenerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.event.Event;
import org.drinkless.tdlib.TdApi;

public class WaitEmailCodeHandler extends RetryBaseHandlerAndListenerWrapper<WaitEmailCodeHandler.EmailCodeEvent> {


    public WaitEmailCodeHandler(Bot bot) {
        super(bot);
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
                 new EmailCodeWaitEvent((TdApi.AuthorizationStateWaitEmailCode) object)
         );

    }




    @Override
    public boolean support(Event event) {
        return EmailCodeEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(EmailCodeEvent event) {
        TdApi.CheckAuthenticationEmailCode code =
                new TdApi.CheckAuthenticationEmailCode(
                        new TdApi.EmailAddressAuthenticationCode(event.getData().getData())
                );
        getBot().getClient().send(code,this);
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitEmailCode.CONSTRUCTOR};
    }


   public class EmailCodeWaitEvent extends RetryBaseEvent<TdApi.AuthorizationStateWaitEmailCode> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<TdApi.AuthorizationStateWaitEmailCode> {
        private EmailCodeWaitEvent(TdApi.AuthorizationStateWaitEmailCode code) {
            super(retryAlready,code);
        }
    }

    public static class EmailCodeEvent extends BaseEvent<String> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<String> {
        public EmailCodeEvent(String code) {
            super(code);
        }
    }

}
