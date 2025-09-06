/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d.terrain;

public final class TerrainConfig {

    /** Grid quads per tile edge. Vertex count = (grid+1)^2. 24–48 is a good range. */
    public final int grid;
    /** Maximum quadtree level (0 = whole face, 1 = 2x2, ...). 6–8 is a good starter. */
    public final int maxLevel;
    /** Crack‑hiding skirt depth in kilometers (extruded down along the normal). */
    public final float skirtDepthKm;
    /** Conservative absolute max terrain height used for bounds (km). */
    public final float maxHeightKm;
    /** SSE threshold in pixels (smaller = finer detail). Try 1.5–3.0. */
    public final float sseThresholdPx;
    /** Simple LRU capacity for cached tile meshes. */
    public final int tileCacheCapacity;

    /** Whether to also build skirt indices (recommended: true). */
    public final boolean useSkirts;

    public TerrainConfig(int grid, int maxLevel, float skirtDepthKm, float maxHeightKm,
                         float sseThresholdPx, int tileCacheCapacity, boolean useSkirts) {
        this.grid = Math.max(8, grid);
        this.maxLevel = Math.max(0, maxLevel);
        this.skirtDepthKm = Math.max(0f, skirtDepthKm);
        this.maxHeightKm = Math.max(0f, maxHeightKm);
        this.sseThresholdPx = Math.max(0.25f, sseThresholdPx);
        this.tileCacheCapacity = Math.max(16, tileCacheCapacity);
        this.useSkirts = useSkirts;
    }

    public static TerrainConfig defaults() {
        return new TerrainConfig(
                32,   // grid
                7,    // maxLevel
                1.5f, // skirtDepthKm
                8.0f, // maxHeightKm (conservative)
                2.0f, // sseThresholdPx
                256,  // tileCacheCapacity
                true  // useSkirts
        );
    }
}
