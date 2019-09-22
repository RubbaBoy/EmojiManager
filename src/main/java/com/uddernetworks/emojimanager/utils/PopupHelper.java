package com.uddernetworks.emojimanager.utils;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PopupHelper {

    public static final Runnable EMPTY_RUNNABLE = () -> {};

    public static void createDialog(String title, String content, int initial, Map<String, Runnable> options) {
        CompletableFuture.runAsync(() -> createBlockingDialog(title, content, initial, options));
    }

    public static void createBlockingDialog(String title, String content, int initial, Map<String, Runnable> options) {
        var displayingOptions = new LinkedHashMap<>(options).entrySet().stream().map(entry -> new NamedRunnable(entry.getKey(), entry.getValue())).collect(Collectors.toCollection(LinkedList::new));
        displayingOptions.get(JOptionPane.showOptionDialog(null, content, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                displayingOptions.toArray(), displayingOptions.get(initial))).run();
    }

    private static class NamedRunnable implements Runnable {
        private String name;
        private Runnable run;

        public NamedRunnable(String name, Runnable run) {
            this.name = name;
            this.run = run;
        }

        @Override
        public void run() {
            run.run();
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
