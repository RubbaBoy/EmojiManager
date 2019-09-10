package com.uddernetworks.emojimanager.tabs;

import com.uddernetworks.emojimanager.backend.DiscordWrapper;
import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.backend.database.DatabaseEmoji;
import com.uddernetworks.emojimanager.tabs.emojis.EmojiCell;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Emojis extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Emojis.class);

    @FXML
    private FlowPane emojiContent;

    private EmojiManager emojiManager;

    public Emojis(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    @Override
    public String getFile() {
        return "emojis.fxml";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing emoji GUI...");

        var discord = emojiManager.getDiscordWrapper();
        emojiManager.getEmojis().forEach(emoji -> emojiContent.getChildren().add(new EmojiCell(discord, emoji).getPane()));
    }
}
