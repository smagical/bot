package io.github.smagical.bot.tg.handler;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.HandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.util.HashSet;

@Slf4j
public class NoopHandler implements HandlerWrapper {

    private Bot bot;
    private HashSet<Integer> blank = blank();
    public NoopHandler(Bot bot) {
        this.bot = bot;
    }


    private final static NoopHandler handler = new NoopHandler(null);
    @Override
    public void onResult(TdApi.Object object) {
        if (!blank.contains(object.getConstructor()))
            log.debug("\n{}",object.toString());
    }

    public static NoopHandler getInstance() {
        return handler;
    }

    @Override
    public int[] support() {
        return new int[]{0};
    }

    @Override
    public Bot getBot() {
        return this.bot;
    }

    private HashSet<Integer> blank(){
        HashSet<Integer> list = new HashSet<>();
        list.add(TdApi.UpdateAttachmentMenuBots.CONSTRUCTOR);
        list.add(TdApi.UpdateChatThemes.CONSTRUCTOR);
        list.add(TdApi.UpdateChatTheme.CONSTRUCTOR);
        list.add(TdApi.UpdateActiveEmojiReactions.CONSTRUCTOR);
        list.add(TdApi.UpdateDiceEmojis.CONSTRUCTOR);
        list.add(TdApi.UpdateDefaultBackground.CONSTRUCTOR);
        list.add(TdApi.UpdateProfileAccentColors.CONSTRUCTOR);
        list.add(TdApi.UpdateAccentColors.CONSTRUCTOR);
        list.add(TdApi.UpdateAnimationSearchParameters.CONSTRUCTOR);
        list.add(TdApi.UpdateAvailableMessageEffects.CONSTRUCTOR);
        return list;
    }
}
