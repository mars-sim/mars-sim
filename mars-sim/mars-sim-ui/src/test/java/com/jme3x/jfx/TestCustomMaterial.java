package com.jme3x.jfx;

import org.lwjgl.opengl.Display;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3x.jfx.cursor.proton.ProtonCursorProvider;
import com.jme3x.jfx.window.FXMLWindow;

public class TestCustomMaterial extends SimpleApplication {
	private static boolean	assertionsEnabled;

	public static void main(final String[] args) {
		assert TestCustomMaterial.enabled();
		if (!TestCustomMaterial.assertionsEnabled) {
			throw new RuntimeException("Assertions must be enabled (vm args -ea");
		}
		new TestCustomMaterial().start();
	}

	private static boolean enabled() {
		TestCustomMaterial.assertionsEnabled = true;
		return true;
	}

	@Override
	public void simpleInitApp() {
		this.setPauseOnLostFocus(false);
		this.flyCam.setDragToRotate(true);
		this.viewPort.setBackgroundColor(ColorRGBA.Red);

		final Material testMaterial = new Material(this.assetManager, "com/jme3x/jfx/FalseColorGui.j3md");

		final GuiManager testguiManager = new GuiManager(this.guiNode, this.assetManager, this, false, new ProtonCursorProvider(this, this.assetManager, this.inputManager), testMaterial);
		/**
		 * 2d gui, use the default input provider
		 */
		this.inputManager.addRawInputListener(testguiManager.getInputRedirector());

		final FXMLHud testhud = new FXMLHud("com/jme3x/jfx/loading_screen.fxml");
		testhud.precache();
		testguiManager.attachHudAsync(testhud);

		final FXMLWindow testwindow = new FXMLWindow("com/jme3x/jfx/loading_screen.fxml");
		testwindow.precache();
		testwindow.titleProperty().set("TestTitle");
		testguiManager.attachHudAsync(testwindow);

		Display.setResizable(true);
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
}
