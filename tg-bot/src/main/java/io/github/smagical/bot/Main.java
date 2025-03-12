package io.github.smagical.bot;

import io.github.smagical.bot.plugin.SmagicalPluginEntity;
import io.github.smagical.bot.tg.BotConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;


@Slf4j
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, SQLException {

        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream("bot.yml");
        for (String arg : args) {
            if (arg.startsWith("--config=")){
                inputStream = new FileInputStream(arg.substring("--config=".length()));
            }
        }
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(true);
        loaderOptions.setEnumCaseSensitive(false);
        Constructor constructor = new Constructor(loaderOptions);
        constructor.setPropertyUtils(new PropertyUtils(){
            @Override
            public Property getProperty(Class<?> type, String name) {
                setSkipMissingProperties(true);
                return super.getProperty(type, name);
            }
        });
        Yaml yaml = new Yaml(constructor);
        BotConfiguration botConfiguration = yaml.loadAs(inputStream, BotConfiguration.class);
        io.github.smagical.bot.tg.Bot bot = new io.github.smagical.bot.tg.Bot(botConfiguration);
        bot.addPlugin(new SmagicalPluginEntity());
        if (botConfiguration.getLoginType() == 0) {
            bot.loginByPthone(botConfiguration.getLoginNumber());
        } else if (botConfiguration.getLoginType() == 1) {
            bot.loginByOcr();
        } else
            bot.loginByBotToken(botConfiguration.getLoginToken());

    }

}

