package io.github.smagical.bot.plugin.handler.command;

import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.datasource.model.TgBotInfo;
import io.github.smagical.bot.plugin.datasource.model.TgMessage;
import io.github.smagical.bot.plugin.datasource.model.TgSpider;
import io.github.smagical.bot.plugin.handler.PageHelper;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.plugin.util.ParamsUtils;
import io.github.smagical.bot.plugin.util.SegUtil;
import io.github.smagical.bot.tg.util.ClientUtils;
import org.drinkless.tdlib.TdApi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SpiderCommand implements CommandHandler{

    private List<CommandInfo> commandInfoList = new ArrayList<>();
    private AdFilter filter;
    private ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()>3?
                    Runtime.getRuntime().availableProcessors()/3*2 : 1
    );
    private SmagicalTgPlugin plugin;

    public SpiderCommand(SmagicalTgPlugin plugin) {
            this.plugin = plugin;
            CommandInfo spiderList = CommandInfo.builder()
                    .cmd("/spider_list")
                    .type(CommandType.USER)
                    .permission(Permission.ADMIN)
                    .description("/spider_list [spider list]")
                    .function(this::spiderList)
                    .build();
            CommandInfo spiderChat = CommandInfo.builder()
                    .cmd("/spider_chat")
                    .type(CommandType.USER)
                    .permission(Permission.ADMIN)
                    .description("/spider_chat chat_id [spider chat_id]")
                    .function(this::spiderChat)
                    .build();
            CommandInfo spiderDel = CommandInfo.builder()
                    .cmd("/spider_del")
                    .type(CommandType.USER)
                    .permission(Permission.ADMIN)
                    .description("/spider_del chat_id [del spider chat_id]")
                    .function(this::spiderDel)
                    .build();
            CommandInfo spiderUpdate = CommandInfo.builder()
                    .cmd("/spider_update")
                    .type(CommandType.USER)
                    .permission(Permission.ADMIN)
                    .description("/spider_update  [update all spiders]")
                    .function(this::spiderUpdate)
                    .build();
        CommandInfo spiderADList = CommandInfo.builder()
                .cmd("/spider_ad_list")
                .type(CommandType.USER)
                .permission(Permission.ADMIN)
                .description("/spider_ad_list  [add AD filter word]")
                .function(this::spiderADList)
                .build();
        CommandInfo spiderADAdd = CommandInfo.builder()
                .cmd("/spider_ad_add")
                .type(CommandType.USER)
                .permission(Permission.ADMIN)
                .description("/spider_ad_add word [add AD filter word]")
                .function(this::spiderADAdd)
                .build();
        CommandInfo spiderADDel = CommandInfo.builder()
                .cmd("/spider_ad_del")
                .type(CommandType.USER)
                .permission(Permission.ADMIN)
                .description("/spider_ad_del word  [del AD filter word]")
                .function(this::spiderADDel)
                .build();
        AdFilterImpl adFilter = new AdFilterImpl();
        adFilter.addFilter(new BlackAndWhiteListAdFilter());
        this.filter = new AdFilterImpl();
        this.commandInfoList.add(spiderList);
        this.commandInfoList.add(spiderChat);
        this.commandInfoList.add(spiderDel);
        this.commandInfoList.add(spiderUpdate);
        this.commandInfoList.add(spiderADAdd);
        this.commandInfoList.add(spiderADDel);
        this.commandInfoList.add(spiderUpdate);
        this.commandInfoList.add(spiderADList);
        this.plugin.getConfiguration()
                .addUpdateListener("AD",str->{
                    if (str == null) {
                        BlackAndWhiteListAdFilter.black.clear();
                        BlackAndWhiteListAdFilter.white.clear();
                        return;
                    }
                    Arrays.stream(str.toString().strip()
                                    .split(","))
                            .forEach(adWord->{
                                if (adWord.strip().startsWith("!")){
                                    BlackAndWhiteListAdFilter.white.add(adWord.strip().substring(1).strip());
                                }  else  {
                                    BlackAndWhiteListAdFilter.black.add(adWord.strip());
                                }
                            });
                });
    }

    private void spiderADDel( CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        if (args[0].strip().startsWith("!")){
            BlackAndWhiteListAdFilter.white.remove(args[0].strip().substring(1).strip());
        } else  {
            BlackAndWhiteListAdFilter.black.remove(args[0].strip());
        }
        withSQLAndNumCatch(()->{
            TgBotInfo info = DbUtil.selectTgBotInfoByName(plugin.getDataSource(),"AD");
            HashSet<String> adSet = new HashSet<>();
            if (info != null) {
                adSet.addAll(Arrays.stream(info.getAttrValue().split(",")).map(String::strip).collect(Collectors.toSet()));
            }else {
                info = new  TgBotInfo();
                info.setAttrName("AD");
            }
            adSet.remove(args[0].strip());
            info.setAttrValue(SegUtil.concat(adSet.stream().toList(),","));
            DbUtil.insertOrUpdateTgBotInfo(plugin.getDataSource(),info);
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format("del AD %s word successfully",args[0].strip())
            );
        },plugin.getBot().getClient(), commandParam.getChatId(),"spider_ad_del");
    }

    private void spiderADAdd(CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        if (args[0].strip().startsWith("!")){
            BlackAndWhiteListAdFilter.white.add(args[0].strip().substring(1).strip());
        }  else  {
            BlackAndWhiteListAdFilter.black.add(args[0].strip());
        }
        withSQLAndNumCatch(()->{
            TgBotInfo info = DbUtil.selectTgBotInfoByName(plugin.getDataSource(),"AD");
            HashSet<String> adSet = new HashSet<>();
            if (info != null) {
                adSet.addAll(Arrays.stream(info.getAttrValue().split(",")).map(String::strip).collect(Collectors.toSet()));
            }else {
               info = new  TgBotInfo();
               info.setAttrName("AD");
            }
            adSet.add(args[0].strip());
            info.setAttrValue(SegUtil.concat(adSet.stream().toList(),","));
            DbUtil.insertOrUpdateTgBotInfo(plugin.getDataSource(),info);
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format("add AD %s word successfully",args[0].strip())
            );
        },plugin.getBot().getClient(), commandParam.getChatId(),"spider_ad_add");

    }

    private void spiderADList( CommandParam commandParam) {
        HashSet<String> set = new HashSet<>();
        set.addAll(BlackAndWhiteListAdFilter.white.stream().map(e->"!"+e).collect(Collectors.toSet()));
        set.addAll(BlackAndWhiteListAdFilter.black);
        if (set.isEmpty()) {
            set.add("not found ad");
        }
        ClientUtils.sendTextMessage(
                plugin.getBot().getClient(),
                commandParam.getChatId(),
                SegUtil.concat(set.stream().toList(),"\n")
        );
    }

    private void spiderUpdate(CommandParam commandParam) {
            withSQLAndNumCatch(()->{
                List<TgSpider> spiders =  DbUtil.selectTgSpiderAll(plugin.getDataSource());
                for (TgSpider spider : spiders) {
                    executor.submit(() -> {
                       withSQLAndNumCatch(()->{
                           spider(commandParam,spider);
                       },plugin.getBot().getClient(), commandParam.getChatId(),String.format("spider chat id %d",spider.getChatId()));
                    });
                }
            },plugin.getBot().getClient(), commandParam.getChatId(),"spider_update ");
    }

    private void spiderDel(CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        withSQLAndNumCatch(()->{
            Long chatId = Long.parseLong(args[0]);
            DbUtil.delTgSpiderById(plugin.getDataSource(), chatId);
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format("%s spider del successful", chatId)
            );
        },plugin.getBot().getClient(), commandParam.getChatId(),"spider del");
    }

    private void spiderChat(CommandParam commandParam) {
        String[] args = commandParam.getArgs();
        ParamsUtils.CheckParamsByLen(args,1);
        withSQLAndNumCatch(()->{
            Long chatId = Long.parseLong(args[0]);
            TgSpider spider = DbUtil.selectLastTgSpiderById(plugin.getDataSource(), chatId);
            if(spider == null) {
                TdApi.Chat chat = plugin.getBot().getChat(chatId);
                if (chat == null) {
                    ClientUtils.sendTextMessage(
                            plugin.getBot().getClient(),
                            commandParam.getChatId(),
                            String.format("not found by chat_id %s", chatId)
                    );
                    return;
                }
                spider = new TgSpider(chatId,chat.title,0l);
                DbUtil.insertTgSpider(plugin.getDataSource(), spider);
            }
            final TgSpider tgSpiderTmp = spider;
            executor.submit(
                    ()->{
                       withSQLAndNumCatch(()->{
                           spider(commandParam,tgSpiderTmp);
                       },plugin.getBot().getClient(),commandParam.getChatId(),String.format("spider chat id %d",chatId));
                    }
            );

        },plugin.getBot().getClient(), commandParam.getChatId(),"spider chat_id");
    }

    private void spiderList(CommandParam commandParam) {
        withSQLAndNumCatch(()->{
            String[] args = commandParam.getArgs();
            PageHelper<TgSpider> helper = new PageHelper<>();

            if (args.length>1){
                helper.setPage(Integer.parseInt(args[0]));
            }
            if (args.length>2){
                helper.setPageSize(Integer.parseInt(args[1]));
            }

            helper = DbUtil.selectTgSpiderAll(plugin.getDataSource(),helper);
            List<String> message = new ArrayList<>();
            Set<Integer> codeIndex = new HashSet<>();
            for (TgSpider helperMessage : helper.getMessages()) {
                message.add(helperMessage.getChatName());
                message.add(": ");
                codeIndex.add(message.size());
                message.add(String.valueOf(helperMessage.getChatId()));
                message.add("\n");
            }
            message.add(String.format("\n%s/%s  共%s条\n",helper.getPage(),helper.getLastPage(),helper.getTotal()));
            ClientUtils.sendTextByCodeType(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    message.toArray(new String[message.size()]),
                    codeIndex,
                    null
            );

        },plugin.getBot().getClient(),commandParam.getChatId(),"spider_list");
    }

    private void spider(CommandParam commandParam,TgSpider spider) throws InterruptedException, SQLException, IOException {
            Long last_id = 0l;
            long spider_last = 0l;
            long space_num = 0l;
            long nums = 0l;
            long SEND_MESSAGE = 1000l;
            long send_message = SEND_MESSAGE;

            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format(" spider %s:%s start", spider.getChatName(),spider.getChatId())
            );

            while (true){
                Collection<TdApi.Message> messageCollections =   ClientUtils.getChatHistory(
                        plugin.getBot().getClient(),
                        spider.getChatId(),
                        spider_last,
                        200
                );
                if (messageCollections.isEmpty()) {
                    space_num++;
                }
                if (space_num > 10){
                    spider.setLastSpiderId(last_id);
                    DbUtil.updateTgSpider(
                            plugin.getDataSource(),
                            spider
                    );
                    ClientUtils.sendTextMessage(
                            plugin.getBot().getClient(),
                            commandParam.getChatId(),
                            String.format(" spider %s:%s total:%s end", spider.getChatName(),spider.getChatId(),nums)
                    );
                    return;
                }
                List<TgMessage> messages =  new ArrayList<>();
                for (TdApi.Message message : messageCollections) {

                    last_id = Math.max(last_id,message.id);
                    if (spider_last == 0) spider_last = message.id;
                    else spider_last =  Math.min(spider_last,message.id);

                    if (message.content.getConstructor() != TdApi.MessageText.CONSTRUCTOR) continue;


                    try {
                        if (message.id < spider.getLastSpiderId()) {
                            DbUtil.insertTgMessage(plugin.getDataSource(),messages);
                            messages.clear();
                            spider.setLastSpiderId(last_id);
                            DbUtil.updateTgSpider(
                                    plugin.getDataSource(),
                                    spider
                            );
                            ClientUtils.sendTextMessage(
                                    plugin.getBot().getClient(),
                                    commandParam.getChatId(),
                                    String.format(" spider %s:%s total:%s end", spider.getChatName(),spider.getChatId(),nums)
                            );
                            return;
                        }
                        TdApi.MessageText messageText = (TdApi.MessageText) message.content;
                        if (filter.filter(messageText.text.text)) {
                            continue;
                        }
                        if (!DbUtil.exitsTgMessage(plugin.getDataSource(), message.id,message.chatId)) {

                            TgMessage tgMessage = new TgMessage();
                            tgMessage.setChatId(message.chatId);
                            tgMessage.setId(message.id);
                            tgMessage.setAblum(message.mediaAlbumId);
                            tgMessage.setLink(ClientUtils.getMessageLink(
                                    plugin.getBot().getClient(),
                                    message.chatId,
                                    message.id
                            ));
                            tgMessage.setMessage(messageText.text.text);

                            messages.add(tgMessage);
                        }
                    }catch (Exception e){
                        int a= 1;
                    }

                }

                DbUtil.insertTgMessage(plugin.getDataSource(),messages);
                nums+=messages.size();
                send_message-=messages.size();
                messages.clear();

                if (send_message < 0){
                    ClientUtils.sendTextMessage(
                            plugin.getBot().getClient(),
                            commandParam.getChatId(),
                            String.format(" spider %s:%s nums %lld", spider.getChatName(),spider.getChatId(),nums)
                    );
                    send_message = SEND_MESSAGE;
                }
            }


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
        return Collections.unmodifiableList(commandInfoList);
    }



    public static interface AdFilter{
        boolean filter(String text);
    }
    public static class AdFilterImpl implements AdFilter{
        CopyOnWriteArrayList<AdFilter> filters = new CopyOnWriteArrayList<>();
        @Override
        public boolean filter(String text) {
           return filters.stream().anyMatch(adFilter -> adFilter.filter(text));
        }
        void addFilter(AdFilter filter){
            filters.add(filter);
        }
        void  removeFilter(AdFilter filter){
            filters.remove(filter);
        }
    }

    public static class BlackAndWhiteListAdFilter implements AdFilter{
        private final static ConcurrentSkipListSet<String> white = new ConcurrentSkipListSet<>();
        private final  static ConcurrentSkipListSet<String> black = new ConcurrentSkipListSet<>();
        @Override
        public boolean filter(String text) {
            if (white.contains(text)) {
                return false;
            }
            if (black.contains(text)) {
                return true;
            }
            return false;
        }
        void addWhite(String word){
            white.add(word);
        }
        void addBlack(String word){
            black.add(word);
        }
        void   removeWhite(String word){
            white.remove(word);
        }
        void   removeBlack(String word){
            black.remove(word);
        }

    }


}
