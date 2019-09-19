package com.uddernetworks.emojimanager.tabs.backups;

import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.tabs.GUITab;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class Backups extends Stage implements GUITab {

    @FXML
    private FlowPane backupContent;

//    @FXML
//    private ListView<String> list;

    private Pane cachedPane;
    private EmojiManager emojiManager;

    public Backups(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    @Override
    public String getFile() {
        return "backups.fxml";
    }

    @Override
    public CompletableFuture<Pane> getCachedPane() throws IOException {
        if (cachedPane != null) return CompletableFuture.completedFuture(cachedPane);
        return CompletableFuture.completedFuture(cachedPane = getPane());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        CompletableFuture.runAsync(() -> {
//            var directory =
//        });

//        list.getItems().addAll("One", "Two");

        backupContent.getChildren().add(new BackupSlot("My Backup", null, System.currentTimeMillis(), 1500000, 2051, false).getPane());
    }
}
