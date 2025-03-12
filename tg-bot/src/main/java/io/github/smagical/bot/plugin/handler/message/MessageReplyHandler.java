package io.github.smagical.bot.plugin.handler.message;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.datasource.model.TgMessage;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.util.ClientUtils;
import org.drinkless.tdlib.TdApi;

import java.nio.charset.StandardCharsets;
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
    public void handler( MessageInfo messageInfo) {

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
        TdApi.InputMessageReplyToMessage replyTo =
                new TdApi.InputMessageReplyToMessage();
        replyTo.messageId = messageInfo.getMessageId();
        TdApi.ReplyMarkup replyMarkup = null;
        if (plugin.getBot().getLoginType() == Bot.LoginType.BOT){
            TdApi.ReplyMarkupInlineKeyboard keyboard = new TdApi.ReplyMarkupInlineKeyboard();
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
                    String.valueOf(messageHelper.getPage()),
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
            replyMarkup = keyboard;

        }else {
            messages.add(
                    String.format("\n%d/%d\n",messageHelper.getPage(),messageHelper.getLastPage())
            );
        }
        ClientUtils.sendTextByTextUrlType(
                plugin.getBot().getClient(),
                messageInfo.getChatId(),
                messages.toArray(new String[messages.size()]),
                messageLink,
                replyTo,
                replyMarkup
        );

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
                return DbUtil.selectTgMessageByHanlp(plugin.getDataSource(),messageHelper);
             }else {
                return DbUtil.selectTgMessagePageByAll(plugin.getDataSource(),messageHelper);
             }

         }, plugin.getBot().getClient(), messageInfo.getChatId(), "message query");

     }


}
