package com.uddernetworks.emojimanager.tabs.backups;

import com.uddernetworks.emojimanager.AttributeUtils;
import com.uddernetworks.emojimanager.backend.EmojiManager;
import com.uddernetworks.emojimanager.tabs.GUITab;
import com.uddernetworks.emojimanager.tabs.emojis.Emojis;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Backups extends Stage implements GUITab {

    private static Logger LOGGER = LoggerFactory.getLogger(Backups.class);

    @FXML
    private FlowPane backupContent;

    private Pane cachedPane;
    private EmojiManager emojiManager;
    private List<BackupSlot> backupSlots = Collections.synchronizedList(new ArrayList<>());

    public Backups(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    @Override
    public String getFile() {
        return "backups.fxml";
    }

    @Override
    public CompletableFuture<Pane> getCachedPane() throws IOException {
        if (cachedPane != null) return refreshBackups(cachedPane);
        return refreshBackups(cachedPane = getPane());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    private CompletableFuture<Pane> refreshBackups(Pane pane) {
        Platform.runLater(() -> backupContent.getChildren().clear());
        return CompletableFuture.supplyAsync(() -> {
            var slots = Arrays.stream(Emojis.BACKUP_PARENT.listFiles()).filter(file -> file.getName().endsWith(".zip")).map(file -> {
                try {
                    var attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                    var emojiCount = AttributeUtils.read(file, "Emojis");
                    return new BackupSlot(file.getName(), file, attrs.creationTime().toMillis(), file.length(), StringUtils.isNumeric(emojiCount) ? Integer.parseInt(emojiCount) : 0, false);
                } catch (IOException e) {
                    LOGGER.info("Error processing backup file " + file.getAbsolutePath(), e);
                    return null;
                }
            }).filter(Objects::nonNull)
                    .peek(backupSlots::add)
                    .map(BackupSlot::getPane)
                    .collect(Collectors.toUnmodifiableList());
            Platform.runLater(() -> backupContent.getChildren().setAll(slots));
            return pane;
        });
    }
}
