package io.github.smagical.bot.plugin.datasource.cache;

import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.function.Supplier;

public abstract class CacheManger {
    public interface Cache<K,V>  {
        public void  put(K key, V value);
        public V get(K key);
        public void remove(K key);
        public void  clear();
    }

    public static class CaffeineCache<K,V> implements Cache<K,V> {
        private com.github.benmanes.caffeine.cache.Cache<K,V> cache;

        public CaffeineCache(com.github.benmanes.caffeine.cache.Cache<K,V> cache) {
            this.cache = cache;
        }

        @Override
        public void put(K key, V value) {
            this.put(key, value);
        }

        @Override
        public V get(K key) {
            return this.cache.getIfPresent(key);
        }

        @Override
        public void remove(K key) {
            this.cache.invalidate(key);
        }

        @Override
        public void clear() {
            this.cache.invalidateAll();
        }


    }

    public static class RedisCache<V> implements Cache<String,V> {
        private RedissonClient redisClient;
        private final String prefix;
        private final Duration duration ;

        public RedisCache(RedissonClient redisClient, String prefix) {
            this.redisClient = redisClient;
            this.prefix = prefix;
            this.duration = Duration.ofHours(1);
        }

        public RedisCache(RedissonClient redisClient, String prefix, Duration duration) {
            this.redisClient = redisClient;
            this.prefix = prefix;
            this.duration = duration;
        }

        @Override
        public void put(String key, V value) {
            this.redisClient.<V>getBucket(prefix+key).set(value);
        }


        @Override
        public V get(String key) {
            return this.redisClient.<V>getBucket(prefix+key).getAndExpire(duration);
        }

        @Override
        public void remove(String key) {
            this.redisClient.getBucket(prefix+key).delete();
        }

        @Override
        public void clear() {
            this.redisClient.getKeys().deleteByPattern(prefix+"*");
        }
    }



    private static final HashMap<String,Cache> caches = new HashMap<>();
    public static synchronized   <K,V> Cache<K,V> getCache(String key){
        return caches.get(key);
    }
    public static synchronized   <K,V> Cache<K,V> getCacheOrDefault(String key, Supplier<Cache<K,V>>  supplier){
        Cache<K,V> cache = getCache(key);
        if ( cache == null){
            cache = supplier.get();
            putCache(key,cache);
        }
        return  cache;
    }

    public static synchronized  <K,V> void putCache(String key,Cache<K,V> value){
         caches.put(key,value);
    }
}
