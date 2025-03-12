package io.github.smagical.bot.tg.listener.plugin;

import io.github.smagical.bot.bus.MessageDispatch;
import io.github.smagical.bot.event.Event;
import io.github.smagical.bot.listener.Listener;
import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.model.Plugin;
import io.github.smagical.bot.tg.model.PluginEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PluginDispatch extends MessageDispatch implements Listener {

    private Bot bot;
    private HashMap<PluginEntity,Plugin> plugins;
    private ExecutorService executorService;

    public PluginDispatch(Bot bot) {
        this.bot = bot;
        this.plugins = HashMap.newHashMap(0);
        String core = bot.getConfiguration().getProperty("plugin.executor.core");
        if (core == null) {
            core = String.valueOf(Runtime.getRuntime().availableProcessors()/2);
        }
        int coreNum = Integer.parseInt(core);
        if (coreNum < 1) {
            coreNum = 1;
        }
        this.executorService = Executors.newFixedThreadPool(coreNum);
    }

    @Override
    public boolean support(Event event) {
        return true;
    }

    @Override
    public void onListener(Event event) {
        for (Plugin plugin : plugins.values()) {
            executorService.submit(()->{
                try {
                    plugin.onHandle(event);
                }catch (Exception e) {

                }
            });
        }
    }

    public synchronized void addPlugin(PluginEntity pluginEntity) {
        try {
            this.plugins.put(pluginEntity,loadPlugin(pluginEntity));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public synchronized void removePlugin(PluginEntity pluginEntity) {
        Plugin plugin;
        if ((plugin = plugins.remove(pluginEntity))!=null) {
            plugin.destroy();
        }
    }

    private Plugin loadPlugin(PluginEntity pluginEntity) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (pluginEntity.getClz() == null) return null;
        if (!Plugin.class.isAssignableFrom(pluginEntity.getClz())) return null;
        Plugin plugin =  (Plugin) pluginEntity.getClz().getDeclaredConstructor().newInstance();
        plugin.init(this.bot);
        return plugin;
    }

    @Override
    public int getOrder() {
        return Listener.super.getOrder();
    }
}
