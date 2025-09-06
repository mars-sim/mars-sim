/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;
import org.mars_sim.msp.libgdx.surface3d.terrain.CubeSpherePlanetLod;
import org.mars_sim.msp.libgdx.surface3d.terrain.TerrainConfig;

public class Surface3DApp extends ApplicationAdapter {

    // 1 unit == 1 kilometer to avoid float precision issues
    public static final float MARS_RADIUS_KM = 3396.2f;

    private PerspectiveCamera cam;
    private ModelBatch batch;
    private Environment env;

    private CubeSpherePlanetLod planetLod;
    private OrbitCameraController orbit;

    @Override
    public void create() {
        // Camera
        cam = new PerspectiveCamera(67f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.near = 0.1f;
        cam.far = 20000f; // 20,000 km draw distance
        float startDistance = MARS_RADIUS_KM * 3.2f;
        cam.position.set(0f, 0f, startDistance);
        cam.lookAt(0f, 0f, 0f);
        cam.up.set(0, 1, 0);
        cam.update();

        // Scene + lights + fog shader
        MarsFogShader.Settings fog = MarsFogShader.Settings.marsDefaults(MARS_RADIUS_KM);
        batch = new ModelBatch(new MarsFogShaderProvider(fog));

        env = new Environment();
        env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.45f, 0.45f, 0.45f, 1f));
        env.add(new DirectionalLight().set(1.0f, 0.98f, 0.93f, -0.7f, -0.9f, -0.3f)); // sun-ish

        // LOD terrain setup
        TerrainConfig cfg = TerrainConfig.defaults();
        Material marsMat = MarsMaterials.dusty();

        // Height function can be replaced later with MOLA sampling; noise is a placeholder.
        planetLod = new CubeSpherePlanetLod(
                MARS_RADIUS_KM,
                new NoiseHeightFunction(/* amplitudeKm= */ 2.5f, /* frequency= */ 8f, /* octaves= */ 3),
                cfg,
                marsMat
        );

        // Input: orbit around planet
        orbit = new OrbitCameraController(cam, /*targetX=*/0, /*targetY=*/0, /*targetZ=*/0);
        orbit.setDistance(startDistance);
        orbit.setClampDistance(MARS_RADIUS_KM * 1.02f, MARS_RADIUS_KM * 12f);
        orbit.setRotateSpeed(0.4f);
        orbit.setScrollZoomStrength(0.12f);
        InputMultiplexer mux = new InputMultiplexer(orbit);
        Gdx.input.setInputProcessor(mux);

        // GPU state
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
    }

    @Override
    public void render() {
        float dt = MathUtils.clamp(Gdx.graphics.getDeltaTime(), 0f, 1f / 30f);
        orbit.update(dt);

        // Update LOD selection for current camera
        planetLod.update(cam);

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.035f, 1f); // near-black space
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        batch.begin(cam);
        batch.render(planetLod, env);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        cam.viewportWidth = width;
        cam.viewportHeight = height;
        cam.update();
    }

    @Override
    public void dispose() {
        if (planetLod != null) planetLod.dispose();
        if (batch != null) batch.dispose();
    }

    // Simple helper materials for Mars-y look
    static final class MarsMaterials {
        static Material dusty() {
            // Clay red/orange diffuse with slight warm tint
            Color marsDust = new Color(0xC95E3BFF); // RGBA8888 literal normalized by LibGDX
            return new Material(ColorAttribute.createDiffuse(marsDust));
        }
    }
}
