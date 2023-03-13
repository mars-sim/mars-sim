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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
 */
public class StyleManager {

    private static final Logger logger = Logger.getLogger(StyleManager.class.getName());


    // Shared generic formatters
    public static final DecimalFormat DECIMAL_PLACES0 = new DecimalFormat("#,###,###,###");
    public static final DecimalFormat DECIMAL_PLACES1 = new DecimalFormat("#,###,##0.0");
    public static final DecimalFormat DECIMAL_PLACES3 = new DecimalFormat("#,###,##0.000");
    public static final DecimalFormat DECIMAL_PLACES2 = new DecimalFormat("#,###,##0.00");
    
    // Unit specific formatters
    public static final DecimalFormat DECIMAL_KM = new DecimalFormat("#,##0.00 km");
    public static final DecimalFormat DECIMAL_KMH = new DecimalFormat("##0.00 km/h");
    public static final DecimalFormat DECIMAL_KG = new DecimalFormat("#,##0.0 kg");
    public static final DecimalFormat DECIMAL_KW = new DecimalFormat("#,##0.0 kW");
    public static final DecimalFormat DECIMAL_KWH = new DecimalFormat("#,##0.0 kWh");
    public static final DecimalFormat DECIMAL_PERC = new DecimalFormat("0 '%'");
    public static final DecimalFormat DECIMAL_PERC2 = new DecimalFormat("0.00 '%'");
    public static final DecimalFormat DECIMAL_SOLS = new DecimalFormat("#,##0.0 Sols");
    public static final DecimalFormat DECIMAL_DEG = new DecimalFormat("0.# \u00B0");
    public static final DecimalFormat DECIMAL_CELCIUS = new DecimalFormat("0.0 \u00B0C");

    // Supported LAFs
    private static final String DARK = "Flat Dark";
    private static final String LIGHT = "Flat Light";
    private static final String LIGHT_BLUE = LIGHT + " - Blue";
    private static final String LIGHT_RED = LIGHT + " - Red";
    private static final String LIGHT_ORANGE = LIGHT + " - Orange";
    private static final String LIGHT_GREEN = LIGHT + " - Green";
    private static final String SYSTEM = "Default System";
    private static final String [] STYLES = {LIGHT_BLUE, LIGHT_GREEN, LIGHT_ORANGE, LIGHT_RED, DARK, SYSTEM};

    // Constants for font definition
    private static final String UIMANAGER_FONT = "defaultFont";
    private static final String DEFAULT_FONT_STYLE = "defaultFont";
    private static final String LABEL_FONT_STYLE = "labelFont";
    private static final String HEADING_FONT_STYLE = "headingFont";
    private static final String SUBHEADING_FONT_STYLE = "subHeadingFont";
    private static final String FONT_FAMILY = "family";
    private static final String FONT_STYLE = "style";
    private static final String FONT_SIZE = "size";

    // Constrants for LaF styling
    private static final String LAF_STYLE = "Look_and_Feel";
    private static final String LAF_NAME = "name";

    private static Font labelFont;
    private static Font systemFont;
    private static Font headingFont;
    private static Font subHeadingFont;

    private static Map<String,Properties> styles = new HashMap<>();

    // Create the builtin defaults
    static {
        Properties defaultProps = new Properties();
        defaultProps.setProperty(FONT_FAMILY, "Segoe UI");
        defaultProps.setProperty(FONT_SIZE, "12");
        defaultProps.setProperty(FONT_STYLE, "PLAIN");
        styles.put(DEFAULT_FONT_STYLE, defaultProps);
        Properties labelProps = new Properties();
        labelProps.setProperty(FONT_STYLE, "BOLD");
        styles.put(LABEL_FONT_STYLE, labelProps);
        Properties headingProps = new Properties();
        headingProps.setProperty(FONT_STYLE, "BOLD");
        headingProps.setProperty(FONT_SIZE, "+8");
        styles.put(HEADING_FONT_STYLE, headingProps);
        Properties subHeadingProps = new Properties();
        subHeadingProps.setProperty(FONT_STYLE, "BOLD");
        subHeadingProps.setProperty(FONT_SIZE, "+4");
        styles.put(SUBHEADING_FONT_STYLE, subHeadingProps);
        Properties lafProps = new Properties();
        lafProps.setProperty(LAF_NAME, LIGHT_RED);
        styles.put(LAF_STYLE, lafProps);
    }

    /**
     * Get available LAF
     */
    public static String[] getAvailableLAF() {
        return STYLES;
    }

    /**
     * Which LAF has been selected.
     */
    public static String getLAF() {
        return styles.get(LAF_STYLE).getProperty(LAF_NAME);
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
                    logger.warning( "Don't know LAF style " + style);
            }

            if (newLAF != null) {
                // Flat LAF supports accent colour
                boolean isFlatLAF = (newLAF instanceof FlatLaf);
                if ((accentColor != null) && isFlatLAF) {
                    applyAccentColor(accentColor);
                }

                calculateFonts();

                // Apply LAF
                UIManager.setLookAndFeel( newLAF );
                styles.get(LAF_STYLE).setProperty(LAF_NAME, style);

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
     * Get the styles used.
     * @return
     */
    public static Map<String, Properties> getStyles() {
        return styles;
    }

    /**
     * Load the styles defintions to use in this UI
     * @param newStyles
     */
    public static void setStyles(Map<String,Properties> newStyles) {
        // MErge the incoming styles with the ones used internally
        for(Entry<String, Properties> usedStyle : styles.entrySet()) {
            Properties overrides = newStyles.get(usedStyle.getKey());
            if (overrides != null) {
                usedStyle.getValue().putAll(overrides);
            }
        }

        // Load LAF to use styles
        setLAF(getLAF());
    }

    /**
     * Calculate the various fonts used in the styling
     */
    private static void calculateFonts() {
        if (systemFont == null) {
            // Get the built-in default font. This is a terrible logic
            systemFont = (new JLabel("Text")).getFont();
        }

        Font defaultFont = createFont(systemFont, styles.get(DEFAULT_FONT_STYLE));
        labelFont = createFont(defaultFont, styles.get(LABEL_FONT_STYLE));
        headingFont = createFont(defaultFont, styles.get(HEADING_FONT_STYLE));
        subHeadingFont = createFont(defaultFont, styles.get(SUBHEADING_FONT_STYLE));

        // Hardcode the default font
        UIManager.put( UIMANAGER_FONT, defaultFont);
    }

    /**
     * Creates a new Font by deriving it from a base font.
     * @param base Base font
     * @param attrs Attributes used to derive a new font
     * @return
     */
    private static Font createFont(Font base, Properties attrs) {
        String family = base.getFamily();
        int style = base.getStyle();
        int size = base.getSize();

        if (attrs == null) {
            return base;
        }

        if (attrs.getProperty(FONT_FAMILY) != null) {
            family = attrs.getProperty(FONT_FAMILY);
        }
        if (attrs.getProperty(FONT_STYLE) != null) {
            switch(attrs.getProperty(FONT_STYLE).toLowerCase()) {
                case "bold": style = Font.BOLD; break;
                case "italic": style = Font.ITALIC; break;
                case "plain": style = Font.PLAIN; break;
                case "bold_italic": style = Font.BOLD | Font.ITALIC; break;
                default:
            }
        }
        if (attrs.getProperty(FONT_SIZE) != null) {
            String sizeText = attrs.getProperty(FONT_SIZE);
            try {
                size = Integer.parseInt(sizeText);
                if (sizeText.startsWith("+") || sizeText.startsWith("-")) {
                    size = base.getSize() + size;
                }
            }
            catch (NumberFormatException n) {
                logger.warning("Font size not correctly formatted " + sizeText);
            }
        }
        return new Font(family, style, size);
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
     * Lookup a color by name. Only supports the static entries in the Color class.
     */
    private static Color getColorByName(String name) {
        try {
            return (Color)Color.class.getField(name.toUpperCase()).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            logger.warning("Don't know Colour called style " + name);
            return Color.RED;
        }
    }

    /**
     * Apply styling to make this a SubHeading; normally applied to a JLabel
     */
    public static void applyHeading(JComponent item) {
        item.setFont(headingFont);
    }

    /**
     * Apply styling to make this a SubHeading; normally applied to a JLabel
     */
    public static void applySubHeading(JComponent item) {
        item.setFont(subHeadingFont);
    }

    /**
     * Crete a Titled border that uses the Label font
     * @param title
     * @return
     */
    public static Border createLabelBorder(String title) {
        return BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
                                                        TitledBorder.DEFAULT_POSITION,
                                                        labelFont, (Color)null);
    }

    /**
     * Get the Bold default font
     * @return
     */
    public static Font getLabelFont() {
        return labelFont;
    }
}
