package com.jme3x.jfx;

import org.lwjgl.opengl.Display;

import com.jme3.app.SimpleApplication;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh.Type;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.system.AppSettings;
import com.jme3x.jfx.cursor.proton.ProtonCursorProvider;
import com.jme3x.jfx.window.FXMLWindow;

public class TestIsCovered extends SimpleApplication {
	private static boolean	assertionsEnabled;

	public static void main(final String[] args) {
		assert TestIsCovered.enabled();
		if (!TestIsCovered.assertionsEnabled) {
			throw new RuntimeException("Assertions must be enabled (vm args -ea");
		}
		final AppSettings settings = new AppSettings(true);
		final TestIsCovered t = new TestIsCovered();
		t.setSettings(settings);
		t.start();
	}

	private static boolean enabled() {
		TestIsCovered.assertionsEnabled = true;
		return true;
	}

	@Override
	public void simpleInitApp() {
		this.setPauseOnLostFocus(false);
		this.flyCam.setEnabled(false);
		this.viewPort.setBackgroundColor(ColorRGBA.Black);

		final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, false, new ProtonCursorProvider(this, this.assetManager, this.inputManager));
		/**
		 * 2d gui, use the default input provider
		 */
		this.inputManager.addRawInputListener(testguiManager.getInputRedirector());

		final FXMLHud<EmptyController> testhud = new FXMLHud<>("com/jme3x/jfx/partial_screen.fxml");
		testhud.precache();
		testguiManager.attachHudAsync(testhud);

		final FXMLWindow<Testcontroller> testwindow = new FXMLWindow<>("com/jme3x/jfx/loading_screen.fxml");
		testwindow.precache();
		testwindow.titleProperty().set("TestTitle");
		testguiManager.attachHudAsync(testwindow);

		Display.setResizable(true);

		this.setupFire();
	}

	@Override
	public void simpleUpdate(final float tpf) {
		if (Display.wasResized()) {
			// keep settings in sync with the actual Display
			int w = Display.getWidth();
			int h = Display.getHeight();
			if (w < 2) {
				w = 2;
			}
			if (h < 2) {
				h = 2;
			}
			this.settings.setWidth(Display.getWidth());
			this.settings.setHeight(Display.getHeight());
			this.reshape(this.settings.getWidth(), this.settings.getHeight());
		}
	}

	// fire should follow the mouse when mouse is not over javafx item
	public void setupFire() {
		final ParticleEmitter fire = new ParticleEmitter("Emitter", Type.Triangle, 30);
		final Material mat = new Material(this.assetManager, "Common/MatDefs/Misc/Particle.j3md");
		fire.setMaterial(mat);
		fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f)); // red
		fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
		fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
		fire.setStartSize(1.0f);
		fire.setEndSize(0.1f);
		fire.setGravity(0, 0, 0);
		fire.setLowLife(0.5f);
		fire.setHighLife(3f);
		fire.getParticleInfluencer().setVelocityVariation(0.3f);
		this.rootNode.attachChild(fire);
		this.inputManager.addMapping("mouseMove", new MouseAxisTrigger(MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_X, false), new MouseAxisTrigger(MouseInput.AXIS_Y, true), new MouseAxisTrigger(MouseInput.AXIS_Y, false));
		this.inputManager.addListener(new AnalogListener() {
			@Override
			public void onAnalog(final String name, final float value, final float tpf) {
				final Vector2f click2d = TestIsCovered.this.inputManager.getCursorPosition();
				final Vector3f click3d = TestIsCovered.this.cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0.95f).clone();
				fire.setLocalTranslation(click3d);
			}
		}, "mouseMove");
	}
}