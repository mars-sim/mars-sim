package org.mars_sim.msp.ui.javafx.svg;

import static org.junit.Assert.assertNotNull;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import org.junit.BeforeClass;
import org.junit.Test;
import org.loadui.testfx.GuiTest;

public class TestLoadImageUsingCss extends GuiTest {

	@BeforeClass
	public static void installSvgLoader() {
		SvgImageLoaderFactory.install();
	}

	@Override
	protected Parent getRootNode() {
		String css = getClass().getClassLoader().getResource("svg/test/imagetest.css")
				.toExternalForm();

		Button button = new Button("Test");
		button.setId("TestButton");

		AnchorPane anchorPane = new AnchorPane(button);
		anchorPane.getStylesheets().add(css);

		return anchorPane;
	}

	@Test
	public void imageShouldBeVisible() {
		Button button = find("#TestButton");
		Node image = button.getGraphic();

		assertNotNull(image);
	}
}
