/**
 * Mars Simulation Project
 * ToolButton.java
 * @version 2.71 2000-10-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard; 

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** The ToolButton class is a UI button for a tool window.
 *  It is displayed in the unit tool bar.
 */
public class ToolButton extends JButton {

	// Data members	
	private String toolName;        // The name of the tool which the button represents.
	private JToolTip toolButtonTip; // Customized tool tip with white background.

	/** Constructs a ToolButton object */
	public ToolButton(String toolName, String imageName) {
		
		// Use JButton constructor	
		super(new ImageIcon(imageName)); 
		
		// Initialize toolName
		this.toolName = new String(toolName);
		
		// Initialize tool tip for button
		toolButtonTip = new JToolTip();
		toolButtonTip.setBackground(Color.white);
		toolButtonTip.setBorder(new LineBorder(Color.green));
		setToolTipText(toolName);
		
		// Prepare default tool button values
		setAlignmentX(.5F);
		setAlignmentY(.5F);
	}
	
	/** Returns tool name */
	public String getToolName() { return new String(toolName); }
	
	/** Overrides JComponent's createToolTip() method */
	public JToolTip createToolTip() { return toolButtonTip; }
}
