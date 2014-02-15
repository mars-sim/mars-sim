package org.mars_sim.msp.ui.ogl.sandbox;

import org.mars_sim.msp.ui.ogl.sandbox.scene.GLDisplay;
import org.mars_sim.msp.ui.ogl.sandbox.scene.SceneGroup;
import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sphere.SphereIcosahedronSolid;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sphere.SphereLongLatSolid;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sphere.SphereTextured;

/**
 * a little opengl demo. sensitive to mouse drag and mouse wheel and WASD+.
 * @author stpa
 */
public class MainOGL {

	private static final String MARS_TOPO = "jmars/jmars_MOLA_128ppd_shade_ne_-_1024_x_512.png";
	private static final String MARS_GEO = "jmars/jmars_viking_geologic_map_skinner_et_al_2006_-_1024_x_512.png";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GLDisplay glDisplay = GLDisplay.createGLDisplay("hello worlds");
		Display display = new Display();
		Input input = new Input(display,glDisplay);
		double[] cameraPosition = {0.0d,0.0d,-10.0d};
		double[] cameraRotation = {90.0d,0.0d,0.0d};
		double[] cameraDelta    = {0.0d,0.0d,3.0d};
		double[] scale          = {1.0d,1.0d,1.0d};
		SceneGroup scene = new SceneGroup(
			cameraPosition,
			cameraRotation,
			cameraDelta,
			scale
		);

		spheres(scene,12,1.5d,4.5d);

		display.setScene(scene);

		glDisplay.addGLEventListener(display);
		glDisplay.addKeyListener(input);
		glDisplay.addMouseListener(input);
		glDisplay.addMouseMotionListener(input);
		glDisplay.addMouseWheelListener(input);

		glDisplay.start();
	}

	/**
	 * adds some random spheres to the scene.
	 * and add two versions of mars.
	 * @param scene
	 */
	private static void spheres(SceneGroup scene,int spheres,double minRad,double maxRad) {
		for (int i = 0; i < spheres; i++) {
			scene.addSubobject(
				new SphereIcosahedronSolid(
					Util.rnd3(-32.0f,32.0f),
					Util.nul3(),
					minRad + Math.random() * (maxRad - minRad),
					0 + (i % 3),
					Util.rnd31(0.2f),
					Util.rnd31(0.2f,1.0f)
				)
			);
			scene.addSubobject(
				new SphereLongLatSolid(
					Util.rnd3(-32.0f,32.0f),
					Util.nul3(),
					minRad + Math.random() * (maxRad - minRad),
					Util.rnd(4,16),
					Util.rnd(8,32),
					Util.rnd31(0.2f),
					Util.rnd31(0.2f,1.0f)
				)
			);
		}
		scene.addSubobject(
			new SphereTextured(
				new double[] {5.0,5.0,0.0},
				Util.nul3(),
				5.0d,
				MARS_TOPO,
				new double[] {1.0d,0.85d,0.5d,1.0d}
			)
		);
		scene.addSubobject(
			new SphereTextured(
				new double[] {-5.0,-5.0,0.0},
				Util.nul3(),
				5.0d,
				MARS_GEO,
				new double[] {1.0d,1.0d,1.0d,1.0d}
			)
		);
	}
}
