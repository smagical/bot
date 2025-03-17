package io.github.smagical.bot.plugin.handler.auth;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.tg.Bot;
import org.redisson.api.RBucket;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class RedisAuthHandler implements AuthenticationHandler{

    private SmagicalTgPlugin plugin;
    private Set<String> commandWhitelist;


    public RedisAuthHandler(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
        commandWhitelist = new HashSet<>();
    }

    @Override
    public boolean authenticate( MessageInfo messageInfo) {
        if (messageInfo.isCommand()){
            if (commandWhitelist.contains(messageInfo.getMessageText().split("\\s+")[0])){
                return true;
            }
        }
        String type = plugin.getBot().getLoginType() == Bot.LoginType.BOT?"bot":"user";
        String uuid = "tg_bot:auth:"+type+":"+messageInfo.getChatId()+":"+ messageInfo.getMessageId();
        RBucket<String> rBuckets =  plugin.getRedissonClient().getBucket(uuid);
         if (rBuckets.setIfAbsent(plugin.getConfiguration().getInstanceId(), Duration.ofSeconds(30))){
             return  true;
         };
         if (uuid.equals(rBuckets.get())){
             rBuckets.expire(Duration.ofSeconds(30));
             return true;
         }
         return  false;
    }

    public void addAllRun(String command){
        commandWhitelist.add(command);
    }

    public void  removeAllRun(String command){
        commandWhitelist.remove(command);
    }

}
