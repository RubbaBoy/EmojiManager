package com.uddernetworks.emojimanager.tabs.emojis;

import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class SearchHelper {

    private List<EmojiCell> originalCells;
    private List<StackPane> allPanes;

    public SearchHelper(List<EmojiCell> originalCells) {
        this.originalCells = originalCells;
        allPanes = originalCells.stream().map(EmojiCell::getPane).collect(Collectors.toUnmodifiableList());
    }

    public List<StackPane> search(String text, boolean unanimated, boolean animated, boolean regex) {
        String lowerText = text.toLowerCase();
        if (!unanimated && !animated) unanimated = animated = true;
        if (lowerText.isBlank() && unanimated && animated) return allPanes;

        Pattern temp = null;
        if (regex) {
            try {
                temp = Pattern.compile(text);
            } catch (PatternSyntaxException ignored) {
                return allPanes;
            }
        }
        Pattern pattern = temp;

        boolean usingAnimated = animated;
        boolean usingUnanimated = unanimated;
        return originalCells.parallelStream()
                .filter(cell -> {
                    var thisAnim = cell.getEmoji().isAnimated();
                    if (usingAnimated && usingUnanimated) return true;
                    if (usingAnimated) return thisAnim;
                    return !thisAnim;
                })
                .filter(cell -> {
                    if (!regex) return cell.getEmoji().getName().toLowerCase().contains(lowerText);
                    return pattern.matcher(cell.getEmoji().getName()).matches();
                })
                .map(EmojiCell::getPane).collect(Collectors.toUnmodifiableList());
    }

}
