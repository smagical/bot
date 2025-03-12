package io.github.smagical.bot.plugin;

import io.github.smagical.bot.tg.model.PluginEntity;

public class SmagicalPluginEntity extends PluginEntity {
    public SmagicalPluginEntity() {
        setAuthor("SmagicalBot");
        setClz(SmagicalTgPlugin.class);
        setId(0l);
        setName("SmagicalBot");
        setVersion("1.0");
    }
}
