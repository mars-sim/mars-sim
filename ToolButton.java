//***************** Tool Button *****************
// Last Modified: 4/6/00

// The ToolButton class is a UI button for a tool window.
// It is displayed in the unit tool bar.

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class ToolButton extends JButton {

	// Data members
	
	private String toolName;        // The name of the tool which the button represents.
	private JToolTip toolButtonTip; // Customized tool tip with white background.

	// Constructor

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
	
	// Returns tool name
	
	public String getToolName() { return new String(toolName); }
	
	// Overrides JComponent's createToolTip() method
	
	public JToolTip createToolTip() { return toolButtonTip; }
}

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA