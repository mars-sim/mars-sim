/**
 * Mars Simulation Project
 * MarsTheme.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;  

import java.awt.*; 
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

/** The MarsTheme class provides a custom color theme to the project
 *  UI.
 */
public class MarsTheme extends DefaultMetalTheme {
	
    // Set primary colors for theme 
    private final ColorUIResource primary1 = new ColorUIResource(0, 150, 0);
    private final ColorUIResource primary2 = new ColorUIResource(0, 150, 0);
    private final ColorUIResource primary3 = new ColorUIResource(0, 190, 0);
	
    protected ColorUIResource getPrimary1() { return primary1; }
    protected ColorUIResource getPrimary2() { return primary2; }
    protected ColorUIResource getPrimary3() { return primary3; }

    // Set default fonts for theme
    private final FontUIResource defaultFont = new FontUIResource("SansSerif", Font.PLAIN, 11);
    private final FontUIResource defaultBoldFont = new FontUIResource("SansSerif", Font.BOLD, 12);
    private final FontUIResource defaultSmallFont = new FontUIResource("SansSerif", Font.PLAIN, 10);

    public FontUIResource getControlTextFont() { return defaultFont; }
    public FontUIResource getSystemTextFont() { return defaultFont; }
    public FontUIResource getUserTextFont() { return defaultSmallFont; }
    public FontUIResource getMenuTextFont() { return defaultBoldFont; }
    public FontUIResource getWindowTitleFont() { return defaultBoldFont; }
    public FontUIResource getSubTextFont() { return defaultFont; }

    // Set default text color for theme
    private final ColorUIResource defaultTextColor = new ColorUIResource(Color.black);
    
    public ColorUIResource getSystemTextColor() { return defaultTextColor; }
    
    /** Returns the theme's name 
     *  @return name of theme
     */
    public String getName() { return "Mars Project Theme"; }
}
