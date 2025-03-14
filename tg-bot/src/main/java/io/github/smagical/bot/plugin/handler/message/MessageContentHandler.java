package io.github.smagical.bot.plugin.handler.message;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.datasource.model.TgMessage;
import io.github.smagical.bot.plugin.handler.PageHelper;
import io.github.smagical.bot.plugin.handler.PluginHandler;
import io.github.smagical.bot.tg.Bot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

public interface MessageContentHandler extends PluginHandler {
    boolean supports(Bot.LoginType loginType);
    void  handler( MessageInfo messageInfo);
    void updateHandler( MessageInfo messageInfo);

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TgMessageHelper extends PageHelper<TgMessage> {
        private  String query = "";

    }

    default TgMessageHelper toTgMessageHelper(MessageInfo messageInfo){
        String[] query = Arrays.stream(
                messageInfo.getMessageText()
                        .strip()
                        .split("\\s+"))
                .map(String::strip)
                .filter(s->!s.isBlank())
                .toArray(String[]::new);
        if (query.length == 1){
            TgMessageHelper helper = new TgMessageHelper();
            helper.setQuery(query[0]);
            return  helper;
        }
        if (query.length == 2){
            Integer page = Integer.valueOf(query[1]);
            TgMessageHelper helper = new TgMessageHelper();
            helper.setQuery(query[0]);
            helper.setPage(page);
            return helper;
        }
        if (query.length >= 3){
            Integer page = Integer.valueOf(query[1]);
            Integer pageSize = Integer.valueOf(query[2]);
            TgMessageHelper helper = new TgMessageHelper();
            helper.setQuery(query[0]);
            helper.setPageSize(pageSize);
            helper.setPage(page);
            return helper;
        }
        throw new RuntimeException("query args error");
    }
}
