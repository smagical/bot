package io.github.smagical.bot.plugin.datasource;

import com.zaxxer.hikari.HikariDataSource;
import io.github.smagical.bot.TgConfiguration;
import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ComplexShardingStrategyConfiguration;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

public class ShardingSphereConfig {
    private static final String TG_MESSAGE_TABLE = "tg_messages";
    private static final List<String> TG_OTHER_TABLES = List.of(
            "tg_group","tg_bot_info","tg_spider"
    );

    public static DataSource getDataSource(TgConfiguration configuration) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, SQLException {
        Map<String,DataSource> dataSourceMap = getDataSourceMap(configuration.getDataSources());
        if (dataSourceMap.size() == 1){
            return dataSourceMap.values().iterator().next();
        }
        Collection<RuleConfiguration> ruleConfigs = List.of(
                createBroadcastRuleConfiguration(),createShardingRuleConfiguration(dataSourceMap)
        );
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString());
        props.setProperty("max-connections-size-per-query", "2");
        props.setProperty("sql-simple", "true");
        return ShardingSphereDataSourceFactory.createDataSource(null, (ModeConfiguration)null, dataSourceMap, ruleConfigs, props);
         
    }

    private static ShardingRuleConfiguration createShardingRuleConfiguration(final Map<String, DataSource> dataSourceMap){
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();

        addShardingAlgorithms(shardingRuleConfiguration);

        shardingRuleConfiguration.getTables()
                .add(createTgMessageTableRuleConfiguration(dataSourceMap));

        return shardingRuleConfiguration;
    }

    private static BroadcastRuleConfiguration  createBroadcastRuleConfiguration(){
        BroadcastRuleConfiguration configuration = new BroadcastRuleConfiguration(
                TG_OTHER_TABLES
        );
        return configuration;
    }

    private static ShardingTableRuleConfiguration createTgMessageTableRuleConfiguration(final Map<String, DataSource> dataSourceMap){
        ShardingTableRuleConfiguration shardingTableRuleConfiguration =
                new ShardingTableRuleConfiguration(TG_MESSAGE_TABLE,
                        dataSourceMap.keySet().stream().map(e->e+"."+TG_MESSAGE_TABLE)
                                .reduce((a,b)->a+","+b).orElseGet(()->TG_MESSAGE_TABLE)
                );
        shardingTableRuleConfiguration.setDatabaseShardingStrategy(
                new ComplexShardingStrategyConfiguration("id,chat_id",DbShardingAlgorithm.NAME)
        );
        return  shardingTableRuleConfiguration;
    }


    private static void addShardingAlgorithms(ShardingRuleConfiguration shardingRuleConfiguration){
        shardingRuleConfiguration.getShardingAlgorithms()
                .put(DbShardingAlgorithm.NAME,DbShardingAlgorithm.getAlgorithmConfiguration());

    }

    private static Map<String, DataSource>  getDataSourceMap(Map<String, HashMap<String,Object>> config) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Map<String, DataSource> dataSourceMap = new HashMap<String, DataSource>();
        for (Map.Entry<String, HashMap<String, Object>> entry : config.entrySet()) {
            HikariDataSource  dataSource = new HikariDataSource();
            setFeild(entry.getValue(),dataSource);
            dataSourceMap.put(entry.getKey(), dataSource);
        }
        return dataSourceMap;
    }

    private static void setFeild(Map<String,Object> map, HikariDataSource value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            String methodName =  "set"+key.substring(0,1).toUpperCase()+key.substring(1);
           try {
               Method method = value.getClass().getMethod(methodName,entry.getValue().getClass());
               method.invoke(value,entry.getValue());
           }catch (NoSuchMethodException e){

               Map<Class,Class>  classClassMap = Map.of(
                       Integer.class,int.class,Long.class,long.class, Short.class,short.class, Byte.class, byte.class, Double.class, double.class,  Float.class, float.class,Character.class,char.class
               );
                boolean flag = false;
               for (Map.Entry<Class, Class> classEntry : classClassMap.entrySet()) {
                       if (classEntry.getKey() == entry.getValue().getClass()){
                           Method method = value.getClass().getMethod(methodName,classEntry.getValue());
                           method.invoke(value,entry.getValue());
                           flag = true;
                           break;
                       }else if (classEntry.getValue() == entry.getValue().getClass()){
                           Method method = value.getClass().getMethod(methodName,classEntry.getKey());
                           method.invoke(value,entry.getValue());
                           flag = true;
                            break;
                       }

               }

                if (!flag){
                    throw e;
                }


           }


        }
    }

}
