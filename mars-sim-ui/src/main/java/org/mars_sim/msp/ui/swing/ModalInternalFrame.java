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
import java.awt.Image;
import java.awt.MenuComponent;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;

/**
 * An internal frame that supports modal behavior. Based on code found in:
 * https://community.oracle.com/thread/1358431?start=0&tstart=0
 * see also https://stackoverflow.com/questions/16422939/jinternalframe-as-modal
 */
public abstract class ModalInternalFrame extends JInternalFrame {

	// Data members
	boolean modal = false;
	
	private static ImageIcon icon = new ImageIcon(SimulationConfigEditor.class.getResource(MainWindow.ICON_IMAGE));
	

	/**
	 * Constructor
	 * 
	 * @param title
	 *            the title of the frame.
	 */
	public ModalInternalFrame(String title) {
		// Call JInternalFrame constructor.
		super(title, false, false, false, false);
	}

	public ModalInternalFrame(String title, boolean resizable, boolean closable, boolean maximizable,
			boolean iconifiable) {
		// Call JInternalFrame constructor.
		super(title, resizable, closable, maximizable, iconifiable);
		
		setFrameIcon(icon);
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

					if (event instanceof MouseEvent) {
						MouseEvent e = (MouseEvent) event;
						MouseEvent m = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, this);

						// Implement this bug fix posted in
						// https://community.oracle.com/thread/1358431?start=0&tstart=0
						//
						if (!this.contains(m.getPoint()) && e.getID() != MouseEvent.MOUSE_DRAGGED
								&& !e.getSource().getClass().getName().equals("javax.swing.Popup$HeavyWeightWindow")
								&& e.getID() != MouseEvent.MOUSE_RELEASED) {
							dispatch = false;

						}
					} 
					
					else if (event instanceof ActiveEvent) {
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
			e.printStackTrace();
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

		String fullImageName = MainWindow.ICON_IMAGE;
		URL resource = ImageLoader.class.getResource(fullImageName);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(resource).getScaledInstance(16, 16, Image.SCALE_DEFAULT);
//		ImageIcon icon = new ImageIcon(ClassLoader.getSystemResource("Iconos/icono.png"));
		ImageIcon icon = new ImageIcon(img);
		super.setFrameIcon(icon);
	}
	
}