/**
 * Mars Simulation Project
 * ToolButton.java
 * @version 3.1.0 2019-02-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.border.LineBorder;

import java.awt.*;

/**
 * The ToolButton class is a UI button for a tool window.
 * It is displayed in the unit tool bar.
 */
public class ToolButton
extends JButton {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	// Data members
	/** The name of the tool which the button represents. */
	private String toolName;
	/** Customized tool tip with white background. */
	private JToolTip toolButtonTip;

	/**
	 * Constructs a ToolButton object.
	 * @param toolName the name of the tool
	 * @param imageName the name of the tool button image
	 */
	public ToolButton(String toolName, String imageName) {
		// Use JButton constructor
		super(ImageLoader.getIcon(imageName));
		// Initialize toolName
		this.toolName = toolName;
		
		init();
	}
	
	/**
	 * Initialize the button
	 */
	public void init() {
		// Initialize tool tip for button
		toolButtonTip = new JToolTip();
		toolButtonTip.setBackground(Color.white);
		toolButtonTip.setBorder(new LineBorder(Color.yellow));
		setToolTipText(toolName);

		// Prepare default tool button values
		setAlignmentX(.5F);
		setAlignmentY(.5F);
		
	}

	
	/**
	 * Returns tool name.
	 * @return tool name
	 */
	public String getToolName() { return toolName; }

	/**
	 * Overrides JComponent's createToolTip() method
	 * @return tool tip for tool
	 */
	@Override
	public JToolTip createToolTip() { return toolButtonTip; }
}
