package io.github.smagical.bot.plugin.datasource;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;

import java.util.*;
import java.util.stream.Collectors;

public class DbShardingAlgorithm<T extends Comparable<?>> implements ComplexKeysShardingAlgorithm<T> {

    public final static String NAME = "dbShardingAlgorithm";

    @Override
    public void init(Properties props) {
        ComplexKeysShardingAlgorithm.super.init(props);
    }


    @Override
    public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<T> shardingValue) {
        if (availableTargetNames.isEmpty())
            throw new RuntimeException("not find any available target names");
        Map<String, Collection<T>> map = shardingValue.getColumnNameAndShardingValuesMap();
        if (!map.containsKey("chat_id") ){
            return availableTargetNames;
        }
        Long chatId = Long.valueOf(map.get("chat_id").stream().map(e->e.toString()).findFirst().get().toString());

        //Long id = Long.valueOf(map.get("id").stream().map(e->e.toString()).findFirst().get().toString());

        List<String> availableTargetNamesList = new ArrayList<>(availableTargetNames);
        StringBuilder result = new StringBuilder();
        while (true) {
            TreeMap<String,List<String>> hashMap = new TreeMap<>();

            for (String targetName : availableTargetNamesList) {
                if (targetName.isBlank()) continue;
                Integer index = targetName.indexOf("_");
                if (index != -1) {
                    String key = targetName.substring(0, index);
                    if (hashMap.containsKey(key)) {
                        hashMap.get(key).add(targetName.substring(index + 1));
                    }else {
                        List<String> list = new ArrayList<>();
                        list.add(targetName.substring(index + 1));
                        hashMap.put(key, list);
                    }
                }else {
                    if (hashMap.containsKey(targetName)) {
                        hashMap.get(targetName).add("");
                    }else {
                        List<String> list = new ArrayList<>();
                        list.add("");
                        hashMap.put(targetName, list);
                    }
                }
            }

            int size = hashMap.size();;
            int res = (int) (Math.abs(chatId) % size);
            ArrayList<String> keyList = new ArrayList<>(hashMap.keySet());
            result.append(keyList.get(res));
            result.append("_");
            availableTargetNamesList = hashMap.get(keyList.get(res))
                    .stream()
                    .filter(e->!e.isBlank())
                    .collect(Collectors.toList());
            if (availableTargetNamesList.isEmpty()) {
                result.deleteCharAt(result.length()-1);
                List<String> resList = new ArrayList<>();
                resList.add(result.toString());

                return  resList;
            }else if (availableTargetNamesList.size() == 1){
                result.append(availableTargetNamesList.get(0));
                List<String> resList = new ArrayList<>();
                resList.add(result.toString());
                return resList;
            }
        }

    }

    public static AlgorithmConfiguration getAlgorithmConfiguration() {
        Properties props = new Properties();
        props.setProperty("strategy", "COMPLEX");
        props.setProperty("algorithmClassName", DbShardingAlgorithm.class.getName());
        return new AlgorithmConfiguration("CLASS_BASED",props);
    }


}
