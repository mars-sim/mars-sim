/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d.terrain;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public final class TerrainTile implements Disposable {
    public final TileKey key;
    public final Renderable renderable;
    public final Vector3 boundCenterKm = new Vector3();
    public float boundRadiusKm;

    private final Mesh mesh;

    public TerrainTile(TileKey key, Material mat, float[] vertices, short[] indices) {
        this.key = key;
        this.mesh = new Mesh(true, vertices.length/6, indices.length,
                new VertexAttribute(Usage.Position, 3, "a_position"),
                new VertexAttribute(Usage.Normal,   3, "a_normal"));
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        renderable = new Renderable();
        renderable.meshPart.mesh = mesh;
        renderable.meshPart.offset = 0;
        renderable.meshPart.size = indices.length;
        renderable.meshPart.primitiveType = GL20.GL_TRIANGLES;
        renderable.material = mat;
        renderable.worldTransform.id();
    }

    @Override public void dispose() {
        mesh.dispose();
    }
}
