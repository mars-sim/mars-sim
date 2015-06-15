package com.jme3x.jfx;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.layout.Region;

public class FXMLHud<ControllerType> extends AbstractHud {

	private String			fxml;
	private ControllerType	controller;

	public FXMLHud(final String fxml) {
		this.fxml = fxml;
	}

	@Override
	protected Region innerInit() throws Exception {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		final URL location = Thread.currentThread().getContextClassLoader().getResource(this.fxml);
		fxmlLoader.setLocation(location);
		final ResourceBundle defaultRessources = fxmlLoader.getResources();
		fxmlLoader.setResources(this.addCustomRessources(defaultRessources));
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		final Region rv = fxmlLoader.load(location.openStream());
		this.controller = fxmlLoader.getController();
		assert FXMLUtils.assertInjection(this);
		return rv;
	}

	public ControllerType getController() {
		return this.controller;
	}

	/**
	 * Hook to add own Resourcebundles if necessary
	 * 
	 * @param defaultRessources
	 *            the currently set value
	 * @return
	 */
	protected ResourceBundle addCustomRessources(final ResourceBundle defaultRessources) {
		return defaultRessources;
	}
}
