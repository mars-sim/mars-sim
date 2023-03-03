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
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
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

    // Shared formatters
    public static final DecimalFormat DECIMAL_PLACES0 = new DecimalFormat("#,###,###,###");
    public static final DecimalFormat DECIMAL_PLACES1 = new DecimalFormat("#,###,##0.0");
    public static final DecimalFormat DECIMAL_PLACES3 = new DecimalFormat("#,###,##0.000");
    public static final DecimalFormat DECIMAL_PLACES2 = new DecimalFormat("#,###,##0.00");
    public static final DecimalFormat DECIMAL_KG = new DecimalFormat("#,##0.0 kg");
    public static final DecimalFormat DECIMAL_KW = new DecimalFormat("#,##0.0 kW");
    public static final DecimalFormat DECIMAL_KWH = new DecimalFormat("#,##0.0 kWh");
    public static final DecimalFormat DECIMAL_PERC = new DecimalFormat("0 '%'");
    public static final DecimalFormat DECIMAL_SOLS = new DecimalFormat("#,###,##0.0 Sols");

    private static final String DARK = "Flat Dark";
    private static final String LIGHT = "Flat Light";
    private static final String LIGHT_BLUE = LIGHT + " - Blue";
    private static final String LIGHT_RED = LIGHT + " - Red";
    private static final String LIGHT_ORANGE = LIGHT + " - Orange";
    private static final String LIGHT_GREEN = LIGHT + " - Green";
    private static final String SYSTEM = "Default System";

    public static final String DEFAULT_LAF = LIGHT_RED;

    private static final String [] STYLES = {LIGHT_BLUE, LIGHT_GREEN, LIGHT_ORANGE, LIGHT_RED, DARK, SYSTEM};

    private static final Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 12);

    private static Font subHeading;

    private static String selectedLAF = SYSTEM;

    /**
     * Get available LAF
     */
    public static String[] getAvailableLAF() {
        return STYLES;
    }

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

        // Check for accent
        String lafName = style;
        Color accentColor = null;
        int split = style.indexOf('-');
        if (split > 0) {
            String accentText = style.substring(split+1).trim();
            accentColor = getColorByName(accentText);
            lafName = style.substring(0, split).trim();
        }

        LookAndFeel newLAF = null;
        try {
            switch(lafName) {
                case LIGHT: 
                    newLAF = new FlatLightLaf();
                    break;

                case DARK:
                    newLAF = new FlatDarkLaf(); 
                    break;

                case SYSTEM:
                    // Feels a bit messy
                    String systemClassName = UIManager.getSystemLookAndFeelClassName();
                    Class<LookAndFeel> lafClass = (Class<LookAndFeel>) Class.forName(systemClassName);
                    newLAF = lafClass.getDeclaredConstructor().newInstance();
                    break;
                
                default:
                    System.err.println( "Don't know LAF style " + style);
            }

            if (newLAF != null) {
                // Flat LAF supports accent colour
                boolean isFlatLAF = (newLAF instanceof FlatLaf);
                if ((accentColor != null) && isFlatLAF) {
                    applyAccentColor(accentColor);
                }

                // Hardcode the default font
                UIManager.put( "defaultFont", DEFAULT_FONT);

                // Apply LAF
                UIManager.setLookAndFeel( newLAF );
                selectedLAF = style;

                subHeading = UIManager.getFont( SUB_HEADING_STYLE + ".font"); 

                // Add color strippng on the table
                UIDefaults defaults = UIManager.getLookAndFeelDefaults();
                if (defaults.get("Table.alternateRowColor") == null) {
                    Color tabBackground = (Color) defaults.get("Table.background");
                    Color selBackground = (Color) defaults.get("Table.selectionBackground");

                    defaults.put("Table.alternateRowColor", getTableAlternativeColor(selBackground, tabBackground));

                    // Header is scalled from the inactive Select colour
                    Color inactiveBackground = (Color) defaults.get("Table.selectionInactiveBackground");
                    if (inactiveBackground != null) {
                        defaults.put("TableHeader.background", getScalledColor(inactiveBackground, 0.8));
                        
                        // Make sure sort icon is noticable
                        defaults.put("Table.sortIconColor", defaults.get("TableHeader.foreground"));
                    }
                }

                // Always use a grid on Tables
                defaults.put("Table.showHorizontalLines", true);
                defaults.put("Table.showVerticalLines", true);
            }
        }
        catch (Exception e) {
            // Many things can go wrong so catch all
            e.printStackTrace();
            newLAF = null;
        } 
        return (newLAF != null);
	}

    /**
     * Which LAF has been selected.
     */
    public static String getLAF() {
        return selectedLAF;
    }

    /**
     * Create a new color by scaling the soruce by a percentge factor.
     * @param source
     * @param factor
     * @return
     */
    private static Color getScalledColor(Color source, double factor) {
        return new Color(Math.max((int)(source.getRed()  * factor), 0),
                        Math.max((int)(source.getGreen()* factor), 0),
                        Math.max((int)(source.getBlue() * factor), 0),
                        source.getAlpha());
    }

    /**
     * Copied approach from ZTable
     * @param selBackground
     * @param tabBackground
     * @return
     */
    private static Color getTableAlternativeColor(Color selBackground, Color tabBackground) {
        final float[] bgHSB = Color.RGBtoHSB(
            tabBackground.getRed( ), tabBackground.getGreen( ),
            tabBackground.getBlue( ), null );
        final float[] selHSB  = Color.RGBtoHSB(
            selBackground.getRed( ), selBackground.getGreen( ), selBackground.getBlue( ), null );
        return Color.getHSBColor(
            (selHSB[1]==0.0||selHSB[2]==0.0) ? bgHSB[0] : selHSB[0],
            0.1f * selHSB[1] + 0.9f * bgHSB[1],
            bgHSB[2] + ((bgHSB[2]<0.5f) ? 0.0005f : -0.0005f) 
            );
    }

    /**
     * Lookup a color by name. Only supports the static entries in the Color class.
     */
    private static Color getColorByName(String name) {
        try {
            return (Color)Color.class.getField(name.toUpperCase()).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
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
    private static void applyAccentColor(Color newColour) {
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
