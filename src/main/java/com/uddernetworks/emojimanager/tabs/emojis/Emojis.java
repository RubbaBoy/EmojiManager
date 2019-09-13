package com.uddernetworks.emojimanager.tabs.emojis;

import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.tabs.GUITab;
import javafx.application.Platform;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    private List<EmojiCell> originalCells = Collections.synchronizedList(new ArrayList<>());
    private List<EmojiCell> selected = Collections.synchronizedList(new ArrayList<>());
    private String lastSearchText = "";

    public Emojis(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    @Override
    public String getFile() {
        return "emojis.fxml";
    }

    @Override
    public CompletableFuture<Pane> getCachedPane() throws IOException {
        if (cachedPane != null) return checkEmojis();
        cachedPane = getPane();
        return checkEmojis();
    }

    private CompletableFuture<Pane> checkEmojis() {
        return CompletableFuture.supplyAsync(() -> {
            if (emojiManager.haveEmojisChanged()) initEmojiCells();
            return cachedPane;
        });
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

        initEmojiCells();

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

    private void initEmojiCells() {
        var discord = emojiManager.getDiscordWrapper();
        originalCells.clear();
        Platform.runLater(() -> emojiContent.getChildren().clear());

        CompletableFuture.runAsync(() -> emojiManager.getEmojis().parallelStream()
                .map(emoji -> new EmojiCell(discord, emoji).setOnSelect(onSelectCell))
                .sorted(Comparator.comparing(cell -> cell.getEmoji().getName()))
                .peek(originalCells::add)
                .map(EmojiCell::getPane)
                .forEach(pane -> Platform.runLater(() -> emojiContent.getChildren().add(pane))))
                .thenRun(() -> searchHelper = new SearchHelper(originalCells));
    }

    private void updateSearch() {
        emojiContent.getChildren().setAll(searchHelper.search(lastSearchText, unanimated.isSelected(), animated.isSelected(), regex.isSelected()));
    }
}
