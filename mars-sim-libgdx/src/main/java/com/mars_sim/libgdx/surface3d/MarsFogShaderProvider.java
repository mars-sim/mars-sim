/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class MarsFogShaderProvider implements ShaderProvider {

    private final MarsFogShader.Settings settings;
    private MarsFogShader shader; // one simple pass for now

    public MarsFogShaderProvider(MarsFogShader.Settings settings) {
        this.settings = settings;
    }

    @Override
    public Shader getShader(Renderable renderable) {
        if (shader == null) {
            shader = new MarsFogShader(settings);
            shader.init();
        }
        return shader;
    }

    @Override
    public void dispose() {
        if (shader != null) shader.dispose();
    }
}
