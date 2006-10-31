/**
 * Mars Simulation Project
 * UnitToolbar.java
 * @version 2.75 2003-07-20
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  

import java.awt.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.unit_display_info.*;

/**
 * The UnitButton class is a UI button for a given unit.
 * It is displayed in the unit tool bar.
 */
public class UnitButton extends JButton {

    // Data members	
	private Unit unit;

    /** 
     * Constructor
     *
     * @param unit the unit the button is for.
     */
	public UnitButton(Unit unit) {
		
		// Use JButton constructor	
		super(unit.getName(), 
            UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getButtonIcon()); 
		
		// Initialize unit
		this.unit = unit;
		
		// Prepare default unit button values
		setFont(new Font("SansSerif", Font.PLAIN, 9));
		setVerticalTextPosition(SwingConstants.BOTTOM);
		setHorizontalTextPosition(SwingConstants.CENTER);
		setAlignmentX(.5F);
		setAlignmentY(.5F);
	}
	
	/** 
     * Gets the button's unit.
     *
     * @return the button's unit
     */
	public Unit getUnit() { 
        return unit; 
    }
}
