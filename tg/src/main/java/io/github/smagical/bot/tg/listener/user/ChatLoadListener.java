package io.github.smagical.bot.tg.listener.user;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.base.BaseHandlerWrapper;
import io.github.smagical.bot.event.BaseEvent;
import io.github.smagical.bot.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class ChatLoadListener implements Listener<ChatLoadListener.LoadChatEvent> {
    private final int LIMIT;
    private Bot bot;
    private boolean loadMainSuccessful = false;
    private boolean loadArchiveSuccessful = false;
    private boolean loadFolderSuccessful = false;



    public ChatLoadListener(Bot bot) {
        this.bot = bot;
        LIMIT = 100;
    }

    public ChatLoadListener(int LIMIT, Bot bot) {
        this.LIMIT = LIMIT;
        this.bot = bot;
    }

    public static class LoadChatEvent extends BaseEvent<LoadChatEvent.ChatType> {

        private static enum ChatType{
            MAIN,ARCHIVE, FOLDER,ALL
        }

        private LoadChatEvent(ChatType code) {
            super(code);
        }
        public final static LoadChatEvent MAIN = new LoadChatEvent(ChatType.MAIN);
        public final static LoadChatEvent ARCHIVE = new LoadChatEvent(ChatType.ARCHIVE);
        public final static LoadChatEvent FOLDER = new LoadChatEvent(ChatType.FOLDER);
        public final static LoadChatEvent ALL = new LoadChatEvent(ChatType.ALL);
    }

    private class Handler extends BaseHandlerWrapper {

        private LoadChatEvent.ChatType type;

        public Handler(Bot bot, LoadChatEvent.ChatType type) {
            super(bot);
            this.type = type;
        }

        @Override
        protected void onOk(TdApi.Ok object) {
            handle(null);
        }

        @Override
        protected void onError(TdApi.Error error) {
            if (error.code == 404){
                switch (type){
                    case MAIN: loadMainSuccessful = true;break;
                    case ARCHIVE: loadArchiveSuccessful = true;break;
                    case FOLDER: loadFolderSuccessful = true;break;
                }
            }else {
                log.error(error.message);
            }
        }

        @Override
        protected void handle(TdApi.Object object) {
            switch (type){
                case MAIN: if (isLoadMainSuccessful())return;break;
                case ARCHIVE: if (isLoadArchiveSuccessful()) return;break;
                case FOLDER:  if (isLoadFolderSuccessful())return;break;
            }
            TdApi.ChatList chatList = null;
            switch (type){
                case MAIN: chatList = new TdApi.ChatListMain(); break;
                case ARCHIVE: chatList = new TdApi.ChatListArchive(); break;
                case FOLDER: chatList = new TdApi.ChatListFolder(); break;
                default: break;
            }
            getBot().getClient().send(
                    new TdApi.LoadChats(chatList,LIMIT),this
            );
        }
    }



    @Override
    public void onListener(LoadChatEvent event) {
        if (bot.getLoginType() == Bot.LoginType.BOT){
            log.info("The method is not available to bots");
            return;
        }
        if (!event.getData().getData().equals(LoadChatEvent.ChatType.ALL))
            new Handler(bot,event.getData().getData()).handle(null);
        else
            for (LoadChatEvent.ChatType type : LoadChatEvent.ChatType.values()) {
                if (type != LoadChatEvent.ChatType.ALL)
                    new Handler(bot,event.getData().getData()).handle(null);
            }
    }

    public boolean isLoadMainSuccessful() {
        return loadMainSuccessful;
    }

    public boolean isLoadArchiveSuccessful() {
        return loadArchiveSuccessful;
    }

    public boolean isLoadFolderSuccessful() {
        return loadFolderSuccessful;
    }
    public boolean isLoadFolderSuccess() {
        return isLoadArchiveSuccessful() && isLoadMainSuccessful() && isLoadFolderSuccessful();
    }
}
