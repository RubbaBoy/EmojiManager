package com.uddernetworks.emojimanager.backend;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.uddernetworks.emojimanager.backend.database.DatabaseEmoji;
import com.uddernetworks.emojimanager.config.ConfigManager;
import javafx.application.Platform;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Icon;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class EmojiManager extends ListenerAdapter {

    private static Logger LOGGER = LoggerFactory.getLogger(EmojiManager.class);

    private JDA jda;
    private long selfId;
    private DiscordWrapper discordWrapper;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private final AtomicBoolean emojisChanged = new AtomicBoolean(false);
    private FileConfig config;
    private AtomicBoolean listeningForGuild = new AtomicBoolean();
    private AtomicReference<Guild> guildJoined = new AtomicReference<>(null);

    private List<DatabaseEmoji> emojis = Collections.synchronizedList(new ArrayList<>());
    private Runnable callback;

    public void connect(Runnable callback) {
        this.callback = callback;
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
        config = configManager.getConfig();

        var serverIds = config.<List<Long>>get("servers");
        config.set("servers", serverIds.stream().filter(id -> jda.getGuildById(id) != null).collect(Collectors.toUnmodifiableList()));

        CompletableFuture.runAsync(this::initEmojis).thenRun(() -> Platform.runLater(callback));
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        if (listeningForGuild.get()) {
            LOGGER.info("Joined {}", event.getGuild().getName());
            guildJoined.set(event.getGuild());
        }
    }

    public void initEmojis() {
        emojisChanged.set(!emojis.isEmpty());
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

    public CompletableFuture<Void> uploadEmojis(List<File> files) {
        return CompletableFuture.runAsync(() -> {
            var servers = new LinkedList<>(config.<List<Long>>get("servers"));
            LOGGER.info("Servers = {}", servers);
            Guild currServer = getServer(servers);
            var currEmojiCount = currServer.getEmotes().size();

            for (File file : files) {
                var name = file.getName();
                if (name.contains(".")) name = name.substring(0, name.indexOf('.'));
                LOGGER.info("Creating emote {} on server {}", name, currServer.getId());
                try {
                    currServer.createEmote(name, Icon.from(file)).queue();
                    if (++currEmojiCount >= 50) {
                        currServer = getServer(servers);
                        currEmojiCount = currServer.getEmotes().size();
                    }
                } catch (Exception e) {
                    LOGGER.error("Error uploading emoji " + name, e);
                }
            }
            if (files.isEmpty()) emojisChanged.set(true);
        });
    }

    public void importEmojis(long server, List<File> files) {
        LOGGER.info("Importing {} emojis to {}", files.size(), server);
        var guild = jda.getGuildById(server);
        if (guild == null) {
            LOGGER.info("Guild {} not found!", server);
            return;
        }

        guild.getEmotes().parallelStream().forEach(emote -> emote.delete().complete());
        files.forEach(file -> {
            var name = file.getName();
            if (name.contains(".")) name = name.substring(0, name.indexOf('.'));
            LOGGER.info("Creating emote {} on server {}", name, server);
            try {
                guild.createEmote(name, Icon.from(file)).queue();
            } catch (IOException e) {
                LOGGER.error("Error uploading emoji " + name, e);
            }
        });
    }

    public Guild getServer(LinkedList<Long> guildQueue) {
        Guild currServer = null;
        while (currServer == null) {
            if (guildQueue.isEmpty()) return createServer();
            currServer = jda.getGuildById(guildQueue.pop());
        }
        return currServer;
    }

    public Guild createServer() {
        var lastIndex = config.<Integer>getOrElse("lastGeneratedServer", 0) + 1;
        config.set("lastGeneratedServer", lastIndex);
        var name = config.getOrElse("serverPrefix", "EM-") + lastIndex;
        LOGGER.info("Creating guild with the name of '{}'", name);
        guildJoined.set(null);
        listeningForGuild.set(true);
        jda.createGuild(name).complete();
        Guild guild;
        while ((guild = guildJoined.get()) == null) {}
        listeningForGuild.set(false);
        var servers = new ArrayList<>(config.<List<Long>>get("servers"));
        servers.add(guild.getIdLong());
        config.set("servers", servers);
        return guild;
    }

    public JDA getJda() {
        return jda;
    }

    public boolean haveEmojisChanged() {
        var res = emojisChanged.get();
        emojisChanged.set(false);
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
