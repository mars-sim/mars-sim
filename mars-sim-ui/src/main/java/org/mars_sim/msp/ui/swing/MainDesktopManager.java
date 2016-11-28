/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing; 

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.*;

/**
 * The MainDesktopManager class is a custom desktop manager for the
 * Mars Simulation Project UI that allows tool and unit detail windows
 * to disappear when their iconify buttons are pressed.
 */
class MainDesktopManager
extends DefaultDesktopManager {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	/** Constructs a MainDesktopManager object */
	public MainDesktopManager() { super(); }

	/**
	 * Iconifies frame (overridden) 
	 * @param frame the internal frame
	 */
	@Override
	public void iconifyFrame(JInternalFrame frame) { frame.setVisible(false); }
	

	// see http://stackoverflow.com/questions/8136944/preventing-jinternalframe-from-being-moved-out-of-a-jdesktoppane#8138986
	@Override
	public void beginDraggingFrame(JComponent f) {
		// Don't do anything. Needed to prevent the DefaultDesktopManager setting the dragMode
	}

	@Override
	public void beginResizingFrame(JComponent f, int direction) {
		// Don't do anything. Needed to prevent the DefaultDesktopManager setting the dragMode
	}
	
	@Override
	public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
		boolean didResize = (f.getWidth() != newWidth || f.getHeight() != newHeight);
		if (!inBounds((JInternalFrame) f, newX, newY, newWidth, newHeight)) {
			Container parent = f.getParent();
			Dimension parentSize = parent.getSize();
			int boundedX = (int) Math.min(Math.max(0, newX), parentSize.getWidth() - newWidth);
			int boundedY = (int) Math.min(Math.max(40, newY), parentSize.getHeight() - 40);//newHeight);
			f.setBounds(boundedX, boundedY, newWidth, newHeight);
		} else {
			f.setBounds(newX, newY, newWidth, newHeight);
		}
		if(didResize) {
			f.validate();
		}
	}

	protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
		if (newX < 0 || newY < 40) return false;
		if (newX + newWidth > f.getDesktopPane().getWidth()) return false;
		if (newY + newHeight > f.getDesktopPane().getHeight()) return false;
		return true;
	}
}
