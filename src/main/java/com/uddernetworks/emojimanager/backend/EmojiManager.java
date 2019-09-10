package com.uddernetworks.emojimanager.backend;

import com.uddernetworks.emojimanager.config.ConfigManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Objects;

public class EmojiManager extends ListenerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(EmojiManager.class);

    private JDA jda;
    private long selfId;
    private DiscordWrapper discordWrapper;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;

    public static void main(String[] args) {
        new EmojiManager().connect();
    }

    public void connect() {
        (configManager = new ConfigManager("config.conf")).init();
        databaseManager = new DatabaseManager(new File("database").getAbsoluteFile());

        discordWrapper = new DiscordWrapper();
        discordWrapper.connect(this);
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("Discord ready! Loading servers...");
        jda = event.getJDA();
        selfId = jda.getSelfUser().getIdLong();

        List<Long> servers = configManager.getConfig().get("servers");

        LOGGER.info("Loading {} guilds", servers.size());
        servers.stream().map(id -> jda.getGuildById(id)).filter(Objects::nonNull).forEach(guild -> {
            var emojis = guild.getEmotes();
            LOGGER.info("[{}] Emojis: {}", guild.getName(), emojis.size());
            databaseManager.addEmojis(emojis);
        });

        LOGGER.info("Done initializing!");
    }
}
