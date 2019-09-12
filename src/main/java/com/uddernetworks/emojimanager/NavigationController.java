package com.uddernetworks.emojimanager;

import com.sun.javafx.collections.ObservableListWrapper;
import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.tabs.emojis.Emojis;
import com.uddernetworks.emojimanager.tabs.TabItem;
import com.uddernetworks.emojimanager.tabs.servers.Servers;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class NavigationController extends Stage {

    private static Logger LOGGER = LoggerFactory.getLogger(NavigationController.class);

    @FXML
    private ListView<TabItem> menuList;

    @FXML
    private BorderPane paneContent;

    private MainGUI mainGUI;

    public NavigationController(MainGUI mainGUI, EmojiManager emojiManager) {
        this.mainGUI = mainGUI;

        GUIUtils.loadScene(this, "/Navigation.fxml", "/menu.css");

        menuList.setItems(new ObservableListWrapper<>(Arrays.asList(
                new TabItem("Emojis", new Emojis(emojiManager)),
                new TabItem("Servers", new Servers(emojiManager)),
                new TabItem("Backups", null),
                new TabItem("Settings", null))
        ));

        menuList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            LOGGER.info("Selected {}", newValue.getName());
            try {
                paneContent.setCenter(newValue.getGuiTab().getCachedPane());
            } catch (IOException e) {
                LOGGER.error("Error loading page " + newValue.getName(), e);
            }
        });

        menuList.getSelectionModel().select(1);

        Platform.runLater(() -> {
            var bounds = menuList.lookup(".list-cell").getBoundsInLocal();
            menuList.setPrefHeight(2 + menuList.getItems().size() * bounds.getHeight());
        });
    }
}
