/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.1.0 2017-02-03
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;


/**
 * The MainDesktopManager class is a custom desktop manager for the Mars
 * Simulation Project UI that allows tool and unit detail windows to disappear
 * when their iconify buttons are pressed.
 */
class MainDesktopManager extends DefaultDesktopManager {

	/** Constructs a MainDesktopManager object */
	public MainDesktopManager() {
		super();
	}

	/**
	 * Iconifies frame (overridden)
	 * 
	 * @param frame
	 *            the internal frame
	 */
	@Override
	public void iconifyFrame(JInternalFrame frame) {
		frame.setVisible(false);
	}

	// see
	// http://stackoverflow.com/questions/8136944/preventing-jinternalframe-from-being-moved-out-of-a-jdesktoppane#8138986
	@Override
	public void beginDraggingFrame(JComponent f) {

	}

	@Override
	public void beginResizingFrame(JComponent f, int direction) {

	}

	@Override
	public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		boolean hitBoundary = (f.getWidth() != newWidth || f.getHeight() != newHeight);

		if (!inBounds((JInternalFrame) f, newX, newY, newWidth, newHeight)) {
			Container parent = f.getParent();
			Dimension parentSize = parent.getSize();

			// Limit the unit window or tool windows to stay inside and never go outside of
			// the desktop
			// or always show up fully (never show up less than the full window)
			int boundedX = (int) Math.min(Math.max(0, newX), parentSize.getWidth() - newWidth);
			int boundedY = (int) Math.min(Math.max(0, newY), parentSize.getHeight() - 40);// newHeight);
			if (f != null)
				f.setBounds(boundedX, boundedY, newWidth, newHeight);
		} else {
			if (f != null)
				f.setBounds(newX, newY, newWidth, newHeight);
		}

		if (hitBoundary) {
			if (f != null)
				f.validate();
		}

	}

	protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
		if (newX < 0 || newY < 0)
			return false;
		if (newX + newWidth > f.getDesktopPane().getWidth())
			return false;
		if (newY + newHeight > f.getDesktopPane().getHeight())
			return false;
		return true;
	}
}
