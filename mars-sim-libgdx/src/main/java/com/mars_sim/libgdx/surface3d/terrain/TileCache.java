/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.mars_sim.libgdx.surface3d.terrain;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.mars_sim.libgdx.surface3d.HeightFunction;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.mars_sim.libgdx.surface3d.terrain.CubeSphereMath.faceVector;
import static com.mars_sim.libgdx.surface3d.terrain.CubeSphereMath.tileUvRange;

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
            @Override
            protected boolean removeEldestEntry(Map.Entry<TileKey, TerrainTile> eldest) {
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
        if (t != null) {
            return t;
        }
        TerrainTile built = build(key);
        lru.put(key, built);
        return built;
    }

    /**
     * Compute conservative bounds without building the mesh (used by the selector too).
     */
    public void computeBounds(TileKey key, Vector3 outCenterKm, float[] outRadiusKm) {
        tileUvRange(key, uv);
        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        Vector3 cdir = new Vector3();
        faceVector(key.face, (u0 + u1) * 0.5f, (v0 + v1) * 0.5f, cdir).nor();

        Vector3 center = outCenterKm.set(cdir).scl(radiusKm);

        // Furthest corner at max radius for safety
        float rMax = radiusKm + cfg.maxHeightKm;
        Vector3[] corners = new Vector3[] {
                faceVector(key.face, u0, v0, new Vector3()).nor().scl(rMax),
                faceVector(key.face, u1, v0, new Vector3()).nor().scl(rMax),
                faceVector(key.face, u0, v1, new Vector3()).nor().scl(rMax),
                faceVector(key.face, u1, v1, new Vector3()).nor().scl(rMax)
        };

        float maxd = 0f;
        for (Vector3 p : corners) {
            maxd = Math.max(maxd, p.dst(center));
        }

        // pad by skirt & max height to keep conservative
        outRadiusKm[0] = maxd + cfg.skirtDepthKm + cfg.maxHeightKm * 0.5f;
    }

    private TerrainTile build(TileKey key) {
        final int grid = cfg.grid;

        final int vertsCore = (grid + 1) * (grid + 1);
        final int vertsSkirt = cfg.useSkirts ? 4 * (grid + 1) : 0;
        final int totalVerts = vertsCore + vertsSkirt;

        // Indices: core quads * 2 triangles * 3 indices
        final int idxCore = grid * grid * 6;
        final int idxSkirt = cfg.useSkirts ? 4 * grid * 6 : 0;
        final int totalIdx = idxCore + idxSkirt;

        float[] verts = new float[totalVerts * 6]; // pos(3) + normal(3)
        short[] idx = new short[totalIdx];

        // UV range for this tile
        tileUvRange(key, uv);
        float u0 = uv[0];
        float v0 = uv[1];
        float u1 = uv[2];
        float v1 = uv[3];

        int vi = buildCoreVertices(key, grid, verts, u0, v0, u1, v1);
        int ii = buildCoreIndices(grid, idx);

        if (cfg.useSkirts) {
            ii = buildSkirts(grid, verts, idx, vi, ii, totalIdx);
        }

        TerrainTile tile = new TerrainTile(key, material, verts, idx);

        // conservative bounds (reuse compute to avoid extra math here)
        float[] r = new float[1];
        computeBounds(key, tile.boundCenterKm, r);
        tile.boundRadiusKm = r[0];

        return tile;
    }

    private int buildCoreVertices(
            TileKey key,
            int grid,
            float[] verts,
            float u0,
            float v0,
            float u1,
            float v1
    ) {
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
        return vi;
    }

    private int buildCoreIndices(int grid, short[] idx) {
        int ii = 0;
        for (int y = 0; y < grid; y++) {
            for (int x = 0; x < grid; x++) {
                int i00 = y * (grid + 1) + x;
                int i10 = y * (grid + 1) + (x + 1);
                int i01 = (y + 1) * (grid + 1) + x;
                int i11 = (y + 1) * (grid + 1) + (x + 1);

                ii = emitTri(idx, ii, i00, i10, i11);
                ii = emitTri(idx, ii, i00, i11, i01);
            }
        }
        return ii;
    }

    private int buildSkirts(
            int grid,
            float[] verts,
            short[] idx,
            int vi,
            int ii,
            int expectedTotalIdx
    ) {
        int[] skirtIndex = new int[4 * (grid + 1)]; // store vertex indices for skirt lines

        // bottom (y=0)
        for (int x = 0; x <= grid; x++) {
            int coreIdx = x; // 0*(grid+1) + x
            vi = addSkirtVertex(verts, vi, coreIdx);
            skirtIndex[x] = (vi / 6) - 1;
        }

        // top (y=grid)
        int offsetTop = (grid + 1);
        for (int x = 0; x <= grid; x++) {
            int coreIdx = grid * (grid + 1) + x;
            vi = addSkirtVertex(verts, vi, coreIdx);
            skirtIndex[offsetTop + x] = (vi / 6) - 1;
        }

        // left (x=0)
        int offsetLeft = 2 * (grid + 1);
        for (int y = 0; y <= grid; y++) {
            int coreIdx = y * (grid + 1);
            vi = addSkirtVertex(verts, vi, coreIdx);
            skirtIndex[offsetLeft + y] = (vi / 6) - 1;
        }

        // right (x=grid)
        int offsetRight = 3 * (grid + 1);
        for (int y = 0; y <= grid; y++) {
            int coreIdx = y * (grid + 1) + grid;
            vi = addSkirtVertex(verts, vi, coreIdx);
            skirtIndex[offsetRight + y] = (vi / 6) - 1;
        }

        // indices for each skirt strip
        // bottom
        for (int x = 0; x < grid; x++) {
            int a0 = x;
            int a1 = x + 1;
            int s0 = skirtIndex[x];
            int s1 = skirtIndex[x + 1];
            ii = emitTri(idx, ii, a0, a1, s1);
            ii = emitTri(idx, ii, a0, s1, s0);
        }

        // top
        for (int x = 0; x < grid; x++) {
            int a0 = grid * (grid + 1) + x;
            int a1 = grid * (grid + 1) + (x + 1);
            int s0 = skirtIndex[offsetTop + x];
            int s1 = skirtIndex[offsetTop + x + 1];
            ii = emitTri(idx, ii, a1, a0, s0);
            ii = emitTri(idx, ii, a1, s0, s1);
        }

        // left
        for (int y = 0; y < grid; y++) {
            int a0 = y * (grid + 1);
            int a1 = (y + 1) * (grid + 1);
            int s0 = skirtIndex[offsetLeft + y];
            int s1 = skirtIndex[offsetLeft + y + 1];
            ii = emitTri(idx, ii, a0, a1, s1);
            ii = emitTri(idx, ii, a0, s1, s0);
        }

        // right
        for (int y = 0; y < grid; y++) {
            int a0 = y * (grid + 1) + grid;
            int a1 = (y + 1) * (grid + 1) + grid;
            int s0 = skirtIndex[offsetRight + y];
            int s1 = skirtIndex[offsetRight + y + 1];
            ii = emitTri(idx, ii, a1, a0, s0);
            ii = emitTri(idx, ii, a1, s0, s1);
        }

        if (ii != expectedTotalIdx) {
            throw new GdxRuntimeException("Skirt index mismatch");
        }
        return ii;
    }

    private int addSkirtVertex(float[] verts, int vi, int coreIdx) {
        float px = verts[coreIdx * 6];
        float py = verts[coreIdx * 6 + 1];
        float pz = verts[coreIdx * 6 + 2];

        float nx = verts[coreIdx * 6 + 3];
        float ny = verts[coreIdx * 6 + 4];
        float nz = verts[coreIdx * 6 + 5];

        // move inward (down along normal) by skirtDepth
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        float invLen = (len != 0f) ? (1f / len) : 0f;

        float sx = px - nx * invLen * cfg.skirtDepthKm;
        float sy = py - ny * invLen * cfg.skirtDepthKm;
        float sz = pz - nz * invLen * cfg.skirtDepthKm;

        verts[vi++] = sx;
        verts[vi++] = sy;
        verts[vi++] = sz;
        verts[vi++] = nx;
        verts[vi++] = ny;
        verts[vi++] = nz;

        return vi;
    }

    private static int emitTri(short[] idx, int ii, int i0, int i1, int i2) {
        idx[ii++] = (short) i0;
        idx[ii++] = (short) i1;
        idx[ii++] = (short) i2;
        return ii;
    }

    @Override
    public void close() {
        for (TerrainTile t : lru.values()) {
            t.dispose();
        }
        lru.clear();
    }
}
