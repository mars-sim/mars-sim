/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d.terrain;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import org.mars_sim.msp.libgdx.surface3d.HeightFunction;

public final class CubeSpherePlanetLod implements RenderableProvider, Disposable {

    private final float radiusKm;
    private final HeightFunction heightFn;
    private final TerrainConfig cfg;
    private final Material material;

    private final TileCache cache;
    private final TileSelector selector;

    private final Array<TileKey> visible = new Array<>(false, 256);
    private final Array<Renderable> outRenderables = new Array<>(false, 512);

    public CubeSpherePlanetLod(float radiusKm, HeightFunction heightFn, TerrainConfig cfg, Material material) {
        this.radiusKm = radiusKm;
        this.heightFn = heightFn;
        this.cfg = cfg;
        this.material = material;

        this.cache = new TileCache(radiusKm, heightFn, cfg, material);
        this.selector = new TileSelector(radiusKm, cfg, cache);
    }

    public void update(Camera cam) {
        selector.select(cam, visible);
        // (Meshes are built lazily in getRenderables)
    }

    @Override
    public void getRenderables(Array<Renderable> renderables, com.badlogic.gdx.utils.Pool<Renderable> pool) {
        outRenderables.clear();
        for (int i = 0; i < visible.size; i++) {
            TileKey key = visible.get(i);
            TerrainTile tile = cache.getOrBuild(key);
            outRenderables.add(tile.renderable);
        }
        // Add to target array
        renderables.addAll(outRenderables);
    }

    @Override
    public void dispose() {
        cache.close();
    }
}
