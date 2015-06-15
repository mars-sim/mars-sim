package com.jme3x.jfx;

import javafx.application.Platform;

/**
 * TODO This Class should be replaced by some Workmanager implemntation
 * in the future
 * @author Heist
 */
public class FxPlatformExecutor {
    
    public static void runOnFxApplication(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            task.run();
        } else {
            Platform.runLater(task);
        }
    }
}
