/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Lightweight, deterministic pseudo-noise for prototyping.
 * Replace with real MOLA/HRSC sampling later.
 */
public class NoiseHeightFunction implements HeightFunction {

    private final float amplitudeKm;
    private final float frequency;
    private final int octaves;

    public NoiseHeightFunction(float amplitudeKm, float frequency, int octaves) {
        this.amplitudeKm = amplitudeKm;
        this.frequency = frequency;
        this.octaves = Math.max(1, octaves);
    }

    @Override
    public float sample(Vector3 dir) {
        // dir is unit length (x,y,z in [-1,1])
        float x = dir.x * frequency;
        float y = dir.y * frequency;
        float z = dir.z * frequency;

        // Simple fractal sum of sines as a stand-in for noise
        float amp = 1f;
        float frq = 1f;
        float sum = 0f;
        float norm = 0f;
        for (int i = 0; i < octaves; i++) {
            float v = sinHash(x * frq, y * frq, z * frq);
            sum += v * amp;
            norm += amp;
            amp *= 0.5f;
            frq *= 2f;
        }
        float n = (norm > 0f) ? sum / norm : 0f; // [-1,1]-ish
        return n * amplitudeKm;
    }

    private static float sinHash(float x, float y, float z) {
        float s = MathUtils.sin(1.23f * x + 2.34f * y + 3.45f * z)
                + 0.5f * MathUtils.sin(2.57f * x - 1.73f * y + 0.91f * z)
                + 0.25f * MathUtils.sin(-1.11f * x + 1.91f * y - 2.71f * z);
        return MathUtils.clamp(s * 0.7f, -1f, 1f);
    }
}
