package com.uddernetworks.emojimanager.tabs;

public class TabItem {

    private String name;
    private GUITab guiTab;

    public TabItem(String name, GUITab guiTab) {
        this.name = name;
        this.guiTab = guiTab;
    }

    public String getName() {
        return name;
    }

    public GUITab getGuiTab() {
        return guiTab;
    }

    @Override
    public String toString() {
        return name;
    }
}
