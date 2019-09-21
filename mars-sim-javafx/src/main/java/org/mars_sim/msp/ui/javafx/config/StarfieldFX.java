/**
 * Mars Simulation Project
 * StarfieldFX.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx.config;

import java.util.Random;

import org.mars_sim.msp.core.tool.RandomUtil;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class StarfieldFX extends Application {
    
    private static final int STAR_COUNT = 1000;//20000;
    
    private final Rectangle[] nodes = new Rectangle[STAR_COUNT];
    private final double[] angles = new double[STAR_COUNT];
    private final long[] start = new long[STAR_COUNT];
    
    private final Random random = new Random();
    
    private Stage primaryStage;

    public StarfieldFX() {

	}
    
    //public StarfieldFX(final Stage primaryStage) {
    	//this.primaryStage = primaryStage;
    	//plotStars(primaryStage);
    //}

    
    @Override
    public void start(final Stage primaryStage) {
    	this.primaryStage = primaryStage;

        final Scene scene = new Scene(createStars(768, 768), Color.BLACK);    	
        primaryStage.setScene(scene);
        primaryStage.show();
     	
    }

    public Group createStars(int w, int h) {
    	
    	generate();  	
    	Group group = new Group(nodes);    	
    	//group.prefHeight(w);
    	//group.prefWidth(h);
    	plotStars(w,h);
     	
     	return group;
    }
    
    public void generate() {

        for (int i=0; i<STAR_COUNT; i++) {
        	
        	int hex = 0;
        	
        	int randColor = RandomUtil.getRandomInt(31);
        	
        	if (randColor == 0)
        		hex = 0xe7420b; // red orange	
        	else if (randColor == 1)
        		hex = 0xd0d0f9;  // light blue
        	else if (randColor == 2)
        		hex = 0xf4df0d; // FFFFE0; // yellow
        	else 
        		hex = 0xffffff; // white
        	
        	if (hex != 0xffffff) {
	        	int rand = RandomUtil.getRandomInt(127);
	        	hex = hex + rand;
        	}
        	
        	String hexString = Integer.toHexString(hex);
         	
        	Color c = Color.web(hexString, 1.0);
     		
       		nodes[i] = new Rectangle(1, 1, c);
        	
        	angles[i] = 2.0 * Math.PI * random.nextDouble();
            //start[i] = random.nextInt(2000000000);
            start[i] = random.nextInt(2000000000);

        }
        
    }
    	
    public void plotStars(int w, int h) {//Stage primaryStage) {
    	

        long now = 10_000_000;
        
        //new AnimationTimer() {
            //@Override
            //public void handle(long now) {
                final double width = 0.5 * w;
                final double height = 0.5 * h;
                final double radius = Math.sqrt(2) * Math.max(width, height);
                for (int i=0; i<STAR_COUNT; i++) {
                    final Node node = nodes[i];
                    final double angle = angles[i];
                    final long t = (now - start[i]) % 2000000000;
                    final double d = t * radius / 2000000000.0;
                    node.setTranslateX(Math.cos(angle) * d + width);
                    node.setTranslateY(Math.sin(angle) * d + height);
                }
            //}
        //}.start();
    }
    
  
    public static void main(String[] args) {
        launch(args);
    }
  
}