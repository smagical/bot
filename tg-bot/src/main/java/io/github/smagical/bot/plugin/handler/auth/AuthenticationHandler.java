package io.github.smagical.bot.plugin.handler.auth;

import io.github.smagical.bot.plugin.MessageInfo;
import io.github.smagical.bot.plugin.handler.PluginHandler;

public interface AuthenticationHandler extends PluginHandler {

      boolean authenticate( MessageInfo messageInfo);
}
