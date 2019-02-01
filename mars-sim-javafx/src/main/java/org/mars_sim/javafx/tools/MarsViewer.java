package org.mars_sim.javafx.tools;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MarsViewer extends Application {

	  private static final double MARS_RADIUS  = 400;
	  private static final double VIEWPORT_SIZE = 800;
	  private static final double ROTATE_SECS   = 30;

	  private static final double MAP_WIDTH  = 2048 / 2d;
	  private static final double MAP_HEIGHT = 1024 / 2d;

	  private static final String DIFFUSE_MAP = "/maps/Mars-Shaded-names-2k.jpg"; //"/maps/MarsV3-Shaded-2k.jpg";
	  private static final String NORMAL_MAP = "/maps/MarsNormalMap-2K.png"; //"/maps/MarsNormal2048x1024.png";
	  //private static final String SPECULAR_MAP = "/maps/rgbmars-spec-2k.jpg";

	  Group root;

	public MarsViewer() {
		root = buildScene();
	}

	public Group getRoot() {
		return root;
	}

	private Group buildScene() {
	    Sphere mars = new Sphere(MARS_RADIUS);
	    mars.setTranslateX(VIEWPORT_SIZE / 2d);
	    mars.setTranslateY(VIEWPORT_SIZE / 2d);

	    PhongMaterial material = new PhongMaterial();
	    material.setDiffuseMap(
	      new Image(this.getClass().getResource(DIFFUSE_MAP).toExternalForm(),
	        MAP_WIDTH,
	        MAP_HEIGHT,
	        true,
	        true
	      )
	    );

	    material.setBumpMap(
	      new Image(this.getClass().getResource(NORMAL_MAP).toExternalForm(),
	        MAP_WIDTH,
	        MAP_HEIGHT,
	        true,
	        true
	      )
	    );
/*
	    material.setSpecularMap(
	      new Image(this.getClass().getResource(SPECULAR_MAP).toExternalForm(),
	        MAP_WIDTH,
	        MAP_HEIGHT,
	        true,
	        true
	      )
	    );
*/
	    mars.setMaterial(
	        material
	    );

	    return new Group(mars);
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) {
		Group group = buildScene();

	    Scene scene = new Scene(
	      new StackPane(group),
	      VIEWPORT_SIZE, VIEWPORT_SIZE,
	      true,
	      SceneAntialiasing.BALANCED
	    );

	    scene.setFill(Color.rgb(10, 10, 40));

	    scene.setCamera(new PerspectiveCamera());

	    stage.setScene(scene);
	    stage.show();

	    stage.setFullScreen(true);

	    rotateAroundYAxis(group).play();
	}

	private RotateTransition rotateAroundYAxis(Node node) {
		RotateTransition rotate = new RotateTransition(
				Duration.seconds(ROTATE_SECS),
				node
				);
	    rotate.setAxis(Rotate.Y_AXIS);
	    rotate.setFromAngle(360);
	    rotate.setToAngle(0);
	    rotate.setInterpolator(Interpolator.LINEAR);
	    rotate.setCycleCount(RotateTransition.INDEFINITE);

	    return rotate;
	}
}