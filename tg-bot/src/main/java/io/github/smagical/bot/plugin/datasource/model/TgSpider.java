package io.github.smagical.bot.plugin.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class TgSpider {
    private Long chatId;
    private String chatName;
    private Long lastSpiderId;


    public static List<TgSpider> getTgSpider(ResultSet resultSet) throws SQLException {
        List<TgSpider> tgTgSpider = new ArrayList<>();
        while (resultSet.next()){
            TgSpider spider =  new TgSpider();
            spider.setLastSpiderId(resultSet.getLong("last_spider_id"));
            spider.setChatName(resultSet.getString("chat_name"));
            spider.setChatId(resultSet.getLong("chat_id"));
            tgTgSpider.add(spider);
        }
        return tgTgSpider;
    }

    public static TgSpider getTgSpiderOne(ResultSet resultSet) throws SQLException {

        if (resultSet.next()){
            TgSpider spider =  new TgSpider();
            spider.setLastSpiderId(resultSet.getLong("last_spider_id"));
            spider.setChatName(resultSet.getString("chat_name"));
            spider.setChatId(resultSet.getLong("chat_id"));
            return spider;
        }
        return  null;
    }
}
