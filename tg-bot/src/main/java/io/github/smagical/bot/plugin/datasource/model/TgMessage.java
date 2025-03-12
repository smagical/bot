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
public class TgMessage {
    private Long id;
    private Long chatId;
    private Long ablum;
    private String message;
    private String link;


    public static List<TgMessage>  getTgMessages(ResultSet resultSet) throws SQLException {
        List<TgMessage> tgMessages = new ArrayList<>();
        while (resultSet.next()){
            TgMessage messages =  new TgMessage();
            messages.setId(resultSet.getLong("id"));
            messages.setChatId(resultSet.getLong("chat_id"));
            messages.setAblum(resultSet.getLong("ablum"));
            messages.setMessage(resultSet.getString("message"));
            messages.setLink(resultSet.getString("link"));
            tgMessages.add(messages);
        }
        return tgMessages;
    }

    public static TgMessage  getTgMessagesOne(ResultSet resultSet) throws SQLException {

        if (resultSet.next()){
            TgMessage messages =  new TgMessage();
            messages.setId(resultSet.getLong("id"));
            messages.setChatId(resultSet.getLong("chat_id"));
            messages.setAblum(resultSet.getLong("ablum"));
            messages.setMessage(resultSet.getString("message"));
            messages.setLink(resultSet.getString("link"));
            return  messages;
        }
        return null;
    }

}
