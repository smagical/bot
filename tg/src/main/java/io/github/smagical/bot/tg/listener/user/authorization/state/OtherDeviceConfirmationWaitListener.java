package io.github.smagical.bot.tg.listener.user.authorization.state;

import com.google.zxing.WriterException;
import io.github.smagical.bot.tg.handler.user.authorization.state.WaitOtherDeviceConfirmationHandler;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import io.github.smagical.bot.tg.util.QRUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OtherDeviceConfirmationWaitListener  implements Listener<WaitOtherDeviceConfirmationHandler.OtherDeviceConfirmationWaitEvent> {

    private MessageDispatch dispatch;

    public OtherDeviceConfirmationWaitListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public boolean support(Event event) {
        return WaitOtherDeviceConfirmationHandler.OtherDeviceConfirmationWaitEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(WaitOtherDeviceConfirmationHandler.OtherDeviceConfirmationWaitEvent confirmationWaitEvent) {
        try {
            QRUtil.printf(confirmationWaitEvent.getData().getData());
        } catch (WriterException e) {
            log.info(confirmationWaitEvent.getOriginalData());
        }
    }
}