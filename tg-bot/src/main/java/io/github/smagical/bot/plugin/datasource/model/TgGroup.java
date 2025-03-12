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
public class TgGroup {
    private Long inviteUserId;
    private Long chatId;
    private String chatName;

    public static List<TgGroup> getTgGroupList(ResultSet resultSet) throws SQLException {
        List<TgGroup> list = new ArrayList<>();
        while (resultSet.next()){
            TgGroup group = new TgGroup();
            group.setInviteUserId(resultSet.getLong("invite_user_id"));
            group.setChatId(resultSet.getLong("chat_id"));
            group.setChatName(resultSet.getString("chat_name"));
            list.add(group);
        }
        return list;
    }

    public  static TgGroup getTgGroupOne(ResultSet resultSet) throws SQLException {
        if (resultSet.next()){
            TgGroup group = new TgGroup();
            group.setInviteUserId(resultSet.getLong("invite_user_id"));
            group.setChatId(resultSet.getLong("chat_id"));
            group.setChatName(resultSet.getString("chat_name"));
            return group;
        }
        return null;
    }
}
