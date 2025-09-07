/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Starter launcher for a LibGDX 3D Mars surface viewer
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class Surface3DLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Mars-Sim • Surface 3D");
        cfg.setWindowedMode(1600, 900);
        cfg.useVsync(true);
        cfg.setBackBufferConfig(
                8, 8, 8, 8,     // RGBA bits
                24,             // depth
                8,              // stencil
                4                // MSAA samples (set 0 if perf/compat is an issue)
        );
        cfg.setResizable(true);
        cfg.setForegroundFPS(0); // uncapped, vsync controls it
        new Lwjgl3Application(new Surface3DApp(), cfg);
    }

    private Surface3DLauncher() {}
}
