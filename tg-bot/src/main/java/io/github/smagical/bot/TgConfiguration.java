package io.github.smagical.bot;


import io.github.smagical.bot.plugin.datasource.model.TgBotInfo;
import io.github.smagical.bot.plugin.util.DbUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Slf4j
public class TgConfiguration {


    private HashMap<String,HashMap<String,Object>>  dataSources = HashMap.newHashMap(0);
    private TgRedisConfiguration redis;
    private String instanceId = UUID.randomUUID().toString();
    private Set<Long> adminIds = new HashSet<>();
    private Map<String,Object> properties = new HashMap<>();
    private boolean onlyAdmin = false;
    private String model = "singleton";
    private  final static Object lock = new Object();

    private ConcurrentHashMap<String, ConcurrentHashMap<Consumer<Object>,Object>> updateConsumers = new ConcurrentHashMap<>();

    public TgConfiguration() {
        addUpdateListener("adminIds",str->
                {
                    if (str == null) adminIds.clear();
                    else if (str instanceof String) {
                        this.adminIds = Arrays.stream(str.toString().split(","))
                                .filter(e->!e.strip().isBlank())
                                .map(Long::parseLong)
                                .collect(Collectors.toSet());
                    }else if (str instanceof List) {
                        Set<Long> admin = new HashSet<>();
                        List list = (List)str;
                        for (Object o : list) {
                            if (o instanceof Long) {
                                admin.add((Long)o);
                            }else {
                                admin.add(Long.parseLong( o.toString()));
                            }
                        }
                        adminIds = admin;
                    }
                }
        );
        addUpdateListener("onlyAdmin",str->{
            if (str instanceof String) {
                this.onlyAdmin = Boolean.parseBoolean(str.toString());
            }else if (str instanceof Boolean) {
                this.onlyAdmin = (Boolean)str;
            }
        });
    }

    public synchronized void addUpdateListener(String key, Consumer<Object> consumer) {
        key = key.strip().toUpperCase();
       if (updateConsumers.containsKey(key)) {
           updateConsumers.get(key).put(consumer,lock);
       }else {
           updateConsumers.put(key, new ConcurrentHashMap<>(){{put(consumer,lock);}});
       }
    }
    public synchronized void  removeUpdateListener(String key, Consumer<Object> consumer) {
        key = key.strip().toUpperCase();
        if (updateConsumers.containsKey(key)) {
            updateConsumers.get(key).remove(consumer);
        }
    }
    //另外加载

    public <T> T getOrDefault(String key, T def){
        key = key.strip().toUpperCase();
        if(properties.containsKey(key)){
            return (T)properties.get(key);
        }
        return def;
    }

    public <T> T getProperty(String key) {
        key = key.strip().toUpperCase();
        return (T) properties.get(key);
    }

    public void  setProperty(String key, Object value) {
        key = key.strip().toUpperCase();
        properties.put(key, value);
        if (updateConsumers.containsKey(key)) {
            updateConsumers.get(key).keySet().forEach(consumer -> consumer.accept(value));
        }
    }

    public boolean exitsProperty(String key) {
        key = key.strip().toUpperCase();
        return properties.containsKey(key);
    }
    public void removeProperty(String key) {
        key = key.strip().toUpperCase();
        properties.remove(key);
        if (updateConsumers.containsKey(key)) {
            updateConsumers.get(key).keySet().forEach(consumer -> consumer.accept(null));
        }
    }

    public void loadProperties(Map<String,String> properties) {
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    public void init(){

        HashMap<String,Object>  map = new HashMap<>();
        map.putAll(properties);
        this.properties.clear();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            setProperty(entry.getKey().strip().toUpperCase(), entry.getValue());
        }
    }

    public void loadFromDatabase(DataSource dataSource) {
        try {
            for (TgBotInfo info : DbUtil.TgBotInfoDb.selectTgBotInfoAll(dataSource)) {
                setProperty(info.getAttrName(), info.getAttrValue());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Long> getAdminIds() {
        return Collections.unmodifiableSet(adminIds);
    }

    public boolean isOnlyAdmin() {
        return onlyAdmin;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Slf4j
    public static class TgRedisConfiguration {
       private String host;
       private int port;
       private String username;
       private String password;
       private int db = 0;
    }


}
