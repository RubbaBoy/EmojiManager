package com.uddernetworks.emojimanager.backend;

import com.uddernetworks.emojimanager.utils.FileDirectoryChooser;
import com.uddernetworks.emojimanager.utils.PopupHelper;
import javafx.stage.FileChooser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static com.uddernetworks.emojimanager.tabs.emojis.Emojis.BACKUP_PARENT;

public class RestoreManager {

    private static Logger LOGGER = LoggerFactory.getLogger(RestoreManager.class);

    private static final String title = "Restore Confirm";
    private static final String description = "Are you sure you want to import these emojis? This will remove all emojis in the backed up servers\n" +
            "(Assuming they are still enabled in the \"Servers\" tab) and re-add them. If you want to simply add\n" +
            "emojis, click the \"Upload\" button, which will upload them to whatever available servers are present.";

    private EmojiManager emojiManager;

    public RestoreManager(EmojiManager emojiManager) {
        this.emojiManager = emojiManager;
    }

    public void restoreEmojis() {
        PopupHelper.createDialog(title, description, 1, Map.of(
                "Yes",
                () -> {
                    LOGGER.info("Choosing backup file...");

                    FileDirectoryChooser.openFileSelector(chooser -> {
                        chooser.setTitle("Choose backup zip");
                        chooser.setInitialDirectory(BACKUP_PARENT);
                        chooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Backup ZIP", "*.zip"));
                    }, zipFile -> {
                        LOGGER.info("Selected {}", zipFile.getAbsolutePath());
                    });
                },
                "No",
                PopupHelper.EMPTY_RUNNABLE
        ));
    }

    public void restoreEmojis(File zipFile) {
        PopupHelper.createDialog(title, description, 1, Map.of(
                "Yes",
                () -> {
                    LOGGER.info("Using selected zip file {}", zipFile.getAbsolutePath());
                    restoreEmojisNoConfirm(zipFile);
                },
                "No",
                PopupHelper.EMPTY_RUNNABLE
        ));
    }

    public void restoreEmojisNoConfirm(File zipFile) {
        File parent = null;
        try {
            parent = Files.createTempDirectory("backup-zip").toFile();

            List<Long> servers = emojiManager.getConfigManager().getConfig().get("servers");
            var fileServers = unZip(zipFile, parent);
            fileServers.entrySet().stream().filter(entry -> servers.contains(entry.getKey())).forEach(entry -> {
                var files = entry.getValue();
                var server = entry.getKey();
                emojiManager.restoreEmojis(server, files);
            });
        } catch (IOException e) {
            LOGGER.error("Error unzipping " + zipFile.getAbsolutePath(), e);
        } finally {
            if (parent != null) {
                try {
                    FileUtils.deleteDirectory(parent);
                } catch (IOException e) {
                    LOGGER.error("Error deleting temp directory " + parent.getAbsolutePath(), e);
                }
            }
        }
    }

    private static Map<Long, List<File>> unZip(File zipFile, File dest) {
        var map = new HashMap<Long, List<File>>();
        if (!dest.exists()) dest.mkdirs();
        var buffer = new byte[1024];
        try (var fis = new FileInputStream(zipFile);
             var zis = new ZipInputStream(fis)) {
            var ze = zis.getNextEntry();
            while (ze != null) {
                var fileName = ze.getName();
                var newFile = new File(dest, fileName);
                LOGGER.info("Unzipping to {}", newFile.getAbsolutePath());
                var parent = new File(newFile.getParent());
                parent.mkdirs();
                map.computeIfAbsent(Long.parseLong(parent.getName()), i -> new ArrayList<>()).add(newFile);
                try (var fos = new FileOutputStream(newFile)) {
                    for (int len; (len = zis.read(buffer)) > 0; ) {
                        fos.write(buffer, 0, len);
                    }
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            LOGGER.error("Error unzipping {}", zipFile.getAbsolutePath());
        }
        return map;
    }

}
