package io.github.smagical.bot.plugin.handler.command;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.datasource.cache.CacheManger;
import io.github.smagical.bot.plugin.datasource.model.TgGroup;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.plugin.util.ParamsUtils;
import io.github.smagical.bot.tg.model.MessageCallBack;
import io.github.smagical.bot.tg.util.ClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.time.Duration;
import java.util.*;

import static io.github.smagical.bot.tg.util.Utils.withSQLAndNumCatch;

@Slf4j
public class ChatCommand implements CommandHandler{

    private List<CommandInfo> commandInfoList = new ArrayList<>();
    private SmagicalTgPlugin plugin;
    private final String AD_KEY_PREFIX = "tg_bot:code:";
    private final String CODE_CACHE_NAME = "CODE_CACHE_NAME";

    public ChatCommand(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
        CommandInfo addCommandInfo = CommandInfo.builder()
                .cmd("/chat_add")
                .type(CommandType.ALL)
                .permission(Permission.USER)
                .function(this::chatAdd)
                .description("/chat_add chat_id [add chat for bot]")
                .build();
        CommandInfo delCommandInfo = CommandInfo.builder()
                    .cmd("/chat_del")
                    .type(CommandType.ALL)
                    .permission(Permission.USER)
                    .function(this::chatDel)
                    .description("/chat_del chat_id [del chat for bot]")
                    .build();
        CommandInfo geneInviteCode =  CommandInfo.builder()
                .cmd("/chat_invite_genera")
                .type(CommandType.ALL)
                .permission(Permission.USER)
                .function(this::chatInviteGenera)
                .description("/chat_invite_genera  [genera invite code]")
                .build();
        CommandInfo inviteCode =  CommandInfo.builder()
                .cmd("/chat_invite_code")
                .type(CommandType.ALL)
                .permission(Permission.CHAT)
                .function(this::chatInviteCode)
                .description("/chat_invite_code code  [chat invite for code]")
                .build();
        CommandInfo groupList = CommandInfo.builder()
                .cmd("/chat_list")
                .type(CommandType.ALL)
                .permission(Permission.ADMIN)
                .function(this::groupList)
                .description("/chat_list  [chat list]")
                .build();
        CommandInfo groupListByUser = CommandInfo.builder()
                .cmd("/chat_list_by_user")
                .type(CommandType.ALL)
                .permission(Permission.USER)
                .function(this::groupListByUser)
                .description("/chat_list_by_user user_id [chat list for user]")
                .build();
        CommandInfo groupUserListByChat = CommandInfo.builder()
                .cmd("/chat_user_list_by_chat")
                .type(CommandType.ALL)
                .permission(Permission.ADMIN)
                .function(this::groupUserListByChat)
                .description("/chat_user_list_by_chat  [user list for group]")
                .build();
        CommandInfo delAllByGroup = CommandInfo.builder()
                .cmd("/chat_del_all_by_chat")
                .type(CommandType.ALL)
                .permission(Permission.ADMIN)
                .function(this::delAllByGroup)
                .description("/chat_del_all_by_chat chat_id  [del chat by chat_id]")
                .build();
        CommandInfo delAllByUser = CommandInfo.builder()
                .cmd("/chat_del_all_by_user")
                .type(CommandType.ALL)
                .permission(Permission.ADMIN)
                .function(this::delAllByUser)
                .description("/chat_del_all_by_user user_id [del chat by user_id]")
                .build();
        commandInfoList.add(addCommandInfo);
        commandInfoList.add(delCommandInfo);
        commandInfoList.add(geneInviteCode);
        commandInfoList.add(inviteCode);
        commandInfoList.add(groupList);
        commandInfoList.add(groupListByUser);
        commandInfoList.add(groupUserListByChat);
        commandInfoList.add(delAllByGroup);
        commandInfoList.add(delAllByUser);

    }

    private void delAllByUser( CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        withSQLAndNumCatch(()->{
            Long userId = Long.parseLong(args[0]);
            DbUtil.TgGroupDb.delAllTgGroupByUserId(plugin.getDataSource(),userId);
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format("del [%s] successful",args[0])
            );

        },plugin.getBot().getClient(), commandParam.getChatId(),"del all user");
    }

    private void delAllByGroup(CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        withSQLAndNumCatch(()->{
            Long chatId = Long.parseLong(args[0]);
            DbUtil.TgGroupDb.delAllTgGroupByChatId(plugin.getDataSource(),chatId);
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format("del [%s] successful",args[0])
            );

        },plugin.getBot().getClient(), commandParam.getChatId(),"del all by chat");
    }

    private void groupList( CommandParam commandParam) {

        withSQLAndNumCatch(()->{
            List<TgGroup> list = DbUtil.TgGroupDb.selectTgGroupAll(plugin.getDataSource());
            HashMap<Long,String> ids = new HashMap<>();
            for (TgGroup tgGroup : list) {
                if (ids.containsKey(tgGroup.getChatId())) continue;
                ids.put(tgGroup.getChatId(),tgGroup.getChatName());
            }
            List<String> messages = new ArrayList<>();
            Set<Integer> codeIndex = new HashSet<>();
            for (Map.Entry<Long, String> entry : ids.entrySet()) {
                messages.add(entry.getValue());
                messages.add(": ");
                codeIndex.add(messages.size());
                messages.add(entry.getKey().toString());
                messages.add("\n");
            }
            if (messages.isEmpty()){
                messages.add("0/0");
            }
            ClientUtils.sendTextByCodeType(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    messages.toArray(new String[messages.size()]),
                    codeIndex,
                    null
            );

        },plugin.getBot().getClient(), commandParam.getChatId(),"all chat");
    }

    private void groupListByUser( CommandParam commandParam ){
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        withSQLAndNumCatch(()->{
            Long userId = Long.parseLong(args[0]);
            List<TgGroup> list =
                    DbUtil.TgGroupDb.selectTgGroupByUserId(plugin.getDataSource(),userId);
            List<TdApi.Chat> chats =
                    list.stream()
                            .map(e->plugin.getBot().getChat(e.getChatId(),true))
                            .filter(Objects::nonNull)
                            .toList();
            List<String> chatNames = new ArrayList<>();
            Set<Integer> codeIndex = new HashSet<>();
            for (TdApi.Chat chat : chats) {
                chatNames.add(chat.title);
                chatNames.add(": ");
                codeIndex.add(chatNames.size());
                chatNames.add(String.valueOf(chat.id));
                chatNames.add("\n");
            }
            if (chatNames.isEmpty()){
                chatNames.add("0/0");
            }
            ClientUtils.sendTextByCodeType(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    chatNames.toArray(new String[chatNames.size()]),
                    codeIndex,
                    null
            );
        },plugin.getBot().getClient(), commandParam.getChatId(),"chat list by user");
    }

    private void groupUserListByChat( CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        withSQLAndNumCatch(()->{
            Long chatId = Long.parseLong(args[0]);
            final List<TgGroup> list =
                    DbUtil.TgGroupDb.selectTgGroupByChatId(plugin.getDataSource(),chatId);
            plugin.getBot().getUserCallBack(
                    list.removeFirst().getInviteUserId(),
                    new MessageCallBack<TdApi.User>() {
                        private final  List<TdApi.User> users = new ArrayList<>();
                        @Override
                        public void accept(TdApi.User message) {
                            users.add(message);
                            next();
                        }

                        @Override
                        public void error(TdApi.Error error) {
                            next();
                        }
                        private void next(){
                            if (list.isEmpty()) {
                                List<String> userNames = new ArrayList<>();
                                Set<Integer> codeIndex = new HashSet<>();
                                for (TdApi.User user : users) {
                                    userNames.add(String.format("%s %s",user.firstName,user.lastName));
                                    userNames.add(": ");
                                    codeIndex.add(userNames.size());
                                    userNames.add(String.valueOf(user.id));
                                    userNames.add("\n");
                                }
                                if (userNames.isEmpty()){
                                    userNames.add("0/0");
                                }
                                ClientUtils.sendTextByCodeType(
                                        plugin.getBot().getClient(),
                                        commandParam.getChatId(),
                                        userNames.toArray(new String[userNames.size()]),
                                        codeIndex,
                                        null
                                );
                            }
                            plugin.getBot().getUserCallBack(
                                    list.removeFirst().getInviteUserId(),
                                    this
                            );
                        }
                    }
            );

        },plugin.getBot().getClient(),commandParam.getChatId(),"user list by chat");
    }



    private void chatInviteCode(CommandParam commandParam) {
             String[] args = commandParam.getArgs();
            ParamsUtils.CheckParamsByLen(args, 1);
            Long userId = null;

        CacheManger.Cache<String,Long> cache;
        if ("singleton".equalsIgnoreCase(plugin.getConfiguration().getModel().strip())){
            cache = CacheManger.getCacheOrDefault(CODE_CACHE_NAME,()->{
                return new CacheManger.CaffeineCache<String,Long>(
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofMinutes(5l))
                                .softValues()
                                .build()
                );
            });
        }else {
            cache = CacheManger.getCacheOrDefault(CODE_CACHE_NAME,()->{
                return new CacheManger.RedisCache<Long>(
                        plugin.getRedissonClient(), AD_KEY_PREFIX,Duration.ofMinutes(5l)
                );
            });
        }

        userId = cache.get(args[0]);

        if (userId != null){
                cache.remove(args[0]);
                final Long userIdTmp = userId;
                withSQLAndNumCatch(()->{
                    TdApi.Chat chat = plugin.getBot().getChat(commandParam.getChatId(),true);
                    TgGroup tgGroup = TgGroup
                            .builder()
                            .chatId(commandParam.getChatId())
                            .inviteUserId(userIdTmp)
                            .chatName(chat.title)
                            .build();
                    DbUtil.TgGroupDb.insertOrUpdateTgGroup(plugin.getDataSource(), tgGroup);
                    ClientUtils.sendTextMessage(
                            plugin.getBot().getClient(),
                            commandParam.getChatId(),
                            "Bot start in this chat"
                    );
                },plugin.getBot().getClient(),commandParam.getChatId(),"chat invite");

            }else {
                ClientUtils.sendTextMessage(
                        plugin.getBot().getClient(),
                        commandParam.getChatId(),
                        "invite_code bot found"
                );
            }
    }

    private void chatInviteGenera(CommandParam commandParam) {

        String uuid = UUID.randomUUID().toString().replace("-", "");
        CacheManger.Cache<String,Long> cache;
        if ("singleton".equalsIgnoreCase(plugin.getConfiguration().getModel().strip())){
            cache = CacheManger.getCacheOrDefault(CODE_CACHE_NAME,()->{
                return new CacheManger.CaffeineCache<String,Long>(
                        Caffeine.newBuilder()
                                .expireAfterWrite(Duration.ofMinutes(5l))
                                .softValues()
                                .build()
                );
            });
        }else {
            cache = CacheManger.getCacheOrDefault(CODE_CACHE_NAME,()->{
                return new CacheManger.RedisCache<Long>(
                        plugin.getRedissonClient(), AD_KEY_PREFIX,Duration.ofMinutes(5l)
                );
            });
        }
        cache.put(uuid, commandParam.getUserId());
        String code = "CODE: ";

        ClientUtils.sendTextByCodeType(
                plugin.getBot().getClient(),
                commandParam.getChatId(),
                new String[]{code,uuid},
                Set.of(1),
                null
        );
    }

    private void  chatAdd( CommandParam commandParam){
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args, 1);
        withSQLAndNumCatch(()->{
            Long chatId = Long.parseLong(args[0]);
            TdApi.Chat chat = plugin.getBot().getChat(chatId,true);

            if ( chat == null){
                ClientUtils.sendTextMessage(
                        plugin.getBot().getClient(),
                        commandParam.getChatId(),
                        String.format("Bot not join %s chat", args[0])
                );
                return;
            }
            TgGroup tgGroup =  TgGroup.builder()
                    .chatId(chatId)
                    .chatName(chat.title)
                    .inviteUserId(commandParam.getUserId())
                    .build();
             if (!DbUtil.TgGroupDb.exitsTgGroupByChatIdAndUserId(plugin.getDataSource(),tgGroup)){
                DbUtil.TgGroupDb.insertTgGroup(
                        plugin.getDataSource(),
                        tgGroup
                );
                ClientUtils.sendTextMessage(
                        plugin.getBot().getClient(),
                        commandParam.getChatId(),
                        String.format("Bot start in %s:%s chat", args[0],chat.title)
                );
            }else {
                ClientUtils.sendTextMessage(
                        plugin.getBot().getClient(),
                        commandParam.getChatId(),
                        String.format("Bot already join  %s:%s chat", args[0],chat.title)
                );
            }
        },plugin.getBot().getClient(),commandParam.getChatId(),"chat add");
    };

    private void chatDel( CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args, 1);
        withSQLAndNumCatch(()->{
            Long chatId = Long.parseLong(args[0]);
            TgGroup tgGroup =  TgGroup.builder()
                    .chatId(chatId)
                    .inviteUserId(commandParam.getUserId())
                    .build();
            DbUtil.TgGroupDb.delTgGroupByChatIdAndUserId(plugin.getDataSource(),tgGroup);
                    ClientUtils.sendTextMessage(
                            plugin.getBot().getClient(),
                            commandParam.getChatId(),
                            String.format("delete chat %s successful", args[0])
                    );
        },
                plugin.getBot().getClient(),
                commandParam.getChatId(),
                "chat delete");
    }

    @Override
    public boolean handler(CommandParam commandParam) {
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
        return commandInfoList;
    }
}
