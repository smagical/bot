package io.github.smagical.bot.tg.handler.user.authorization.state;

import io.github.smagical.bot.tg.Bot;
import io.github.smagical.bot.tg.BotConfig;
import io.github.smagical.bot.tg.BotConfiguration;
import io.github.smagical.bot.tg.handler.base.RetryBaseHandlerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Slf4j
public class WaitTdlibParametersHandler extends RetryBaseHandlerWrapper {


    public WaitTdlibParametersHandler(Bot bot) {
        super(bot);
    }

    @Override
    public int[] support() {
        return new int[]{TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR};
    }

    @Override
    protected void error(TdApi.Error error) {
        getBot().send(
                new AuthorizationStateDispatchHandler.LoginFailureEvent(error)
        );
    }

    @Override
    public void handle(TdApi.Object object) {
        TdApi.SetTdlibParameters request = new TdApi.SetTdlibParameters();
        BotConfiguration.TdlibConfiguration tdlibConfiguration = getBot().getConfiguration().getTdlib();
        request.apiId = tdlibConfiguration.getApiId();
        request.apiHash = tdlibConfiguration.getApiHash();
        request.applicationVersion = tdlibConfiguration.getApplicationVersion();
        request.deviceModel = System.getProperty("os.name","未知");
        request.systemLanguageCode = tdlibConfiguration.getLanguageCode();
        request.useTestDc = tdlibConfiguration.isUseTest();
        request.useChatInfoDatabase = true;
        request.useFileDatabase = true;
        request.useSecretChats = true;
        request.databaseDirectory = BotConfig.getInstance().getDbBase(getBot().getBotId());
        File file = new File(request.databaseDirectory);
        if (file.isFile()) {
            file.delete();
        }
        if (!file.exists()) {
            file.mkdirs();
        }
        request.databaseEncryptionKey = tdlibConfiguration.getDatabaseEncryptionKey().getBytes(StandardCharsets.UTF_8);
        getBot().getClient().send(request,this::onResult);

    }

}
