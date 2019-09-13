package com.uddernetworks.emojimanager.backend;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DiscordWrapper {

    private static Logger LOGGER = LoggerFactory.getLogger(DiscordWrapper.class);

    private Map<Long, String> guildNameCache = Collections.synchronizedMap(new HashMap<>());

    private JDA jda;

    public void connect(Object... listeners) {
        new TokenListener().getToken(token -> {
            LOGGER.info("Found token: {}", token);

            try {
                jda = new JDABuilder(AccountType.CLIENT)
                        .setToken(token)
                        .setStatus(OnlineStatus.ONLINE)
                        .setActivity(Activity.playing("Emoji Manager"))
                        .addEventListeners(listeners)
                        .build();
            } catch (LoginException e) {
                LOGGER.error("Error while initializing JDA", e);
            }
        });
    }

    public String getServerName(long id) {
        return guildNameCache.computeIfAbsent(id, i -> {
            var guild = jda.getGuildById(id);
            if (guild == null) return "Guild not found";
            return guild.getName();
        });
    }
}
