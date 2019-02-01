package org.mars_sim.javafx.tools;

import javafx.animation.*;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EarthViewer extends Application {

  private static final double EARTH_RADIUS  = 400;
  private static final double VIEWPORT_SIZE = 800;
  private static final double ROTATE_SECS   = 30;

  private static final double MAP_WIDTH  = 8192 / 2d;
  private static final double MAP_HEIGHT = 4092 / 2d;

  private static final String DIFFUSE_MAP =
      "http://planetmaker.wthr.us/img/earth_gebco8_texture_8192x4096.jpg";
  private static final String NORMAL_MAP =
      "http://planetmaker.wthr.us/img/earth_normalmap_flat_8192x4096.jpg";
  private static final String SPECULAR_MAP =
      "http://planetmaker.wthr.us/img/earth_specularmap_flat_8192x4096.jpg";

  private Group buildScene() {
    Sphere earth = new Sphere(EARTH_RADIUS);
    earth.setTranslateX(VIEWPORT_SIZE / 2d);
    earth.setTranslateY(VIEWPORT_SIZE / 2d);

    PhongMaterial earthMaterial = new PhongMaterial();
    earthMaterial.setDiffuseMap(
      new Image(
        DIFFUSE_MAP,
        MAP_WIDTH,
        MAP_HEIGHT,
        true,
        true
      )
    );
    earthMaterial.setBumpMap(
      new Image(
        NORMAL_MAP,
        MAP_WIDTH,
        MAP_HEIGHT,
        true,
        true
      )
    );
    earthMaterial.setSpecularMap(
      new Image(
        SPECULAR_MAP,
        MAP_WIDTH,
        MAP_HEIGHT,
        true,
        true
      )
    );

    earth.setMaterial(
        earthMaterial
    );

    return new Group(earth);
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

  public static void main(String[] args) {
    launch(args);
  }
}