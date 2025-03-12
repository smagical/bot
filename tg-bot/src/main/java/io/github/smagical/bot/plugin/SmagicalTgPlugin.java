package io.github.smagical.bot.plugin;

import io.github.smagical.bot.TgConfiguration;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.plugin.datasource.DataSoureFactory;
import io.github.smagical.bot.plugin.handler.auth.AuthenticationHandler;
import io.github.smagical.bot.plugin.handler.auth.ChatJoinAuthHandler;
import io.github.smagical.bot.plugin.handler.auth.RedisAuthHandler;
import io.github.smagical.bot.plugin.handler.command.*;
import io.github.smagical.bot.plugin.handler.message.MessageContentHandler;
import io.github.smagical.bot.plugin.handler.message.MessageReplyHandler;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.plugin.util.ParamsUtils;
import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.chat.update.bot.NewCallbackQueryHandler;
import io.github.smagical.bot.tg.handler.chat.update.message.NewMessageHandler;
import io.github.smagical.bot.tg.model.Plugin;
import io.github.smagical.bot.tg.util.ClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class SmagicalTgPlugin implements Plugin {

    public final static String CONFIGURATION_KEY = "SMAGICAL_TG_PLUGIN";
    private TgConfiguration configuration;

    private  Bot bot;
    private final static long START_TIME = Instant.now().getEpochSecond();

    private CopyOnWriteArrayList<CommandHandler> commandHandlers = new CopyOnWriteArrayList<>();
    private  CopyOnWriteArrayList<AuthenticationHandler> authenticationHandlers = new CopyOnWriteArrayList<>();

    private CopyOnWriteArrayList<MessageContentHandler> messageHandlers = new CopyOnWriteArrayList<>();

    private DataSource dataSource;

    private RedissonClient redissonClient;


    @Override
    public void onHandle(Event event) {
        if ((event instanceof NewMessageHandler.MessageEvent)) onMessageEvent((NewMessageHandler.MessageEvent) event);
        if (event instanceof NewCallbackQueryHandler.NewCallbackQueryEvent)
            onCallbackQueryEvent((NewCallbackQueryHandler.NewCallbackQueryEvent) event);

    }
    private void onCallbackQueryEvent(NewCallbackQueryHandler.NewCallbackQueryEvent event){
       TdApi.UpdateNewCallbackQuery callbackQuery  =  event.getData().getData();
       if (callbackQuery.payload.getConstructor() != TdApi.CallbackQueryPayloadData.CONSTRUCTOR)
           return;
        TdApi.CallbackQueryPayloadData data = (TdApi.CallbackQueryPayloadData) callbackQuery.payload;
        String message = new String(data.data, StandardCharsets.UTF_16);
        String[] split = Arrays.stream(message.split(" ")).map(s->s.strip()).filter(s -> !s.isEmpty()).toArray(String[]::new);
        ParamsUtils.CheckParamsByLen(split,4);
        if (split[1].equals(split[3])) return;
        MessageInfo  messageInfo = new MessageInfo();
        messageInfo.setChatId(callbackQuery.chatId);
        messageInfo.setMessageId(callbackQuery.messageId);
        messageInfo.setMessageText(message);
        for (MessageContentHandler messageHandler : messageHandlers) {
            if (messageHandler.supports(bot.getLoginType())){
                messageHandler.handler(messageInfo);
            }
        }
    }
    private void onMessageEvent(NewMessageHandler.MessageEvent event){
        TdApi.Message message = event.getData().getData();
        if (message.date < START_TIME) return;
        if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR){
            MessageInfo messageInfo = new MessageInfo();
            if (bot.getLoginType() == Bot.LoginType.BOT){
                messageInfo.setBot(true);
            }
            TdApi.MessageText messageContent = (TdApi.MessageText)message.content;
            messageInfo.setMessageText(messageContent.text.text.strip());
            messageInfo.setChatId(message.chatId);
            messageInfo.setMessageId(message.id);
            if (messageInfo.isSenderIsBot()) return;

            if (message.senderId.getConstructor() == TdApi.MessageSenderUser.CONSTRUCTOR){
                long userId = ((TdApi.MessageSenderUser)message.senderId).userId;
                messageInfo.setUserId(userId);
                TdApi.User user = bot.getUser(userId);
                if (user == null || user.type.getConstructor() == TdApi.UserTypeBot.CONSTRUCTOR) {
                    messageInfo.setSenderIsBot(true);
                    return;
                }

                TdApi.Chat chat = bot.getChat(message.chatId);
                if (chat == null) return;
                if (chat.type.getConstructor() == TdApi.ChatTypePrivate.CONSTRUCTOR) {
                    messageInfo.setChatType(MessageInfo.ChatType.USER);
                    TdApi.User me = bot.getMe();
                    if (me != null && me.id == messageInfo.getUserId())
                        messageInfo.setChatType(MessageInfo.ChatType.ADMIN);
                    else if (configuration.getAdminIds().contains(messageInfo.getUserId())){
                        messageInfo.setChatType(MessageInfo.ChatType.ADMIN);
                    }
                }else {
                    messageInfo.setChatType(MessageInfo.ChatType.CHAT);
                }

            }else if (message.senderId.getConstructor() == TdApi.MessageSenderChat.CONSTRUCTOR){
                messageInfo.setChatType(MessageInfo.ChatType.CHAT);
            }else {
                return;
            }




            boolean auth = authenticationHandlers.stream()
                    .allMatch(a -> a.authenticate(messageInfo));
            if (!auth) {
                return;
            }

            if (messageInfo.isCommand()){
                CommandHandler.CommandParam commandParam =
                        new CommandHandler.CommandParam(messageInfo);

                int count = 0;
                for (CommandHandler commandHandler : commandHandlers) {
                    if (commandHandler.support(commandParam)) {
                        if (count == 2){
                            throw new RuntimeException("command handler have multiple support");
                        }

                        try {
                            commandHandler.handler( commandParam);
                            count++;
                        }catch (Exception e){}
                    }

                }
                if (count == 0){
                    sendCommandTextInfo(messageInfo);
                }
            }else {
                for (MessageContentHandler messageHandler : messageHandlers) {
                    if (messageHandler.supports(bot.getLoginType())){
                        messageHandler.handler(messageInfo);
                    }
                }
            }


        }
    }

    private void sendCommandTextInfo(MessageInfo messageInfo){
        List<CommandHandler.CommandInfo> commandInfoList = new ArrayList<>();
        commandInfoList.addAll(this.commandHandlers.stream().map(CommandHandler::getCommandList).flatMap(List::stream).collect(Collectors.toList()));
        List<String> messages = new ArrayList<>();
        Set<Integer> indexes = new HashSet<>();
        for (CommandHandler.CommandInfo info : commandInfoList) {
            if (!testPermission(messageInfo.getChatType(),info.getPermission())) continue;
            if (!testPermission(getBot().getLoginType(),info.getType())) continue;
            indexes.add(messages.size());
            messages.add(info.getCmd());
            messages.add(": ");
            messages.add(info.getDescription().startsWith(info.getCmd())?
                    info.getDescription().substring(info.getCmd().length()):info.getDescription());
            messages.add("\n");
        }
        TdApi.InputMessageReplyTo replyTo =
                new TdApi.InputMessageReplyToMessage(messageInfo.getMessageId(),null);
        if (getBot().getLoginType() == Bot.LoginType.BOT) {
            ClientUtils.sendTextByType(
                    getBot().getClient(),
                    messageInfo.getChatId(),
                    messages.toArray(new String[messages.size()]),
                    indexes,
                    (a,b,c)->{
                        TdApi.TextEntity textEntity = new TdApi.TextEntity();
                        textEntity.offset = a.length();
                        textEntity.length = c.length();
                        textEntity.type = new TdApi.TextEntityTypeBotCommand();
                        return textEntity;
                    },
                    replyTo
            );

        }else {
            ClientUtils.sendTextByCodeType(
                    getBot().getClient(),
                    messageInfo.getChatId(),
                    messages.toArray(new String[messages.size()]),
                    indexes,
                    replyTo
            );
        }
    }

    private boolean testPermission(Bot.LoginType type, CommandHandler.CommandType commandType){
        CommandHandler.CommandType typ = type == Bot.LoginType.BOT ?
                        CommandHandler.CommandType.BOT : CommandHandler.CommandType.USER;
        if (commandType ==  CommandHandler.CommandType.ALL){
            return true;
        }
        return commandType == typ;
    }

    private boolean testPermission(MessageInfo.ChatType chatType, CommandHandler.Permission permission){
        if (chatType == MessageInfo.ChatType.CHAT && permission != CommandHandler.Permission.CHAT){
            return false;
        }
        if (chatType == MessageInfo.ChatType.USER && permission == CommandHandler.Permission.ADMIN){
            return false;
        }
        return true;
    }

    @Override
    public void init(Bot bot) {
        this.bot = bot;
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(true);
        loaderOptions.setEnumCaseSensitive(false);
        Constructor constructor = new Constructor(loaderOptions);
        constructor.setPropertyUtils(new PropertyUtils(){
            @Override
            public Property getProperty(Class<?> type, String name) {
                setSkipMissingProperties(true);
                return super.getProperty(type, name);
            }
        });
        Yaml yaml = new Yaml(constructor);

        String path = bot.getConfiguration().getProperty(CONFIGURATION_KEY);
        if (path.startsWith("classpath:")){
            configuration = yaml.loadAs(Test.class.getClassLoader().getResourceAsStream(
                    path.substring("classpath:".length())
            ),TgConfiguration.class);
        }else if (path.startsWith("string:")){
            configuration = yaml.loadAs(path.substring("string:".length()), TgConfiguration.class);
        } else {
            try {
                configuration = yaml.loadAs(new FileInputStream(path),TgConfiguration.class);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            this.dataSource = DataSoureFactory.getDataSource(configuration);
            DbUtil.initSchem(dataSource,"classpath:tg.sql");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1976648);
        }


        if ("cluster".equals(configuration.getModel().strip().toLowerCase())){
            Config redisConfig = new Config();
            redisConfig.useSingleServer()
                    .setUsername(configuration.getRedis().getUsername())
                    .setPassword(configuration.getRedis().getPassword())
                    .setDatabase(configuration.getRedis().getDb())
                    .setAddress("redis://" + configuration.getRedis().getHost() + ":" + configuration.getRedis().getPort())
                    .setClientName("tg_bot");
            this.redissonClient = Redisson.create(redisConfig);
            this.authenticationHandlers.add(new RedisAuthHandler(this));
        }

        this.messageHandlers.add(new MessageReplyHandler(this));
        this.commandHandlers.add(new ChatCommand(this));
        this.commandHandlers.add(new SpiderCommand(this));
        this.commandHandlers.add(new UserCommand(this));
        this.authenticationHandlers.add(new ChatJoinAuthHandler(this));
        this.commandHandlers.add(new ConfigCommand(this));
        this.configuration.init();
        this.configuration.loadFromDatabase(getDataSource());
    }

    public TgConfiguration getConfiguration() {
        return configuration;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Bot getBot() {
        return bot;
    }

    @Override
    public void destroy() {
        if (this.redissonClient!=null){
            this.redissonClient.shutdown();
        }

    }


}
