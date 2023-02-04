/*
 * Mars Simulation Project
 * StyleManager.java
 * @date 2023-02-03
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

/**
 * This class provides a means to control the styles used in the UI.
 * This provides an abstract to the FlatLAF approach to managing fonts
 * {@link} https://www.formdev.com/flatlaf/typography/
 */
public class StyleManager {

    /**
     *
     */
    private static final String FLAT_LAF_STYLE_CLASS = "FlatLaf.styleClass";

    public static final String HEADING_STYLE = "h2";
    public static final String SUB_HEADING_STYLE = "h3";

    public static final DecimalFormat DECIMAL_PLACES0 = new DecimalFormat("#,###,###,###");
    public static final DecimalFormat DECIMAL_PLACES1 = new DecimalFormat("#,###,##0.0");
    public static final DecimalFormat DECIMAL_PLACES3 = new DecimalFormat("#,###,##0.000");
    public static final DecimalFormat DECIMAL_PLACES2 = new DecimalFormat("#,###,##0.00");
    public static final DecimalFormat DECIMAL_KG = new DecimalFormat("#,##0.0 kg");
    public static final DecimalFormat DECIMAL_KW = new DecimalFormat("#,##0.0 kW");
    public static final DecimalFormat DECIMAL_KWH = new DecimalFormat("#,##0.0 kWh");


    private static final String DARK = "dark";
    private static final String LIGHT = "light";
    private static final String METAL = "metal";
    private static final String SYSTEM = "system";

    private static Font subHeading;

    /**
	 * Sets the look and feel of the UI. This is fixed but need to be made variable
	 * and moved to the UIConfig class.
	 *
	 * @param choice
	 */
	public static boolean setLAF(String style) {
        if (style == null) {
            style = LIGHT;
        }

		boolean result = true;
        try {
            switch(style) {
                case LIGHT: 
                    // Set Accent color as Red
                    //applyAccentColor(Color.RED);
                    UIManager.setLookAndFeel( new FlatLightLaf() );
                    subHeading = UIManager.getFont( SUB_HEADING_STYLE + ".font"); 
                    break;

                case DARK:
                    UIManager.setLookAndFeel( new FlatDarkLaf() );
                    subHeading = UIManager.getFont( SUB_HEADING_STYLE + ".font"); 
                    break;

                case METAL:
                    break;
                
                default:
                    System.err.println( "Don't know LAF style " + style);
                    result = false;
            }


		} catch( Exception ex ) {
			System.err.println( "Failed to initialize LaF" );
            result = false;

		}

        return result;
	}

    /**
     * Apply styling to make this a SubHeading; normally applied to a JLabel
     */
    public static void applyHeading(JComponent item) {
        item.putClientProperty(FLAT_LAF_STYLE_CLASS, HEADING_STYLE);
    }

    /**
     * Apply styling to make this a SubHeading; normally applied to a JLabel
     */
    public static void applySubHeading(JComponent item) {
        item.putClientProperty(FLAT_LAF_STYLE_CLASS, SUB_HEADING_STYLE);
    }
    
    /**
     * Set the accent colour
     * @param newColour
     */
    public static void applyAccentColor(Color newColour) {
        // Set Accent color. Code taken from the description of the setSyetmColorGetter method
        FlatLaf.setSystemColorGetter( name -> {
            return name.equals( "accent" ) ? newColour : null;
        } );
    }


    /**
     * Crete a Titled border that uses the SubHeading font
     * @param title
     * @return
     */
    public static Border createSubHeadingBorder(String title) {
        return BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                                                subHeading, (Color)null);
    }

}
