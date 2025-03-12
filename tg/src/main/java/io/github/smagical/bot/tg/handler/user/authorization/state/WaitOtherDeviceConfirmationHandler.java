package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import org.drinkless.tdlib.TdApi;

public class WaitOtherDeviceConfirmationHandler
        extends BaseHandlerWrapper {

    public WaitOtherDeviceConfirmationHandler(Bot bot) {
        super(bot);
    }

    @Override
    protected void handle(TdApi.Object object) {
        TdApi.AuthorizationStateWaitOtherDeviceConfirmation oc =
                (TdApi.AuthorizationStateWaitOtherDeviceConfirmation) object;
        String link = oc.link;
        getBot().send(
                new OtherDeviceConfirmationWaitEvent(link)
        );
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR};
    }

    public  class OtherDeviceConfirmationWaitEvent extends BaseEvent<String> implements AuthorizationStateDispatchHandler.AuthorizationStateEvent<String>{
        private OtherDeviceConfirmationWaitEvent(String code) {
            super(code);
        }
    }


}
