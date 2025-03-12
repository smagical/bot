package io.github.smagical.bot.plugin.handler;

import io.github.smagical.bot.tg.util.ClientUtils;
import org.drinkless.tdlib.Client;

import java.sql.SQLException;


public interface PluginHandler {

    @FunctionalInterface
    static interface CatchSQLAndNumber{
        public void  run() throws Exception;
    }
    default void withSQLAndNumCatch(CatchSQLAndNumber runnable, Client client, Long chatId, String debug){
        try {
            runnable.run();
        }catch (NumberFormatException e){
            ClientUtils.sendTextMessage(
                    client,
                    chatId,
                    String.format("[%s] args not number",debug)
            );
            e.printStackTrace();
        } catch (SQLException e) {
            ClientUtils.sendTextMessage(
                    client,
                    chatId,
                    String.format("[%s] system error by sql",debug)
            );
            e.printStackTrace();
        }catch (Exception e){
            ClientUtils.sendTextMessage(
                    client,
                    chatId,
                    String.format("[%s] system error",debug)
            );
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    static interface CatchSQLAndNumberFuture<T>{
        public T run() throws Exception;
    }

    default <T> T withSQLAndNumCatchFuture(CatchSQLAndNumberFuture<T> runnable, Client client, Long chatId, String debug){
        try {
            return runnable.run();
        }catch (NumberFormatException e){
            ClientUtils.sendTextMessage(
                    client,
                    chatId,
                    String.format("[%s] args not number",debug)
            );
            e.printStackTrace();
        } catch (SQLException e) {
            ClientUtils.sendTextMessage(
                    client,
                    chatId,
                    String.format("[%s] system error by sql",debug)
            );
            e.printStackTrace();
        }catch (Exception e){
            ClientUtils.sendTextMessage(
                    client,
                    chatId,
                    String.format("[%s] system error",debug)
            );
            e.printStackTrace();
        }
        return  null;
    }
}
