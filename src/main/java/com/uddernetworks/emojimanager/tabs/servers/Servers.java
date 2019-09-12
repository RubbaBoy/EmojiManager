package com.uddernetworks.emojimanager.tabs.servers;

import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.tabs.GUITab;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class Servers extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Servers.class);

    @FXML
    private FlowPane serverContent;

    private Pane cachedPane;
    private EmojiManager emojiManager;
    private List<ServerSlot> serverSlots;
    private BiConsumer<Long, Boolean> onServerEnableToggle;

    public Servers(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    @Override
    public String getFile() {
        return "servers.fxml";
    }

    @Override
    public Pane getCachedPane() throws IOException {
        if (cachedPane != null) return cachedPane;
        return (cachedPane = getPane());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing server GUI...");
        serverContent.getStylesheets().add("servers.css");
        List<Long> servers = emojiManager.getConfigManager().getConfig().get("servers");

        onServerEnableToggle = (serverId, enabled) -> {
            if (enabled) {
                servers.add(serverId);
            } else {
                servers.remove(serverId);
            }
            emojiManager.initEmojis();
            layoutServers();
        };

        var slots = new ArrayList<ServerSlot>();
        var jda = emojiManager.getJda();
        jda.getGuilds().stream().filter(guild -> guild.getOwnerIdLong() == jda.getSelfUser().getIdLong()).forEach(guild -> {
            var slot = new ServerSlot(guild.getName(), guild.getIdLong(), guild.getEmotes().size(), guild.getFeatures().contains("MORE_EMOJI") ? 150 : 100, servers.contains(guild.getIdLong())).setOnEnableToggle(onServerEnableToggle);
            slots.add(slot);
        });
        serverSlots = List.copyOf(slots);
        layoutServers();
    }

    private void layoutServers() {
        serverSlots.stream()
                .sorted(Comparator.comparing(ServerSlot::getName))
                .sorted(Comparator.comparing(slot -> !slot.isEnabled()))
                .forEach(slot -> serverContent.getChildren().add(slot.getPane()));
    }
}
