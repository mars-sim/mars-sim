//***************** Tool Window *****************
// Last Modified: 4/2/00

// The ToolWindow class is an abstract UI window for a tool.
// Particular tool windows should be derived from this.

import javax.swing.*;

public abstract class ToolWindow extends JInternalFrame {

	// Data members
	
	protected String toolName;    // The name of the tool the window is for.
	protected boolean notOpened; // True if window hasn't yet been opened.

	// Constructor

	public ToolWindow(String toolName) {
		
		// Use JInternalFrame constructor
		
		super(toolName, false, true, false, false); 
		
		// Initialize tool name
		
		this.toolName = new String(toolName);
		
		// Set notOpened to true
		
		notOpened = true;
	}
	
	// Returns tool name
	
	public String getToolName() { return new String(toolName); }
	
	// Returns true if tool window has not previously been opened.
	
	public boolean hasNotBeenOpened() { return notOpened; }
	
	// Sets notOpened to false.
	
	public void setOpened() { notOpened = false; }
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