package io.github.smagical.bot.tg;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BotConfig {
    private String dbBase = "tddb";
    private AtomicInteger id = new AtomicInteger(1);
    private HashMap<String,String> idAndPath = new HashMap<>();
    private volatile static boolean flag = false;

    private final static long WAITE_TIME = 5 *60 * 1000l;
    private  Thread thread = null;
    private  AtomicLong counter = new AtomicLong(0);


    private BotConfig() {
        if (flag) throw new RuntimeException("Can't instantiate BotConfig");
        flag = true;
    }

    private final static BotConfig INSTANCE = new BotConfig();
    public static BotConfig getInstance() {
        return INSTANCE;
    }

    static {

        //TODO 处理多用户加载

    }

    public String getDbBase(String botId) {
        return dbBase +"/"+botId;
    }
    public void setId(int id) {
        this.id.set(id);
    }
    public void setDbBase(String dbBase) {
        this.dbBase = dbBase;
    }
    public int getId() {
        //TODO 处理多用户加载
        return 0;
    }

    public final  void initThreadExecutor(){
        if (thread == null){
            synchronized (this){
                if (thread == null){
                    thread = new ThreadExecutor();
                    thread.start();
                }
            }
        }
        counter.incrementAndGet();
    }

    public final  void stopThreadExecutor(){
        counter.decrementAndGet();
    }

    private  class ThreadExecutor extends Thread {

        private Random random = new Random();
        private final long TIME = 20;
        private final long WAITE_TIME = BotConfig.WAITE_TIME/TIME;



        public ThreadExecutor() {

        }

        @Override
        public void run() {
            while (counter.getAcquire() > 0) {
                while (counter.getAcquire()>1) {
                    try {
                        sleep(random.nextInt(100,6000_0));
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
                for (int i = 0;i < TIME;i++) {
                    try {
                        if (counter.getAcquire() > 0) break;
                        sleep(WAITE_TIME);
                    } catch (InterruptedException e) {
                        interrupt();
                    }
                }
            }
            synchronized (BotConfig.this){
                thread = null;
            }
        }
    }

}
