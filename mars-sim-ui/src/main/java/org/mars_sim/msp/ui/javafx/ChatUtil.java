package org.mars_sim.msp.ui.javafx;

import java.util.Objects;
import javafx.application.Platform;

public final class ChatUtil {
    private ChatUtil() {
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