/*
 * Mars Simulation Project
 * SwingHelper.java
 * @date 2023-01-22
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.utils;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.net.URI;

import javax.swing.JDialog;
import javax.swing.JPanel;

import com.formdev.flatlaf.FlatLaf;


/**
 * This is a static helper class of Swing methods
 */
public final class SwingHelper {

	private SwingHelper() {
	}
	
    /**
     * Creates a popup window that displays a content panel.
     * It is shown below the current mouse position
     * but can be offset in the X & Y directions.
     * 
     * Size will default to the preferred size of the content unless overridden.
     * @param content Content to display
     * @param width Fixed width; can be -1
     * @param height Fixed height; can be -1
     * @param xOffset Offset of popup point in X
     * @param yOffset  Offset of popup point in X
     * @return
     */
    public static JDialog createPoupWindow(JPanel content, int width, int height, int xOffset, int yOffset) {
		JDialog d = new JDialog();
		d.setUndecorated(true);
                
		if (width <= 0 || height <= 0) {
			Dimension dims = content.getPreferredSize();
			width = (int) dims.getWidth();
			height = (int) dims.getHeight();
		}
		d.setSize(width, height);
		d.setResizable(false);
		d.add(content);

		// Make it to appear at the mouse cursor
		Point location = MouseInfo.getPointerInfo().getLocation();
		location.translate(xOffset, yOffset);
		d.setLocation(location);

		d.addWindowFocusListener(new WindowFocusListener() {
			public void windowLostFocus(WindowEvent e) {
				d.dispose();
			}
			public void windowGainedFocus(WindowEvent e) {
				// placeholder
			}
		});

		// Call to update the all components if a new theme is chosen
		FlatLaf.updateUI();
		
		return d;
	}

    
	/**
	 * Opens the default browser on a URL.
	 */
	public static void openBrowser(String address) {
		try {
			Desktop.getDesktop().browse(new URI(address));
		} catch (Exception e) {
			// placeholder
		}
	}
}
