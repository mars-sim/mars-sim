/**
 * Mars Simulation Project
 * ModalInternalFrame.java
 * @version 3.08 2015-06-30
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.event.MouseEvent;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

/**
 * An internal frame that supports modal behavior.
 * Based on code found in: 
 * https://community.oracle.com/thread/1358431?start=0&tstart=0
 */
public abstract class ModalInternalFrame extends JInternalFrame {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    // Data members
    boolean modal = false;

    /**
     * Constructor
     * @param title the title of the frame.
     */
    public ModalInternalFrame(String title) {
        // Call JInternalFrame constructor.
        super(title, false, false, false, false);
    }

    public ModalInternalFrame(String title, 
    		 boolean resizable, 
    		 boolean closable, 
    		 boolean maximizable, 
    		 boolean iconifiable) {
        // Call JInternalFrame constructor.
        super(title, 
        		resizable, 
        		closable, 
        		maximizable, 
        		iconifiable);
    }
    
    @Override
    public void show() {
        super.show();
        if (this.modal) {
            startModal();
        }
    }

    @Override
    public void setVisible(boolean value) {
        super.setVisible(value);
        if (modal) {
            if (value) {
                startModal();
            } 
            else {
                stopModal();
            }
        }
    }

    /**
     * Start the modal behavior.
     */
    private synchronized void startModal() {

        try {
            if (SwingUtilities.isEventDispatchThread()) {
                EventQueue theQueue = getToolkit().getSystemEventQueue();
                while (isVisible()) {
                    AWTEvent event = theQueue.getNextEvent();
                    Object source = event.getSource();
                    boolean dispatch = true;

                    if (event instanceof MouseEvent) {
                        MouseEvent e = (MouseEvent) event;
                        MouseEvent m = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, this);
                        if (!this.contains(m.getPoint()) && (e.getID() != MouseEvent.MOUSE_DRAGGED)) {
                            dispatch = false;
                        }
                    }

                    if (dispatch) {
                        if (event instanceof ActiveEvent) {
                            ((ActiveEvent) event).dispatch();
                        } 
                        else if (source instanceof Component) {
                            ((Component) source).dispatchEvent(event);
                        } 
                        else if (source instanceof MenuComponent) {
                            ((MenuComponent) source).dispatchEvent(event);
                        } 
                        else {
                            System.err.println("Unable to dispatch: " + event);
                        }
                    }
                }
            } 
            else {
                while (isVisible()) {
                    wait();
                }
            }
        } 
        catch (InterruptedException ignored) {
            // Do nothing
        }
    }

    /**
     * Stop the modal behavior.
     */
    private synchronized void stopModal() {
        notifyAll();
    }

    /**
     * Set if the frame is modal.
     * @param modal true if frame is modal.
     */
    public void setModal(boolean modal) {
        this.modal = modal;
    }

    /**
     * Checks if the frame is modal.
     * @return true if frame is modal.
     */
    public boolean isModal() {
        return this.modal;
    }
}