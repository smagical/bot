package io.github.smagical.bot.plugin;

import io.github.smagical.bot.tg.util.ClientUtils;

public class MessageInfo {
    private long messageId;
    private String messageText;
    private long chatId;
    private long userId;
    private boolean isCommand = false;
    private boolean senderIsBot = false;
    private ChatType chatType;
    private boolean TgLoginForBot;

    public static enum ChatType {
        USER,
        CHAT,
        ADMIN
    }

    public MessageInfo() {
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        if (messageText == null) messageText = "";
        this.messageText = messageText.strip();
        if (messageText.endsWith(ClientUtils.END)) senderIsBot = true;
        if (messageText.startsWith("/")) isCommand = true;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isCommand() {
        return isCommand;
    }

    public void setCommand(boolean command) {
        isCommand = command;
    }

    public boolean isBot() {
        return TgLoginForBot;
    }

    public void setBot(boolean bot) {
        TgLoginForBot = bot;
    }

    public ChatType getChatType() {
        return chatType;
    }

    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }

    public boolean isSenderIsBot() {
        return senderIsBot;
    }

    public void setSenderIsBot(boolean senderIsBot) {
        this.senderIsBot = senderIsBot;
    }
}
