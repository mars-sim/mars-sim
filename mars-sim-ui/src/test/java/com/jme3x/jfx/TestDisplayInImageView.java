package com.jme3x.jfx;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.RawInputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.system.AppSettings;
import com.jme3.util.SkyFactory;
import com.jme3.util.TangentBinormalGenerator;
import com.jme3.util.TempVars;
import com.jme3x.jfx.injfx.JmeForImageView;
import com.jme3x.jfx.util.JFXUtils;

public class TestDisplayInImageView extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		final FXMLLoader fxmlLoader = new FXMLLoader();
		final URL location = Thread.currentThread().getContextClassLoader().getResource(this.getClass().getCanonicalName().replace('.', '/')+".fxml");
		fxmlLoader.setLocation(location);
		//final ResourceBundle defaultRessources = fxmlLoader.getResources();
		//fxmlLoader.setResources(this.addCustomRessources(defaultRessources));
		fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
		final Region root = fxmlLoader.load(location.openStream());
		Controller controller = fxmlLoader.getController();

		JmeForImageView jme = new JmeForImageView();
		jme.bind(controller.image);

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		      public void handle(WindowEvent e){
				jme.stop(true);
		      }
		});

		bindOtherControls(jme, controller);
		jme.enqueue(TestDisplayInImageView::createScene);

		Scene scene = new Scene(root, 600, 400);
		stage.setTitle(this.getClass().getSimpleName());
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		Platform.exit();
	}

	static void bindOtherControls(JmeForImageView jme, Controller controller) {
		controller.bgColor.valueProperty().addListener((ov, o, n) -> {
			jme.enqueue((jmeApp) -> {
				jmeApp.getViewPort().setBackgroundColor(new ColorRGBA((float)n.getRed(), (float)n.getGreen(), (float)n.getBlue(), (float)n.getOpacity()));
				return null;
			});
		});
		controller.bgColor.setValue(Color.LIGHTGRAY);

		controller.showStats.selectedProperty().addListener((ov, o, n) -> {
			jme.enqueue((jmeApp) -> {
				jmeApp.setDisplayStatView(n);
				jmeApp.setDisplayFps(n);
				return null;
			});
		});
		controller.showStats.setSelected(!controller.showStats.isSelected());

		controller.fpsReq.valueProperty().addListener((ov, o, n) -> {
			jme.enqueue((jmeApp) -> {
				AppSettings settings = new AppSettings(false);
				settings.setFullscreen(false);
				settings.setUseInput(false);
				settings.setFrameRate(n.intValue());
				settings.setCustomRenderer(com.jme3x.jfx.injfx.JmeContextOffscreenSurface.class);
				jmeApp.setSettings(settings);
				jmeApp.restart();
				return null;
			});
		});
		controller.fpsReq.setValue(30);
	}

	/**
	 * Create a similar scene to Tutorial "Hello Material" but without texture
	 * http://hub.jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_material
	 *
	 * @param jmeApp the application where to create a Scene
	 */
	static boolean createScene(SimpleApplication jmeApp) {
		Node rootNode = jmeApp.getRootNode();
		AssetManager assetManager = jmeApp.getAssetManager();

		 //com.jme3.app.Application.setPauseOnLostFocus(false);
		 //com.jme3.app.Application.flyCam.setDragToRotate(true);
		 //com.jme3.app.Application.flyCam.setMoveSpeed(20);


    	Spatial gameLevel = assetManager.loadModel("/Scenes/town/main.scene");
    	gameLevel.setLocalTranslation(0, -5.2f, 0);
    	gameLevel.setLocalScale(2);
    	rootNode.attachChild(gameLevel);

        Spatial teapot = assetManager.loadModel("Models/Teapot/Teapot.obj");
        Material mat_default = new Material(
            assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");
        teapot.setMaterial(mat_default);
        rootNode.attachChild(teapot);

        // Create a wall with a simple texture from test_data
        Box box = new Box(2.5f,2.5f,1.0f);
        Spatial wall = new Geometry("Box", box );
        Material mat_brick = new Material(
            assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat_brick.setTexture("ColorMap",
            assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
        wall.setMaterial(mat_brick);
        wall.setLocalTranslation(2.0f,-2.5f,0.0f);
        rootNode.attachChild(wall);

        // Load a model from test_data (OgreXML + material + texture)
        Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        ninja.scale(0.05f, 0.05f, 0.05f);
        ninja.rotate(0.0f, -3.0f, 0.0f);
        ninja.setLocalTranslation(0.0f, -5.0f, -2.0f);
        rootNode.attachChild(ninja);

        // You must add a light to make the model visible
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

        // Add a sky
        Spatial sky = SkyFactory.createSky(
                assetManager, "Textures/Sky/desertplains.jpg", true);
        rootNode.attachChild(sky);
        sky.setQueueBucket(Bucket.Sky);

        InputManager inputManager = jmeApp.getInputManager();
	    inputManager.addMapping("Pause",  new KeyTrigger(KeyInput.KEY_P));
	    inputManager.addMapping("Left",   new KeyTrigger(KeyInput.KEY_J));
	    inputManager.addMapping("Right",  new KeyTrigger(KeyInput.KEY_K));
	    inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_SPACE), // spacebar!
	                                      new MouseButtonTrigger(MouseInput.BUTTON_LEFT) );        // left click!
	    /** Add the named mappings to the action listeners. */
	    //inputManager.addListener(actionListener,"Pause");
	    //inputManager.addListener(analogListener,"Left", "Right", "Rotate");
/*
	    Camera cam = new Camera();

	    inputManager.addRawInputListener(new RawInputListener() {

            @Override
            public void onTouchEvent(TouchEvent evt) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMouseMotionEvent(MouseMotionEvent evt) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onMouseButtonEvent(MouseButtonEvent evt) {
                CollisionResults results = new CollisionResults();

                Vector2f click2d = inputManager.getCursorPosition();
                Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
                Ray ray = new Ray(click3d, dir);
                rootNode.collideWith(ray, results);

                CollisionResult collision = results.getClosestCollision();
                if (collision == null) {
                    return;
                }
                Geometry geom = collision.getGeometry();
                //JmeFxTextureContainer container = containers.get(geom);
                //if (container == null) {
                //    return;
                //}

                int ti = collision.getTriangleIndex();
                TempVars tmp = TempVars.get();
                try {
                    Vector3f p0 = tmp.vect1;
                    Vector3f p1 = tmp.vect2;
                    Vector3f p2 = tmp.vect3;
                    Vector2f t0 = tmp.vect2d;
                    Vector2f t1 = tmp.vect2d2;
                    Vector2f t2 = new Vector2f();


                    JFXUtils.getTriangle(geom.getMesh(),VertexBuffer.Type.Position, ti, p0, p1, p2);
                    JFXUtils.getTriangle(geom.getMesh(),VertexBuffer.Type.TexCoord, ti, t0, t1, t2);

                    Vector3f cp = collision.getContactPoint();
                    geom.worldToLocal(cp,cp);

                    Vector3f vn = p2.subtract(p1, tmp.vect4).crossLocal(p1.subtract(p0,tmp.vect5));
                    float A = vn.length();
                    Vector3f n = tmp.vect6.set(vn).divideLocal(A);
                    float u = FastMath.abs((p2.subtract(p1,tmp.vect7).crossLocal(cp.subtract(p1,tmp.vect8))).dot(n) / A);
                    float v = FastMath.abs((p0.subtract(p2,tmp.vect7).crossLocal(cp.subtract(p2,tmp.vect8))).dot(n) / A);
                    float w = 1 - u - v;

                    float s = t0.x * u + t1.x * v + t2.x * w;
                    float t = t0.y * u + t1.y * v + t2.y * w;

                    float x = container.pWidth * s + 0.5f;
                    float y = container.pHeight * (1 - t) + 0.5f;

                    MouseButtonEvent nevt = new MouseButtonEvent(evt.getButtonIndex(), evt.isPressed(), (int) x, (int) y);
                    nevt.setTime(evt.getTime());
                    container.getInputListener().onMouseButtonEvent(nevt);
                    if (nevt.isConsumed()) {
                        evt.setConsumed();
                    }

                } finally {
                    tmp.release();
                }

            }

            @Override
            public void onKeyEvent(KeyInputEvent evt) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onJoyButtonEvent(JoyButtonEvent evt) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onJoyAxisEvent(JoyAxisEvent evt) {
                // TODO Auto-generated method stub

            }

            @Override
            public void endInput() {
                // TODO Auto-generated method stub

            }

            @Override
            public void beginInput() {
                // TODO Auto-generated method stub

            }
        });


*/
		return true;
	}



	public static class Controller {

		@FXML
		public ImageView image;

		@FXML
		public ColorPicker bgColor;

		@FXML
		public CheckBox showStats;

		@FXML
		private Label fpsLabel;

		@FXML
		public Slider fpsReq;

		@FXML
		public void initialize() {
			//To resize image when parent is resize
			//image is wrapped into a "VBOX" or "HBOX" to allow resize smaller
			//see http://stackoverflow.com/questions/15951284/javafx-image-resizing
			Pane p = (Pane)image.getParent();
			image.fitHeightProperty().bind(p.heightProperty());
			image.fitWidthProperty().bind(p.widthProperty());

			fpsReq.valueProperty().addListener((ov, o, n) -> fpsLabel.setText(String.format("fps : %4d", n.intValue())));
			image.setPreserveRatio(false);
		}

	}
}
