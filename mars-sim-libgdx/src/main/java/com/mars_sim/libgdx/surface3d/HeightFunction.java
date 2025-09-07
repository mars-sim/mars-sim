/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.math.Vector3;

@FunctionalInterface
public interface HeightFunction {
    /**
     * @param unitDir unit vector from planet center (x,y,z on unit sphere)
     * @return radial height offset in kilometers relative to the base radius
     */
    float sample(Vector3 unitDir);

    static HeightFunction flat() {
        return d -> 0f;
    }
}
