//*********************** Mars Simulation Project ***********************
// Last Modified: 3/15/00

// The MarsProject class is the main class for the application.
// It creates both virtual Mars and the user interface.

import java.io.*;
import javax.swing.plaf.metal.*;
import javax.swing.*;
import java.awt.*;

public class MarsProject {

	// Constructor

	public MarsProject() {
		
		// Set system err output to a text file
		// (Used for debugging purposes)
		
		/*
		try {
			FileOutputStream errFileStream = new FileOutputStream("err.txt");
			System.setErr(new PrintStream(errFileStream));
		}
		catch(FileNotFoundException e) { System.out.println("err.txt could not be opened"); }
		*/
		
		// Prepare custom UI color theme
		
		MetalLookAndFeel.setCurrentTheme(new MarsTheme());
		
		// Create a splash window
		
		SplashWindow splashWindow = new SplashWindow();
		
		// Create virtual mars
		
		VirtualMars mars = new VirtualMars();
		
		// Create main desktop window
		
		MainWindow window = new MainWindow(mars);
		
		// Free memory from splash window
		
		splashWindow.dispose();
		splashWindow = null;
	}

	// Main Method

	public static void main(String args[]) { MarsProject marsSim = new MarsProject(); }
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