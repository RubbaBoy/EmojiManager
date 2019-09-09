package com.uddernetworks.emojimanager.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;

public class TokenListener {

    private static Logger LOGGER = LoggerFactory.getLogger(TokenListener.class);

    public void getToken(Consumer<String> token) {
        getTokenFile().ifPresentOrElse(token, () -> startListening(token));
    }

    public Optional<String> getTokenFile() {
        var file = new File("token");
        if (!file.exists()) return Optional.empty();
        try {
            return Optional.of(new String(Files.readAllBytes(file.toPath())));
        } catch (IOException e) {
            LOGGER.error("Error while trying to read " + file.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

    public void writeToken(String token) {
        var file = new File("token").getAbsoluteFile();
        file.getParentFile().mkdirs();
        try {
            Files.write(file.toPath(), token.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            LOGGER.error("Error while tyring to read " + file.getAbsolutePath(), e);
        }
    }

    public void startListening(Consumer<String> token) {
        LOGGER.info("Waiting for token...");
        try (var listener = new ServerSocket(6979)) {
            var socket = listener.accept();
            var in = new Scanner(socket.getInputStream());

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (!line.contains("/token:")) continue;
                var tokenString = line.replaceAll("(/token:|GET|\\s+|HTTP.*$)", "");
                writeToken(tokenString);
                token.accept(tokenString);
                break;
            }

            socket.close();
        } catch (Exception e) {
            LOGGER.error("Error during socket connection", e);
        }
    }


}
