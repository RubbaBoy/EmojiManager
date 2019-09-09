package com.uddernetworks.emojimanager;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Map;
import java.util.ResourceBundle;

public class EmojiManager extends Application {

    private static Logger LOGGER = LoggerFactory.getLogger(EmojiManager.class);

    public static void main(String[] args) {
        UIDefaults uiDefaults = UIManager.getDefaults();
        uiDefaults.put("activeCaption", new javax.swing.plaf.ColorUIResource(Color.red));
        uiDefaults.put("activeCaptionText", new javax.swing.plaf.ColorUIResource(Color.red));
        JFrame.setDefaultLookAndFeelDecorated(true);

        UIManager.put("InternalFrame.activeTitleBackground", new ColorUIResource(Color.RED ));
        UIManager.put("InternalFrame.activeTitleForeground", new ColorUIResource(Color.RED));
        UIManager.put("InternalFrame.titleFont", new Font("Dialog", Font.PLAIN, 11));

        launch(args);
    }

    public EmojiManager() {
        LOGGER.info("Constructor!");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        var controller = new NavigationController(this);
    }

    private static CheckMenuItem createMenuItem (String title){
        CheckMenuItem cmi = new CheckMenuItem(title);
        cmi.setSelected(true);
        return cmi;
    }
}
