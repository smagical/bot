package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.DispatchHandler;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.event.user.LoginEvent;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class AuthorizationStateDispatchHandler extends DispatchHandler {

    public AuthorizationStateDispatchHandler(Bot bot) {
        super(bot);
        addHandler(new WaitTdlibParametersHandler(bot));
        addHandler(new WaitPhoneNumberHandler(bot));
        addHandler(new WaitCodeHandler(bot));
        addHandler(new WaitEmailAddressHandler(bot));
        addHandler(new WaitEmailCodeHandler(bot));
        addHandler(new WaitOtherDeviceConfirmationHandler(bot));
        addHandler(new ReadyHandler(bot));
        addHandler(new LoggingOutHandler(bot));
        addHandler(new ClosingHandler(bot));
        addHandler(new ClosedHandler(bot));
        addHandler(new WaitPassword(bot));
        //AuthorizationStateWaitRegistration
        //AuthorizationStateWaitPassword未设置
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.UpdateAuthorizationState.CONSTRUCTOR};
    }

    @Override
    public void onResult(TdApi.Object object) {
        super.onResult(
                ((TdApi.UpdateAuthorizationState)object).authorizationState
        );
    }

    public static interface AuthorizationStateEvent<T> extends Event<T> {}
    public static class LoginSuccessEvent<T> extends io.github.smagical.bot.event.user.LoginEvent.LoginSuccessEvent<T> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<T>{

        LoginSuccessEvent(T data) {
            super(data);
        }
    }

    public static class LoginFailureEvent<T> extends LoginEvent.LoginFailureEvent<T> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<T>{

         LoginFailureEvent(T data) {
            super(data);
        }
    }

}
