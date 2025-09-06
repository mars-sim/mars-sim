/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package org.mars_sim.msp.libgdx.surface3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MarsFogShader implements Shader {

    // ======== Public settings you can tweak without touching shader code ========
    public static final class Settings {
        public float planetRadiusKm = 3396.2f;

        /** Base extinction coefficient in 1/km (larger → denser fog). Try 0.0015–0.008. */
        public float fogDensity = 0.0025f;

        /** Fog height falloff (scale height) in km; smaller → fog hugs surface more. Try 8–15. */
        public float fogHeightFalloffKm = 12f;

        /** Fog color (Martian dust): a warm orange-red. */
        public final Color fogColor = new Color(0.74f, 0.43f, 0.34f, 1f);

        /** Ambient fallback if Scene has none. */
        public final Color ambientFallback = new Color(0.55f, 0.55f, 0.55f, 1f);

        /** Sunlight color fallback. */
        public final Color sunColorFallback = new Color(1.0f, 0.98f, 0.93f, 1f);

        /** Sunlight direction fallback (from sun towards scene). */
        public final Vector3 sunDirFallback = new Vector3(-0.7f, -0.9f, -0.3f).nor();

        public static Settings marsDefaults(float planetRadiusKm) {
            Settings s = new Settings();
            s.planetRadiusKm = planetRadiusKm;
            return s;
        }
    }

    private final Settings settings;
    private ShaderProgram program;
    private Camera camera;
    private RenderContext context;

    // Cached uniforms
    private int u_projViewTrans, u_worldTrans, u_cameraPos, u_normalMatrix;
    private int u_albedo, u_ambientColor, u_dirLightDir, u_dirLightColor;
    private int u_fogColor, u_fogDensity, u_fogHeightFalloff, u_planetRadiusKm;

    private final Matrix3 tmpNormal = new Matrix3();

    public MarsFogShader(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void init() {
        ShaderProgram.pedantic = false; // be forgiving cross-platform
        program = new ShaderProgram(VERT_SRC, FRAG_SRC);
        if (!program.isCompiled())
            throw new GdxRuntimeException("MarsFogShader compile error:\n" + program.getLog());

        // Look up uniforms
        u_projViewTrans     = program.fetchUniformLocation("u_projViewTrans", true);
        u_worldTrans        = program.fetchUniformLocation("u_worldTrans", true);
        u_normalMatrix      = program.fetchUniformLocation("u_normalMatrix", true);
        u_cameraPos         = program.fetchUniformLocation("u_cameraPos", true);
        u_albedo            = program.fetchUniformLocation("u_diffuseColor", true);
        u_ambientColor      = program.fetchUniformLocation("u_ambientColor", true);
        u_dirLightDir       = program.fetchUniformLocation("u_dirLightDir", true);
        u_dirLightColor     = program.fetchUniformLocation("u_dirLightColor", true);
        u_fogColor          = program.fetchUniformLocation("u_fogColor", true);
        u_fogDensity        = program.fetchUniformLocation("u_fogDensity", true);
        u_fogHeightFalloff  = program.fetchUniformLocation("u_fogHeightFalloff", true);
        u_planetRadiusKm    = program.fetchUniformLocation("u_planetRadiusKm", true);
    }

    @Override
    public void dispose() {
        if (program != null) program.dispose();
    }

    @Override
    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;

        program.bind();
        context.setDepthTest(GL20.GL_LEQUAL);
        context.setCullFace(GL20.GL_BACK);
        context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        program.setUniformMatrix(u_projViewTrans, camera.combined);
        program.setUniformf(u_cameraPos, camera.position);

        // Static per-frame uniforms
        program.setUniformf(u_fogColor, settings.fogColor);
        program.setUniformf(u_fogDensity, settings.fogDensity);
        program.setUniformf(u_fogHeightFalloff, settings.fogHeightFalloffKm);
        program.setUniformf(u_planetRadiusKm, settings.planetRadiusKm);
    }

    @Override
    public void render(Renderable renderable) {
        // World transforms
        program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
        tmpNormal.set(renderable.worldTransform).inv().transpose();
        program.setUniformMatrix(u_normalMatrix, tmpNormal);

        // Albedo / diffuse color from material (fallback to neutral dust)
        Color diffuse = Color.BROWN; // fallback
        ColorAttribute diffAttr = (ColorAttribute) renderable.material.get(ColorAttribute.Diffuse);
        if (diffAttr != null) diffuse = diffAttr.color;
        program.setUniformf(u_albedo, diffuse);

        // Ambient & Sun from Environment (with sane fallbacks)
        Color ambient = settings.ambientFallback;
        Vector3 sunDir = settings.sunDirFallback;
        Color sunColor = settings.sunColorFallback;

        if (renderable.environment != null) {
            ColorAttribute amb = (ColorAttribute) renderable.environment.get(ColorAttribute.AmbientLight);
            if (amb != null) ambient = amb.color;

            DirectionalLightsAttribute dla = (DirectionalLightsAttribute)
                    renderable.environment.get(DirectionalLightsAttribute.Type);
            if (dla != null && dla.lights.size > 0) {
                DirectionalLight sun = dla.lights.get(0);
                sunDir.set(sun.direction).nor();
                sunColor.set(sun.color);
            }
        }
        program.setUniformf(u_ambientColor, ambient);
        program.setUniformf(u_dirLightDir, sunDir);
        program.setUniformf(u_dirLightColor, sunColor);

        // Issue draw
        renderable.meshPart.render(program);
    }

    @Override
    public void end() {
        // nothing extra
    }

    @Override
    public boolean canRender(Renderable instance) {
        // We only need position + normal; that's exactly what our planet mesh has.
        return instance.meshPart.mesh.getVertexAttributes().findByUsage(com.badlogic.gdx.graphics.VertexAttributes.Usage.Position) != null
            && instance.meshPart.mesh.getVertexAttributes().findByUsage(com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal) != null;
    }

    // ======================= GLSL sources =======================

    private static final String VERT_SRC =
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "attribute vec3 a_position;\n" +
            "attribute vec3 a_normal;\n" +
            "uniform mat4 u_projViewTrans;\n" +
            "uniform mat4 u_worldTrans;\n" +
            "uniform mat3 u_normalMatrix;\n" +
            "varying vec3 v_worldPos;\n" +
            "varying vec3 v_worldNormal;\n" +
            "void main() {\n" +
            "    vec4 wp = u_worldTrans * vec4(a_position, 1.0);\n" +
            "    v_worldPos = wp.xyz;\n" +
            "    v_worldNormal = normalize(u_normalMatrix * a_normal);\n" +
            "    gl_Position = u_projViewTrans * wp;\n" +
            "}\n";

    private static final String FRAG_SRC =
            "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "uniform vec3 u_cameraPos;\n" +
            "uniform vec3 u_diffuseColor;\n" +
            "uniform vec3 u_ambientColor;\n" +
            "uniform vec3 u_dirLightDir;   // direction FROM sun TOWARD scene\n" +
            "uniform vec3 u_dirLightColor;\n" +
            "uniform vec3 u_fogColor;\n" +
            "uniform float u_fogDensity;        // base density in 1/km\n" +
            "uniform float u_fogHeightFalloff;  // km\n" +
            "uniform float u_planetRadiusKm;\n" +
            "varying vec3 v_worldPos;\n" +
            "varying vec3 v_worldNormal;\n" +
            "\n" +
            "// Simple Lambert + height-aware exponential fog\n" +
            "void main() {\n" +
            "    vec3 N = normalize(v_worldNormal);\n" +
            "    vec3 L = normalize(-u_dirLightDir); // convert light-to-scene into scene-to-light\n" +
            "    float ndl = max(dot(N, L), 0.0);\n" +
            "    vec3 lit = u_diffuseColor * (u_ambientColor + u_dirLightColor * ndl);\n" +
            "\n" +
            "    // Fog: approximate path integral using average of camera+fragment altitudes\n" +
            "    float d = length(v_worldPos - u_cameraPos);            // km (our world units)\n" +
            "    float hFrag = max(length(v_worldPos) - u_planetRadiusKm, 0.0);\n" +
            "    float hCam  = max(length(u_cameraPos) - u_planetRadiusKm, 0.0);\n" +
            "    float avgH  = 0.5 * (hFrag + hCam);\n" +
            "    float density = u_fogDensity * exp(-avgH / max(u_fogHeightFalloff, 0.0001));\n" +
            "    float fogAmt = 1.0 - exp(-density * d);\n" +
            "    fogAmt = clamp(fogAmt, 0.0, 1.0);\n" +
            "\n" +
            "    vec3 color = mix(lit, u_fogColor, fogAmt);\n" +
            "    gl_FragColor = vec4(color, 1.0);\n" +
            "}\n";
}
