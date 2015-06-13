package org.mars_sim.msp.ui.jme3.jme3FX;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.TangentBinormalGenerator;

public class ImagePanelDemo {

	public static void main(String[] args){
		makeGUI().setVisible(true);
	}

	public static JFrame makeGUI(){
		final JFrame mainFrame = new JFrame("Java Swing Examples");
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		mainFrame.setSize(400,400);
		mainFrame.setLayout(new BorderLayout());
		JLabel headerLabel = new JLabel("", JLabel.CENTER);
		JLabel statusLabel = new JLabel("",JLabel.CENTER);

		statusLabel.setSize(350,100);

		final ImagePanel imagePanel = new ImagePanel();
		imagePanel.setBackground(Color.GREEN);
		imagePanel.setSize(600,400);
		imagePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				e.getComponent().requestFocus();
			}
		});
		//controlPanel.setLayout(new FlowLayout());

		final JmeForImagePanel jme = new JmeForImagePanel();
		jme.bind(imagePanel);
		jme.enqueue(new Function<SimpleApplication, Boolean>() {
			@Override
			public Boolean apply(SimpleApplication t) {
				//return createScene(t);
				t.getStateManager().attach(new HelloPicking(imagePanel));
				t.getStateManager().attach(new CameraDriverAppState());

				//imagePanel.setFocusable(true);
				//imagePanel.requestFocusInWindow();
				CameraDriverInput driver = new CameraDriverInput();
				driver.jme = t;
				driver.speed = 1.0f;
				CameraDriverInput.bindDefaults(imagePanel, driver);
				return true;
			}
		});

		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowIconified(java.awt.event.WindowEvent e) {
				// TODO Auto-generated method stub
				jme.enqueue(new Function<SimpleApplication, Boolean>() {
					@Override
					public Boolean apply(SimpleApplication t) {
						t.loseFocus();
						return true;
					}
				});

			}

			@Override
			public void windowDeiconified(java.awt.event.WindowEvent e) {
				jme.enqueue(new Function<SimpleApplication, Boolean>() {
					@Override
					public Boolean apply(SimpleApplication t) {
						t.gainFocus();
						return true;
					}
				});
			}

			@Override
			public void windowClosing(java.awt.event.WindowEvent e) {
				jme.stop(true);
			}
		});

		JTextArea text = new JTextArea(20, 20);

		mainFrame.add(headerLabel, BorderLayout.NORTH);
		mainFrame.add(statusLabel, BorderLayout.SOUTH);
		mainFrame.add(text, BorderLayout.WEST);

		final Action toggleVisibility = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue(String key) {
				if (Action.SELECTED_KEY.equals(key)){
					return imagePanel.isVisible();
				}
				return super.getValue(key);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean v = !imagePanel.isVisible();
				imagePanel.setVisible(v);
				//putValue(Action.SELECTED_KEY, v);
			}
		};
		final Action toggleAttachement = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object getValue(String key) {
				if (Action.SELECTED_KEY.equals(key)){
					for (Component c : mainFrame.getComponents()) {
						if (c == imagePanel) return true;
					}
					return false;
				}
				return super.getValue(key);
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean attach = !(boolean) getValue(Action.SELECTED_KEY);
				if (attach) {
					mainFrame.add(imagePanel, BorderLayout.CENTER);
					imagePanel.repaint();
					//imagePanel.requestFocus();
				} else {
					mainFrame.remove(imagePanel);
				}
				mainFrame.repaint();
				putValue(Action.SELECTED_KEY, attach);
			}
		};
		JMenuBar menu = createFakeMenuBar(toggleVisibility, toggleAttachement);
		mainFrame.setJMenuBar(menu);

		//mainFrame.getRootPane().getInputMap(JComponent.WHEN_).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), toggleVisibility);
		mainFrame.getRootPane().getActionMap().put(toggleVisibility, toggleVisibility);
		toggleAttachement.actionPerformed(null);
		return mainFrame;
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

		/** A simple textured cube -- in good MIP map quality. */
		Box cube1Mesh = new Box( 1f,1f,1f);
		Geometry cube1Geo = new Geometry("My Textured Box", cube1Mesh);
		cube1Geo.setLocalTranslation(new Vector3f(-3f,1.1f,0f));
		Material cube1Mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		cube1Mat.setColor("Color", ColorRGBA.Blue);
		cube1Geo.setMaterial(cube1Mat);
		rootNode.attachChild(cube1Geo);

		/** A translucent/transparent texture, similar to a window frame. */
		Box cube2Mesh = new Box( 1f,1f,0.01f);
		Geometry cube2Geo = new Geometry("window frame", cube2Mesh);
		Material cube2Mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
		cube2Mat.setColor("Color", ColorRGBA.Brown);
		cube2Geo.setQueueBucket(Bucket.Transparent);
		cube2Geo.setMaterial(cube2Mat);
		rootNode.attachChild(cube2Geo);

		/** A bumpy rock with a shiny light effect.*/
		Sphere sphereMesh = new Sphere(32,32, 2f);
		Geometry sphereGeo = new Geometry("Shiny rock", sphereMesh);
		sphereMesh.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
		TangentBinormalGenerator.generate(sphereMesh);           // for lighting effect
		Material sphereMat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
		sphereMat.setBoolean("UseMaterialColors",true);
		sphereMat.setColor("Diffuse",ColorRGBA.Pink);
		sphereMat.setColor("Specular",ColorRGBA.White);
		sphereMat.setFloat("Shininess", 64f);  // [0,128]
		sphereGeo.setMaterial(sphereMat);
		sphereGeo.setLocalTranslation(0,2,-2); // Move it a bit
		sphereGeo.rotate(1.6f, 0, 0);          // Rotate it a bit
		rootNode.attachChild(sphereGeo);

		/** Must add a light to make the lit object visible! */
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
		sun.setColor(ColorRGBA.White);
		rootNode.addLight(sun);
		return true;
	}

	static JMenuBar createFakeMenuBar(Action toggleVisibility, Action toggleAttachment) {

		//Where the GUI is created:
		JMenuBar menuBar;
		JMenu menu, submenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		JCheckBoxMenuItem cbMenuItem;

		//Create the menu bar.
		menuBar = new JMenuBar();

		//Build the first menu.
		menu = new JMenu("A Menu");
		menu.setMnemonic(KeyEvent.VK_A);
		menu.getAccessibleContext().setAccessibleDescription(
		        "The only menu in this program that has menu items");
		menuBar.add(menu);

		//a group of JMenuItems
		menuItem = new JMenuItem("A text-only menu item",
		                         KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_1, ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription(
		        "This doesn't really do anything");
		menu.add(menuItem);

		menuItem = new JMenuItem("Both text and icon",
		                         new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_B);
		menu.add(menuItem);

		menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
		menuItem.setMnemonic(KeyEvent.VK_D);
		menu.add(menuItem);

		//a group of radio button menu items
		menu.addSeparator();
		ButtonGroup group = new ButtonGroup();
		rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
		rbMenuItem.setSelected(true);
		rbMenuItem.setMnemonic(KeyEvent.VK_R);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		rbMenuItem = new JRadioButtonMenuItem("Another one");
		rbMenuItem.setMnemonic(KeyEvent.VK_O);
		group.add(rbMenuItem);
		menu.add(rbMenuItem);

		//a group of check box menu items
		menu.addSeparator();
		cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
		cbMenuItem.setMnemonic(KeyEvent.VK_C);
		menu.add(cbMenuItem);

		cbMenuItem = new JCheckBoxMenuItem("Another one");
		cbMenuItem.setMnemonic(KeyEvent.VK_H);
		menu.add(cbMenuItem);

		//a submenu
		menu.addSeparator();
		submenu = new JMenu("A submenu");
		submenu.setMnemonic(KeyEvent.VK_S);

		menuItem = new JMenuItem("An item in the submenu");
		menuItem.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_2, ActionEvent.ALT_MASK));
		submenu.add(menuItem);

		menuItem = new JMenuItem("Another item");
		submenu.add(menuItem);
		menu.add(submenu);

		//Build second menu in the menu bar.
		menu = new JMenu("Another Menu");
		menu.setMnemonic(KeyEvent.VK_N);
		menu.getAccessibleContext().setAccessibleDescription(
		        "This menu does nothing");
		menuBar.add(menu);

		menu = new JMenu("3D");
		toggleVisibility.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_SPACE);
		toggleVisibility.putValue(Action.NAME, "Toggle Visibility");
		menu.add(new JCheckBoxMenuItem(toggleVisibility));
		toggleAttachment.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
		toggleAttachment.putValue(Action.NAME, "Toggle Attachment");
		menu.add(new JCheckBoxMenuItem(toggleAttachment));
		menuBar.add(menu);
		return menuBar;
	}

}