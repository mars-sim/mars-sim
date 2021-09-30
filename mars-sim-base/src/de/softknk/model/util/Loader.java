package de.softknk.model.util;

import javafx.scene.image.*;
import javafx.scene.text.Font;

public interface Loader {

    static Image loadImage(String fileName) {
        return new Image(Loader.class.getResourceAsStream("/assets/textures/" + fileName));
    }

    static Font loadFont(String fileName, double size) {
        return Font.loadFont(Loader.class.getResourceAsStream("/assets/ui/fonts/" + fileName), size);
    }
}
