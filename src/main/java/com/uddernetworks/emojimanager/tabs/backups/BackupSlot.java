package com.uddernetworks.emojimanager.tabs.backups;

import com.uddernetworks.emojimanager.tabs.servers.ServerSlot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiConsumer;

public class BackupSlot  {

    private static Logger LOGGER = LoggerFactory.getLogger(ServerSlot.class);

    private StackPane pane;
    private boolean selected;
    private BiConsumer<File, Boolean> onSelectedToggle;
    private String name;

    public BackupSlot(String name, File file, long date, long size, int emojis, boolean selected) {
        this.name = name;
        this.selected = selected;
        pane = new StackPane();
        pane.setPrefHeight(250);
        pane.setPrefWidth(250);
        pane.setMaxHeight(100);
        pane.setMaxWidth(100);
        pane.setMinWidth(250);
        pane.setMinHeight(100);

        var paneClasses = pane.getStyleClass();
        paneClasses.add("backupSlot");

        var contentContainer = new VBox();
        contentContainer.setFillWidth(true);
        contentContainer.setPrefWidth(250);
        contentContainer.setPrefHeight(100);

        var titleLabel = new Label(name);
        titleLabel.getStyleClass().add("title");
        titleLabel.setPrefWidth(200);
        titleLabel.setPrefHeight(30);
        titleLabel.setPadding(new Insets(5, 0, 0, 10));
        titleLabel.setAlignment(Pos.TOP_LEFT);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        var dateFormat = new SimpleDateFormat("MM/dd/yyyy");

        var dateLabel = new Label(dateFormat.format(new Date(date)));
        dateLabel.getStyleClass().add("otherText");
        dateLabel.setPrefWidth(200);
        dateLabel.setPrefHeight(30);
        dateLabel.setPadding(new Insets(-3, 0, 0, 10));
        dateLabel.setAlignment(Pos.TOP_LEFT);
        dateLabel.setTextAlignment(TextAlignment.CENTER);

        var sizeLabel = new Label(readableFileSize(size));
        sizeLabel.getStyleClass().add("sizeText");
        sizeLabel.setPrefWidth(100);
        sizeLabel.setPrefHeight(30);
        sizeLabel.setPadding(new Insets(-6, 0, 0, 10));
        sizeLabel.setAlignment(Pos.TOP_LEFT);
        sizeLabel.setTextAlignment(TextAlignment.CENTER);

        var countLabel = new Label(emojis + " Emojis");
        countLabel.getStyleClass().add("countText");
        countLabel.setPrefHeight(30);
        countLabel.setPadding(new Insets(-6, 0, 0, 0));
        countLabel.setAlignment(Pos.TOP_LEFT);
        countLabel.setTextAlignment(TextAlignment.CENTER);

        var bottomText = new HBox(sizeLabel, countLabel);
        bottomText.setPrefWidth(250);
        bottomText.setAlignment(Pos.TOP_LEFT);

        var checkBox = new CheckBox();
        checkBox.setAlignment(Pos.TOP_RIGHT);
        checkBox.setPadding(new Insets(0, 0, 75 - 15, 225 - 15));
        checkBox.setSelected(selected);
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.selected = newValue;
            if (onSelectedToggle != null) onSelectedToggle.accept(file, newValue);
        });

        pane.setOnMouseClicked(event -> {
            if (this.selected = !this.selected) {
                paneClasses.add("selected");
            } else {
                paneClasses.remove("selected");
            }

            checkBox.setSelected(this.selected);
        });

        contentContainer.getChildren().addAll(titleLabel, dateLabel, bottomText);
        pane.getChildren().addAll(contentContainer, checkBox);
    }

    public StackPane getPane() {
        return pane;
    }

    public BackupSlot setOnSelectedToggle(BiConsumer<File, Boolean> onSelectedToggle) {
        this.onSelectedToggle = onSelectedToggle;
        return this;
    }

    public boolean isSelected() {
        return selected;
    }

    public String getName() {
        return name;
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "kB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
