package com.uddernetworks.emojimanager;

import com.uddernetworks.emojimanager.backend.EmojiManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class MainGUI extends Application {

    private static Logger LOGGER = LoggerFactory.getLogger(MainGUI.class);

    private EmojiManager emojiManager;

    public static void main(String[] args) {
        launch(args);
    }

    public MainGUI() {
        LOGGER.info("Constructor!");
    }

    @Override
    public void start(Stage primaryStage) {
        CompletableFuture.runAsync(() -> {
            (emojiManager = new EmojiManager()).connect(() -> {
                Platform.runLater(() -> {
                    new NavigationController(this, emojiManager);
                });
            });
        })
        .exceptionally(t -> {
            LOGGER.error("Error starting", t);
            return null;
        });
    }
}
