/**
 * Mars Simulation Project
 * UniversalUnitWindowListener.java
 * @version 
 * @author Dima Stepanchuk
 */
package org.mars_sim.msp.ui.standard;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import org.mars_sim.msp.ui.standard.unit_window.*;
import org.mars_sim.msp.simulation.*;

public class UniversalUnitWindowListener implements InternalFrameListener {

	private UnitInspector myInspector;
	public UniversalUnitWindowListener (UnitInspector inspector)
	{
		myInspector=inspector;
	}
	
	public void internalFrameActivated(InternalFrameEvent e) {
		Unit unit = ((UnitWindow)e.getSource()).getUnit();
		myInspector.focused(unit);
		
	}

	public void internalFrameDeactivated(InternalFrameEvent e) {
		Unit unit = ((UnitWindow)e.getSource()).getUnit();
		myInspector.unFocused(unit);
		
	}

	
	
	
	public void internalFrameOpened(InternalFrameEvent e) {	
		}
	public void internalFrameClosing(InternalFrameEvent e) {
		}
	public void internalFrameClosed(InternalFrameEvent e) {
	}
	public void internalFrameIconified(InternalFrameEvent e) {
	}
	public void internalFrameDeiconified(InternalFrameEvent e) {
	}
	
}
