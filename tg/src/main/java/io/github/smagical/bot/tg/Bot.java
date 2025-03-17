package io.github.smagical.bot.tg;


import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.user.LoginEvent;
import io.github.smagical.bot.listener.Listener;
import io.github.smagical.bot.tg.handler.DispatchHandler;
import io.github.smagical.bot.tg.handler.LogHandler;
import io.github.smagical.bot.tg.handler.UpdateOptionHandler;
import io.github.smagical.bot.tg.handler.chat.ChatDispatchHandler;
import io.github.smagical.bot.tg.handler.user.authorization.state.AuthorizationStateDispatchHandler;
import io.github.smagical.bot.tg.handler.user.update.UpdateUserDispatchHandler;
import io.github.smagical.bot.tg.listener.plugin.PluginDispatch;
import io.github.smagical.bot.tg.listener.plugin.PluginInitListener;
import io.github.smagical.bot.tg.listener.user.ChatLoadListener;
import io.github.smagical.bot.tg.listener.user.UserListener;
import io.github.smagical.bot.tg.listener.user.authorization.state.AuthorizationStateListener;
import io.github.smagical.bot.tg.listener.user.chat.LoginForChatInitListener;
import io.github.smagical.bot.tg.listener.user.chat.message.MessageDispatchListener;
import io.github.smagical.bot.tg.model.MessageCallBack;
import io.github.smagical.bot.tg.model.PluginEntity;
import io.github.smagical.bot.tg.util.ClientUtils;
import io.github.smagical.bot.tg.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.io.IOError;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;

@Slf4j
public class Bot extends MessageDispatch implements io.github.smagical.bot.Bot {

    private String id;
    private Client client;
    private volatile DispatchHandler dispatchHandler;
    private volatile PluginDispatch pluginDispatchHandler;
    private LoginType loginType = LoginType.PHONE_NUMBER;
    private Cache<Long,TdApi.Chat> chatCache;
    private Cache<Long,TdApi.SecretChat> secretCache;
   // private UserMap users = new UserMap();
    private Cache<Long,TdApi.User> userCache;
    private Cache<Long,TdApi.UserFullInfo> userFullInfoCache;
    private volatile Boolean isRunning = false;
    private volatile TdApi.User me = null;


    private BotConfiguration configuration;

    public Bot(BotConfiguration configuration) throws SQLException, IOException {
        this.configuration = configuration;
        this.id = String.valueOf(BotConfig.getInstance()
                .getId());
        this.pluginDispatchHandler = new PluginDispatch(this);
    }

    private String phoneNumber;
    private String botToken;

    public String getBotToken() {
        return botToken;
    }

    public static enum LoginType {
        PHONE_NUMBER, OCR,BOT;
    }



    public void loginByOcr() {
        checkRunning();
        login(LoginType.OCR);
    }

    public void loginByPthone(String phoneNumber) {
        checkRunning();
        synchronized (isRunning){
            this.phoneNumber = phoneNumber;
            this.id = phoneNumber;
            login(LoginType.PHONE_NUMBER);
        }
    }
    public void loginByBotToken(String botToken) {
        checkRunning();
        synchronized (isRunning){
            this.botToken = botToken;
            this.id = botToken.replace(":","_");
            login(LoginType.BOT);
        }
    }

    private void login(LoginType loginType) {
        checkRunning();
        synchronized (isRunning) {
            this.loginType = loginType;
            this.dispatchHandler = new DispatchHandler(this);
            this.userCache = Caffeine.newBuilder()
                    .maximumSize(10000)
                    .expireAfterAccess(Duration.ofDays(365))
                    .softValues()
                    .build(key -> ClientUtils.getUser(client,key));
            this.userFullInfoCache = Caffeine.newBuilder()
                    .maximumSize(10000)
                    .expireAfterAccess(Duration.ofDays(365))
                    .softValues()
                    .build();
            this.chatCache = Caffeine.newBuilder()
                    .maximumSize(10000)
                    .expireAfterAccess(Duration.ofDays(365))
                    .softValues()
                    .build();
            this.secretCache = Caffeine.newBuilder()
                    .maximumSize(10000)
                    .expireAfterAccess(Duration.ofDays(365))
                    .softValues()
                    .build();

            Client.setLogMessageHandler(0, LogHandler.getInstance());
            // disable TDLib log and redirect fatal errors and plain log messages to a file
            try {
                Client.execute(new TdApi.SetLogVerbosityLevel(0));
                Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false)));

            } catch (Client.ExecutionException error) {
                throw new IOError(new IOException("Write access to the current directory is required"));
            }
            initHandler();
            initListener();
            this.client = Client.create(this.dispatchHandler, LogHandler.getInstance(), null);
            isRunning = true;
            BotConfig.getInstance().initThreadExecutor();
        }


    }

    public void logout(){
        synchronized (isRunning) {
            if (!isRunning) return;
            this.isRunning = false;
            BotConfig.getInstance().stopThreadExecutor();
            this.chatCache.invalidateAll();
            this.userCache.invalidateAll();
            this.userFullInfoCache.invalidateAll();
        }

    }

    private void initHandler(){
        this.dispatchHandler.addHandler(new AuthorizationStateDispatchHandler(this));
        this.dispatchHandler.addHandler(new UpdateOptionHandler());
        this.dispatchHandler.addHandler(new UpdateUserDispatchHandler(this));
        this.dispatchHandler.addHandler(new ChatDispatchHandler(this));
    }

    private void initListener(){


        addListener(new AuthorizationStateListener(this));
        addListener(new UserListener(this));
        addListener(new ChatLoadListener(this));
        addListener(new LoginForChatInitListener(this));
        addListener(new MessageDispatchListener(this));
        addListener(new GetMeHandler());
        addListener(new PluginInitListener(this,this.pluginDispatchHandler));

    }


    public synchronized void addPlugin(PluginEntity pluginEntity){
        this.pluginDispatchHandler.addPlugin(pluginEntity);
    }

    public synchronized  void removePlugin(PluginEntity pluginEntity){
        this.pluginDispatchHandler.removePlugin(pluginEntity);
    }

    public Client getClient() {
        return client;
    }

    public String getBotId() {
        return id;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public LoginType getLoginType() {
        return this.loginType;
    }


    public boolean addChat(TdApi.Chat chat) {
        chatCache.put(chat.id, chat);
        return true;
    }

    public boolean addSecretChat(TdApi.SecretChat secretChat) {
        secretCache.put((long) secretChat.id,secretChat);
        return true;
    }

    public TdApi.Chat getChat(long chatId) {
        return getChat(chatId,false);
    }

    public TdApi.Chat getChat(long chatId,boolean netWork) {
        if (!netWork) {
            return chatCache.getIfPresent(chatId);
        }
        return chatCache.get(chatId,id-> {
            try {
                return ClientUtils.getChat(client,id);
            } catch (InterruptedException e) {
                return null;
            }
        });
    }

    public void getChatCallBack(long chatId, MessageCallBack<TdApi.Chat>  consumer) {
        TdApi.Chat chat = chatCache.getIfPresent(chatId);
        if (chat == null){
            Utils.withException(()->{
                ClientUtils.getChatCallBack(this.getClient(), chatId, new MessageCallBack<TdApi.Chat>() {
                    @Override
                    public void accept(TdApi.Chat message) {
                        chatCache.put(chatId, message);
                        consumer.accept(message);
                    }

                    @Override
                    public void error(TdApi.Error error) {
                        consumer.error(error);
                    }
                });
            });
        }else {
            consumer.accept(chat);
        }
    }

    public TdApi.SecretChat getSecretChat(long id) {
        return secretCache.getIfPresent(id);
    }

    public boolean removeChat(long id) {
         chatCache.invalidate(id);
         return true;
    }

    public Map<Long, TdApi.Chat> getAllChats() {
        return chatCache.asMap();
    }

    public TdApi.User getUser(long userId) {
        return getUser(userId,false);
    }

    public TdApi.User getUser(long userId,boolean netWork) {
        if (!netWork) {
            return  userCache.getIfPresent(Long.valueOf(userId));
        }
        return userCache.get(Long.valueOf(userId),id-> {
            try {
                return ClientUtils.getUser(client,id);
            } catch (InterruptedException e) {
                return null;
            }
        });
    }

    public void getUserCallBack(long userId,MessageCallBack<TdApi.User> callBack) {
        TdApi.User user = userCache.getIfPresent(userId);
       if (user == null) {
           ClientUtils.getUserCallBack(
                   getClient(),
                   userId,
                   new MessageCallBack<TdApi.User>() {
                       @Override
                       public void accept(TdApi.User user1) {
                           userCache.put(userId, user1);
                           callBack.accept(user1);
                       }

                       @Override
                       public void error(TdApi.Error error) {
                            callBack.error(error);
                       }
                   }
           );
       }else {
           callBack.accept(user);
       }
    }

    public TdApi.UserFullInfo getUserFullInfo(long id) {
        return userFullInfoCache.getIfPresent(id);
    }

    public void addUser(TdApi.User user) {
        userCache.put(user.id,user);
    }
    public void addUserFullInfo(Long userId,TdApi.UserFullInfo userFullInfo) {
        userFullInfoCache.put(userId,userFullInfo);
    }
    public boolean removeUser(long id) {
         userCache.invalidate(id);
        return true;
    }

    public TdApi.User getMe(){
        return me;
    }
    private void checkRunning() {
        synchronized (isRunning){
            if (isRunning) {
                throw new IllegalStateException("Bot is already running");
            }
        }
    }

    public BotConfiguration getConfiguration() {
        return configuration;
    }

    private class GetMeHandler implements Listener<LoginEvent.LoginSuccessEvent> , Client.ResultHandler{
        private int retryCount = 3;
        public GetMeHandler() {
        }

        @Override
        public void onResult(TdApi.Object object) {
            if (object.getConstructor() == TdApi.User.CONSTRUCTOR)
                Bot.this.me = (TdApi.User) object;
            else if (retryCount > 0){
                retryCount--;
                onListener(null);
            }

        }


        @Override
        public void onListener(LoginEvent.LoginSuccessEvent event) {
            getClient()
                    .send(
                            new TdApi.GetMe(),this
                    );
        }
    }

}


