package org.mars_sim.msp.ui.javafx.svg;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

public class TestLoadImageUsingClass extends GuiTest {

	@BeforeClass
	public static void installSvgLoader() {
		SvgImageLoaderFactory.install();
	}

	@Override
	protected Parent getRootNode() {
		InputStream imageData = this.getClass().getClassLoader()
				.getResourceAsStream("svg/test/bacon.svg");

		Image image = new Image(imageData);
		ImageView imageView = new ImageView(image);
		imageView.setId("TestImage");

		return new AnchorPane(imageView);
	}

	@Test
	public void imageShouldBeVisible() {
		ImageView imageView = find("#TestImage");
		Image image = imageView.getImage();

		assertNotNull(image);
		assertFalse(image.errorProperty().get());
	}
}
