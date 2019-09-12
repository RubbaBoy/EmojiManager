package com.uddernetworks.emojimanager.tabs.emojis;

import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.tabs.GUITab;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Emojis extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Emojis.class);

    @FXML
    private FlowPane emojiContent;

    @FXML
    private TextField search;

    @FXML
    private CheckBox unanimated;

    @FXML
    private CheckBox animated;

    @FXML
    private CheckBox regex;

    @FXML
    private Button uploadButton;

    @FXML
    private Button importButton;

    @FXML
    private Button downloadButton;

    @FXML
    private Button deleteButton;

    private Pane cachedPane;
    private SearchHelper searchHelper;
    private EmojiManager emojiManager;
    private BiConsumer<EmojiCell, Boolean> onSelectCell;
    private List<EmojiCell> originalCells;
    private List<EmojiCell> selected = Collections.synchronizedList(new ArrayList<>());
    private String lastSearchText = "";

    public Emojis(EmojiManager emojiManager) {
        LOGGER.warn("NEW EMOJI!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.emojiManager = emojiManager;
    }

    @Override
    public String getFile() {
        return "emojis.fxml";
    }

    @Override
    public Pane getCachedPane() throws IOException {
        if (cachedPane != null) return cachedPane;
        return (cachedPane = getPane());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Initializing emoji GUI...");

        onSelectCell = (cell, selected) -> {
            if (selected) {
                this.selected.add(cell);
            } else {
                this.selected.remove(cell);
            }

            var size = this.selected.size();
            downloadButton.setDisable(size == 0);
            deleteButton.setDisable(size == 0);

            downloadButton.setText("Download (" + size + ")");
            deleteButton.setText("Delete (" + size + ")");
        };

        var discord = emojiManager.getDiscordWrapper();
        originalCells = emojiManager.getEmojis().stream().map(emoji -> new EmojiCell(discord, emoji).setOnSelect(onSelectCell)).collect(Collectors.toUnmodifiableList());
        originalCells.forEach(cell -> emojiContent.getChildren().add(cell.getPane()));

        searchHelper = new SearchHelper(originalCells);

        search.textProperty().addListener((observable, oldValue, newValue) -> {
            lastSearchText = newValue;
            updateSearch();
        });

        unanimated.selectedProperty().addListener((observable, oldValue, newValue) -> updateSearch());
        animated.selectedProperty().addListener((observable, oldValue, newValue) -> updateSearch());
        regex.selectedProperty().addListener((observable, oldValue, newValue) -> updateSearch());

        uploadButton.setOnMouseClicked(event -> {
            LOGGER.info("Upload prompt");
        });

        importButton.setOnMouseClicked(event -> {
            LOGGER.info("Import from backup prompt");
        });

        downloadButton.setOnMouseClicked(event -> {
            LOGGER.info("Downloading {} emojis with prompt", selected.size());
        });

        deleteButton.setOnMouseClicked(event -> {
            LOGGER.info("Deleting {} emojis", selected.size());
        });
    }

    private void updateSearch() {
        emojiContent.getChildren().setAll(searchHelper.search(lastSearchText, unanimated.isSelected(), animated.isSelected(), regex.isSelected()));
    }
}
