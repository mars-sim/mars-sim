//************************** Master Clock For Virtual Mars **************************
// Last Modified: 2/20/00

// The MasterClock represents the simulated time clock on virtual Mars.
// Virtual Mars has only one master clock.
// The master clock delivers a clock pulse the virtual Mars every second or so,
// which represents 10 minutes of simulated time.
// All actions taken with virtual Mars and its units are synchronized with this clock pulse.

// Note: Later the master clock will control calendaring information as well, so Martian 
// calendars and clocks can be displayed.

public class MasterClock extends Thread {
	
	// Data members
	
	private VirtualMars mars;       // Virtual Mars
	
	// Constructor
	
	public MasterClock(VirtualMars mars) {
	
		// Initialize data members
	
		this.mars = mars;
	}
	
	// Run clock
	
	public void run() {

		// Endless clock pulse loop

		while(true) { 
			
			// Pause for 1 second between clock pulses

			try { sleep(1000); }
			catch (InterruptedException e) {}
			
			// Send virtual Mars a clock pulse representing 10 minutes (600 seconds)
			
			mars.clockPulse(600);
		}
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