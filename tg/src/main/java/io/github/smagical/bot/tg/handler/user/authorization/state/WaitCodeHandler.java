package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.RetryBaseEvent;
import io.github.smagical.bot.tg.handler.base.RetryBaseHandlerAndListenerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.event.EventData;
import org.drinkless.tdlib.TdApi;

public class WaitCodeHandler extends RetryBaseHandlerAndListenerWrapper<WaitCodeHandler.CodeEvent> {

    public WaitCodeHandler(Bot bot) {
        super(bot);
        retryCount =0;
    }

    public class CodeWaitEvent extends RetryBaseEvent<TdApi.AuthorizationStateWaitCode> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<TdApi.AuthorizationStateWaitCode> {
        private CodeWaitEvent(TdApi.AuthorizationStateWaitCode code) {
            super(retryAlready, code);
        }
    }
    public static class CodeEvent extends BaseEvent<String> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<String> {
        private EventData<String> code;
        public CodeEvent(String code) {
            super(code);
        }
    }


    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitCode.CONSTRUCTOR};
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
                new CodeWaitEvent((TdApi.AuthorizationStateWaitCode) object)
        );
    }

    @Override
    public boolean support(Event event) {
        return CodeEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(CodeEvent event) {
        TdApi.CheckAuthenticationCode code =
                new TdApi.CheckAuthenticationCode(
                        event.getData().getData()
                );
        getBot().getClient().send(code,this);
    }


}
