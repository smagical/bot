package io.github.smagical.bot.plugin.handler.auth;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.util.DbUtil;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChatJoinAuthHandler implements AuthenticationHandler{

    private ConcurrentSkipListSet<String> commandWhitelist = new ConcurrentSkipListSet<>();
    public final static String COMMAND_WHITE_KEY = "COMMAND_WHITE";
    private SmagicalTgPlugin plugin;

    public ChatJoinAuthHandler(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
        this.plugin.getConfiguration()
                .addUpdateListener(COMMAND_WHITE_KEY,
                        str->{
                            if (str == null) {
                                commandWhitelist.clear();
                                return;
                            }
                            ConcurrentSkipListSet<String> commandWhitelist2 = new ConcurrentSkipListSet<>();
                             Arrays.stream(str.toString().split(","))
                                    .filter(s -> !s.strip().isBlank())
                                    .forEach(s -> commandWhitelist2.add(s));
                             commandWhitelist = commandWhitelist2;
                        });
    }

    @Override
    public boolean authenticate( MessageInfo messageInfo) {
        if (messageInfo.getChatType() == MessageInfo.ChatType.USER
                ||messageInfo.getChatType() == MessageInfo.ChatType.ADMIN){
            return true;
        }
        if (messageInfo.isCommand()){
            if (commandWhitelist.contains(messageInfo.getMessageText().split("\\s+")[0])){
                return true;
            }
        }

        try {
            return DbUtil.exitsTgGroupByChatIdAndUserId(plugin.getDataSource(),messageInfo.getChatId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    public void addCommandWhite(String command){
        commandWhitelist.add(command);
    }
    public  void removeCommandWhite(String command){
        commandWhitelist.remove(command);
    }

}
