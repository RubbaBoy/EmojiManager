package com.uddernetworks.emojimanager.backend;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class DiscordWrapper {

    private static Logger LOGGER = LoggerFactory.getLogger(DiscordWrapper.class);

    public static void main(String[] args) {
        new DiscordWrapper().connect();
    }

    public void connect() {

        new TokenListener().getToken(token -> {
            LOGGER.info("Found token: {}", token);

            try {
                new JDABuilder(AccountType.CLIENT)
                        .setToken(token)
                        .setStatus(OnlineStatus.ONLINE)
                        .setActivity(Activity.playing("Emoji Manager"))
//                        .addEventListeners(this)
                        .build();

                Thread.sleep(10000);
            } catch (LoginException | InterruptedException e) {
                LOGGER.error("Error while initializing JDA", e);
            }
        });
    }

}
