/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector3;

public class CubeSpherePlanet {

    private final float radiusKm;
    private final HeightFunction heightFn;
    private final int resolution; // grid per face (quads), default 96

    public CubeSpherePlanet(float radiusKm, HeightFunction heightFn) {
        this(radiusKm, heightFn, 96);
    }

    public CubeSpherePlanet(float radiusKm, HeightFunction heightFn, int resolution) {
        this.radiusKm = radiusKm;
        this.heightFn = heightFn == null ? HeightFunction.flat() : heightFn;
        this.resolution = Math.max(4, resolution);
    }

    public Model buildModel(String id, Material material) {
        ModelBuilder mb = new ModelBuilder();
        mb.begin();
        long usage = Usage.Position | Usage.Normal;
        MeshPartBuilder pb = mb.part(id, GL20.GL_TRIANGLES, usage, material);

        final Vector3 dir = new Vector3();
        final VertexInfo vi = new VertexInfo().setUV(0, 0);

        // Build 6 faces (+X, -X, +Y, -Y, +Z, -Z)
        for (int face = 0; face < 6; face++) {
            // Pre-allocate vertex indices for reuse (avoid duplicates)
            short[][] indices = new short[resolution + 1][resolution + 1];

            // Create grid vertices
            for (int y = 0; y <= resolution; y++) {
                float v = -1f + 2f * (y / (float) resolution); // [-1, 1]
                for (int x = 0; x <= resolution; x++) {
                    float u = -1f + 2f * (x / (float) resolution); // [-1, 1]
                    faceVector(face, u, v, dir).nor();
                    float hKm = heightFn.sample(dir); // radial offset in km
                    float r = radiusKm + hKm;
                    Vector3 pos = new Vector3(dir).scl(r);
                    Vector3 nor = new Vector3(dir); // radial normal is fine for small h

                    indices[y][x] = pb.vertex(vi.setPos(pos).setNor(nor));
                }
            }

            // Create triangles
            for (int y = 0; y < resolution; y++) {
                for (int x = 0; x < resolution; x++) {
                    short i00 = indices[y][x];
                    short i10 = indices[y][x + 1];
                    short i01 = indices[y + 1][x];
                    short i11 = indices[y + 1][x + 1];

                    pb.triangle(i00, i10, i11);
                    pb.triangle(i00, i11, i01);
                }
            }
        }

        return mb.end();
    }

    /** Maps (u,v) in [-1,1]x[-1,1] to a direction vector on the given cube face, then returns it (not normalized). */
    private static Vector3 faceVector(int face, float u, float v, Vector3 out) {
        switch (face) {
            case 0: // +X
                return out.set( 1f,   v,  -u);
            case 1: // -X
                return out.set(-1f,   v,   u);
            case 2: // +Y
                return out.set(  u,  1f,  -v);
            case 3: // -Y
                return out.set(  u, -1f,   v);
            case 4: // +Z
                return out.set(  u,   v,  1f);
            default: // 5: -Z
                return out.set( -u,   v, -1f);
        }
    }
}
