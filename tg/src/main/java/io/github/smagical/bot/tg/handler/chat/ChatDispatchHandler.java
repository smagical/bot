package io.github.smagical.bot.tg.handler.chat;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.handler.DispatchHandler;
import io.github.smagical.bot.tg.handler.chat.update.*;
import io.github.smagical.bot.tg.handler.chat.update.bot.NewCallbackQueryHandler;
import io.github.smagical.bot.tg.handler.chat.update.info.*;
import io.github.smagical.bot.tg.handler.chat.update.message.ChatDraftMessageHandler;
import io.github.smagical.bot.tg.handler.chat.update.message.ChatLastMessageHandler;
import io.github.smagical.bot.tg.handler.chat.update.message.NewMessageHandler;
import io.github.smagical.bot.tg.handler.chat.update.ui.ChatBackgroundHandler;
import io.github.smagical.bot.tg.handler.chat.update.ui.ChatPositionHandler;
import io.github.smagical.bot.tg.handler.chat.update.ui.ChatThemeHandler;

public class ChatDispatchHandler extends DispatchHandler {
    public ChatDispatchHandler(Bot bot) {
        super(bot);

        //info
        addHandler(new BasicGroupHandler(bot));
        addHandler(new BasicGroupFullInfoHandler(bot));
        addHandler(new ChatDefaultDisableNotificationHandler(bot));
        addHandler(new ChatNotificationSettingsHandler(bot));
        addHandler(new ChatPermissionsHandler(bot));
        addHandler(new ChatPhotoHandler(bot));
        addHandler(new ChatTitleHandler(bot));
        addHandler(new SupergroupFullInfoHandler(bot));
        addHandler(new SupergroupHandler(bot));

        //ui
        addHandler(new ChatBackgroundHandler(bot));
        addHandler(new ChatThemeHandler(bot));

        //message
        addHandler(new ChatLastMessageHandler(bot));
        addHandler(new NewMessageHandler(bot));
        addHandler(new ChatDraftMessageHandler(bot));

        //update
        addHandler(new ChatActionBarHandler(bot));
        addHandler(new ChatAvailableReactionsHandler(bot));
        addHandler(new ChatBlockListHandler(bot));
        addHandler(new ChatHasProtectedContentHandler(bot));
        addHandler(new ChatHasScheduledMessagesHandler(bot));
        addHandler(new ChatIsMarkedAsUnreadHandler(bot));
        addHandler(new ChatIsTranslatableHandler(bot));


        addHandler(new ChatMessageAutoDeleteTimeHandler(bot));
        addHandler(new ChatMessageSenderHandler(bot));
        addHandler(new ChatPendingJoinRequestsHandler(bot));
        addHandler(new ChatPositionHandler(bot));
        addHandler(new ChatReadInboxHandler(bot));
        addHandler(new ChatReadOutboxHandler(bot));
        addHandler(new ChatReplyMarkupHandler(bot));
        addHandler(new ChatUnreadMentionCountHandler(bot));
        addHandler(new ChatUnreadReactionCountHandler(bot));

        addHandler(new ChatVideoChatHandler(bot));
        addHandler(new MessageMentionReadHandler(bot));
        addHandler(new MessageUnreadReactionsHandler(bot));
        addHandler(new NewChatHandler(bot));
        addHandler(new SecretChatHandler(bot));

        //bot
        addHandler(new NewCallbackQueryHandler(bot));
    }

}
