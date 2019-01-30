/**
 * Mars Simulation Project
 * DragDrop.java
 * @version 3.1.0 2017-05-28
 * @author Manny Kung
 */

package org.mars_sim.javafx;

import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

	/**
	 *
	 * @web http://java-buddy.blogspot.com/
	 */
	public class DragDrop {

	    public StackPane createDragDropBox() {

	    	StackPane pane = new StackPane();

	        HBox hBox1 = new HBox();
	        hBox1.setPrefWidth(100);
	        hBox1.setPrefHeight(100);
	        hBox1.setStyle("-fx-border-color: green;"
	              + "-fx-border-width: 2;"
	              + "-fx-border-style: solid;");

	        HBox hBox2 = new HBox();
	        hBox2.setPrefWidth(100);
	        hBox2.setPrefHeight(100);
	        hBox2.setStyle("-fx-border-color: orange;"
	              + "-fx-border-width: 2;"
	              + "-fx-border-style: solid;");

	        //insertImage(new Image(getClass().getResourceAsStream("/images/MaleIcon.png")), hBox1);
	        //insertImage(new Image(getClass().getResourceAsStream("/images/FemaleIcon.png")), hBox2);
	        //insertImage(new Image(getClass().getResourceAsStream("/images/RobotIcon.png")), hBox1);

	        insertImage("bee32.png", hBox1);
	        insertImage("branch32.png", hBox1);
	        insertImage("carrot32.png", hBox1);
	        insertImage("ladybug32.png", hBox1);
	        insertImage("leaf32.png", hBox1);	        

	        //setupGestureTarget(hBox1);
	        setupGestureTarget(hBox2);
	        
	        VBox vBox = new VBox();
	        vBox.getChildren().addAll(hBox1, hBox2);
	        pane.getChildren().addAll(vBox);

	        return pane;
	    }

	    private void insertImage(String s, HBox hb){

	    	Image i = new Image(getClass().getResourceAsStream("/icons/farming/" + s));
	    	insertImage(i, hb);
	    }


	    private void insertImage(Image i, HBox hb){

	        ImageView iv = new ImageView();
	        iv.setImage(i);

	        setupGestureSource(iv);

	        hb.getChildren().add(iv);
	    }

	    
	    /**
	     * Sets up the image view as the source
	     * @param source
	     */
	    private void setupGestureSource(final ImageView source){

	        source.setOnDragDetected(new EventHandler <MouseEvent>() {
	           @Override
	           public void handle(MouseEvent event) {

	               /* allow any transfer mode */
	               Dragboard db = source.startDragAndDrop(TransferMode.COPY);

	               /* put a image on dragboard */
	               ClipboardContent content = new ClipboardContent();

	               Image sourceImage = source.getImage();
	               content.putImage(sourceImage);
	               db.setContent(content);

	               event.consume();
	           }
	       });

	    }
	    
	    /**
	     * Sets up the HBox to accept the image view
	     * @param targetBox
	     */
	    private void setupGestureTarget(final HBox targetBox){

	        targetBox.setOnDragOver(new EventHandler <DragEvent>() {
	            @Override
	            public void handle(DragEvent event) {

	                Dragboard db = event.getDragboard();

	                if (db.hasImage()) {
	                	
	                    event.acceptTransferModes(TransferMode.COPY);
	                }

	                event.consume();
	            }
	        });

	        targetBox.setOnDragDropped(new EventHandler <DragEvent>() {
	            @Override
	            public void handle(DragEvent event) {

	                Dragboard db = event.getDragboard();

	                if (db.hasImage()) {

	                    insertImage(db.getImage(), targetBox);

	                    event.setDropCompleted(true);
	                }
	                
	                else {
	                    event.setDropCompleted(false);
	                }

	                event.consume();
	            }
	        });

	    }

	}