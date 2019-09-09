package com.uddernetworks.emojimanager.tabs;

import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class Emojis extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Emojis.class);

    @Override
    public String getFile() {
        return "emojis.fxml";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initialized Emojis");
    }
}
