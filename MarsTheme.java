//*********************** Color Theme For UI ***********************
// Last Modified: 2/22/00

// The MarsTheme class provides a custom color theme to the project UI.

import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

public class MarsTheme extends DefaultMetalTheme {
	
	public String getName() { return "Mars Project"; }
	
	private final ColorUIResource primary1 = new ColorUIResource(0, 150, 0);
	private final ColorUIResource primary2 = new ColorUIResource(0, 150, 0);
	private final ColorUIResource primary3 = new ColorUIResource(0, 190, 0);
	
	protected ColorUIResource getPrimary1() { return primary1; }
	protected ColorUIResource getPrimary2() { return primary2; }
	protected ColorUIResource getPrimary3() { return primary3; }
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