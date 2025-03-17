package io.github.smagical.bot.plugin.handler.command;

import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.datasource.model.TgBotInfo;
import io.github.smagical.bot.plugin.datasource.model.TgMessage;
import io.github.smagical.bot.plugin.datasource.model.TgSpider;
import io.github.smagical.bot.plugin.handler.PageHelper;
import io.github.smagical.bot.plugin.util.DbUtil;
import io.github.smagical.bot.plugin.util.ParamsUtils;
import io.github.smagical.bot.plugin.util.SegUtil;
import io.github.smagical.bot.tg.model.MessageCallBack;
import io.github.smagical.bot.tg.util.ClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.github.smagical.bot.tg.util.Utils.withException;
import static io.github.smagical.bot.tg.util.Utils.withSQLAndNumCatch;


@Slf4j
public class SpiderCommand implements CommandHandler{

    private List<CommandInfo> commandInfoList = new ArrayList<>();
    private AdFilter filter;
    private ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()>3?
                    Runtime.getRuntime().availableProcessors()/3*2 : 1
    );
    private ExecutorService linkedexecutor = Executors.newFixedThreadPool(
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
                    BlackAndWhiteListAdFilter.black.clear();
                    BlackAndWhiteListAdFilter.white.clear();
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
            TgBotInfo info = DbUtil.TgBotInfoDb.selectTgBotInfoByName(plugin.getDataSource(),"AD");
            HashSet<String> adSet = new HashSet<>();
            if (info != null) {
                adSet.addAll(Arrays.stream(info.getAttrValue().split(",")).map(String::strip).collect(Collectors.toSet()));
            }else {
                info = new  TgBotInfo();
                info.setAttrName("AD");
            }
            adSet.remove(args[0].strip());
            info.setAttrValue(SegUtil.concat(adSet.stream().toList(),","));
            DbUtil.TgBotInfoDb.insertOrUpdateTgBotInfo(plugin.getDataSource(),info);
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
            TgBotInfo info = DbUtil.TgBotInfoDb.selectTgBotInfoByName(plugin.getDataSource(),"AD");
            HashSet<String> adSet = new HashSet<>();
            if (info != null) {
                adSet.addAll(Arrays.stream(info.getAttrValue().split(",")).map(String::strip).collect(Collectors.toSet()));
            }else {
               info = new  TgBotInfo();
               info.setAttrName("AD");
            }
            adSet.add(args[0].strip());
            info.setAttrValue(SegUtil.concat(adSet.stream().toList(),","));
            DbUtil.TgBotInfoDb.insertOrUpdateTgBotInfo(plugin.getDataSource(),info);
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
                List<TgSpider> spiders =  DbUtil.TgSpiderDb.selectTgSpiderAll(plugin.getDataSource());
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
            DbUtil.TgSpiderDb.delTgSpiderById(plugin.getDataSource(), chatId);
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
            TgSpider spider = DbUtil.TgSpiderDb.selectLastTgSpiderById(plugin.getDataSource(), chatId);
            if(spider == null) {
                TdApi.Chat chat = plugin.getBot().getChat(chatId,true);
                if (chat == null) {
                    ClientUtils.sendTextMessage(
                            plugin.getBot().getClient(),
                            commandParam.getChatId(),
                            String.format("not found by chat_id %s", chatId)
                    );
                    return;
                }
                spider = new TgSpider(chatId,chat.title,0l);
                DbUtil.TgSpiderDb.insertTgSpider(plugin.getDataSource(), spider);
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

            helper.setMessages(
                    DbUtil.TgSpiderDb.selectTgSpiderAll(plugin.getDataSource(),helper.getPageSize(),helper.getPage())
            );
            helper.setTotal(
                    DbUtil.TgSpiderDb.selectTgSpiderCountAll(plugin.getDataSource())
            );
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
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    String.format(" spider %s:%s start", spider.getChatName(),spider.getChatId())
            );

            plugin.getBot().getChatCallBack(
                    spider.getChatId(),
                    new MessageCallBack<TdApi.Chat>() {
                        @Override
                        public void accept(TdApi.Chat message) {
                            withException(()->{
                                final AtomicLong  spiderLast = new AtomicLong(0);
                                final int SEND_MESSAGE_TOTAL = 1000;
                                final AtomicInteger sendCount = new AtomicInteger(0);
                                ClientUtils.getChatHistoryCallBack(
                                        plugin.getBot().getClient(),
                                        spider.getChatId(),
                                        spiderLast.get(),
                                        new MessageCallBack<TdApi.Message[]>() {
                                            private  int spiderRetryCount = 3;
                                            private  long waitSpiderLast = 0;
                                            private AtomicInteger total = new AtomicInteger(0);
                                            @Override
                                            public void accept(TdApi.Message[] messages) {
                                                log.info("{} messages {}",spider.getChatName(),messages.length);
                                                messages = Arrays.stream(messages).filter(message -> message.id != spiderLast.get()).toArray(TdApi.Message[]::new);
                                                if (messages.length == 0){
                                                    if (-- spiderRetryCount <=0 ) {
                                                        if (spider.getLastSpiderId() == 0){
                                                            spider.setLastSpiderId(waitSpiderLast);
                                                            withException(()->{
                                                                DbUtil.TgSpiderDb.updateTgSpider(
                                                                        plugin.getDataSource(),
                                                                        spider
                                                                );
                                                                ClientUtils.sendTextMessage(
                                                                        plugin.getBot().getClient(),
                                                                        commandParam.getChatId(),
                                                                        String.format(" spider %s:%s total:%s end", spider.getChatName(),spider.getChatId(),total.get())
                                                                );
                                                            });

                                                        }
                                                        return;
                                                    }
                                                    withException(()->{
                                                        ClientUtils.getChatHistoryCallBack(
                                                                plugin.getBot().getClient(),
                                                                spider.getChatId(),
                                                                spiderLast.get(),
                                                                this
                                                        );
                                                    });
                                                }

                                                boolean updateSpiderLast = false;
                                                List<TgMessage> messagesList =  new ArrayList<>();
                                                for (TdApi.Message message : messages) {
                                                    waitSpiderLast=Math.max(waitSpiderLast,message.id);
                                                    if (spiderLast.get() == 0) spiderLast.set(message.id);
                                                    else spiderLast.set(Math.min(spiderLast.get(),message.id));
                                                    TgMessage tgMessage = new TgMessage();
                                                    tgMessage.setChatId(message.chatId);
                                                    tgMessage.setId(message.id);
                                                    tgMessage.setAlbum(message.mediaAlbumId);

                                                    if (message.content.getConstructor() == TdApi.MessageText.CONSTRUCTOR){
                                                        tgMessage.setMessage(
                                                                ((TdApi.MessageText)message.content).text.text
                                                        );
                                                    }else if (message.content.getConstructor() == TdApi.MessageVideo.CONSTRUCTOR){
                                                        TdApi.MessageVideo video = (TdApi.MessageVideo)message.content;
                                                        tgMessage.setMessage(video.caption.text);
                                                    }else if (message.content.getConstructor() == TdApi.MessagePhoto.CONSTRUCTOR){
                                                        TdApi.MessagePhoto photo = (TdApi.MessagePhoto)message.content;
                                                        tgMessage.setMessage(photo.caption.text);
                                                    }

                                                    if (message.id < spider.getLastSpiderId()) {
                                                        updateSpiderLast = true;
                                                        break;
                                                    }
                                                    if (tgMessage.getMessage() == null || tgMessage.getMessage().isBlank()) {
                                                        continue;
                                                    }

                                                    if (filter.filter(tgMessage.getMessage())) {
                                                        continue;
                                                    }

                                                    messagesList.add(tgMessage);


                                                }
                                                log.info("{} ad filter messages {}",spider.getChatName(),messagesList.size());
                                                for (final TgMessage tgMessage : messagesList) {
                                                    linkedexecutor.submit(()->{
                                                        withException(()->{
                                                            if (DbUtil.TgMessageDb.exitsTgMessage(plugin.getDataSource(), tgMessage.getId(),tgMessage.getChatId())){
                                                                return;
                                                            }

                                                            ClientUtils.getMessageLinkCallBack(
                                                                    plugin.getBot().getClient(),
                                                                    tgMessage.getChatId(),
                                                                    tgMessage.getId(),
                                                                    new MessageCallBack<TdApi.MessageLink>() {
                                                                        @Override
                                                                        public void accept(TdApi.MessageLink messageLink) {
                                                                            String link = messageLink.link;
                                                                            tgMessage.setLink(link);
                                                                            withException(()->{
                                                                                DbUtil.TgMessageDb.insertTgMessage(
                                                                                        plugin.getDataSource(),
                                                                                        List.of(tgMessage)
                                                                                );
                                                                                log.debug("save {} {}  total: {}",spider.getChatName(),tgMessage,total.incrementAndGet());
                                                                                int num = total.get() / SEND_MESSAGE_TOTAL;
                                                                                if (num > sendCount.get() && sendCount.compareAndSet(num-1,num)){
                                                                                    ClientUtils.sendTextMessage(
                                                                                            plugin.getBot().getClient(),
                                                                                            commandParam.getChatId(),
                                                                                            String.format(" spider %s:%s total:%s", spider.getChatName(),spider.getChatId(),total.get())
                                                                                    );
                                                                                }
                                                                            });
                                                                        }

                                                                        @Override
                                                                        public void error(TdApi.Error error) {
                                                                            log.debug("get link {} {} error: {}",spider.getChatName(),tgMessage,error);
                                                                        }
                                                                    }
                                                            );
                                                        });
                                                    });
                                                }

                                                if (updateSpiderLast) {
                                                    spider.setLastSpiderId(waitSpiderLast);
                                                    withException(()->{
                                                        DbUtil.TgSpiderDb.updateTgSpider(
                                                                plugin.getDataSource(),
                                                                spider
                                                        );
                                                        ClientUtils.sendTextMessage(
                                                                plugin.getBot().getClient(),
                                                                commandParam.getChatId(),
                                                                String.format(" spider %s:%s total:%s end", spider.getChatName(),spider.getChatId(),total.get())
                                                        );
                                                    });

                                                }else {
                                                    withException(()->{
                                                        ClientUtils.getChatHistoryCallBack(
                                                                plugin.getBot().getClient(),
                                                                spider.getChatId(),
                                                                spiderLast.get(),
                                                                this
                                                        );
                                                    });
                                                }
                                            }

                                            @Override
                                            public void error(TdApi.Error error) {
                                                log.debug("{} {} {}",spider.getChatName(),spider.getChatId(),error);
                                            }
                                        }

                                );
                            });
                        }

                        @Override
                        public void error(TdApi.Error error) {
                            ClientUtils.sendTextMessage(
                                    plugin.getBot().getClient(),
                                    commandParam.getChatId(),
                                    String.format(" spider %s not found", spider.getChatId())
                            );
                        }
                    }
            );

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
