package com.uddernetworks.emojimanager.tabs;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface GUITab extends Initializable {
    String getFile();
    default Pane getPane() throws IOException {
        var file = getFile();
        var loader = new FXMLLoader(getClass().getClassLoader().getResource(file));
        loader.setController(this);
        Parent root = loader.load();
        Node node = root.lookup("*");
        if (!(node instanceof Pane)) throw new LoadException("Root element of " + file + " not a pane!");
        return (Pane) node;
    }
}
