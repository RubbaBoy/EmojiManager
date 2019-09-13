package com.uddernetworks.emojimanager.backend;

import com.uddernetworks.emojimanager.backend.database.DatabaseEmoji;
import com.uddernetworks.emojimanager.config.ConfigManager;
import javafx.application.Platform;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class EmojiManager extends ListenerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(EmojiManager.class);

    private JDA jda;
    private long selfId;
    private DiscordWrapper discordWrapper;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private boolean emojisChanged;

    private List<DatabaseEmoji> emojis = Collections.synchronizedList(new ArrayList<>());
    private Runnable callback;

    public static void main(String[] args) {
        new EmojiManager().connect(() -> {});
    }

    public void connect(Runnable callback) {
        this.callback = callback;
        (configManager = new ConfigManager("config.conf")).init();
        databaseManager = new DatabaseManager(new File("database").getAbsoluteFile());

        discordWrapper = new DiscordWrapper();
        discordWrapper.connect(this);
        LOGGER.info("DONE CONNECTING@");
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        LOGGER.info("Discord ready! Loading servers...");
        jda = event.getJDA();
        selfId = jda.getSelfUser().getIdLong();

        CompletableFuture.runAsync(this::initEmojis).thenRun(() -> Platform.runLater(callback));
    }

    public void initEmojis() {
        emojisChanged = !emojis.isEmpty();
        List<Long> servers = configManager.getConfig().get("servers");

        LOGGER.info("Loading {} guilds", servers.size());

        emojis.clear();
        servers.stream().map(id -> jda.getGuildById(id)).filter(Objects::nonNull).forEach(guild -> {
            var emojis = guild.getEmotes().stream().map(DatabaseEmoji::new).collect(Collectors.toList());
            this.emojis.addAll(emojis);
            LOGGER.info("[{}] Emojis: {}", guild.getName(), emojis.size());

            LOGGER.info("Removing emojis from database no longer in servers...");
            databaseManager.removeNotContaining(guild.getIdLong(), emojis);

            LOGGER.info("Adding the nonexistent emojis...");
            databaseManager.addEmojis(emojis);
        });

        LOGGER.info("Done initializing {} total emojis", emojis.size());
    }

    public JDA getJda() {
        return jda;
    }

    public boolean haveEmojisChanged() {
        var res = emojisChanged;
        emojisChanged = false;
        return res;
    }

    public List<DatabaseEmoji> getEmojis() {
        return emojis;
    }

    public DiscordWrapper getDiscordWrapper() {
        return discordWrapper;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
