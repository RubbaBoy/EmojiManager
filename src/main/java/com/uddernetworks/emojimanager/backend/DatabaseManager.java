package com.uddernetworks.emojimanager.backend;

import com.uddernetworks.emojimanager.backend.database.BasicSQLBinder;
import com.uddernetworks.emojimanager.backend.database.DatabaseEmoji;
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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

    public CompletableFuture<Void> addEmojis(List<DatabaseEmoji> emojis) {
        return CompletableFuture.runAsync(() -> {
            if (emojis.isEmpty()) return;
            Connection conn = null;
            try (var connection = getConnection();
                 var statement = connection.prepareStatement(addEmoji)) {
                conn = connection;
                connection.setAutoCommit(false);
                for (DatabaseEmoji emoji : emojis) {
                    statement.setLong(1, emoji.getId());
                    statement.setString(2, emoji.getName());
                    statement.setString(3, emoji.getImage());
                    statement.setBoolean(4, emoji.isAnimated());
                    statement.setLong(5, emoji.getServer());
                    statement.setLong(6, emoji.getCreated());
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
        });
    }

    public CompletableFuture<Void> removeNotContaining(long serverId, List<DatabaseEmoji> emojis) {
        return CompletableFuture.runAsync(() -> {
            var string = emojis.stream().map(emoji -> "id != " + emoji.getId() + " AND ").collect(Collectors.joining());
            string = string.substring(0, string.length() - 5) + ";";
            try (var connection = getConnection();
                 var statement = connection.prepareStatement("DELETE FROM emojis WHERE server = " + serverId + " AND " + string)) {
                statement.executeUpdate();
            } catch (SQLException e) {
                LOGGER.error("Error removing extra emojis", e);
            }
        });
    }

    private List<GenericMap> iterResultSet(ResultSet resultSet) throws SQLException {
        var meta = resultSet.getMetaData();
        int columns = meta.getColumnCount();

        var rows = new ArrayList<GenericMap>();
        while (resultSet.next()) {
            var row = new GenericMap();
            for(int i = 1; i <= columns; ++i){
                row.put(meta.getColumnName(i).toLowerCase(), resultSet.getObject(i));
            }
            rows.add(row);
        }

        return List.copyOf(rows);
    }

}
