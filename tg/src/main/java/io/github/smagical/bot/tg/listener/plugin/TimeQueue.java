package io.github.smagical.bot.tg.listener.plugin;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class TimeQueue{
    private   int TIME = 5;
    private   int CODE_TIME_OUT = 5;
    private final Map<String,Long>[] codes;
    private int[] hours;

    public TimeQueue() {
        int size = 60/TIME;
        codes = new HashMap[size];
        hours = new int[size];
        Arrays.fill(hours, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        for (int i = 0; i < codes.length; i++) {
            codes[i] = new HashMap<>();
        }
    }


    public void addCode(String code,Long userId){
        synchronized (codes) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE);
            if (hour != hours[minute/ TIME]){
                codes[hour].clear();
                hours[minute/ TIME] = hour;
            }
            codes[minute/TIME].put(code,userId);
        }
    }

    public Long codeExists(String code){
        synchronized (codes) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            int minute = Calendar.getInstance().get(Calendar.MINUTE);
            int start = minute - CODE_TIME_OUT;
            int end = minute;
            for (int i = start; i <= end; i+=TIME) {
                int now =(i + 60)%60;
                if (codes[now/TIME].containsKey(code)){
                    return codes[now/TIME].remove(code);
                }
            }
            return null;
        }
    }
}