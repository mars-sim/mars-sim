/**
 * Mars Simulation Project
 * MarsTheme.java
 * @version 2.71 2000-10-07
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  
 
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

/** The MarsTheme class provides a custom color theme to the project
 *  UI.
 */
public class MarsTheme extends DefaultMetalTheme {
	
    // Set primary colors for UI
    private final ColorUIResource primary1 = new ColorUIResource(0, 150, 0);
    private final ColorUIResource primary2 = new ColorUIResource(0, 150, 0);
    private final ColorUIResource primary3 = new ColorUIResource(0, 190, 0);
	
    protected ColorUIResource getPrimary1() { return primary1; }
    protected ColorUIResource getPrimary2() { return primary2; }
    protected ColorUIResource getPrimary3() { return primary3; }

    /** Returns the theme's name */
    public String getName() { return "Mars Project"; }
}
