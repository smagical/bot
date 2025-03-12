package io.github.smagical.bot.tg.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class PluginEntity {
    private Long id;
    private String name;
    private String description;
    private String author;
    private String version;
    private Class clz;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PluginEntity pluginEntity)) return false;
        return Objects.equals(id, pluginEntity.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, author, version, clz);
    }
}
