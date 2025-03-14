package io.github.smagical.bot.plugin.handler.command;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.handler.PluginHandler;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

;


public   interface CommandHandler extends PluginHandler {

    /**
     * 使用命令的权限
     */
    public static enum Permission {
        CHAT(0),
        USER(1),
        ADMIN(Integer.MAX_VALUE/2),
        UNKNOWN(-1);
        private int level;

        Permission(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * 机器人还是用户登录
     */
    public static enum CommandType {
        USER,
        BOT,
        ALL;

    }


    @FunctionalInterface
    public static interface CommandFunction{
        /**
         *
         * @param plugin 插件本身
         * @param args 命令参数
         * @param messageInfo 查询信息
         */
        void  execute(CommandParam commandParam);
    }



    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CommandInfo{
        String cmd;
        String description;
        Permission permission = Permission.USER;
        CommandType type = CommandType.USER;
        CommandFunction function;
        boolean isAllRun = false;
    }

    /**
     * 命令消息的封装
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class CommandParam extends  MessageInfo{
        private String cmd;
        private String[] args;
        private Permission permission = Permission.UNKNOWN;
        private CommandType type;

        public CommandParam(MessageInfo messageInfo){
            if (!messageInfo.isCommand())
                throw new RuntimeException("not command");
            this.setCommand(true);
            this.setBot(messageInfo.isBot());
            this.setChatId(messageInfo.getChatId());
            this.setMessageId(messageInfo.getMessageId());
            this.setUserId(messageInfo.getUserId());
            this.setMessageText(messageInfo.getMessageText());
            this.setChatType(messageInfo.getChatType());
            this.setSenderIsBot(messageInfo.isSenderIsBot());

            String[] args = Arrays.stream(
                    getMessageText().
                            split("\\s+"))
                    .filter(s -> !s.isBlank())
                    .map(String::strip).toArray(String[]::new);

            if (ChatType.ADMIN == getChatType()) permission = CommandHandler.Permission.ADMIN;
            else if (ChatType.CHAT == getChatType()) permission = CommandHandler.Permission.CHAT;
            else if (ChatType.USER == getChatType()) permission = CommandHandler.Permission.USER;
            else  permission = CommandHandler.Permission.UNKNOWN;
            this.args = Arrays.copyOfRange(args, 1, args.length);
            if (isBot()){
                type = CommandHandler.CommandType.BOT;
            }else {
                type = CommandType.USER;
            }
            this.cmd = args[0].strip();


        }
    }



    public static boolean testCommand(CommandParam commandParam, CommandInfo commandInfo){
        if(commandParam.getPermission().getLevel() < commandInfo.getPermission().getLevel()){
            return false;
        }

        if (commandInfo.getType() != CommandType.ALL && commandParam.getType() != commandInfo.type) {
            return false;
        }
        if (!commandParam.cmd.strip().toUpperCase().equals(commandInfo.cmd.toUpperCase())) {
            return false;
        }
        return true;
    }

    default int getOrder() {
        return Integer.MAX_VALUE - 10;
    };
    boolean handler(CommandParam commandParam);
    default boolean support(CommandParam commandParam){
        for (CommandInfo commandInfo : getCommandList()) {
            if (testCommand(commandParam, commandInfo)) {
                return true;
            }
        }
        return false;
    };
    List<CommandInfo> getCommandList();



}