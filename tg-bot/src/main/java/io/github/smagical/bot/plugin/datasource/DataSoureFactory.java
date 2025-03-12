package io.github.smagical.bot.plugin.datasource;

import io.github.smagical.bot.TgConfiguration;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public class DataSoureFactory {
    public static DataSource getDataSource(TgConfiguration configuration) throws IOException, SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
       return ShardingSphereConfig.getDataSource(configuration);
    }
}

