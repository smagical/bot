package io.github.smagical.bot.tg;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class BotConfiguration {

    private int requestRetry;
    //0 1 2
    private int loginType;
    private String loginToken;
    private String loginNumber;

    private TdlibConfiguration tdlib;
    private Map<String,Object> properties;

    public <T> T getOrDefault(String key, T def){
        if(properties.containsKey(key)){
            return (T)properties.get(key);
        }
        return def;
    }
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    public void  setProperty(String key, Object value) {
        properties.put(key, value);
    }

    public boolean exitsProperty(String key) {
        return properties.containsKey(key);
    }
    public void removeProperty(String key) {
        properties.remove(key);
    }

    public void loadProperties(Map<String,Object> properties) {
        properties.putAll(properties);
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Slf4j
    public static class TdlibConfiguration{
        private Integer apiId;
        private String apiHash;
        private String title;
        private String shortName;
        private String applicationVersion;
        private String languageCode;
        private boolean useTest;
        private String databaseEncryptionKey;
    }


}
