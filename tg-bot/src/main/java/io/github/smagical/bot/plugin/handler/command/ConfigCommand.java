package io.github.smagical.bot.plugin.handler.command;

import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.datasource.model.TgBotInfo;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.plugin.util.ParamsUtils;
import io.github.smagical.bot.tg.util.ClientUtils;

import java.util.*;

public class ConfigCommand implements CommandHandler{
    private List<CommandInfo> commandInfoList = new ArrayList<>();
    private SmagicalTgPlugin plugin;

    public ConfigCommand(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
        CommandInfo configRef = CommandInfo.builder()
                .type(CommandType.USER)
                .permission(Permission.ADMIN)
                .description("/config_pull [pull db config]")
                .cmd("/config_pull")
                .function(this::pullConfig)
                .build();
        CommandInfo configUpdate= CommandInfo.builder()
                .type(CommandType.USER)
                .permission(Permission.ADMIN)
                .description("/config_update key value [config_update key value]")
                .cmd("/config_update")
                .function(this::updateConfig)
                .build();
        CommandInfo configGet= CommandInfo.builder()
                .type(CommandType.USER)
                .permission(Permission.ADMIN)
                .description("/config_get key  [config_update key value]")
                .cmd("/config_get")
                .function(this::getConfig)
                .build();
        commandInfoList.add(configRef);
        commandInfoList.add(configUpdate);
        commandInfoList.add(configGet);
    }

    private void getConfig(CommandParam commandParam) {
       withSQLAndNumCatch(()->{
           List<String> message = new ArrayList<>();
           Set<Integer> index = new HashSet<>();
           if (commandParam.getArgs().length == 0) {
               for (TgBotInfo info : DbUtil.selectTgBotInfoAll(plugin.getDataSource())) {
                   index.add(message.size());
                   message.add(info.getAttrName());
                   message.add(": ");
                   index.add(message.size());
                   message.add(info.getAttrValue());
                   message.add("\n");
               }
           }else {
               TgBotInfo info = DbUtil.selectTgBotInfoByName(plugin.getDataSource(),commandParam.getArgs()[0].strip().toUpperCase());
                 index.add(message.size());
                 message.add(info.getAttrName());
                 message.add(": ");
                 index.add(message.size());
                 message.add(info.getAttrValue());
                 message.add("\n");
           }
           ClientUtils.sendTextByCodeType(
                   plugin.getBot().getClient(),
                   commandParam.getChatId(),
                   message.toArray(new String[message.size()]),
                   index,
                   null
           );
       },plugin.getBot().getClient(),commandParam.getChatId(),"getConfig ");
    }

    private void updateConfig(CommandParam commandParam) {
        ParamsUtils.CheckParamsByLen(commandParam.getArgs(),2);
        TgBotInfo tgBotInfo = new TgBotInfo();
        tgBotInfo.setAttrName(commandParam.getArgs()[0].strip().toUpperCase());
        tgBotInfo.setAttrValue(commandParam.getArgs()[1].strip());
        withSQLAndNumCatch(()->{
            DbUtil.insertOrUpdateTgBotInfo(plugin.getDataSource(),tgBotInfo);
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    "设置成功"
            );
        },plugin.getBot().getClient(),commandParam.getChatId(),"updateConfig ");
    }

    private void pullConfig(CommandParam commandParam) {
        this.plugin.getConfiguration().loadFromDatabase(plugin.getDataSource());
        ClientUtils.sendTextMessage(
                plugin.getBot().getClient(),
                commandParam.getChatId(),
                "更新成功"
        );
    }

    @Override
    public boolean handler( CommandParam commandParam) {
        for (CommandInfo commandInfo : commandInfoList) {
            if (CommandHandler.testCommand(commandParam, commandInfo)) {
                commandInfo.getFunction().execute(commandParam);
                return true;
            }
        }
        return false;
    }


    @Override
    public List<CommandInfo> getCommandList() {
        return Collections.unmodifiableList(commandInfoList);
    }
}
