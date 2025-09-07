/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.mars_sim.libgdx.surface3d.terrain;

import com.badlogic.gdx.math.Vector3;

public final class CubeSphereMath {

    private CubeSphereMath() {
    }

    /**
     * Map cube face + (u,v) in [-1,1] to a direction vector (not normalized).
     */
    public static Vector3 faceVector(int face, float u, float v, Vector3 out) {
        switch (face) {
            case 0:
                return out.set(1f, v, -u);  // +X
            case 1:
                return out.set(-1f, v, u);  // -X
            case 2:
                return out.set(u, 1f, -v);  // +Y
            case 3:
                return out.set(u, -1f, v);  // -Y
            case 4:
                return out.set(u, v, 1f);   // +Z
            default:
                return out.set(-u, v, -1f); // -Z
        }
    }

    /**
     * Get the (u0,v0,u1,v1) range on the face for a tile key.
     */
    public static void tileUvRange(TileKey key, float[] outUV) {
        int div = 1 << key.level;
        float span = 2f / div; // face range [-1,1] has length 2

        float u0 = -1f + key.x * span;
        float v0 = -1f + key.y * span;
        float u1 = u0 + span;
        float v1 = v0 + span;

        outUV[0] = u0;
        outUV[1] = v0;
        outUV[2] = u1;
        outUV[3] = v1;
    }
}
