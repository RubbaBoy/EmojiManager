package com.uddernetworks.emojimanager;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class NavigationController extends Stage {

    private static Logger LOGGER = LoggerFactory.getLogger(NavigationController.class);

    @FXML
    private ListView<String> menuList;

    private EmojiManager emojiManager;

    public NavigationController(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;

        GUIUtils.loadScene(this, "/Navigation.fxml", "/menu.css");

        menuList.setItems(new ObservableListWrapper<>(Arrays.asList("Emojis", "Servers", "Backups", "Settings")));

        Platform.runLater(() -> {
            var bounds = menuList.lookup(".list-cell").getBoundsInLocal();
            menuList.setPrefHeight(2 + menuList.getItems().size() * bounds.getHeight());
        });
    }
}
