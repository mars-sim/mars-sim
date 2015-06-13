package org.mars_sim.msp.ui.jme3.jme3FX;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Callable;

import javax.swing.JComponent;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;

// http://hub.jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_picking
// http://hub.jmonkeyengine.org/wiki/doku.php/jme3:advanced:mouse_picking
public class HelloPicking extends AbstractAppState {

	private Node shootables;
	private Geometry mark;
	private Node rootNode;
	private AssetManager assetManager;
	private Camera cam;
	private SimpleApplication app;
	private JComponent panel;

	public HelloPicking(JComponent imagePanel) {
		panel = imagePanel;
	}

	@Override
	public void initialize(AppStateManager stateManager, Application app0) {
		super.initialize(stateManager, app0);
		app = (SimpleApplication)app0;
		rootNode = app.getRootNode();
		assetManager = app.getAssetManager();
		cam = app.getCamera();

		initCrossHairs(); // a "+" in the middle of the screen to help aiming
		initKeys();       // load custom key mappings
		initMark();       // a red sphere to mark the hit


		/** create four colored boxes and a floor to shoot at: */
		shootables = new Node("Shootables");
		rootNode.attachChild(shootables);
		shootables.attachChild(makeCube("a Dragon", -2f, 0f, 1f, 1f));
		shootables.attachChild(makeCube("a tin can", 1f, -2f, 0f, 0.5f));
		shootables.attachChild(makeCube("the Sheriff", 0f, 1f, -2f, 0.5f));
		shootables.attachChild(makeCube("the Deputy", 1f, 0f, -4f, 1f));
		shootables.attachChild(makeFloor());
		//shootables.attachChild(makeCharacter());
	}

	/** Declaring the "Shoot" action and mapping to its triggers. */
	private void initKeys() {
		panel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					app.enqueue(new Callable<Void>(){
						@Override
						public Void call() throws Exception {
							HelloPicking.this.shoot(new Vector2f(e.getX(), e.getComponent().getHeight() - e.getY())); // y is inversed
							return null;
						}
					});
				}
			}
		});
	}

	public void shoot(Vector2f screenPos) {
		System.out.println("screenPos : "+ screenPos);
		// 1. Reset results list.
		CollisionResults results = new CollisionResults();
		// 2. Aim the ray from cam loc to cam direction.
		Vector3f click3d = cam.getWorldCoordinates(screenPos, 0f).clone();
		Vector3f dir = cam.getWorldCoordinates(screenPos, 1f).subtractLocal(click3d).normalizeLocal();
		Ray ray = new Ray(click3d, dir);
		// 3. Collect intersections between Ray and Shootables in results list.
		shootables.collideWith(ray, results);
		// 4. Print the results
		System.out.println("----- Collisions? " + results.size() + "-----");
		for (int i = 0; i < results.size(); i++) {
			// For each hit, we know distance, impact point, name of geometry.
			float dist = results.getCollision(i).getDistance();
			Vector3f pt = results.getCollision(i).getContactPoint();
			String hit = results.getCollision(i).getGeometry().getName();
			System.out.println("* Collision #" + i);
			System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
		}
		// 5. Use the results (we mark the hit object)
		if (results.size() > 0) {
			// The closest collision point is what was truly hit:
			CollisionResult closest = results.getClosestCollision();
			// Let's interact - we mark the hit with a red dot.
			mark.setLocalTranslation(closest.getContactPoint());
			rootNode.attachChild(mark);
		} else {
			// No hits? Then remove the red mark.
			rootNode.detachChild(mark);
		}
	}

	private BitmapText ch;

	/** A cube object for target practice */
	protected Geometry makeCube(String name, float x, float y, float z, float alpha) {
		Box box = new Box(1, 1, 1);
		Geometry cube = new Geometry(name, box);
		cube.setLocalTranslation(x, y, z);
		Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		ColorRGBA c = ColorRGBA.randomColor();
		c.a = alpha;
		mat1.setColor("Color", c);
		if (alpha < 0.99f) {
			mat1.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);  // !
			cube.setQueueBucket(Bucket.Transparent);
		}
		cube.setMaterial(mat1);
		return cube;
	}

	/** A floor to show that the "shot" can go through several objects. */
	protected Geometry makeFloor() {
		Box box = new Box(15, .2f, 15);
		Geometry floor = new Geometry("the Floor", box);
		floor.setLocalTranslation(0, -4, -5);
		Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mat1.setColor("Color", ColorRGBA.Gray);
		floor.setMaterial(mat1);
		return floor;
	}

	/** A red ball that marks the last spot that was "hit" by the "shot". */
	protected void initMark() {
		Sphere sphere = new Sphere(30, 30, 0.2f);
		mark = new Geometry("BOOM!", sphere);
		Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		mark_mat.setColor("Color", ColorRGBA.Red);
		mark.setMaterial(mark_mat);
	}

	/** A centred plus sign to help the player aim. */
	protected void initCrossHairs() {
		BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
		ch = new BitmapText(guiFont, false);
		ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
		ch.setText("+"); // crosshairs
		app.getGuiNode().attachChild(ch);
	}

	protected Spatial makeCharacter() {
		// load a character from jme3test-test-data
		Spatial golem = assetManager.loadModel("Models/Oto/Oto.mesh.xml");
		golem.scale(0.5f);
		golem.setLocalTranslation(-1.0f, -1.5f, -0.6f);

		// We must add a light to make the model visible
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
		golem.addLight(sun);
		return golem;
	}

	@Override
	public void update(float tpf) {
		super.update(tpf);
		ch.setLocalTranslation(app.getCamera().getWidth() / 2 - ch.getLineWidth()/2, app.getCamera().getHeight() / 2 + ch.getLineHeight()/2, 0);
	}
}