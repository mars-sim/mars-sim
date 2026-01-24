/*
 * Mars Simulation Project
 * SwingHelper.java
 * @date 2025-08-28
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.utils;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatLaf;
import com.mars_sim.ui.swing.StyleManager;

import io.github.parubok.text.multiline.MultilineLabel;


/**
 * This is a static helper class of Swing methods
 */
public final class SwingHelper {

	private static boolean USE_TEXTARAEA_FOR_TEXT_BLOCKS = false;

	private SwingHelper() {
	}
	
    /**
     * Creates a popup window that displays a content panel.
     * It is shown below the current mouse position
     * but can be offset in the X & Y directions.
     * 
     * Size will default to the preferred size of the content unless overridden.
     * @param unit 
     * @param content Content to display
     * @param width Fixed width; can be -1
     * @param height Fixed height; can be -1
     * @param xOffset Offset of popup point in X
     * @param yOffset  Offset of popup point in X
     * @return
     */
    public static JDialog createPopupWindow(JPanel content, int width, int height, int xOffset, int yOffset) {
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
				// no change
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
			openBrowser(new URI(address));
		} catch (Exception e) {
			// placeholder
		}
	}

	public static void openBrowser(URI address) {
		try {
			Desktop.getDesktop().browse(address);
		} catch (IOException e) {
			//placeholder
		}
	}

	
    /**
     * Creates a titled border that uses the sub title font.
     * 
     * @param title
     * @return
     */
    public static Border createLabelBorder(String title) {
        return BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
                                                        TitledBorder.DEFAULT_POSITION,
                                                        StyleManager.getSubTitleFont(), (Color)null);
    }
	
	/*
	 * Creates a text block.
	 * 
	 * @param title Title for the surrounding border
	 * @param content Content for the text area
	 * @return The Swing component
	 */
	public static JComponent createTextBlock(String title, String content) {
		if (USE_TEXTARAEA_FOR_TEXT_BLOCKS) {
			return createTextBlockArea(title, content);
		}
		return createTextBlockMulti(title, content);
	}
		
	/*
	 * Creates a text block using a JTextArea
	 * 
	 * @param title Title for the surrounding border
	 * @param content Content for the text area
	 */
	private static JTextArea createTextBlockArea(String title, String content) {
		JTextArea ta = new JTextArea(content);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
	
		var border = BorderFactory.createCompoundBorder(createLabelBorder(title),
					BorderFactory.createEmptyBorder(10, 10, 10, 10));
		ta.setBorder(border);
		return ta;
	}

	/*
	 * Creates a text block using Multiline component
	 * 
	 * @param title Title for the surrounding border
	 * @param content Content for the text area
	 */
	private static JComponent createTextBlockMulti(String title, String content) {

		var label = new MultilineLabel(content);
		label.setMaxLines(10);
		label.setUseCurrentWidthForPreferredSize(false);
		
		var border = BorderFactory.createCompoundBorder(createLabelBorder(title),
					BorderFactory.createEmptyBorder(10, 10, 10, 10));
		label.setBorder(border);

		// Set the preferred size to force wrapping when resizing
		label.setMinimumSize(new Dimension(50,50));
		return label;
	}

	
    /**
     * Creates a scroll pane with border and title
     * 
     * @param title Title for the border
     * @param content Content to be shown in scroller
	 * @param dim Preferred size; can be null
     */
    public static JScrollPane createScrollBorder(String title, JComponent content, Dimension dim) {
		JScrollPane listScroller = new JScrollPane(content);
		listScroller.setBorder(createLabelBorder(title));
		if (dim != null) {
			listScroller.setPreferredSize(dim);
			listScroller.setMinimumSize(dim);
		}
        return listScroller;
    }
}
