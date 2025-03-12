package io.github.smagical.bot.tg.listener.user.authorization.state;

import io.github.smagical.bot.tg.handler.user.authorization.state.WaitCodeHandler;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

import static io.github.smagical.bot.tg.util.Utils.promptString;

@Slf4j
public class CodeWaitListener implements Listener<WaitCodeHandler.CodeWaitEvent> {

    private MessageDispatch dispatch;

    public CodeWaitListener(MessageDispatch dispatch) {
        this.dispatch = dispatch;
    }

    @Override
    public boolean support(Event event) {
        return WaitCodeHandler.CodeWaitEvent.class.isAssignableFrom(event.getClass());
    }

    @Override
    public void onListener(WaitCodeHandler.CodeWaitEvent codeWaitEvent) {
//        if (TgInfo.getPropertyBool(TgInfo.USE_TEST)){
//            dispatch.send(new WaitCodeHandler.CodeEvent("22222"));
//            return;
//        }
        String msg = null;
        int retry = codeWaitEvent.getRetry();
        if (retry > 1) {
            msg = String.format("输入错误 请再次输入 %s 的验证码 :",codeWaitEvent.getData().getData().codeInfo.phoneNumber);
        }else {
            msg = String.format("请输入 %s 的验证码 :",codeWaitEvent.getData().getData().codeInfo.phoneNumber);
        }
        dispatch.send(new WaitCodeHandler.CodeEvent(promptString(msg)));
    }
}
