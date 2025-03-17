package io.github.smagical.bot.plugin.handler.command;

import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.handler.PageHelper;
import io.github.smagical.bot.tg.util.ClientUtils;
import org.drinkless.tdlib.TdApi;

import java.util.*;

import static io.github.smagical.bot.tg.util.Utils.withSQLAndNumCatch;

public class UserCommand implements CommandHandler{

    private final List<CommandInfo> commandInfoList = new ArrayList<>();
    private final SmagicalTgPlugin plugin;
    public UserCommand(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
        CommandInfo chatList = CommandInfo.builder()
                .type(CommandType.USER)
                .cmd("/user_list")
                .description("/user_list ")
                .permission(Permission.ADMIN)
                .function(this::getChatList)
                .build();
        CommandInfo userInfo = CommandInfo.builder()
                .cmd("/user_info")
                .description("/user_info [get info]")
                .type(CommandType.ALL)
                .permission(Permission.CHAT)
                .function(this::getUserInfo)
                .build();
        commandInfoList.add(chatList);
        commandInfoList.add(userInfo);
    }

    private void getUserInfo( CommandParam commandParam) {
        withSQLAndNumCatch(()->{
            TdApi.Chat chat = plugin.getBot().getChat(commandParam.getChatId(),true);
            if (chat == null) {
                return;
            }
            ClientUtils.sendTextByCodeType(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    new String[]{chat.title,": ", String.valueOf(chat.id)},
                    Set.of(2),
                    null
            );
        },plugin.getBot().getClient(),commandParam.getChatId(),"user info");
    }

    private void getChatList(CommandParam commandParam) {

        withSQLAndNumCatch(()->{
            PageHelper<TdApi.Chat> helper = new PageHelper<>();
            helper.setPage(0);
            helper.setPageSize(100000);
            String[] args = commandParam.getArgs();
            if (args.length >= 1) {
                helper.setPage(Integer.parseInt(args[0]));
            }
            if (args.length >= 2) {
                helper.setPageSize(Integer.parseInt(args[1]));
            }

            Map<Long,TdApi.Chat> chatMap = plugin.getBot().getAllChats();
            List<Long> chatList = new ArrayList<>(chatMap.keySet());
            chatList.sort(Long::compareTo);
            List<String> message = new ArrayList<>();
            Set<Integer> index = new HashSet<>();
            helper.setTotal(chatMap.size());
            int count = 0;
            for (int start = helper.getOffset(); start < Math.min(chatList.size(),helper.getOffset() + helper.getPageSize()) ; start++) {
                TdApi.Chat chat = chatMap.get(chatList.get(start));
                message.add(String.format("%d.",count++));
                message.add(chat.title);
                message.add(": ");
                index.add(message.size());
                message.add(String.valueOf(chat.id));
                message.add("\n");
            }
            message.add(String.format("\n%d/%d\n",helper.getPage(),helper.getLastPage()));
            ClientUtils.sendTextByCodeType(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    message.toArray(new String[message.size()]),
                    index,
                    null
            );

        },plugin.getBot().getClient(), commandParam.getChatId(),"getChatList");
    }


    @Override
    public boolean handler( CommandParam commandParam) {
        for (CommandInfo commandInfo : commandInfoList) {
            if (CommandHandler.testCommand(commandParam, commandInfo)) {
                commandInfo.getFunction().execute(commandParam);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<CommandInfo> getCommandList() {
        return commandInfoList;
    }
}
