//*********************** Splash Window ***********************
// Last Modified: 4/5/00

// The SplashWindow class is a splash screen shown when the project is loading.

import java.awt.*;
import javax.swing.*;

public class SplashWindow extends JWindow {

	// Constructor

	public SplashWindow() {
		
		// Don't display until window is created.
		
		setVisible(false);
		
		// Set the background to black.
		
		setBackground(Color.black);
		
		// Create ImageIcon from SplashImage.jpg.
		
		ImageIcon splashIcon = new ImageIcon("SplashImage.jpg");
		
		// Put image on label and add it to the splash window.
		
		JLabel splashLabel = new JLabel(splashIcon);
		getContentPane().add(splashLabel);
		
		// Pack the splash window to it's minimum size with the image.
		
		pack();
		
		// Sets root pane to double buffered.
		
		getRootPane().setDoubleBuffered(true);
		
		// Center the splash window on the screen.
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension windowSize = getSize();
		setLocation(((screenSize.width - windowSize.width) / 2), ((screenSize.height - windowSize.height) / 2));
		
		// Display the splash window.
		
		setVisible(true);
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