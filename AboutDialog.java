//******************* About Mars Simulation Project Window ****************
// Last Modified: 5/14/00

// The AboutDialog is an information window that is called from the "About The Mars Simulation Project"
// item in the MainWindowMenu.  It provides information about the project, credit to contributors and the GPL license.

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;

public class AboutDialog extends JDialog implements ActionListener, ComponentListener {

	// Data Members
	
	private JButton closeButton;  // The close button
	private JViewport viewPort;   // The view port for the text pane
	
	// Constructor
	
	public AboutDialog(MainWindow mainWindow) {
	
		// Use JDialog constructor
		
		super(mainWindow, "About The Mars Simulation Project", true);
	
		// Create the main panel
		
		JPanel mainPane = new JPanel(new BorderLayout());
		mainPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(mainPane);
		
		// Create the text panel
		
		JTextPane textPane = new JTextPane();
		DefaultStyledDocument document = new DefaultStyledDocument();
		textPane.setStyledDocument(document);
		textPane.setBackground(Color.lightGray);
		textPane.setBorder(new EmptyBorder(2, 2, 2, 2));
		textPane.setEditable(false);
		
		// Create the document
		
		try {
			document.insertString(0, "The Mars Simulation Project v2.7\n\n", null);
			document.insertString(document.getLength(), "Web Site: http://mars-sim.sourceforge.net\n\n", null);
			
			document.insertString(document.getLength(), "Developers:\n", null);
			document.insertString(document.getLength(), "  Scott Davis - Java programming, graphics\n", null);
			document.insertString(document.getLength(), "  James Barnard - 3D graphics, sound\n\n", null);
			
			document.insertString(document.getLength(), "Testing and recommendations:\n", null);
			document.insertString(document.getLength(), "Mike Jones\n", null);
			document.insertString(document.getLength(), "Dan Sepanski\n", null);
			document.insertString(document.getLength(), "Joe Wagner\n\n", null);
			
			document.insertString(document.getLength(), "This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License ", null);
			document.insertString(document.getLength(), "as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.\n\n", null);
			document.insertString(document.getLength(), "This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty ", null);
			document.insertString(document.getLength(), "of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.", null);
		}
		catch (BadLocationException e) { System.out.println(e.toString()); }
		
		JScrollPane scrollPane = new JScrollPane(textPane);
		viewPort = scrollPane.getViewport();
		viewPort.addComponentListener(this);
		viewPort.setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
		
		mainPane.add(scrollPane);
		
		// Create close button panel
		
		JPanel closeButtonPane = new JPanel(new FlowLayout());
		closeButtonPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		mainPane.add(closeButtonPane, "South");
		
		// Create close button
		
		closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		closeButtonPane.add(closeButton);
		
		// Set the size of the window
		
		setSize(350, 400);
		
		// Center the window on the parent window.
		
		Point parentLocation = mainWindow.getLocation();
		int Xloc = (int) parentLocation.getX() + ((mainWindow.getWidth() - 350) / 2);
		int Yloc = (int) parentLocation.getY() + ((mainWindow.getHeight() - 400) / 2);
		setLocation(Xloc, Yloc);
	
		// Prevent the window from being resized by the user.
	
		setResizable(false);
		
		// Show the window
		
		setVisible(true);
	}
	
	// Implementing ActionListener method
	
	public void actionPerformed(ActionEvent event) { dispose(); }
	
	// Implement ComponentListener interface.
	
	public void componentResized(ComponentEvent e) { viewPort.setViewPosition(new Point(0, 0)); }
	public void componentMoved(ComponentEvent e) {}
	public void componentShown(ComponentEvent e) {}
	public void componentHidden(ComponentEvent e) {}
}

// Mars Simulation Project
// Copyright (C) 2000 Scott Davis
//
// For questions or comments on this project, email:
// mars-sim-users@lists.sourceforge.net
//
// or visit the project's Web site at:
// http://mars-sim@sourceforge.net
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