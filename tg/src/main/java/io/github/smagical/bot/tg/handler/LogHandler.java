package io.github.smagical.bot.tg.handler;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;

@Slf4j
public class LogHandler implements Client.LogMessageHandler,Client.ExceptionHandler{
    private final static LogHandler handler = new LogHandler();

    @Override
    public void onLogMessage(int verbosityLevel, String message) {
        log.info("\n{}",message.toString());
    }

    public static LogHandler getInstance() {
        return handler;
    }

    @Override
    public void onException(Throwable e) {
        e.printStackTrace();
    }
}
