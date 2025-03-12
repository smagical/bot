package io.github.smagical.bot.plugin.handler.command;

import io.github.smagical.bot.plugin.SmagicalTgPlugin;
import io.github.smagical.bot.plugin.util.SegUtil;
import io.github.smagical.bot.tg.util.ClientUtils;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.util.*;

public class BotCommand implements CommandHandler{

    public static final String BOT_ONILEN_PREFIX_KEY = "tg_bot:bot:config:online:";

    private List<CommandInfo> commandInfoList  = new ArrayList<>();
    private SmagicalTgPlugin plugin;
    private Timer timer = new Timer();
    private TimerTask timerTask;

    public BotCommand(SmagicalTgPlugin plugin) {
        this.plugin = plugin;
        CommandInfo commandInfo = CommandInfo.builder()
                .type(CommandType.ALL)
                .permission(Permission.ADMIN)
                .description("/bot_online")
                .cmd("/bot_online")
                .function(this::getOnline)
                .build();
         commandInfoList.add(commandInfo);

    }

    private void getOnline(CommandParam commandParam) {
        if ("singleton".equalsIgnoreCase(plugin.getConfiguration().getModel())){
            ClientUtils.sendTextMessage(
                    plugin.getBot().getClient(),
                    commandParam.getChatId(),
                    plugin.getConfiguration().getModel() + "not support"
            );
            return;
        }
        Iterable<String> iterator = plugin.getRedissonClient().getKeys()
                .getKeys(
                        KeysScanOptions.defaults().limit(1000).pattern(BOT_ONILEN_PREFIX_KEY+"*")
                );
        List<String> keys = new ArrayList<>();
        iterator.forEach(key -> {keys.add(key);});
        Map<String,String> map = plugin.getRedissonClient().getBuckets()
                .<String>get(keys.toArray(new String[keys.size()]));
        List<String> messages = new ArrayList<>();
        int count = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            messages.add(String.format("%d.  %s: %s\n",count++ ,entry.getKey().substring(BOT_ONILEN_PREFIX_KEY.length()), entry.getValue()));
        }
        messages.add(String.format("\n\n%d online bot",count));
        ClientUtils.sendTextMessage(
                plugin.getBot().getClient(),
                commandParam.getChatId(),
                SegUtil.concat(messages,"")
        );
    }

    public synchronized void reportOnline(){
        if (timerTask != null){
            return;
        }
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                Date startDate = new Date();
                TimeZone tz = TimeZone.getTimeZone("GMT+8");
                startDate.setTime(startDate.getTime() + tz.getRawOffset());
                plugin.getRedissonClient()
                        .getBucket(BOT_ONILEN_PREFIX_KEY+plugin.getConfiguration().getInstanceId())
                        .set(startDate.toString(), Duration.ofMinutes(5l));
            }
        };
        timer.scheduleAtFixedRate(
                timerTask, 0l,Duration.ofMinutes(3l).toMillis()
        );
        Runtime.getRuntime().addShutdownHook(
               new Thread(()->{ timerTask.cancel();})
        );
    }

    @Override
    public boolean handler(CommandParam commandParam) {
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
