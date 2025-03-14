package io.github.smagical.bot.tg.util;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ClientUtils {
    public final static int LIMIT = 50;
    public final static int RETRY = 5;
    public final static long WAITE_TIME = 1 * 1000;
    public final static String END= "\u200c";

    public static Collection<TdApi.Message> getChatHistory(
            Client client,Long chatId,long lastMessageId,int limit
    ) throws InterruptedException {
        PriorityQueue<TdApi.Message> res =
                new PriorityQueue<TdApi.Message>(
                        (a,b)->Integer.compare(a.date,b.date)
                );
        Set<Long> dist = new HashSet<Long>();
        dist.add(lastMessageId);
        final AtomicBoolean flag = new AtomicBoolean(true);
        final AtomicInteger count = new AtomicInteger(RETRY);


        while (flag.get() && count.get() > 0) {
            int lastCount = res.size();
            CountDownLatch latch = new CountDownLatch(1);
            TdApi.GetChatHistory getChatHistory = new TdApi.GetChatHistory(
                    chatId,lastMessageId,0,limit - res.size() +1 > LIMIT?LIMIT:limit - res.size() +1,false
            );
            client.send(
                    getChatHistory,
                    new Client.ResultHandler() {
                        @Override
                        public void onResult(TdApi.Object object) {

                            switch (object.getConstructor()){
                                case TdApi.Error.CONSTRUCTOR : {
                                    flag.set(false);
                                    latch.countDown();
                                    log.error("{}",object);
                                    break;
                                }
                                case TdApi.Messages.CONSTRUCTOR : {
                                    TdApi.Messages messages = (TdApi.Messages)object;
                                    TdApi.Message[] messagesArray = messages.messages;
                                    synchronized (res){
                                        for (int i = 0; i < messagesArray.length; i++) {
                                            if (dist.contains(messagesArray[i].id)) {
                                                continue;
                                            }
                                            res.add(messagesArray[i]);
                                            dist.add(messagesArray[i].id);
                                        }
                                    }
                                    if (res.size() >= limit) {
                                        flag.set(false);
                                    }
                                    latch.countDown();
                                    break;
                                }
                            }
                        }
                    }
            );
            if (!res.isEmpty())
                lastMessageId = res.peek().id;
            latch.await(WAITE_TIME, TimeUnit.MILLISECONDS);
            if (res.size() == lastCount){
                count.decrementAndGet();
            }else {
                count.set(RETRY);
            }
        }
        while (res.size() > limit) res.poll();
        return res;
    }

    public static String getMessageLink(Client client,Long chatId,Long messageId) throws InterruptedException {
        final StringBuilder result = new StringBuilder();
        TdApi.GetMessageLink getMessageLink = new TdApi.GetMessageLink(
                chatId,messageId,0,true,false
        );
        CountDownLatch latch = new CountDownLatch(1);
        client.send(getMessageLink, new Client.ResultHandler() {

            @Override
            public void onResult(TdApi.Object object) {
                if (object.getConstructor() == TdApi.MessageLink.CONSTRUCTOR) {
                    result.append(((TdApi.MessageLink)object).link) ;
                }
                latch.countDown();
            }
        });
        latch.await(WAITE_TIME * 5, TimeUnit.MILLISECONDS);
        return result.length() == 0 ? null : result.toString();
    }

    public static TdApi.Chat getChat(Client client,Long chatId) throws InterruptedException {
        TdApi.GetChat getChat = new TdApi.GetChat(chatId);
        CountDownLatch latch = new CountDownLatch(1);
        final LinkedList<TdApi.Chat> chats = new LinkedList<>();
        chats.add(null);
        client.send(
                getChat,
                new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR) {
                            chats.addLast((TdApi.Chat)object);
                        }
                        latch.countDown();
                    }
                }
        );
        latch.await(WAITE_TIME, TimeUnit.MILLISECONDS);
        return chats.getLast();
    }

    public static TdApi.User getUser(Client client, long userId) throws InterruptedException {
        TdApi.GetUser getUser = new TdApi.GetUser(userId);
        CountDownLatch latch = new CountDownLatch(1);
        final LinkedList<TdApi.User> chats = new LinkedList<>();
        chats.add(null);
        client.send(
                getUser,
                new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        if (object.getConstructor() == TdApi.User.CONSTRUCTOR) {
                            chats.addLast((TdApi.User)object);
                        }
                        latch.countDown();
                    }
                }
        );
        latch.await(WAITE_TIME, TimeUnit.MILLISECONDS);
        return chats.getLast();
    }

    public static void setCommand(Client client,HashMap<String,String> map,TdApi.BotCommandScope scope){
        TdApi.BotCommand[] commands =
                map.entrySet().stream().map(
                        e->new TdApi.BotCommand(e.getKey(),e.getValue())
                ).toArray(TdApi.BotCommand[]::new);
        client.send(
                new TdApi.SetCommands(
                        scope,
                        "zh",
                        commands
                ),
                new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {}
                        else log.info("{}",object);
                    }
                });
    }

    public static void LeaveChat(Client client,long chatId){
        client.send(
                new TdApi.LeaveChat(
                     chatId
                ),
                new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.Object object) {
                        if (object.getConstructor() == TdApi.Ok.CONSTRUCTOR) {}
                        else log.info("{}",object);
                    }
                });
    }

    public static void sendTextMessage(Client client,Long chatId,String message){
        sendTextMessage(client,chatId,message,RETRY);
    }
    public static void sendTextMessage(Client client,Long chatId,String message,int retryCount){
        TdApi.FormattedText formattedText = new TdApi.FormattedText(addEnd(message),new TdApi.TextEntity[0]);
        send(client,chatId,formattedText,null,null,retryCount);
    }


    public static void sendTextByTextUrlType(Client client, Long chatId, String[] messages, final Map<Integer,String> link, TdApi.InputMessageReplyTo replyTo) {
        sendTextByTextUrlType(client,chatId,messages,link,replyTo,null,RETRY);
    }

    public static void sendTextByTextUrlType(Client client, Long chatId, String[] messages, final Map<Integer,String> link, TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup) {
        sendTextByTextUrlType(client,chatId,messages,link,replyTo,replyMarkup,RETRY);
    }

    public static void sendTextByTextUrlType(Client client, Long chatId, String[] messages, final Map<Integer,String> link, TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup,int retryCount) {

        sendTextByType(client, chatId, messages, link.keySet(), (a, b,c) -> {
            TdApi.TextEntity textEntity = new TdApi.TextEntity();
            textEntity.offset = a.length();
            TdApi.TextEntityTypeTextUrl textUrl = new TdApi.TextEntityTypeTextUrl();
            textUrl.url = link.get(b);
            textEntity.type = textUrl;
            textEntity.length = c.length();
            return textEntity;
        }, replyTo, replyMarkup, retryCount);
    }

    public static void sendTextByCodeType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TdApi.InputMessageReplyTo replyTo){
        sendTextByCodeType(client,chatId,messages,codeIndex,replyTo,null,RETRY);
    }

    public static void sendTextByCodeType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TdApi.InputMessageReplyTo replyTo,int  retryCount){
        sendTextByCodeType(client,chatId,messages,codeIndex,replyTo,null,retryCount);
    }

    public static void sendTextByCodeType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup){
        sendTextByCodeType(client,chatId,messages,codeIndex,replyTo,replyMarkup,RETRY);
    }

    public static void sendTextByCodeType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup,int retryCount){

        sendTextByType(client,chatId,messages,codeIndex,(a,b,c)->{
            TdApi.TextEntity textEntity = new TdApi.TextEntity();
            textEntity.offset = a.length();
            textEntity.type = new TdApi.TextEntityTypeCode();
            textEntity.length = c.length();
            return textEntity;
        },replyTo,replyMarkup,retryCount);
    }

    @FunctionalInterface
    public static interface TextEntitySupplier{
        public TdApi.TextEntity get(String alreadyMessage,Integer index,String nowMessage);
    }

    public static void sendTextByType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TextEntitySupplier textEntitySupplier ,TdApi.InputMessageReplyTo replyTo){
        sendTextByType(client,chatId,messages,codeIndex,textEntitySupplier,replyTo,null,RETRY);
    }

    public static void sendTextByType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TextEntitySupplier textEntitySupplier ,TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup){
       sendTextByType(client,chatId,messages,codeIndex,textEntitySupplier,replyTo,replyMarkup,RETRY);
    }

    public static void sendTextByType(Client client, Long chatId, String[] messages, Set<Integer> codeIndex, TextEntitySupplier textEntitySupplier ,TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup, int retryCount){
        StringBuilder message = new StringBuilder();
        List<TdApi.TextEntity>  textEntities = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            if (codeIndex.contains(i)){
                textEntities.add(textEntitySupplier.get(message.toString(),i,messages[i]));
            }
            message.append(messages[i]);
        }

        TdApi.FormattedText formattedText = new TdApi.FormattedText();
        formattedText.text = addEnd(message.toString());
        formattedText.entities = textEntities.toArray(new TdApi.TextEntity[0]);
        send(client,chatId,formattedText,replyTo,replyMarkup,retryCount);
    }


    public static void send(Client client, Long chatId, TdApi.FormattedText formattedText){
        send(client,chatId,formattedText,null,null,RETRY);
    }

    public static void send(Client client, Long chatId, TdApi.FormattedText formattedText, int retryCount){
        send(client,chatId,formattedText,null,null,retryCount);
    }

    public static void send(Client client, Long chatId, TdApi.FormattedText formattedText, TdApi.InputMessageReplyTo replyTo){
        send(client,chatId,formattedText,replyTo,null,RETRY);
    }

    public static void send(Client client, Long chatId, TdApi.FormattedText formattedText, TdApi.InputMessageReplyTo replyTo, int retryCount){
        send(client,chatId,formattedText,replyTo,null,retryCount);
    }

    public static void send(Client client, Long chatId, TdApi.FormattedText formattedText, TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup){
        send(client,chatId,formattedText,replyTo,null,RETRY);
    }

    public static void send(Client client, Long chatId, TdApi.FormattedText formattedText, TdApi.InputMessageReplyTo replyTo, TdApi.ReplyMarkup replyMarkup, int retryCount){
        TdApi.SendMessage sendMessage = new TdApi.SendMessage();
        sendMessage.chatId = chatId;
        TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText();
        formattedText.text = addEnd(formattedText.text);
        inputMessageText.text = formattedText;
        inputMessageText.linkPreviewOptions = new TdApi.LinkPreviewOptions();
        inputMessageText.linkPreviewOptions.isDisabled = true;
        sendMessage.replyTo = replyTo;
        sendMessage.replyMarkup = replyMarkup;
        sendMessage.inputMessageContent = inputMessageText;
        client.send(sendMessage, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                    log.error("{}",object);
                    if (retryCount <= 0){
                        return;
                    }
                    send(client,chatId,formattedText,replyTo,replyMarkup,retryCount-1);
                }
            }
        });
    }


    public static void editSendTextByTextUrlType(Client client, Long chatId,Long messageId , String[] messages, final Map<Integer,String> link) {
        editSendTextByTextUrlType(client,chatId,messageId,messages,link);
    }

    public static void editSendTextByTextUrlType(Client client, Long chatId,Long messageId , String[] messages, final Map<Integer,String> link,int retryCount) {
        editSendTextByTextUrlType(client,chatId,messageId,messages,link,retryCount);
    }

    public static void editSendTextByTextUrlType(Client client, Long chatId,Long messageId , String[] messages, final Map<Integer,String> link,  TdApi.ReplyMarkup replyMarkup) {
        editSendTextByTextUrlType(client,chatId,messageId,messages,link,replyMarkup,RETRY);
    }

    public static void editSendTextByTextUrlType(Client client, Long chatId,Long messageId , String[] messages, final Map<Integer,String> link,  TdApi.ReplyMarkup replyMarkup,int retryCount) {

        editSendTextByType(client, chatId,messageId, messages, link.keySet(), (a, b,c) -> {
            TdApi.TextEntity textEntity = new TdApi.TextEntity();
            textEntity.offset = a.length();
            TdApi.TextEntityTypeTextUrl textUrl = new TdApi.TextEntityTypeTextUrl();
            textUrl.url = link.get(b);
            textEntity.type = textUrl;
            textEntity.length = c.length();
            return textEntity;
        },  replyMarkup, retryCount);
    }

    public static void editSendTextByCodeType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex, TdApi.InputMessageReplyTo replyTo){
        editSendTextByCodeType(client,chatId,messageId,messages,codeIndex,null,RETRY);
    }

    public static void editSendTextByCodeType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex,int  retryCount){
        editSendTextByCodeType(client,chatId,messageId,messages,codeIndex,retryCount);
    }

    public static void editSendTextByCodeType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex,  TdApi.ReplyMarkup replyMarkup){
        editSendTextByCodeType(client,chatId,messageId,messages,codeIndex,replyMarkup,RETRY);
    }

    public static void editSendTextByCodeType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex,  TdApi.ReplyMarkup replyMarkup,int retryCount){

        editSendTextByType(client,chatId,messageId,messages,codeIndex,(a,b,c)->{
            TdApi.TextEntity textEntity = new TdApi.TextEntity();
            textEntity.offset = a.length();
            textEntity.type = new TdApi.TextEntityTypeCode();
            textEntity.length = c.length();
            return textEntity;
        },replyMarkup,retryCount);
    }

    public static void editSendTextByType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex, TextEntitySupplier textEntitySupplier ,TdApi.InputMessageReplyTo replyTo){
        editSendTextByType(client,chatId,messageId,messages,codeIndex,textEntitySupplier,null,RETRY);
    }

    public static void editSendTextByType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex, TextEntitySupplier textEntitySupplier , TdApi.ReplyMarkup replyMarkup){
        editSendTextByType(client,chatId,messageId,messages,codeIndex,textEntitySupplier,replyMarkup,RETRY);
    }

    public static void editSendTextByType(Client client, Long chatId,Long messageId , String[] messages, Set<Integer> codeIndex, TextEntitySupplier textEntitySupplier , TdApi.ReplyMarkup replyMarkup, int retryCount){
        StringBuilder message = new StringBuilder();
        List<TdApi.TextEntity>  textEntities = new ArrayList<>();
        for (int i = 0; i < messages.length; i++) {
            if (codeIndex.contains(i)){
                textEntities.add(textEntitySupplier.get(message.toString(),i,messages[i]));
            }
            message.append(messages[i]);
        }

        TdApi.FormattedText formattedText = new TdApi.FormattedText();
        formattedText.text = addEnd(message.toString());
        formattedText.entities = textEntities.toArray(new TdApi.TextEntity[0]);
        editSend(client,chatId,messageId,formattedText,replyMarkup,retryCount);
    }




    public static void editSend(Client client, Long chatId,Long messageId , TdApi.FormattedText formattedText, int retryCount){
        editSend(client,chatId,messageId,formattedText,null,retryCount);
    }

    public static void editSend(Client client, Long chatId,Long messageId , TdApi.FormattedText formattedText){
        editSend(client,chatId,messageId,formattedText,null,RETRY);
    }



    public static void editSend(Client client, Long chatId,Long messageId , TdApi.FormattedText formattedText, TdApi.ReplyMarkup replyMarkup){
        editSend(client,chatId,messageId,formattedText,null,RETRY);
    }


    public static void editSend(Client client, Long chatId,Long messageId ,TdApi.FormattedText formattedText,  TdApi.ReplyMarkup replyMarkup, int retryCount){
        TdApi.EditMessageText editSendMessage = new TdApi.EditMessageText();
        editSendMessage.chatId = chatId;
        editSendMessage.messageId = messageId;
        TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText();
        formattedText.text = addEnd(formattedText.text);
        inputMessageText.text = formattedText;
        inputMessageText.linkPreviewOptions = new TdApi.LinkPreviewOptions();
        inputMessageText.linkPreviewOptions.isDisabled = true;
        editSendMessage.replyMarkup = replyMarkup;
        editSendMessage.inputMessageContent = inputMessageText;
        client.send(editSendMessage, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.Object object) {
                if (object.getConstructor() == TdApi.Error.CONSTRUCTOR) {
                    log.error("{}",object);
                    if (retryCount <= 0){
                        return;
                    }
                    editSend(client,chatId,messageId,formattedText,replyMarkup,retryCount-1);
                }
            }
        });
    }

    private static String addEnd(String text){
        if (text == null || text.length() == 0)
            return END;
        if (!text.endsWith(END)){
            return  text+END;
        }
        return  text;
    }
}
