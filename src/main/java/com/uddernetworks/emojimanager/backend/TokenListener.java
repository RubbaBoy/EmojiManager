package com.uddernetworks.emojimanager.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class TokenListener {

    private static Logger LOGGER = LoggerFactory.getLogger(TokenListener.class);

    private static final String CONSOLE_CODE = "XMLHttpRequest.prototype.wrappedSetRequestHeader = XMLHttpRequest.prototype.setRequestHeader; XMLHttpRequest.prototype.setRequestHeader = function (header, value) { this.wrappedSetRequestHeader(header, value); if (header === 'Authorization') { let socket = new WebSocket(`ws://127.0.0.1:6979/token:${value}`); XMLHttpRequest.prototype.setRequestHeader = this.wrappedSetRequestHeader; setTimeout(socket.close, 1000); }};";

    public void getToken(Consumer<String> token) {
        getTokenFile().ifPresentOrElse(token, () -> startListening(token));
    }

    public Optional<String> getTokenFile() {
        var file = new File("token").getAbsoluteFile();
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
            file.delete();
            Files.write(file.toPath(), token.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            LOGGER.error("Error while trying to write " + file.getAbsolutePath(), e);
        }
    }

    public void startListening(Consumer<String> token) {
        LOGGER.info("Paste the following in your Discord console:\n\n{}", CONSOLE_CODE);
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
