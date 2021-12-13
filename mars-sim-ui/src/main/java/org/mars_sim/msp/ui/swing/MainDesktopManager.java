/*
 * Mars Simulation Project
 * MainDesktopManager.java
 * @date 2021-12-07
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
@SuppressWarnings("serial")
class MainDesktopManager extends DefaultDesktopManager {

	// See https://stackoverflow.com/questions/8136944/preventing-jinternalframe-from-being-moved-out-of-a-jdesktoppane#8138986

	/** The value of GAP is to ensure the frame is flushed exactly at the edge of the main window. */
	private static final int GAP = 8;

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
			// Note: Ensure the jinternalframe stay inside and never go outside of
			// the desktop
			Container parent = f.getParent();
			Dimension parentSize = parent.getSize();

			// If the width of the frame is greater than the width of
			// the parent, then both boundedX (the top left starting point)
			// of the frame will be set to -GAP
			int boundedX = -GAP;
			// If the height of the frame is greater than the height of
			// the parent, then both boundedY (the top left starting point)
			// of the frame will be set to -GAP
			int boundedY = -GAP;

			if (parentSize.getHeight() >= newHeight) {
				boundedY = (int) (Math.min(Math.max(-GAP, newY), parentSize.getHeight() - newHeight + GAP));
			}

			if (parentSize.getWidth() >= newWidth) {
				boundedX = (int) (Math.min(Math.max(-GAP, newX), parentSize.getWidth() - newWidth + GAP));
			}

			if (f != null)
				f.setBounds(boundedX, boundedY, newWidth, newHeight);
		}
		else {
			if (f != null)
				f.setBounds(newX, newY, newWidth, newHeight);
		}

		if (hitBoundary) {
			if (f != null)
				f.validate();
		}
	}

	/**
	 * Is the frame within the bound of its parent ?
	 *
	 * @param f
	 * @param newX
	 * @param newY
	 * @param newWidth
	 * @param newHeight
	 * @return
	 */
	protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
		if (newX < 0 || newY < 0)
			return false;
		if (newX + newWidth > f.getDesktopPane().getWidth())
			return false;
        return newY + newHeight <= f.getDesktopPane().getHeight();
    }
}
