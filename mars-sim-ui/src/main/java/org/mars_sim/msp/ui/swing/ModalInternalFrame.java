/**
 * Mars Simulation Project
 * ModalInternalFrame.java
 * @version 3.1.0 2017-03-12
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.core.structure.Settlement;

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
        		//System.out.println("ModalInternalFrame : just done calling startModal()");
            }
            else {
                //System.out.println("ModalInternalFrame : just started calling stopModal()");
                stopModal();
                //System.out.println("ModalInternalFrame : just done calling stopModal()");
            }
        }
    }

    /**
     * Start the modal behavior.
     */
    private synchronized void startModal() {

        try {
            if (SwingUtilities.isEventDispatchThread()) {
            	//System.out.println("ModalInternalFrame's startModal() : SwingUtilities.isEventDispatchThread() is true");//false");isVisible() is true");
                EventQueue theQueue = getToolkit().getSystemEventQueue();
            	//System.out.println("ModalInternalFrame's startModal() : just created an instance of theQueue.");
                while (isVisible()) {
            		//System.out.println("ModalInternalFrame's startModal() : isVisible() is true");
                    AWTEvent event = theQueue.getNextEvent();
                    Object source = event.getSource();
                    boolean dispatch = true;
                	//System.out.println("ModalInternalFrame's startModal() : just created instance of event and source");
                    if (event instanceof MouseEvent) {
                        MouseEvent e = (MouseEvent) event;
                        MouseEvent m = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, this);
                    	//System.out.println("ModalInternalFrame's startModal() : just create an instance of e and m");
                        //if (!this.contains(m.getPoint()) && (e.getID() != MouseEvent.MOUSE_DRAGGED)) {
                        //    dispatch = false;
                        //}
                        // 2016-11-15 Implemented this bug fix posted in https://community.oracle.com/thread/1358431?start=0&tstart=0
                        //
                        if (!this.contains(m.getPoint())
                        	&& e.getID() != MouseEvent.MOUSE_DRAGGED
                        	&& !e.getSource().getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow")
                        	&& e.getID() != MouseEvent.MOUSE_RELEASED) {
                        	    dispatch = false;
                        		//System.out.println("ModalInternalFrame's startModal() : dispatch is set to false");
                        }
                    } else if (event instanceof ActiveEvent) {
                        	//System.out.println("ModalInternalFrame's startModal() : event is an instance of ActiveEvent");
                        	ActiveEvent activeEvent = (ActiveEvent) event;
                        	//System.out.println("ModalInternalFrame's startModal() : activeEvent is good");

                        	activeEvent.dispatch();
                        	//System.out.println("ModalInternalFrame's startModal() : activeEvent is dispatched");
                        	//((ActiveEvent) event).dispatch(); // Monitor Window frozen when createPieSelector() in ColumnSelector is being executed the 2nd time.
                        	//System.out.println("ModalInternalFrame's startModal() : done calling ((ActiveEvent) event).dispatch()");
                    }
                    //else
                    //	System.out.println("ModalInternalFrame's startModal() : event is not an instance of ActiveEvent or MouseEvent");

                    if (dispatch) {
                		//System.out.println("ModalInternalFrame's startModal() : dispatch is true");
                        if (source instanceof Component) {
                        	// 2016-11-15 Attempting to give a text field correct focus for linux
                        	if (source instanceof JTextField) {
                        		//System.out.println("ModalInternalFrame : you just clicked on a Swing JTextField.");
                        		((JTextField) source).setEditable(true);
                        		((JTextField) source).requestFocusInWindow();
                        	}

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
        		//System.out.println("ModalInternalFrame's startModal() : SwingUtilities.isEventDispatchThread() is false");
                while (isVisible()) {
                    wait();
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop the modal behavior.
     */
    private synchronized void stopModal() {
		//System.out.println("ModalInternalFrame's stopModal() : before calliong notifyAll()");
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