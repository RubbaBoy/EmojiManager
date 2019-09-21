package com.uddernetworks.emojimanager.utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PopupHelper {

    public static final Runnable EMPTY_RUNNABLE = () -> {};

    public static void createDialog(String title, String content, int initial, Map<String, Runnable> options) {
        CompletableFuture.runAsync(() -> createBlockingDialog(title, content, initial, options));
    }

    public static void createBlockingDialog(String title, String content, int initial, Map<String, Runnable> options) {

        var displayingOptions = new LinkedList<Runnable>();
        options.forEach((key, value) -> displayingOptions.add(new NamedRunnable(key, value)));

        var res = JOptionPane.showOptionDialog(null, content, title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,
                displayingOptions.toArray(), displayingOptions.get(initial));
        displayingOptions.get(res).run();
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
