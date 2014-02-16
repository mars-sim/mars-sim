package org.mars_sim.msp.ui.ogl.sandbox;

import org.mars_sim.msp.ui.ogl.sandbox.scene.GLDisplay;
import org.mars_sim.msp.ui.ogl.sandbox.scene.SceneGroup;
import org.mars_sim.msp.ui.ogl.sandbox.scene.Util;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sphere.SphereIcosahedronSolid;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sphere.SphereLongLatSolid;
import org.mars_sim.msp.ui.ogl.sandbox.scene.sphere.SpherePlanet;

/**
 * a little opengl demo. sensitive to mouse drag and mouse wheel and WASD+.
 * @author stpa
 */
public class MainOGL {

	private static final String MARS_TOPO = "jmars/jmars_MOLA_128ppd_shade_ne_-_2048_x_1024.png";
	private static final String MARS_GEO = "jmars/jmars_viking_geologic_map_skinner_et_al_2006_-_2048_x_1024.png";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GLDisplay glDisplay = GLDisplay.createGLDisplay("hello worlds");
		Display display = new Display();
		Input input = new Input(display,glDisplay);
		double[] cameraPosition = {0.0d,0.0d,-10.0d};
		double[] cameraRotation = {90.0d,0.0d,0.0d};
		double[] cameraRotationDelta = {0.0d,0.0d,3.0d};
		double[] scale = {1.0d,1.0d,1.0d};
		SceneGroup scene = new SceneGroup(
			cameraPosition,
			cameraRotation,
			cameraRotationDelta,
			scale
		);

		spheres(scene,12,0.5d,2.5d);

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
	 * and add some versions of mars.
	 * @param scene
	 */
	private static void spheres(SceneGroup scene,int spheres,double minRad,double maxRad) {
		for (int i = 0; i < spheres; i++) {
			scene.addSubobject(
				new SphereIcosahedronSolid(
					Util.rnd3(-32d,32d), // position
					Util.nul3(), // direction
					Util.nul3(), // autorotation
					minRad + Math.random() * (maxRad - minRad), // radius
					0 + (i % 3), // recursion depth
					Util.rnd31(0.2d), // primary color
					Util.rnd31(0.2d,1.0d) // secondary color
				)
			);
			scene.addSubobject(
				new SphereLongLatSolid(
					Util.rnd3(-32d,32d),
					Util.nul3(),
					Util.nul3(),
					minRad + Math.random() * (maxRad - minRad),
					Util.rnd(4,16),
					Util.rnd(8,32),
					Util.rnd31(0.2d),
					Util.rnd31(0.2d,1.0d)
				)
			);
		}
		for (int i = 0; i < 2; i++) {
			scene.addSubobject(
				new SpherePlanet(
					new double[] {5d,5d + i * 20d,0d},
					Util.nul3(),
					new double[] {8d + 8 * 2d,0d,0d},
					3.0d + i,
					MARS_TOPO,
					"topographical map of mars",
					new double[] {1d,0.85d,0.5d,1d}
				)
			);
			scene.addSubobject(
				new SpherePlanet(
					new double[] {-5d,-5d - i * 20d,0d},
					Util.nul3(),
					new double[] {-8d - i * 8d,0d,0d},
					4.0d + i,
					MARS_GEO,
					"geological map of mars",
					new double[] {1.0d,1.0d,1.0d,1.0d}
				)
			);
		}
	}
}
