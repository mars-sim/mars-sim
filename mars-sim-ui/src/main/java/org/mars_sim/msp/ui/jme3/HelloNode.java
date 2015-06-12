package org.mars_sim.msp.ui.jme3;

import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

/** Sample 2 - How to use nodes as handles to manipulate objects in the scene.
* You can rotate, translate, and scale objects by manipulating their parent nodes.
* The Root Node is special: Only what is attached to the Root Node appears in the scene. */
public class HelloNode extends SimpleApplication {

   public static void main(String[] args){
       HelloNode app = new HelloNode();
       app.start();
   }

   @Override
   public void simpleInitApp() {

       /** create a blue box at coordinates (1,-1,1) */
       Box box1 = new Box(1,1,1);
       Geometry blue = new Geometry("Box", box1);
       blue.setLocalTranslation(new Vector3f(1,-1,1));
       Material mat1 = new Material(assetManager,
               "Common/MatDefs/Misc/Unshaded.j3md");
       mat1.setColor("Color", ColorRGBA.Blue);
       blue.setMaterial(mat1);

       /** create a red box straight above the blue one at (1,3,1) */
       Box box2 = new Box(1,1,1);
       Geometry red = new Geometry("Box", box2);
       red.setLocalTranslation(new Vector3f(1,3,1));
       Material mat2 = new Material(assetManager,
               "Common/MatDefs/Misc/Unshaded.j3md");
       mat2.setColor("Color", ColorRGBA.Red);
       red.setMaterial(mat2);

       /** Create a pivot node at (0,0,0) and attach it to the root node */
       Node pivot = new Node("pivot");
       rootNode.attachChild(pivot); // put this node in the scene

       /** Attach the two boxes to the *pivot* node. (And transitively to the root node.) */
       pivot.attachChild(blue);
       pivot.attachChild(red);
       /** Rotate the pivot node: Note that both boxes have rotated! */
       pivot.rotate(.4f,.4f,0f);
   }
}