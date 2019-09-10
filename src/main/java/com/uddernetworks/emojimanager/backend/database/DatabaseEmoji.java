package com.uddernetworks.emojimanager.backend.database;

import com.uddernetworks.emojimanager.backend.DatabaseManager;
import net.dv8tion.jda.api.entities.Emote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class DatabaseEmoji {

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseEmoji.class);

    private final long id;
    private String name;
    private InputStream image;
    private boolean animated;
    private long server;
    private long created;

    private boolean modified;

    public DatabaseEmoji(Emote emoji) {
        this(emoji.getIdLong(), emoji.getName(), createBytesFromUrl(emoji.getImageUrl()), emoji.isAnimated(), emoji.getGuild().getIdLong(), emoji.getTimeCreated().toEpochSecond());
    }

    public DatabaseEmoji(long id, String name, InputStream image, boolean animated, long server, long created) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.animated = animated;
        this.server = server;
        this.created = created;
    }

    private static InputStream createBytesFromUrl(String url) {
        try {
            return new URL(url).openConnection().getInputStream();
        } catch (IOException e) {
            LOGGER.error("Error while reading image URL: " + url, e);
            return null;
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        modified = true;
    }

    public InputStream getImage() {
        return new BufferedInputStream(image);
    }

    public void setImage(InputStream image) {
        this.image = image;
        modified = true;
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
        modified = true;
    }

    public long getServer() {
        return server;
    }

    public void setServer(long server) {
        this.server = server;
        modified = true;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }
}
