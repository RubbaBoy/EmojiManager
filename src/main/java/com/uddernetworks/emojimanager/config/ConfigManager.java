package com.uddernetworks.emojimanager.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

public class ConfigManager {

    private FileConfig config;
    private String fileName;

    public ConfigManager(String fileName) {
        this.fileName = fileName;
    }

    public void init() {
        config = CommentedFileConfig.builder(fileName).autosave().build();
        config.load();
    }

    public FileConfig getConfig() {
        return config;
    }
}
