/*
 * Mars Simulation Project
 * ModalInternalFrame.java
 * @date 2021-08-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * An internal frame that supports modal behavior. Based on code found in:
 * https://community.oracle.com/thread/1358431?start=0&tstart=0
 * see also https://stackoverflow.com/questions/16422939/jinternalframe-as-modal
 */
@SuppressWarnings("serial")
public abstract class ModalInternalFrame extends JInternalFrame {

    /** default logger. */
    private static final Logger logger = Logger.getLogger(ModalInternalFrame.class.getName());

	// Data members
	boolean modal = false;

	/**
	 * Constructor
	 *
	 * @param title
	 *            the title of the frame.
	 */
	public ModalInternalFrame(String title) {
		// Call JInternalFrame constructor.
		super(title, false, false, false, false);

		setFrameIcon(MainWindow.getLanderIcon());
	}

	public ModalInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		// Call JInternalFrame constructor.
		super(title, resizable, closable, maximizable, iconifiable);

		setFrameIcon(MainWindow.getLanderIcon());
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
			} else {
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

//					if (event instanceof MouseEvent) {
//						MouseEvent e = (MouseEvent) event;
//						MouseEvent m = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, this);
						// Implement this bug fix posted in
						// https://community.oracle.com/thread/1358431?start=0&tstart=0
//						if (!this.contains(m.getPoint()) && e.getID() != MouseEvent.MOUSE_DRAGGED
//								&& !e.getSource().getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow")
//								&& e.getID() != MouseEvent.MOUSE_RELEASED) {
//							dispatch = false;
//						}
//					}

					if (event instanceof ActiveEvent) {
						ActiveEvent activeEvent = (ActiveEvent) event;
						activeEvent.dispatch();
					}

					if (dispatch) {

						if (source instanceof Component) {
							// Attempting to give a text field correct focus for linux
							if (source instanceof JTextField) {
								((JTextField) source).setEditable(true);
							}

							((Component) source).dispatchEvent(event);
						} else if (source instanceof MenuComponent) {
							((MenuComponent) source).dispatchEvent(event);
						}

					}
				}
			} else {

				while (isVisible()) {
					wait();
				}
			}
		} catch (InterruptedException e) {
			logger.config("Problem in startModal's EventQueue" + e.getMessage());
		    // Restore interrupted state
		    Thread.currentThread().interrupt();
		}
	}

	/**
	 * Stops the modal behavior.
	 */
	private synchronized void stopModal() {
		notifyAll();
	}

	/**
	 * Sets if the frame is modal.
	 *
	 * @param modal
	 *            true if frame is modal.
	 */
	public void setModal(boolean modal) {
		this.modal = modal;
	}

	/**
	 * Checks if the frame is modal.
	 *
	 * @return true if frame is modal.
	 */
	public boolean isModal() {
		return this.modal;
	}

	/**
	 * Sets the icon image for the main window.
	 */
	public void setIconImage() {
		super.setFrameIcon(MainWindow.getLanderIcon());
	}

}
