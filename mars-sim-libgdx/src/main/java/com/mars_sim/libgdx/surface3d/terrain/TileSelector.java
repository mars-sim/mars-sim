/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d.terrain;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public final class TileSelector {

    private final float radiusKm;
    private final TerrainConfig cfg;
    private final TileCache cache;

    // Scratch
    private final Vector3 c = new Vector3();
    private final float[] r = new float[1];

    public TileSelector(float radiusKm, TerrainConfig cfg, TileCache cache) {
        this.radiusKm = radiusKm;
        this.cfg = cfg;
        this.cache = cache;
    }

    public void select(Camera cam, Array<TileKey> out) {
        out.clear();
        for (int face = 0; face < 6; face++) {
            recurse(cam, new TileKey(face, 0, 0, 0), out);
        }
    }

    private void recurse(Camera cam, TileKey key, Array<TileKey> out) {
        cache.computeBounds(key, c, r);
        float radius = r[0];

        // Frustum culling
        if (!cam.frustum.sphereInFrustum(c, radius)) {
            return;
        }

        // Distance to tile (km), keep conservative to avoid popping
        float dist = cam.position.dst(c) - radius * 0.8f;
        dist = Math.max(dist, 0.001f);

        // Screen space error heuristic
        float sse = projectedErrorPx(cam, geometricErrorKm(key.level), dist);
        boolean refine = (sse > cfg.sseThresholdPx) && key.level < cfg.maxLevel;

        if (refine) {
            // 4 children
            recurse(cam, key.child(0), out);
            recurse(cam, key.child(1), out);
            recurse(cam, key.child(2), out);
            recurse(cam, key.child(3), out);
        } else {
            out.add(key);
        }
    }

    /** Simple geometric error per LOD level based on face angular span and grid density. */
    private float geometricErrorKm(int level) {
        // face angular span (approx) is PI/2; tile span decreases by 2^level
        double faceAngle = Math.PI * 0.5;
        double tileAngle = faceAngle / (1 << level);
        // error proportional to arc length / grid resolution (with small fudge)
        double err = radiusKm * tileAngle / (double) cfg.grid;
        return (float) (err * 1.5);
    }

    private static float projectedErrorPx(Camera cam, float geomErrorKm, float distanceKm) {
        // Pixel scale for vertical resolution: H / (2*tan(fov/2))
        float H = Gdx.graphics.getHeight();
        float k = (float) (H / (2.0 * Math.tan(Math.toRadians(cam.fieldOfView * 0.5f))));
        return k * (geomErrorKm / distanceKm);
    }
}
