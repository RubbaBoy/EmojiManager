package com.uddernetworks.emojimanager.tabs.servers;

import com.uddernetworks.emojimanager.tabs.GUITab;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class Servers extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Servers.class);

    @FXML
    private FlowPane emojiContent;

    @Override
    public String getFile() {
        return "servers.fxml";
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing server GUI...");
        emojiContent.getStylesheets().add("servers.css");

        emojiContent.getChildren().add(new ServerSlot("MS Paint IDE", 69, 100, true).getPane());
    }
}
