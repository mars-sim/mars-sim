//******************* About Mars Simulation Project Window ****************
// Last Modified: 4/10/00

// The AboutDialog is an information window that is called from the "About The Mars Simulation Project"
// item in the MainWindowMenu.  It provides information about the project, credit to contributors and the GPL license.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class AboutDialog extends JDialog implements ActionListener {

	// Data Members
	
	private JButton closeButton;  // The close button
	
	// Constructor
	
	public AboutDialog(MainWindow mainWindow) {
	
		// Use JDialog constructor
		
		super(mainWindow, "About The Mars Simulation Project", true);
	
		// Create the main panel
		
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
	
		// Create the content string
	
		StringBuffer content = new StringBuffer();
		
		content.append("The Mars Simulation Project v2.7\n\n");
		
		content.append("Web Site: http://mars-sim.sourceforge.net\n\n");
		
		content.append("Developers:\n");
		content.append("  Scott Davis - Java programming, graphics\n");
		content.append("  James Barnard - 3D graphics\n\n");
		
		content.append("Special thanks to Mike Jones and Dan Sepanski for testing and recommendations.\n\n");
		
		content.append("This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License ");
		content.append("as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.\n\n");
		
		content.append("This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty ");
		content.append("of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.");
		
		// Create content text area
		
		JTextArea contentArea = new JTextArea(new String(content));
		contentArea.setBackground(Color.lightGray);
		contentArea.setBorder(new EmptyBorder(2, 2, 2, 2));
		contentArea.setLineWrap(true);
		contentArea.setWrapStyleWord(true);
		contentArea.setEditable(false);
		mainPane.add(new JScrollPane(contentArea), "Center");
		
		// Create close button panel
		
		JPanel closeButtonPane = new JPanel(new FlowLayout());
		closeButtonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.add(closeButtonPane, "South");
		
		// Create close button
		
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButtonPane.add(closeButton);
		
		// Set the size of the window
		
		setSize(300, 400);
		
		// Center the window on the parent window.
		
		Point parentLocation = mainWindow.getLocation();
		int Xloc = (int) parentLocation.getX() + ((mainWindow.getWidth() - 300) / 2);
		int Yloc = (int) parentLocation.getY() + ((mainWindow.getHeight() - 400) / 2);
		setLocation(Xloc, Yloc);
		
		// Display window
		
		setVisible(true);
	}
	
	// Implementing ActionListener method
	
	public void actionPerformed(ActionEvent event) {
		dispose();	
	}
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