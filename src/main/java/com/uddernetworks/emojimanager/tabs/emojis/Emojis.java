package com.uddernetworks.emojimanager.tabs.emojis;

import com.uddernetworks.emojimanager.AttributeUtils;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Emojis extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Emojis.class);
    public static final File EM_PARENT = new File(System.getProperty("user.home"), "EmojiManager");
    public static final File BACKUP_PARENT = new File(EM_PARENT, "Backups");

    static {
        BACKUP_PARENT.mkdirs();
    }

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

            try {
                var parent = Files.createTempDirectory("em");
                LOGGER.info("Temp directory at: {}", parent.toAbsolutePath());

                var dateFormat = new SimpleDateFormat("MM-dd-yyyy__HH-mm-ss");

                var zipFile = new File(BACKUP_PARENT, "backup_" + dateFormat.format(new Date(System.currentTimeMillis())) + ".zip");
                try (var fos = new FileOutputStream(zipFile);
                     var zos = new ZipOutputStream(fos)) {
                    selected.parallelStream().map(EmojiCell::getEmoji).map(emoji -> {
                        var file = new File(parent.toFile(), emoji.getName() + (emoji.isAnimated() ? ".gif" : ".png"));
                        try {
                            var readableByteChannel = Channels.newChannel(new URL(emoji.getImage()).openStream());
                            var fileOutputStream = new FileOutputStream(file);
                            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
                        } catch (IOException e) {
                            LOGGER.error("Error reading emoji " + emoji.getName() + " at URL " + emoji.getImage(), e);
                        }
                        return file;
                    }).sequential().forEach(file -> {
                        try {
                            zos.putNextEntry(new ZipEntry(file.getName()));
                            byte[] bytes = Files.readAllBytes(file.toPath());
                            zos.write(bytes, 0, bytes.length);
                            zos.closeEntry();
                        } catch (IOException e) {
                            LOGGER.error("Error adding files to ZIP", e);
                        }
                    });
                    AttributeUtils.write(zipFile, "Emojis", selected.size());
                } finally {
                    LOGGER.info("Completed zip in {}", BACKUP_PARENT.getAbsolutePath());
                }
            } catch (IOException e) {
                LOGGER.error("Error downloading emojis and creating ZIP", e);
            }
        });

        deleteButton.setOnMouseClicked(event -> {
            LOGGER.info("Deleting {} emojis", selected.size());
        });
    }

    private void initEmojiCells() {
        var discord = emojiManager.getDiscordWrapper();
        originalCells.clear();
        Platform.runLater(() -> emojiContent.getChildren().clear());

        CompletableFuture.supplyAsync(() -> emojiManager.getEmojis().parallelStream()
                .map(emoji -> new EmojiCell(discord, emoji).setOnSelect(onSelectCell))
                .sorted(Comparator.comparing(cell -> cell.getEmoji().getName()))
                .peek(originalCells::add)
                .map(EmojiCell::getPane)
                .collect(Collectors.toCollection(LinkedList::new)))
                .thenAccept(list -> Platform.runLater(() -> emojiContent.getChildren().setAll(list)))
                .thenRun(() -> searchHelper = new SearchHelper(originalCells));
    }

    private void updateSearch() {
        emojiContent.getChildren().setAll(searchHelper.search(lastSearchText, unanimated.isSelected(), animated.isSelected(), regex.isSelected()));
    }
}
