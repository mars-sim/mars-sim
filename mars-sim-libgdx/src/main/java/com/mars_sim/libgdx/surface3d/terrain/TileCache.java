/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d.terrain;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.mars_sim.msp.libgdx.surface3d.HeightFunction;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mars_sim.msp.libgdx.surface3d.terrain.CubeSphereMath.faceVector;
import static org.mars_sim.msp.libgdx.surface3d.terrain.CubeSphereMath.tileUvRange;

public final class TileCache implements AutoCloseable {

    private final float radiusKm;
    private final HeightFunction heightFn;
    private final TerrainConfig cfg;
    private final Material material;

    private final LinkedHashMap<TileKey, TerrainTile> lru;

    // Scratch
    private final Vector3 tmp = new Vector3();
    private final float[] uv = new float[4];

    public TileCache(float radiusKm, HeightFunction heightFn, TerrainConfig cfg, Material material) {
        this.radiusKm = radiusKm;
        this.heightFn = heightFn;
        this.cfg = cfg;
        this.material = material;

        this.lru = new LinkedHashMap<TileKey, TerrainTile>(cfg.tileCacheCapacity, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<TileKey, TerrainTile> eldest) {
                if (size() > cfg.tileCacheCapacity) {
                    eldest.getValue().dispose();
                    return true;
                }
                return false;
            }
        };
    }

    public TerrainTile getOrBuild(TileKey key) {
        TerrainTile t = lru.get(key);
        if (t != null) return t;
        t = build(key);
        lru.put(key, t);
        return t;
    }

    /** Compute conservative bounds without building the mesh (used by the selector too). */
    public void computeBounds(TileKey key, Vector3 outCenterKm, float[] outRadiusKm) {
        tileUvRange(key, uv);
        float u0 = uv[0], v0 = uv[1], u1 = uv[2], v1 = uv[3];

        Vector3 cdir = new Vector3();
        faceVector(key.face, (u0 + u1) * 0.5f, (v0 + v1) * 0.5f, cdir).nor();
        Vector3 center = outCenterKm.set(cdir).scl(radiusKm);

        // Furthest corner at max radius for safety
        float rMax = radiusKm + cfg.maxHeightKm;
        Vector3[] corners = new Vector3[]{
                faceVector(key.face, u0, v0, new Vector3()).nor().scl(rMax),
                faceVector(key.face, u1, v0, new Vector3()).nor().scl(rMax),
                faceVector(key.face, u0, v1, new Vector3()).nor().scl(rMax),
                faceVector(key.face, u1, v1, new Vector3()).nor().scl(rMax)
        };

        float maxd = 0f;
        for (Vector3 p : corners) maxd = Math.max(maxd, p.dst(center));
        // pad by skirt & max height to keep conservative
        outRadiusKm[0] = maxd + cfg.skirtDepthKm + cfg.maxHeightKm * 0.5f;
    }

    private TerrainTile build(TileKey key) {
        int grid = cfg.grid;
        int vertsCore = (grid + 1) * (grid + 1);

        // Skirt vertex counts
        int vertsSkirt = cfg.useSkirts ? 4 * (grid + 1) : 0;
        int totalVerts = vertsCore + vertsSkirt;

        // Indices: core quads * 2 triangles * 3 indices
        int idxCore = grid * grid * 6;
        int idxSkirt = cfg.useSkirts ? 4 * grid * 6 : 0;
        int totalIdx = idxCore + idxSkirt;

        float[] verts = new float[totalVerts * 6]; // pos(3) + normal(3)
        short[] idx = new short[totalIdx];

        // UV range for this tile
        tileUvRange(key, uv);
        float u0 = uv[0], v0 = uv[1], u1 = uv[2], v1 = uv[3];

        // ----- build core grid -----
        int vi = 0;
        for (int y = 0; y <= grid; y++) {
            float ty = (float) y / grid;
            float v = v0 + (v1 - v0) * ty;
            for (int x = 0; x <= grid; x++) {
                float tx = (float) x / grid;
                float u = u0 + (u1 - u0) * tx;

                faceVector(key.face, u, v, tmp).nor();
                float hKm = heightFn.sample(tmp);
                float r = radiusKm + hKm;

                // position
                verts[vi++] = tmp.x * r;
                verts[vi++] = tmp.y * r;
                verts[vi++] = tmp.z * r;
                // normal (radial approx)
                verts[vi++] = tmp.x;
                verts[vi++] = tmp.y;
                verts[vi++] = tmp.z;
            }
        }

        // core indices
        int ii = 0;
        for (int y = 0; y < grid; y++) {
            for (int x = 0; x < grid; x++) {
                int i00 =  y      * (grid + 1) + x;
                int i10 =  y      * (grid + 1) + (x + 1);
                int i01 = (y + 1) * (grid + 1) + x;
                int i11 = (y + 1) * (grid + 1) + (x + 1);

                idx[ii++] = (short)i00; idx[ii++] = (short)i10; idx[ii++] = (short)i11;
                idx[ii++] = (short)i00; idx[ii++] = (short)i11; idx[ii++] = (short)i01;
            }
        }

        // ----- skirts -----
        if (cfg.useSkirts) {
            int baseSkirt = vertsCore;
            int[] skirtIndex = new int[4 * (grid + 1)]; // store vertex indices for skirt line

            // Lambda to add one vertex (pos+normal)
            java.util.function.IntFunction<Integer> emitEdgeVert = (Integer coreIdx) -> {
                int c = coreIdx;
                float px = verts[c * 6];
                float py = verts[c * 6 + 1];
                float pz = verts[c * 6 + 2];
                float nx = verts[c * 6 + 3];
                float ny = verts[c * 6 + 4];
                float nz = verts[c * 6 + 5];

                // move inward (down along normal) by skirtDepth
                float len = (float)Math.sqrt(nx*nx + ny*ny + nz*nz);
                float sx = px - (nx/len) * cfg.skirtDepthKm;
                float sy = py - (ny/len) * cfg.skirtDepthKm;
                float sz = pz - (nz/len) * cfg.skirtDepthKm;

                int out = vi / 6;
                verts[vi++] = sx; verts[vi++] = sy; verts[vi++] = sz;
                verts[vi++] = nx; verts[vi++] = ny; verts[vi++] = nz;
                return out;
            };

            // bottom (y=0)
            for (int x = 0; x <= grid; x++) {
                int coreIdx = 0 * (grid + 1) + x;
                skirtIndex[x] = emitEdgeVert.apply(coreIdx);
            }
            // top (y=grid)
            int offsetTop = (grid + 1);
            for (int x = 0; x <= grid; x++) {
                int coreIdx = grid * (grid + 1) + x;
                skirtIndex[offsetTop + x] = emitEdgeVert.apply(coreIdx);
            }
            // left (x=0)
            int offsetLeft = 2 * (grid + 1);
            for (int y = 0; y <= grid; y++) {
                int coreIdx = y * (grid + 1) + 0;
                skirtIndex[offsetLeft + y] = emitEdgeVert.apply(coreIdx);
            }
            // right (x=grid)
            int offsetRight = 3 * (grid + 1);
            for (int y = 0; y <= grid; y++) {
                int coreIdx = y * (grid + 1) + grid;
                skirtIndex[offsetRight + y] = emitEdgeVert.apply(coreIdx);
            }

            // indices for each skirt strip
            // bottom
            for (int x = 0; x < grid; x++) {
                int a0 = 0 * (grid + 1) + x;
                int a1 = 0 * (grid + 1) + (x + 1);
                int s0 = skirtIndex[x];
                int s1 = skirtIndex[x + 1];

                idx[ii++] = (short)a0; idx[ii++] = (short)a1; idx[ii++] = (short)s1;
                idx[ii++] = (short)a0; idx[ii++] = (short)s1; idx[ii++] = (short)s0;
            }
            // top
            for (int x = 0; x < grid; x++) {
                int a0 = grid * (grid + 1) + x;
                int a1 = grid * (grid + 1) + (x + 1);
                int s0 = skirtIndex[offsetTop + x];
                int s1 = skirtIndex[offsetTop + x + 1];

                idx[ii++] = (short)a1; idx[ii++] = (short)a0; idx[ii++] = (short)s0;
                idx[ii++] = (short)a1; idx[ii++] = (short)s0; idx[ii++] = (short)s1;
            }
            // left
            for (int y = 0; y < grid; y++) {
                int a0 = y * (grid + 1) + 0;
                int a1 = (y + 1) * (grid + 1) + 0;
                int s0 = skirtIndex[offsetLeft + y];
                int s1 = skirtIndex[offsetLeft + y + 1];

                idx[ii++] = (short)a0; idx[ii++] = (short)a1; idx[ii++] = (short)s1;
                idx[ii++] = (short)a0; idx[ii++] = (short)s1; idx[ii++] = (short)s0;
            }
            // right
            for (int y = 0; y < grid; y++) {
                int a0 = y * (grid + 1) + grid;
                int a1 = (y + 1) * (grid + 1) + grid;
                int s0 = skirtIndex[offsetRight + y];
                int s1 = skirtIndex[offsetRight + y + 1];

                idx[ii++] = (short)a1; idx[ii++] = (short)a0; idx[ii++] = (short)s0;
                idx[ii++] = (short)a1; idx[ii++] = (short)s0; idx[ii++] = (short)s1;
            }

            if (ii != totalIdx) throw new GdxRuntimeException("Skirt index mismatch");
        }

        TerrainTile tile = new TerrainTile(key, material, verts, idx);

        // conservative bounds (reuse compute to avoid extra math here)
        float[] r = new float[1];
        computeBounds(key, tile.boundCenterKm, r);
        tile.boundRadiusKm = r[0];

        return tile;
    }

    @Override public void close() {
        for (TerrainTile t : lru.values()) t.dispose();
        lru.clear();
    }
}
