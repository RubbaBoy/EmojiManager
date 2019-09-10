package com.uddernetworks.emojimanager.backend;

import com.uddernetworks.emojimanager.backend.database.BasicSQLBinder;
import com.uddernetworks.emojimanager.backend.database.SQLBound;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.api.entities.Emote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DatabaseManager {

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseManager.class);

    @SQLBound
    private String addEmoji;

    private DataSource dataSource;

    public DatabaseManager(File directory) {
        var config = new HikariConfig();

        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        var databaseFile = new File(directory, "emojidb");
        directory.mkdirs();
        config.setJdbcUrl("jdbc:hsqldb:file:" + databaseFile.getAbsolutePath());
        config.setUsername("SA");
        config.setPassword("");

        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "1000");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "8192");

        dataSource = new HikariDataSource(config);

        new BasicSQLBinder().createBindings(this);

        try (var connection = this.dataSource.getConnection();
             var useMySQL = connection.prepareStatement("SET DATABASE SQL SYNTAX MYS TRUE")) {
            useMySQL.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Error while trying to set MySQL dialect", e);
        }

        Stream.of("emojis.sql").forEach(table -> {
            try (var reader = new BufferedReader(new InputStreamReader(DatabaseManager.class.getResourceAsStream("/" + table)));
                 var connection = dataSource.getConnection();
                 var statement = connection.prepareStatement(reader.lines().collect(Collectors.joining("\n")))) {
                statement.executeUpdate();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void addEmojis(List<Emote> emojis) {
        if (emojis.isEmpty()) return;
        Connection conn = null;
        try (var connection = getConnection();
                var statement = connection.prepareStatement(addEmoji)) {
            conn = connection;
            connection.setAutoCommit(false);
            for (Map.Entry<Emote, InputStream> emojiEntry : getBase64Emojis(emojis).entrySet()) {
                var emoji = emojiEntry.getKey();
                var guild = emoji.getGuild();
                if (guild == null) {
                    LOGGER.error("Guild for {} is null!", emoji.getName());
                    continue;
                }
                var inputStream = emojiEntry.getValue();
                statement.setLong(1, emoji.getIdLong());
                statement.setString(2, emoji.getName());
                statement.setBlob(3, inputStream);
                statement.setBoolean(4, emoji.isAnimated());
                statement.setLong(5, guild.getIdLong());
                statement.setLong(6, emoji.getTimeCreated().toEpochSecond() * 1000);
                statement.addBatch();
            }
            statement.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            LOGGER.error("Error adding emojis. Rolling back transaction.", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.error("Error rolling back. You're pretty much fucked.", e);
                }
            }
        }
    }

    public Map<Emote, InputStream> getBase64Emojis(List<Emote> emojis) {
        return emojis.parallelStream().collect(Collectors.toMap(e -> e, e -> {
            try {
                return new BufferedInputStream(new URL(e.getImageUrl()).openConnection().getInputStream());
            } catch (IOException ex) {
                LOGGER.error("Error fetching image", ex);
                return null;
            }
        }));
    }

}
