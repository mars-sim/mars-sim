/**
 * Mars Simulation Project
 * MarsProjectJME.java
 * @version 3.08 2015-11-08
 * @author Manny Kung
 */
package org.mars_sim.main.javafx;

public class MarsProjectJME { }

//import com.jme3.app.SimpleApplication;
//import com.jme3.material.Material;
//import com.jme3.math.ColorRGBA;
//import com.jme3.scene.Geometry;
//import com.jme3.scene.shape.Box;
//import com.jme3.system.AppSettings;
//
//
// // The MarsProjectJME class starts a JME's thread and calls on a static method in MarsProjectUtility to create a JavaFX thread
// //
//public class MarsProjectJME extends SimpleApplication {
//
//    *
//     * Constructor 1.
//     * @param args command line arguments.
//     
//    public MarsProjectJME() {
//    }
//
//    public static void main(String[] arArgs) {
//
//    	// NOTE: hit ESC to quit the 3D demo cube
//    	MarsProjectJME mpj = new MarsProjectJME();
//    	//mpj.start();
//
//    	// Bypassing jME app config screen with the following settings
//    	mpj.setShowSettings(false);
//    	AppSettings settings = new AppSettings(true);
//    	settings.put("Width", 1280);
//    	settings.put("Height", 720);
//    	settings.put("Title", "My awesome Game");
//    	settings.put("VSync", true);
//    	//Anti-Aliasing
//    	settings.put("Samples", 4);
//    	mpj.setSettings(settings);
//
//    	mpj.start();
//
//
//    	MarsProjectUtility.launchApp((app, stage) -> {
//
// 				//MarsProjectJME mpj = new MarsProjectJME();
//                //Button but = mpj.button;
//                //stage.setWidth(300);
//                //stage.setHeight(300);
//                //Scene scene = new Scene(but);
//                //stage.setScene(scene);
//                //System.out.print(app.getParameters());
//                //stage.show();
//
//            }, arArgs);
//    }
//
//    @Override
//    public void simpleUpdate(float tpf) {
//       // Interact with game events in the main loop 
//    }
//
//    @Override
//    public void simpleInitApp() {
//        Box b = new Box(1, 1, 1); // create cube shape
//        Geometry geom = new Geometry("Box", b);  // create cube geometry from the shape
//        Material mat = new Material(assetManager,
//          "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
//        mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
//        geom.setMaterial(mat);                   // set the cube's material
//        rootNode.attachChild(geom);              // make the cube appear in the scene
//    }
//}
