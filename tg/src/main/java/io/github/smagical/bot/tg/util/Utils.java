package io.github.smagical.bot.tg.util;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;

@Slf4j
public class Utils {

    public final static String promptString(String prompt) {
        if (prompt != null)
            log.info(prompt);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String code  = reader.readLine();
            return code;
        } catch (IOException e) {
            return null;
        }
    }

    @FunctionalInterface
    public static interface CatchSQLAndNumber{
        public void  run() throws Exception;
    }
    public static void withSQLAndNumCatch(CatchSQLAndNumber runnable, Client client, Long chatId, String debug){
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
    public static interface CatchSQLAndNumberReturn<T>{
        public T run() throws Exception;
    }

    public static  <T> T withSQLAndNumCatchReturn(CatchSQLAndNumberReturn<T> runnable, Client client, Long chatId, String debug){
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


    @FunctionalInterface
    public static interface ExceptionRunAble{
        public void run() throws Exception;
    }
    public static void withException(ExceptionRunAble runnable){
        try {
            runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
