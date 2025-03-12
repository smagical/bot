package io.github.smagical.bot.plugin.handler.auth;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import org.redisson.api.RBucket;

import java.time.Duration;

public class RedisAuthHandler implements AuthenticationHandler{
    private SmagicalTgPlugin plugin;

    public RedisAuthHandler(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean authenticate( MessageInfo messageInfo) {
        String uuid = "tg_bot:auth:"+messageInfo.getChatId()+":"+ messageInfo.getMessageId();
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
}
