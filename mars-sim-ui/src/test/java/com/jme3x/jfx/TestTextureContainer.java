package com.jme3x.jfx;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;

import com.jme3.app.SimpleApplication;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.RawInputListener;
import com.jme3.input.event.JoyAxisEvent;
import com.jme3.input.event.JoyButtonEvent;
import com.jme3.input.event.KeyInputEvent;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.input.event.MouseMotionEvent;
import com.jme3.input.event.TouchEvent;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Quad;
import com.jme3.system.AppSettings;
import com.jme3.util.TempVars;
import com.jme3x.jfx.util.JFXUtils;
import com.sun.javafx.application.PlatformImpl;

public class TestTextureContainer extends SimpleApplication {

    Map<Geometry, JmeFxTextureContainer> containers = new IdentityHashMap<>();

    public static void main(final String[] args) {

        PlatformImpl.startup(() -> {
        });

        final TestTextureContainer app = new TestTextureContainer();

        app.setSettings(new AppSettings(false));
        app.settings.setFrameRate(60);

        app.start();
    }

    @Override
    public void simpleInitApp() {

        this.setPauseOnLostFocus(false);
        this.flyCam.setDragToRotate(true);
        this.flyCam.setMoveSpeed(20);

        for (int i = 0; i < 5; i++) {

            final Geometry screen = new Geometry("Screen1", new Quad(20, 20, true));
            final Material mat = new Material(this.assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            screen.setLocalTranslation(i * 30 - 50, 0, 0);

            if ( i == 3 ) {
                screen.addControl(new RotationControl(new Vector3f(0,0.1f,0)));
            }
            
            JmeFxTextureContainer container = JmeFxContainer.createTextureContainer(this, 1600, 1200);
            mat.setTexture("ColorMap", container.getTexture());
            screen.setMaterial(mat);
            this.rootNode.attachChild(screen);
            Platform.runLater(() -> container.setScene(createScene()));
            containers.put(screen, container);
        }

        this.cam.setLocation(new Vector3f(10, 10, 15));

        getInputManager().addRawInputListener(new RawInputListener() {

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
                JmeFxTextureContainer container = containers.get(geom);
                if (container == null) {
                    return;
                }
                
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

    }

    public static Scene createScene() {

        Group root = new Group();

        Scene scene = new Scene(root, 600, 600, true);
        scene.setFill(new Color(0, 0, 0, 0));

        final TreeItem<String> treeRoot = new TreeItem<String>("Root node");
        treeRoot.getChildren().addAll(
                Arrays.asList(new TreeItem<String>("Child Node 1"), new TreeItem<String>("Child Node 2"), new TreeItem<String>("Child Node 3")));
        treeRoot.getChildren()
                .get(2)
                .getChildren()
                .addAll(Arrays.asList(new TreeItem<String>("Child Node 4"), new TreeItem<String>("Child Node 5"), new TreeItem<String>("Child Node 6"),
                        new TreeItem<String>("Child Node 7"), new TreeItem<String>("Child Node 8"), new TreeItem<String>("Child Node 9"), new TreeItem<String>(
                                "Child Node 10"), new TreeItem<String>("Child Node 11"), new TreeItem<String>("Child Node 12")));

        final TreeView treeView = new TreeView();
        treeView.setShowRoot(true);

        treeView.setRoot(treeRoot);

        treeRoot.setExpanded(true);
        treeView.setLayoutY(100);

        Button test1 = new Button("Test1");
        test1.setLayoutX(500);
        test1.setLayoutY(500);

        CheckBox test2 = new CheckBox("Test2");
        test2.setLayoutX(700);
        test2.setLayoutY(700);

        MenuBar bar = new MenuBar();

        Menu testMenu = new Menu("Test");
        bar.getMenus().add(testMenu);
        MenuItem i1 = new MenuItem("Entry1");
        MenuItem i2 = new MenuItem("Entry2");
        Menu sub = new Menu("Submenu");
        sub.getItems().addAll(new MenuItem("Sub entry 1"), new MenuItem("Sub Entry 2"));
        testMenu.getItems().addAll(i1, sub, i2);

        TextArea ta = new TextArea();
        ta.setOpacity(0.7);
        ta.setLayoutX(400);
        ta.setLayoutY(300);

        ChoiceBox<String> cb = new ChoiceBox<>();

        cb.setItems(FXCollections.observableArrayList("Alfa", "Beta"));
        cb.setLayoutX(300);
        cb.setLayoutY(200);

        root.getChildren().addAll(treeView, test1, bar, test2, ta, cb);

        return scene;
    }

    @Override
    public void destroy() {
        super.destroy();
        PlatformImpl.exit();
    }

}
