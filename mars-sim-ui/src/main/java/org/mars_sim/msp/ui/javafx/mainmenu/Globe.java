/**
 * Mars Simulation Project
 * Mars3DGlobe.java
 * @version 3.08 2015-11-27
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import static javafx.scene.input.KeyCode.*;

import org.mars_sim.msp.ui.javafx.mainmenu.Xform;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.scene.Node;


public class Globe { 
    
    double ONE_FRAME = 1.0 / 24.0;
    double DELTA_MULTIPLIER = 200.0;
    double CONTROL_MULTIPLIER = 0.1;
    double SHIFT_MULTIPLIER = 0.1;
    double ALT_MULTIPLIER = 0.5;

    // Earth
    //Image sImage = new Image(this.getClass().getResource("/maps/earth-s.jpg").toExternalForm());
    //Image dImage = new Image(this.getClass().getResource("/maps/earth-d.jpg").toExternalForm());
    //Image nImage = new Image(this.getClass().getResource("/maps/earth-n.jpg").toExternalForm()); //.toString());
    //Image siImage = new Image(this.getClass().getResource("/maps/earth-l.jpg").toExternalForm()); //.toString());

    // Earth. Maps from planetmaker
    //Image dImage = new Image("http://planetmaker.wthr.us/img/earth_gebco8_texture_1024x512.jpg");
    //Image nImage = new Image("http://planetmaker.wthr.us/img/earth_normalmap_flat_1024x512.jpg");
 	//Image sImage = new Image("http://planetmaker.wthr.us/img/earth_specularmap_flat_1024x512.jpg");

    	  
 	// Mars 
	//Image sImage = new Image(this.getClass().getResource("/maps/Mars_Clouds.jpg").toExternalForm());
    //Image dImage = new Image(this.getClass().getResource("/maps/Mars_Map.jpg").toExternalForm());
    //Image nImage = new Image(this.getClass().getResource("/maps/Mars_Normal.jpg").toExternalForm()); //.toString());
    //Image siImage = new Image(this.getClass().getResource("/maps/Mars_Clouds.jpg").toExternalForm()); //.toString());

    // Mars 2k maps
    Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec-2k.jpg").toExternalForm());
    Image dImage = new Image(this.getClass().getResource("/maps/Mars-Shaded-names-2k.jpg").toExternalForm());
    Image nImage = new Image(this.getClass().getResource("/maps/MarsNormalMap-2K.png").toExternalForm()); //.toString());

    //Image siImage = new Image(this.getClass().getResource("/maps/rgbmars-names-2k.png").toExternalForm()); //.toString());
    //Image siImage = new Image(this.getClass().getResource("/maps/names-2k-grey.png").toExternalForm()); //.toString());

    // Mars 1k maps
    //Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec1k.jpg").toExternalForm());
    //Image dImage = new Image(this.getClass().getResource("/maps/MarsV3Shaded1k.jpg").toExternalForm());
    //Image nImage = new Image(this.getClass().getResource("/maps/MarsNormal1k.png").toExternalForm()); //.toString());
    //Image siImage = new Image(this.getClass().getResource("/maps/rgbmars-names-1k.png").toExternalForm()); //.toString());
	
    // Mars 1k maps
	//Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec1k.jpg").toExternalForm());
    //Image dImage = new Image(this.getClass().getResource("/maps/mars_1k_color.jpg").toExternalForm());
    //Image nImage = new Image(this.getClass().getResource("/maps/mars_1k_normal.jpg").toExternalForm()); //.toString());
    //Image nImage = new Image(this.getClass().getResource("/maps/MarsNormal1k.png").toExternalForm()); //.toString());
    //Image siImage = new Image(this.getClass().getResource("/maps/rgbmars-names-1k.png").toExternalForm()); //.toString());

	private final BooleanProperty diffuseMap = new SimpleBooleanProperty(true);
    private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);

    final Group root = new Group();
    final Group axisGroup = new Group();
    
    final PerspectiveCamera camera = new PerspectiveCamera(true);  
    final double cameraDistance = 1450;//450;
    
    final Xform world = new Xform();
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final Xform sphereGroup = new Xform();
    
    private Timeline timeline;
    
    boolean timelinePlaying = false;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;


	public Globe() {
	    root.getChildren().addAll(world);
	    buildCamera();
	    buildSphereGroup();	
	}

	public Parent getRoot() {
		return root;
	}

	public Xform getWorld() {
		return world;
	}
	
    private void buildCamera() {
	    root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(0);//180.0);

        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-cameraDistance);
        cameraXform.ry.setAngle(0.0);//320.0);
        cameraXform.rx.setAngle(-20);//40);
    }
    
    public PerspectiveCamera getCamera(){ //StackPane root) {
    	return camera;
    }

    private void buildSphereGroup() {

        final PhongMaterial material = new PhongMaterial();
        
        material.setDiffuseColor(Color.WHITE);//TRANSPARENT);//BROWN);
        material.diffuseMapProperty().bind(Bindings.when(diffuseMap).then(dImage).otherwise((Image) null));
        
        material.setSpecularColor(Color.TRANSPARENT);
        material.specularMapProperty().bind(Bindings.when(specularMap).then(sImage).otherwise((Image) null));
        
        material.bumpMapProperty().bind(Bindings.when(bumpMap).then(nImage).otherwise((Image) null));
        
        //material.selfIlluminationMapProperty().bind(Bindings.when(selfIlluminationMap).then(siImage).otherwise((Image) null));

        Xform marsXform = new Xform();
        Sphere mars = new Sphere(300.0);
        mars.setMaterial(material);
        marsXform.getChildren().add(mars);
        sphereGroup.getChildren().add(marsXform);

        world.getChildren().addAll(sphereGroup);//, ambientXform);
    }

    void handleMouse(Scene scene) {//, final Node root) {
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = -(mousePosX - mouseOldX);
                mouseDeltaY = -(mousePosY - mouseOldY);

                double modifier = 1.0;
                double modifierFactor = 0.1;

                if (me.isControlDown()) {
                    modifier = 0.1;
                }
                if (me.isShiftDown()) {
                    modifier = 10.0;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 1.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 1.0);  // -
                } else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX * modifierFactor * modifier;
                    camera.setTranslateZ(newZ);
                } else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * modifierFactor * modifier * 0.3);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * modifierFactor * modifier * 0.3);  // -
                }
            }
        });
    }

    void handleKeyboard(Scene scene) {//, final Node root) {
        final boolean moveCamera = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                Duration currentTime;
                switch (event.getCode()) {
                    case Z:
                        if (event.isShiftDown()) {
                            cameraXform.ry.setAngle(0.0);
                            cameraXform.rx.setAngle(0.0);
                            camera.setTranslateZ(-300.0);
                        }
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        break;
                    case X:
                        if (event.isControlDown()) {
                            if (axisGroup.isVisible()) {
                                axisGroup.setVisible(false);
                            } else {
                                axisGroup.setVisible(true);
                            }
                        }
                        break;
                    case S:
                        if (event.isControlDown()) {
                            if (sphereGroup.isVisible()) {
                                sphereGroup.setVisible(false);
                            } else {
                                sphereGroup.setVisible(true);
                            }
                        }
                        break;
                    case SPACE:
                        if (timelinePlaying) {
                            timeline.pause();
                            timelinePlaying = false;
                        } else {
                            timeline.play();
                            timelinePlaying = true;
                        }
                        break;
                    case UP:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case DOWN:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                        } else if (event.isShiftDown()) {
                            double z = camera.getTranslateZ();
                            double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                            camera.setTranslateZ(newZ);
                        }
                        break;
                    case RIGHT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                        }
                        break;
                    case LEFT:
                        if (event.isControlDown() && event.isShiftDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown() && event.isShiftDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                        } else if (event.isControlDown()) {
                            cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                        } else if (event.isAltDown()) {
                            cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                        }
                        break;
                }
            }
        });
    }
}