package org.mars_sim.javafx;

import java.util.Objects;
import javafx.application.Platform;

public final class ChatSafe {
    private ChatSafe() {
        throw new UnsupportedOperationException();
    }

    public static void runSafe(final Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        if (Platform.isFxApplicationThread()) {
            runnable.run();
        }
        else {
            Platform.runLater(runnable);
        }
    }
}