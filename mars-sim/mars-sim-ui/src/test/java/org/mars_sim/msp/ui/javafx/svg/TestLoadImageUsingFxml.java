package org.mars_sim.msp.ui.javafx.svg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

public class TestLoadImageUsingFxml extends GuiTest {

	@BeforeClass
	public static void installSvgLoader() {
		SvgImageLoaderFactory.install();
	}

	@Override
	protected Parent getRootNode() {
		try {
			return FXMLLoader.load(getClass().getClassLoader().getResource(
					"svg/test/imagetest.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Test
	public void imageShouldBeVisible() {
		ImageView imageView = find("#TestImage");
		Image image = imageView.getImage();

		assertNotNull(image);
		assertFalse(image.errorProperty().get());
	}
}
