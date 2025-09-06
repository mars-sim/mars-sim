/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d.terrain;

public final class TileKey {
    public final int face;   // 0..5 ( +X,-X,+Y,-Y,+Z,-Z )
    public final int level;  // quadtree depth
    public final int x;      // 0..(2^level-1)
    public final int y;      // 0..(2^level-1)

    public TileKey(int face, int level, int x, int y) {
        this.face = face;
        this.level = level;
        this.x = x;
        this.y = y;
    }

    public TileKey child(int i) {
        int cx = (i & 1);
        int cy = ((i >> 1) & 1);
        return new TileKey(face, level + 1, x * 2 + cx, y * 2 + cy);
    }

    @Override public boolean equals(Object o) {
        if (!(o instanceof TileKey)) return false;
        TileKey k = (TileKey)o;
        return face == k.face && level == k.level && x == k.x && y == k.y;
    }

    @Override public int hashCode() {
        int h = face;
        h = 31*h + level;
        h = 31*h + x;
        h = 31*h + y;
        return h;
    }

    @Override public String toString() {
        return "F"+face+" L"+level+" ["+x+","+y+"]";
    }
}
