package com.uddernetworks.emojimanager.tabs.servers;

import impl.jfxtras.styles.jmetro.ToggleSwitchSkin;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class ServerSlot {

    private static Logger LOGGER = LoggerFactory.getLogger(ServerSlot.class);

    private StackPane pane;

    public ServerSlot(String name, int used, int total, boolean enabled) {
        pane = new StackPane();
        pane.setPrefHeight(200.0);
        pane.setPrefWidth(200.0);
        pane.setMaxHeight(200.0);
        pane.setMaxWidth(200.0);
        pane.getStyleClass().add("serverSlot");

        var circleBackground = new AnchorPane();
        circleBackground.getStyleClass().add("background");
        circleBackground.setPrefHeight(200.0);
        circleBackground.setPrefWidth(200.0);
        circleBackground.setMaxHeight(200.0);
        circleBackground.setMaxWidth(200.0);

        var outerArc = new Arc();
        outerArc.getStyleClass().add("outerArc");
        outerArc.setRadiusX(100F);
        outerArc.setRadiusY(100F);
        outerArc.setCenterX(100);
        outerArc.setCenterY(100);
        outerArc.setLength(360);
        outerArc.setStrokeWidth(1);
        outerArc.setStrokeType(StrokeType.OUTSIDE);
        outerArc.setStrokeLineCap(StrokeLineCap.ROUND);
        outerArc.setFill(null);
        outerArc.setType(ArcType.OPEN);

        var arc = new Arc();
        arc.getStyleClass().add("arc");
        arc.setRadiusX(100F);
        arc.setRadiusY(100F);
        arc.setCenterX(100);
        arc.setCenterY(100);
        arc.setStartAngle(90);
        arc.setLength((double) used / total * -360D);
        arc.setStrokeWidth(10);
        arc.setStrokeType(StrokeType.CENTERED);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.setFill(null);
        arc.setType(ArcType.OPEN);

        Group centeredArcGroup = new Group(new Rectangle(200, 200, Color.TRANSPARENT), arc);
        Group centeredOuterArcGroup = new Group(new Rectangle(200, 200, Color.TRANSPARENT), outerArc);

        pane.getChildren().add(centeredOuterArcGroup);
        pane.getChildren().add(centeredArcGroup);

        var contentContainer = new VBox();
        contentContainer.setFillWidth(true);
        contentContainer.setPrefHeight(200);
        contentContainer.setPrefWidth(200);

        var titleLabel = new Label(name);
        titleLabel.getStyleClass().add("title");
        titleLabel.setPrefWidth(200);
        titleLabel.setPrefHeight(30);
        titleLabel.setPadding(new Insets(100 - 30, 0, 0, 0));
        titleLabel.setAlignment(Pos.TOP_CENTER);
        titleLabel.setTextAlignment(TextAlignment.CENTER);


        var amountLabel = new Label(used + "/" + total);
        amountLabel.getStyleClass().add("amount");
        amountLabel.setPrefWidth(200);
        amountLabel.setPadding(new Insets(8, 0, 0, 0));
        amountLabel.setAlignment(Pos.TOP_CENTER);
        amountLabel.setTextAlignment(TextAlignment.CENTER);

        var toggle = new ToggleSwitch();
        toggle.setSelected(enabled);
        Platform.runLater(() -> {
            var thumbArea = (StackPane) toggle.getChildrenUnmodifiable().get(1);
            toggle.setPadding(new Insets(15, 0, 0, 100 - (thumbArea.getWidth() / 2D)));
        });

        contentContainer.getChildren().addAll(titleLabel, amountLabel, toggle);
        pane.getChildren().add(contentContainer);
    }

    public StackPane getPane() {
        return pane;
    }
}
