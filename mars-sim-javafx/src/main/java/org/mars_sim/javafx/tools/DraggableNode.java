/**
 * Mars Simulation Project
 * DraggableNode.java
 * @version 3.1.0 2018-06-22
 * @author Manny Kung
 */

package org.mars_sim.javafx.tools;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Simple draggable node.
 * 
 * Dragging code based on {@link http://blog.ngopal.com.np/2011/06/09/draggable-node-in-javafx-2-0/}
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class DraggableNode extends Pane {

    // node position
    private double x = 0;
    private double y = 0;
    private double w = 0;
    private double h = 0;
    // mouse position
    private double mousex = 0;
    private double mousey = 0;
    private Node view;
    private boolean dragging = false;
    private boolean moveToFront = true;
    
    private Stage stage;


    public DraggableNode() {
        init();
    }

    public DraggableNode(Node view, Stage stage, double w, double h) {
        this.view = view;
        this.stage = stage;
        this.w = w;
        this.h = h;

        getChildren().add(view);
        init();
    }

    private void init() {

    	view.setOnMouseReleased(new EventHandler<MouseEvent>() {
    	      @Override public void handle(MouseEvent mouseEvent) {
    	    	  view.setCursor(Cursor.HAND);
    	      }
    	    });
    	
        onMousePressedProperty().set(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // record the current mouse X and Y position on Node
                mousex = event.getSceneX();
                mousey = event.getSceneY();

                x = getLayoutX();
                y = getLayoutY();
                
                view.setCursor(Cursor.MOVE);
                
                if (isMoveToFront()) {
                    toFront();
                }
            }
        });

        //Event Listener for MouseDragged
        onMouseDraggedProperty().set(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                // Get the exact moved X and Y

                double offsetX = event.getSceneX() - mousex;
                double offsetY = event.getSceneY() - mousey;

                if (x < stage.getWidth() - w
	                	&& y < stage.getHeight() - h
	                	&& stage.getWidth() > w + view.getLayoutX()
	                	&& stage.getHeight() > h + view.getLayoutY()) {
                	
                	x += offsetX;
                	y += offsetY;

	                double scaledX = x;
	                double scaledY = y;
	
	                if (scaledX < stage.getWidth() - w - 20
	                	&& scaledY < stage.getHeight() - h - 40
	                	&& scaledX > 0
	                	&& scaledY > 0) {
	                	
		                setLayoutX(scaledX);
		                setLayoutY(scaledY);

//		                System.out.println("w , h : (" + w + ", " + h + ")");
//		                System.out.println("Stage : (" + stage.getWidth() + ", " + stage.getHeight() + ")");
//		                System.out.println("Mouse : (" + x + ", " + y + ")");
		                dragging = true;
		
		                // again set current Mouse x AND y position
		                mousex = event.getSceneX();
		                mousey = event.getSceneY();
		                
	                }
	                
                }
                
                event.consume();
            }
        });

        onMouseClickedProperty().set(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                dragging = false;
            }
        });

    }

    /**
     * @return the dragging
     */
    protected boolean isDragging() {
        return dragging;
    }


    /**
     * @return the view
     */
    public Node getView() {
        return view;
    }

    /**
     * @param moveToFront the moveToFront to set
     */
    public void setMoveToFront(boolean moveToFront) {
        this.moveToFront = moveToFront;
    }

    /**
     * @return the moveToFront
     */
    public boolean isMoveToFront() {
        return moveToFront;
    }
    
    public void removeNode(Node n) {
        getChildren().remove(n);
    }
}
