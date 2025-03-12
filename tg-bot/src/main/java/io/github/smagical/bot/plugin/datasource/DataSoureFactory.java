package io.github.smagical.bot.plugin.datasource;

import io.github.smagical.bot.TgConfiguration;
import org.apache.shardingsphere.driver.api.yaml.YamlShardingSphereDataSourceFactory;
import org.apache.tools.ant.filters.StringInputStream;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class DataSoureFactory {
    public static DataSource getDataSource(TgConfiguration configuration) throws IOException, SQLException {
        if (configuration.getDataSource().getDataSourceType() == 0){
            if (configuration.getDataSource().getSharedingYmlPath() == null){
                throw new RuntimeException("not found sharedingYmlPath");
            }

            InputStream in = null;
            String yaml =  configuration.getDataSource().getSharedingYmlPath();
            if (yaml .startsWith("classpath:")){
                in =  DataSoureFactory.class.getClassLoader().getResourceAsStream(
                        configuration.getDataSource().getSharedingYmlPath().substring("classpath:".length())
                );
            }else if (yaml .startsWith("string:")){
                in = new StringInputStream(yaml.substring("string:".length()));
            }else
                in = new FileInputStream(configuration.getDataSource().getSharedingYmlPath());
            return YamlShardingSphereDataSourceFactory.createDataSource(in.readAllBytes());
        }else if (configuration.getDataSource().getDataSourceType() == 1){
            PGSimpleDataSource simpleDataSource = new PGSimpleDataSource();
            simpleDataSource.setUrl(configuration.getDataSource().getDataSourceUrl());
            simpleDataSource.setUser(configuration.getDataSource().getUsername());
            simpleDataSource.setPassword(configuration.getDataSource().getPassword());
            return simpleDataSource;
        }else {
            return configuration.getDataSource().getDataSourceSupplier().get();
        }
    }
}

