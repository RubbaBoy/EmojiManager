package com.uddernetworks.emojimanager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GUIUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(GUIUtils.class);

    public static void loadScene(Stage stage, String fxml, String... stylesheets) {
        try {
            FXMLLoader loader = new FXMLLoader(stage.getClass().getResource(fxml));
            loader.setController(stage);
            Parent root = loader.load();
            var scene = new Scene(root);
            for (var stylesheet : stylesheets) {
                scene.getStylesheets().add(stylesheet);
            }
            root.setStyle("-fx-background-color: #F9F9F9;");
            new JMetro(root, Style.LIGHT);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.error("Error loading scene!", e);
        }
    }

}
