package io.github.smagical.bot.plugin.handler.message;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.datasource.cache.CacheManger;
import io.github.smagical.bot.plugin.datasource.model.TgMessage;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.util.ClientUtils;
import org.drinkless.tdlib.TdApi;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public  class MessageReplyHandler implements MessageContentHandler{
    public final static Integer MESSAGE_LENGTH = 30;

    //0 HANLP 1 SPACE
    public final  static Integer SPLIT_FUNTION = 0;

    private SmagicalTgPlugin plugin;
    public MessageReplyHandler(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean supports(Bot.LoginType loginType) {
        return true;
    }

    @Override
    public void updateHandler(MessageInfo messageInfo) {
        sendReply(messageInfo,true);
    }

    @Override
    public void handler( MessageInfo messageInfo) {
        sendReply(messageInfo,false);
    }

    private void sendReply(MessageInfo messageInfo , boolean update){

        TgMessageHelper messageHelper = getQuery(plugin, messageInfo);
        if (messageHelper == null) {
            return;
        }
        List<String> messages = new ArrayList<>();
        HashMap<Integer, String> messageLink = new HashMap<>();
        int messageLength = plugin.getConfiguration().getOrDefault("message-length",MESSAGE_LENGTH);
        int count = 0;
        for (TgMessage message : messageHelper.getMessages()) {
            int len = Math.min(message.getMessage().length(), messageLength);
            messages.add(String.format("%d. ",count));
            messageLink.put(messages.size(),message.getLink());
            messages.add(message.getMessage().substring(0, len));
            messages.add("\n");
            count++;
        }


        TdApi.ReplyMarkup replyMarkup = null;
        if (plugin.getBot().getLoginType() == Bot.LoginType.BOT){
            TdApi.ReplyMarkupInlineKeyboard keyboard = new TdApi.ReplyMarkupInlineKeyboard();
            keyboard.rows = createInlineKeyboardButton(messageHelper);
            replyMarkup = keyboard;
            if (messages.isEmpty()){
                messages.add("not found");
            }
        }else {
            messages.add(
                    String.format("\n%d/%d\n 共%d条",messageHelper.getPage(),messageHelper.getLastPage(),messageHelper.getTotal())
            );
        }
        if (update){
            ClientUtils.editSendTextByTextUrlType(
                    plugin.getBot().getClient(),
                    messageInfo.getChatId(),
                    messageInfo.getMessageId(),
                    messages.toArray(new String[messages.size()]),
                    messageLink,
                    replyMarkup
            );
        }
        else  {
            TdApi.InputMessageReplyToMessage replyTo =
                    new TdApi.InputMessageReplyToMessage();
            replyTo.messageId = messageInfo.getMessageId();

            ClientUtils.sendTextByTextUrlType(
                    plugin.getBot().getClient(),
                    messageInfo.getChatId(),
                    messages.toArray(new String[messages.size()]),
                    messageLink,
                    replyTo,
                    replyMarkup
            );
        }
    }

    private TdApi.InlineKeyboardButton[][] createInlineKeyboardButton(TgMessageHelper messageHelper) {
        List<TdApi.InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(createInlineKeyboardButton(
                        "首页",
                        String.format("%s %d %d %d",
                                messageHelper.getQuery(),
                                messageHelper.getFristPage(),
                                messageHelper.getPageSize(),
                                messageHelper.getPage())
                )
        );
        if (messageHelper.hasPre()){
            buttons.add(createInlineKeyboardButton(
                            "上一页",
                            String.format("%s %d %d %d",
                                    messageHelper.getQuery(),
                                    messageHelper.prevPage(),
                                    messageHelper.getPageSize(),
                                    messageHelper.getPage())
                    )
            );
        }
        buttons.add(createInlineKeyboardButton(
                        String.format("%s/%s",messageHelper.getPage(),messageHelper.getLastPage()),
                        String.format("%s %d %d %d",
                                messageHelper.getQuery(),
                                messageHelper.getPage(),
                                messageHelper.getPageSize(),
                                messageHelper.getPage())
                )
        );
        if (messageHelper.hasNext()){
            buttons.add(createInlineKeyboardButton(
                            "下一页",
                            String.format("%s %d %d %d",
                                    messageHelper.getQuery(),
                                    messageHelper.nextPage(),
                                    messageHelper.getPageSize(),
                                    messageHelper.getPage())
                    )
            );
        }
        buttons.add(createInlineKeyboardButton(
                        "尾页",
                        String.format("%s %d %d %d",
                                messageHelper.getQuery(),
                                messageHelper.getLastPage(),
                                messageHelper.getPageSize(),
                                messageHelper.getPage())
                )
        );
        return new TdApi.InlineKeyboardButton[][]{buttons.toArray(new TdApi.InlineKeyboardButton[buttons.size()])};
    }

    private TdApi.InlineKeyboardButton createInlineKeyboardButton(String text,String data){
        TdApi.InlineKeyboardButton button = new TdApi.InlineKeyboardButton();
        TdApi.InlineKeyboardButtonTypeCallback callback = new TdApi.InlineKeyboardButtonTypeCallback();
        callback.data = data.getBytes(StandardCharsets.UTF_16);
        button.text = text;
        button.type = callback;
        return button;
    }

     protected TgMessageHelper getQuery(SmagicalTgPlugin plugin, MessageInfo messageInfo){

         return withSQLAndNumCatchFuture(()->{
             TgMessageHelper messageHelper = toTgMessageHelper(messageInfo);
             int segType = plugin.getConfiguration().getOrDefault("segType",SPLIT_FUNTION);
             if (segType > 1) segType = SPLIT_FUNTION;
             List<TgMessage> result = null;
             if (segType  == 0){
                messageHelper.setMessages(
                        DbUtil.TgMessageDb.selectTgMessageByHanlp(plugin.getDataSource(),messageHelper.getQuery(),messageHelper.getPageSize(),messageHelper.getOffset())
                );
                messageHelper.setTotal(getQueryTotal(plugin.getDataSource(), messageHelper.getQuery(), 0));
             }else {
                 messageHelper.setMessages(
                         DbUtil.TgMessageDb.selectTgMessagePageByAll(plugin.getDataSource(),messageHelper.getQuery(),messageHelper.getPageSize(),messageHelper.getOffset())
                 );
                 messageHelper.setTotal(getQueryTotal(plugin.getDataSource(), messageHelper.getQuery(), 1));
             }
            return messageHelper;
         }, plugin.getBot().getClient(), messageInfo.getChatId(), "message query");

     }


     private final static String QUERY_TOTAL_CACHE_NAME = "message_query_total";
     private final static String QUERY_TOTAL_PREFIX = "tg_bot:query:query_total:";
     protected int getQueryTotal(DataSource dataSource, String query, int segType) throws SQLException {

         CacheManger.Cache<String,Integer> cache;
         if ("singleton".equalsIgnoreCase(plugin.getConfiguration().getModel().strip())){
             cache = CacheManger.getCacheOrDefault(QUERY_TOTAL_CACHE_NAME,()->{
                 return new CacheManger.CaffeineCache<String,Integer>(
                         Caffeine.newBuilder()
                                 .expireAfterWrite(Duration.ofMinutes(5l))
                                 .softValues()
                                 .build()
                 );
             });
         }else {
             cache = CacheManger.getCacheOrDefault(QUERY_TOTAL_CACHE_NAME,()->{
                 return new CacheManger.RedisCache<Integer>(
                         plugin.getRedissonClient(), QUERY_TOTAL_PREFIX,Duration.ofMinutes(5l)
                 );
             });
         }
         String chaheKey = query+"_"+segType;
         Integer res = null;
         res = cache.get(chaheKey);

         if (res != null){
             return res.intValue();
         }

         if (segType  == 0){
             res =  DbUtil.TgMessageDb.selectTgMessageCountByHanlp(dataSource,query);
         }else {
             res =  DbUtil.TgMessageDb.selectTgMessageCountByAll(dataSource,query);

         }
         cache.put(chaheKey,res);
         return  res.intValue();
     }


}
