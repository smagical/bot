package io.github.smagical.bot.tg.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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



}
