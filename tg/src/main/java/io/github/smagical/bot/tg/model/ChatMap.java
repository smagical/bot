package io.github.smagical.bot.tg.model;

import org.drinkless.tdlib.TdApi;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class ChatMap {
    private static final long serialVersionUID = 1L;
    private ConcurrentHashMap<Long, TdApi.SecretChat> secretChatMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, TdApi.Chat> chatMap = new ConcurrentHashMap<>();

    public TdApi.Object put(Long key,TdApi.Object value) {
        if (value instanceof TdApi.SecretChat) {
            return secretChatMap.put(key, (TdApi.SecretChat) value);
        }else {
            return chatMap.put(key, (TdApi.Chat) value);
        }

    }

    public TdApi.Object putIfAbsent(Long key, TdApi.Object value) {
        if (value instanceof TdApi.SecretChat) {
            return secretChatMap.putIfAbsent(key, (TdApi.SecretChat) value);
        }else {
            return chatMap.putIfAbsent(key, (TdApi.Chat) value);
        }
    }

    public TdApi.Object get(Long key) {
        TdApi.Object result =  chatMap.get(key);
        if (result == null) {
            result = secretChatMap.get(key);
        }
        return result;
    }

    public TdApi.Chat getChat(Long key) {
        return chatMap.get(key);
    }

    public TdApi.SecretChat getSecretChat(Long key) {
        return secretChatMap.get(key);
    }

    public boolean remove(long id) {
        if (chatMap.remove(id) != null) {
            return true;
        }else if (secretChatMap.remove(id) != null) {
            return true;
        }
        return false;
    }

    public Collection<TdApi.Chat> getAllChat() {
        return Collections.unmodifiableCollection(chatMap.values());
    }
}