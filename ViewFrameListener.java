//*********************** General View Frame Listener ***********************
// Last Modified: 2/24/00

// The ViewFrameListener class manages internal frame behaviors.

import javax.swing.*;
import javax.swing.event.*;

public class ViewFrameListener extends InternalFrameAdapter {
	
	// Open internal frame (overridden)
	
	public void internalFrameOpened(InternalFrameEvent e) { 
		JInternalFrame frame = (JInternalFrame) e.getSource();
		try { frame.setClosed(false); }
		catch(java.beans.PropertyVetoException v) { System.out.println(frame.getTitle() + " setClosed() is Vetoed!"); }
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