package com.uddernetworks.emojimanager;

import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainGUI extends Application {

    private static Logger LOGGER = LoggerFactory.getLogger(MainGUI.class);

    public static void main(String[] args) {
        launch(args);
    }

    public MainGUI() {
        LOGGER.info("Constructor!");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        new NavigationController(this);
    }
}
