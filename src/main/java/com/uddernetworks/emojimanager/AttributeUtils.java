package com.uddernetworks.emojimanager;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.UserDefinedFileAttributeView;

public class AttributeUtils {

    public static void write(File file, String name, Object value) throws IOException {
        write(file.toPath(), name, value);
    }

    public static void write(Path path, String name, Object value) throws IOException {
        var view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        byte[] bytes = value == null ? new byte[0] : value.toString().getBytes(StandardCharsets.UTF_8);
        ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
        writeBuffer.put(bytes);
        writeBuffer.flip();
        view.write(name, writeBuffer);
    }

    public static String read(File file, String name) throws IOException {
        return read(file.toPath(), name);
    }

    public static String read(Path path, String name) throws IOException {
        var view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
        var readBuffer = ByteBuffer.allocate(view.size(name));
        view.read(name, readBuffer);
        readBuffer.flip();
        return new String(readBuffer.array());
    }

}
