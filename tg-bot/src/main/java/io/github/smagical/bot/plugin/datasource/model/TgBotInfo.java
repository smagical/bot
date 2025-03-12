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
public class TgBotInfo {
    private String attrName;
    private String attrValue;


    public static List<TgBotInfo> getTgBotInfoList(ResultSet resultSet) throws SQLException {
        List<TgBotInfo> tgBotInfoList = new ArrayList<>();
        while (resultSet.next()){
            TgBotInfo tgBotInfo = new TgBotInfo();
            tgBotInfo.setAttrName(resultSet.getString("attr_name"));
            tgBotInfo.setAttrValue(resultSet.getString("attr_value"));
            tgBotInfoList.add(tgBotInfo);
        }
        return tgBotInfoList;
    }

    public static TgBotInfo getTgBotInfoListOne(ResultSet resultSet) throws SQLException {

        if (resultSet.next()){
            TgBotInfo tgBotInfo = new TgBotInfo();
            tgBotInfo.setAttrName(resultSet.getString("attr_name"));
            tgBotInfo.setAttrValue(resultSet.getString("attr_value"));
            return tgBotInfo;
        }
        return null;
    }
}
