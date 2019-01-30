/* Mars Simulation Project
 * WebEventDispatcher.java
 * @version 3.1.0 2017-04-07
 * @author Manny Kung
 */

package org.mars_sim.javafx;

import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class WebEventDispatcher implements EventDispatcher {

    private final EventDispatcher oldDispatcher;
    private Point2D limit;

    public WebEventDispatcher(EventDispatcher oldDispatcher) {
        this.oldDispatcher = oldDispatcher;
    }

    public void setLimit(Point2D limit){
        this.limit = limit;
    }

    private boolean allowDrag = false;

    @Override
    public Event dispatchEvent(Event event, EventDispatchChain tail) {

    	if (event instanceof MouseEvent){
            MouseEvent m = (MouseEvent)event;
            if (event.getEventType().equals(MouseEvent.MOUSE_CLICKED) ||
                event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
                Point2D origin = new Point2D(m.getX(),m.getY());
                if (limit != null)
                	allowDrag = !(origin.getX() < limit.getX() && origin.getY() < limit.getY());
            }

            // avoid selection with mouse dragging, allowing dragging the scrollbars
            if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
                if(!allowDrag){
                    event.consume();
                }
            }
            // Avoid selection of word, line, paragraph with mouse click
            if(m.getClickCount() > 1){
                event.consume();
            }
        }

        if (event instanceof KeyEvent && event.getEventType().equals(KeyEvent.KEY_PRESSED)){
            KeyEvent k = (KeyEvent)event;
            // Avoid copy with Ctrl+C or Ctrl+Insert
            if((k.getCode().equals(KeyCode.C) || k.getCode().equals(KeyCode.INSERT)) && k.isControlDown()){
                event.consume();
            }
            // Avoid selection with shift+Arrow
            if(k.isShiftDown() && (k.getCode().equals(KeyCode.RIGHT) || k.getCode().equals(KeyCode.LEFT) ||
                k.getCode().equals(KeyCode.UP) || k.getCode().equals(KeyCode.DOWN))){
                event.consume();
            }
        }
        return oldDispatcher.dispatchEvent(event, tail);
    }
}