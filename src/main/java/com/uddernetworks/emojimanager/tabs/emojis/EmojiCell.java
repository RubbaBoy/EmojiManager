package com.uddernetworks.emojimanager.tabs.emojis;

import com.uddernetworks.emojimanager.backend.DiscordWrapper;
import com.uddernetworks.emojimanager.backend.database.DatabaseEmoji;
import javafx.fxml.FXMLLoader;
import javafx.fxml.LoadException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EmojiCell {

    private DatabaseEmoji emoji;
    private StackPane pane;
    private boolean selected = false;
    private BiConsumer<EmojiCell, Boolean> onSelect = (e, b) -> {};

    public EmojiCell(DiscordWrapper discord, DatabaseEmoji emoji) {
        this.emoji = emoji;

        pane = new StackPane();
        pane.setPrefHeight(200.0);
        pane.setPrefWidth(200.0);

        var paneClasses = pane.getStyleClass();
        paneClasses.add("emojiCell");

        ImageView imageView1 = new ImageView(new Image(emoji.getImage()));
        imageView1.getStyleClass().add("image");
        imageView1.setPickOnBounds(true);
        imageView1.setFitWidth(140.0);
        imageView1.setFitHeight(140.0);
        imageView1.setPreserveRatio(true);

        var imageContainer = new VBox(imageView1);
        imageContainer.setPrefWidth(140);
        imageContainer.setMaxWidth(140);
        imageContainer.setPrefHeight(140);
        imageContainer.setMaxHeight(140);
        imageContainer.setPadding(new Insets(5, 0, 5, 0));
        StackPane.setAlignment(imageContainer, Pos.TOP_CENTER);

        pane.getChildren().add(imageContainer);

        VBox vBox3 = new VBox();
        vBox3.setPrefHeight(50.0);
        vBox3.setFillWidth(false);
        vBox3.setMaxHeight(50.0);
        vBox3.setPrefWidth(200.0);
        StackPane.setAlignment(vBox3, Pos.BOTTOM_CENTER);

        Label name = new Label();
        name.setPrefHeight(25.0);
        name.setMaxHeight(25.0);
        name.setPrefWidth(200.0);
        name.setPadding(new Insets(5, 0, 0, 0));
        name.getStyleClass().add("emojiName");
        name.setText(emoji.getName());
        name.setAlignment(Pos.CENTER);

        vBox3.getChildren().add(name);
        HBox hBox5 = new HBox();
        hBox5.setPrefHeight(20.0);
        hBox5.setPrefWidth(200.0);

        Label server = new Label();
        HBox.setHgrow(server, Priority.ALWAYS);
        server.setPrefWidth(200.0);
        server.setText(discord.getServerName(emoji.getServer()));
        server.setAlignment(Pos.CENTER);

        hBox5.getChildren().add(server);
        vBox3.getChildren().add(hBox5);
        pane.getChildren().add(vBox3);

        pane.setOnMouseClicked(event -> {
            selected = !selected;

            if (selected) {
                paneClasses.add("selected");
            } else {
                paneClasses.remove("selected");
            }

            onSelect.accept(this, selected);
        });
    }

    public void noEventSelect() {
        var paneClasses = pane.getStyleClass();
        if (!paneClasses.contains("selected")) paneClasses.add("selected");
    }

    public void noEventUnSelect() {
        pane.getStyleClass().remove("selected");
    }

    public DatabaseEmoji getEmoji() {
        return emoji;
    }

    public StackPane getPane() {
        return pane;
    }

    public EmojiCell setOnSelect(BiConsumer<EmojiCell, Boolean> onSelect) {
        this.onSelect = onSelect;
        return this;
    }
}
