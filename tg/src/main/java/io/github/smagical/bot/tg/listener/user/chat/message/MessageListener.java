package io.github.smagical.bot.tg.listener.user.chat.message;

import io.github.smagical.bot.event.message.MessageEvent;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageListener implements Listener<MessageEvent> {



    @Override
    public void onListener(MessageEvent event) {
//        TdApi.Message lastMessage = event.getData().getData();
//        TdApi.Message message = lastMessage.lastMessage;
//        if (message == null) return;
//
//        //todo实现 只支持text
////        if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR){
////            TdApi.MessageText messageText = (TdApi.MessageText)message.content;
////            String contentText = messageText.text.text;
////
////        }else if (message.content.getConstructor() == TdApi.MessagePhoto.CONSTRUCTOR){
////            TdApi.MessagePhoto messageText = (TdApi.MessagePhoto)message.content;
////            String contentText = messageText.caption.text;
////        }
//
//        long messageId = message.id;
//        long senderId = 0;
//        if (message.senderId.getConstructor() == TdApi.MessageSenderChat.CONSTRUCTOR){
//
//        }else if (message.senderId.getConstructor() == TdApi.MessageSenderUser.CONSTRUCTOR){
//
//        }
    }
}

