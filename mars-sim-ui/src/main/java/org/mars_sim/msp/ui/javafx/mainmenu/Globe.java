/**
 * Mars Simulation Project
 * Globe.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.mainmenu;

import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.PerspectiveCamera;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import org.mars_sim.msp.ui.javafx.mainmenu.Xform;

public class Globe {

	protected double ONE_FRAME = 1.0 / 24.0;
	protected double DELTA_MULTIPLIER = 200.0;
	protected double CONTROL_MULTIPLIER = 0.1;
	protected double SHIFT_MULTIPLIER = 0.1;
	protected double ALT_MULTIPLIER = 0.5;

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
	protected Image sImage = new Image(this.getClass().getResource("/maps/rgbmars-spec-2k.jpg").toExternalForm());
	protected Image dImage = new Image(this.getClass().getResource("/maps/Mars-Shaded-names-2k.jpg").toExternalForm());
	protected Image nImage = new Image(this.getClass().getResource("/maps/MarsNormalMap-2K.png").toExternalForm()); //.toString());

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
    //private final BooleanProperty specularMap = new SimpleBooleanProperty(true);
    private final BooleanProperty bumpMap = new SimpleBooleanProperty(true);
    //private final BooleanProperty selfIlluminationMap = new SimpleBooleanProperty(true);

    private Group root = new Group();
    private Group axisGroup = new Group();

    private StackPane stackPane;
    
    private PerspectiveCamera camera = new PerspectiveCamera(true);
    private final double cameraDistance = 1450;//1450;//450;

    private Xform world = new Xform();
    private Xform cameraXform = new Xform();
    private Xform cameraXform2 = new Xform();
    private Xform cameraXform3 = new Xform();
    private Xform sphereGroup = new Xform();

    private Timeline timeline;

    private boolean timelinePlaying = false;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;


	public Globe() {
		stackPane = new StackPane();
		stackPane.getChildren().add(world);
	    root.getChildren().addAll(stackPane);
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
	    stackPane.getChildren().add(cameraXform);
	    
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
        material.bumpMapProperty().bind(Bindings.when(bumpMap).then(nImage).otherwise((Image) null));
        material.setSpecularColor(Color.LIGHTGRAY);
        //material.selfIlluminationMapProperty().bind(Bindings.when(selfIlluminationMap).then(siImage).otherwise((Image) null));

        Xform marsXform = new Xform();
        Sphere mars = new Sphere(300.0);
        mars.setMaterial(material);
        marsXform.getChildren().add(mars);
        sphereGroup.getChildren().add(marsXform);

        world.getChildren().addAll(sphereGroup);//, ambientXform);
    }

    protected void handleMouse(Node scene) {//, final Node root) {
 /*   	scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
*/   	
    	scene.addEventFilter(MouseEvent.MOUSE_PRESSED,
                me -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseOldX = me.getSceneX();
                    mouseOldY = me.getSceneY();
                });
    	scene.addEventFilter(MouseEvent.MOUSE_DRAGGED,
                me -> {
                   	
                	if (mousePosX > root.getLayoutX()
                			&& mousePosX < root.getLayoutX() + SpinningGlobe.WIDTH
                			&& mousePosY > root.getLayoutY()
                			&& mousePosY < root.getLayoutY() + SpinningGlobe.HEIGHT) {
    	                mouseOldX = mousePosX;
    	                mouseOldY = mousePosY;
    	                mousePosX = me.getSceneX();
    	                mousePosY = me.getSceneY();
    	                mouseDeltaX = -(mousePosX - mouseOldX);
    	                mouseDeltaY = -(mousePosY - mouseOldY);
    	
    	                double modifier = 0.05;
    	                double modifierFactor = 3.5;
    	
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
/*    	
    	scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me) {
            	
            	if (mousePosX > root.getLayoutX()
            			&& mousePosX < root.getLayoutX() + SpinningGlobe.WIDTH
            			&& mousePosY > root.getLayoutY()
            			&& mousePosY < root.getLayoutY() + SpinningGlobe.HEIGHT) {
	                mouseOldX = mousePosX;
	                mouseOldY = mousePosY;
	                mousePosX = me.getSceneX();
	                mousePosY = me.getSceneY();
	                mouseDeltaX = -(mousePosX - mouseOldX);
	                mouseDeltaY = -(mousePosY - mouseOldY);
	
	                double modifier = 0.05;
	                double modifierFactor = 3.5;
	
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
            }
        });
*/        
    }

    protected void handleKeyboard(Node scene) {//, final Node root) {
        //final boolean moveCamera = true;
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                //Duration currentTime;
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
				default:
					break;
                }
            }
        });
    }
    
 	public void destroy() {
 		sImage = null;
 		dImage = null;
 		nImage = null;
 		root = null;
 	    axisGroup = null;
 	    stackPane = null;
 	    camera  = null;
 	    world = null;
 	    cameraXform2 = null;
 	    cameraXform = null;
 	    cameraXform3 = null;
 	    sphereGroup = null;
 	    timeline = null;
 	}
 	
}