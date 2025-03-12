package io.github.smagical.bot.plugin.datasource;

import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class DbShardingAlgorithm implements ComplexKeysShardingAlgorithm<Long> {

    @Override
    public void init(Properties props) {
        ComplexKeysShardingAlgorithm.super.init(props);
    }


    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<Long> shardingValue) {
        Map<String, Collection<Long>> map = shardingValue.getColumnNameAndShardingValuesMap();
        Long chatId = map.get("chat_id").stream().findFirst().get();
        Long id = map.get("id").stream().findFirst().get();
        int size = availableTargetNames.size();
        int res = (int) (chatId % size);
        for (String availableTargetName : availableTargetNames) {
            if (res == 0) return Collections.singleton(availableTargetName);
            res--;
        }
        throw new RuntimeException("not find any available target names");
    }
}
