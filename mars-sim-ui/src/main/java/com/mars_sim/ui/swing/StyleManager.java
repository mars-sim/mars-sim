/*
 * Mars Simulation Project
 * StyleManager.java
 * @date 2025-06-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ColorUIResource;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatHiberbeeDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTLightOwlIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTMonokaiProIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTNightOwlIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedDarkIJTheme;
import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatMTSolarizedLightIJTheme;

import com.formdev.flatlaf.util.HSLColor;
import com.mars_sim.core.tool.Msg;

/**
 * This class provides a means to control the styles used in the UI.
 */
public class StyleManager {

    private static final Logger logger = Logger.getLogger(StyleManager.class.getName());


    // Shared generic formatters 
    public static final DecimalFormat CURRENCY_PLACES1 = new DecimalFormat("$ #,###,##0.0");
    public static final DecimalFormat DECIMAL_PLACES0 = new DecimalFormat("#,###,###,###");
    public static final DecimalFormat DECIMAL_PLACES1 = new DecimalFormat("#,###,##0.0");
    public static final DecimalFormat DECIMAL_PLACES3 = new DecimalFormat("#,###,##0.000");
    public static final DecimalFormat DECIMAL_PLACES4 = new DecimalFormat("#,###,##0.0000");
    public static final DecimalFormat DECIMAL_PLACES2 = new DecimalFormat("#,###,##0.00");
    
    // Unit specific formatters
    public static final DecimalFormat DECIMAL_AH = new DecimalFormat("#,##0.0 Ah");
    public static final DecimalFormat DECIMAL_LITER2 = new DecimalFormat("#,##0.0 Liter");
    
    public static final DecimalFormat DECIMAL_KM_KG = new DecimalFormat("#,##0.00 km/kg");
    public static final DecimalFormat DECIMAL_WH_KM = new DecimalFormat("#,##0.00 Wh/km");
    public static final DecimalFormat DECIMAL_KWH_KM = new DecimalFormat("#,##0.000 kWh/km");
    
    public static final DecimalFormat DECIMAL_KWH_KG = new DecimalFormat("#,##0.00 kWh/kg");
    public static final DecimalFormat DECIMAL_WH_KG = new DecimalFormat("#,##0.00 Wh/kg");
    
    public static final DecimalFormat DECIMAL_M2 = new DecimalFormat("#,##0.00 m\u00B2");
    public static final DecimalFormat DECIMAL_M_S2 = new DecimalFormat("#,##0.00 "
									+ Msg.getString("unit.meterperssecsquared")); //-NLS-1$   
    public static final DecimalFormat DECIMAL_M_S = new DecimalFormat("#,##0.00 " 
    								+ Msg.getString("unit.meterpersec")); //-NLS-1$
    
    public static final DecimalFormat DECIMAL_KJ = new DecimalFormat("#,##0.0 kJ");
    public static final DecimalFormat DECIMAL_KM = new DecimalFormat("#,##0.00 km");
    public static final DecimalFormat DECIMAL3_KM = new DecimalFormat("#,##0.000 km");
    
    public static final DecimalFormat DECIMAL_M = new DecimalFormat("#,##0.00 m");
    
    public static final DecimalFormat DECIMAL_KPH = new DecimalFormat("##0.00 kph");
    
    public static final DecimalFormat DECIMAL_KG = new DecimalFormat("#,##0.0 kg");
    public static final DecimalFormat DECIMAL_KG2 = new DecimalFormat("#,##0.00 kg");
    
    public static final DecimalFormat DECIMAL1_KG_SOL = new DecimalFormat("#,##0.0 kg/sol");
    public static final DecimalFormat DECIMAL2_G_LITER = new DecimalFormat("#,##0.00 g/L");
	
    public static final DecimalFormat DECIMAL_KW = new DecimalFormat("#,##0.0 kW");
    public static final DecimalFormat DECIMAL2_KW = new DecimalFormat("#,##0.00 kW");
    public static final DecimalFormat DECIMAL_KWH = new DecimalFormat("#,##0.0 kWh");
    public static final DecimalFormat DECIMAL2_KWH = new DecimalFormat("#,##0.00 kWh");
    public static final DecimalFormat DECIMAL3_N = new DecimalFormat("#,##0.000 N");
    
    public static final DecimalFormat DECIMAL_PERC = new DecimalFormat("0 '%'");
    public static final DecimalFormat DECIMAL1_PERC = new DecimalFormat("0.0 '%'");
    public static final DecimalFormat DECIMAL2_PERC = new DecimalFormat("0.00 '%'");
    public static final DecimalFormat DECIMAL_SOLS = new DecimalFormat("#,##0 sols");
    public static final DecimalFormat DECIMAL1_SOLS = new DecimalFormat("#,##0.0 sols");
    public static final DecimalFormat DECIMAL2_SOLS = new DecimalFormat("#,##0.00 sols");
    public static final DecimalFormat DECIMAL3_SOLS = new DecimalFormat("#,##0.000 sols");
    public static final DecimalFormat DECIMAL_DEG = new DecimalFormat("0.# \u00B0");
    public static final DecimalFormat DECIMAL_CELCIUS = new DecimalFormat("0.0 \u00B0C");
    public static final DecimalFormat DECIMAL_V = new DecimalFormat("#,##0.0 V");
    public static final DecimalFormat DECIMAL_MSOL = new DecimalFormat("#,##0.0 msol");
    public static final DecimalFormat DECIMAL2_MSOL = new DecimalFormat("#,##0.00 msol");
    
    // For solar irradiance
    public static final DecimalFormat DECIMAL_W_M2 = new DecimalFormat("#,##0.00 " 
    								+ Msg.getString("unit.wattpermetersquared")); //-NLS-1$
    // For air density
    public static final DecimalFormat DECIMAL_G_M3 = new DecimalFormat("#,##0.00 " 
    								+ Msg.getString("unit.grampercubicmeter")); //-NLS-1$
    // For air pressure
    public static final DecimalFormat DECIMAL_KPA = new DecimalFormat("#,##0.00 " 
    								+ Msg.getString("pressure.unit.kPa")); //-NLS-1$

    public static final String SYSTEM = "Default System";
    
    public static final String LIGHT = "Flat Light";
    public static final String LIGHT_RED = LIGHT + " - Red";
    public static final String LIGHT_GREEN = LIGHT + " - Green";
    public static final String LIGHT_BLUE = LIGHT + " - Blue";
    public static final String LIGHT_ORANGE = LIGHT + " - Orange";
    public static final String LIGHT_OWL = "Light Owl";
    public static final String SOLARIZED_LIGHT = "Solarized Light";
    
    public static final String DARK = "Flat Dark";
    public static final String HIBERBEE_DARK = "Hiberbee Dark";
    public static final String NIGHT_OWL = "Night Owl";
    public static final String MONOKAI_DARK = "Monokai Dark";
    public static final String SOLARIZED_DARK = "Solarized Dark";
    
    private static final String [] LAF_LIGHT_STYLES = {
    		SYSTEM, LIGHT_RED, LIGHT_GREEN, LIGHT_BLUE, LIGHT_ORANGE, LIGHT_OWL, SOLARIZED_LIGHT};
    
    private static final String [] LAF_DARK_STYLES = {
    		DARK, HIBERBEE_DARK, MONOKAI_DARK, NIGHT_OWL, SOLARIZED_DARK};
    
 // Create a new static string array that combines the elements of array1 and array2
    public static String[] LAF_STYLES = new String[LAF_LIGHT_STYLES.length + LAF_DARK_STYLES.length];
    
    // Initialize the combined array
    static {
        System.arraycopy(LAF_LIGHT_STYLES, 0, LAF_STYLES, 0, LAF_LIGHT_STYLES.length);
        System.arraycopy(LAF_DARK_STYLES, 0, LAF_STYLES, LAF_LIGHT_STYLES.length, LAF_DARK_STYLES.length);
    }
    
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
    private static Font smallFont;
    private static Font smallLabelFont;

    private static Map<String,Properties> styles = new HashMap<>();

    public static boolean isLightTheme;
    
    // Creates the built-in defaults.
    static {
        // Default Font
        Properties defaultProps = new Properties();
        defaultProps.setProperty(FONT_FAMILY, "Segoe UI");
        defaultProps.setProperty(FONT_SIZE, "12");
        defaultProps.setProperty(FONT_STYLE, "PLAIN");
        styles.put(DEFAULT_FONT_STYLE, defaultProps);

        // Label font for name value pairs and borders is the label but bold
        Properties labelProps = new Properties();
        labelProps.setProperty(FONT_STYLE, "BOLD");
        styles.put(LABEL_FONT_STYLE, labelProps);

        // Heading font of main section is bold and size +6
        Properties headingProps = new Properties();
        headingProps.setProperty(FONT_STYLE, "BOLD");
        headingProps.setProperty(FONT_SIZE, "+6");
        styles.put(HEADING_FONT_STYLE, headingProps);

        // Sub heading font used inside panels is BOLD & size +4
        Properties subHeadingProps = new Properties();
        subHeadingProps.setProperty(FONT_STYLE, "BOLD");
        subHeadingProps.setProperty(FONT_SIZE, "+3");
        styles.put(SUBHEADING_FONT_STYLE, subHeadingProps);

        // Default is the LAF with a Red accent colour
        Properties lafProps = new Properties();
        lafProps.setProperty(LAF_NAME, LIGHT_RED);
        styles.put(LAF_STYLE, lafProps);
    }

    private StyleManager() {
        // Stop instantiation
    }
    
    /**
     * Gets available LAF.
     */
    public static String[] getAvailableLAF() {
        return LAF_STYLES;
    }
    
    /**
     * Gets available light color LAF.
     */
    public static String[] getAvailableLightLAF() {
        return LAF_LIGHT_STYLES;
    }
    
    /**
     * Gets available dark color LAF.
     */
    public static String[] getAvailableDarkLAF() {
        return LAF_DARK_STYLES;
    }
    
    /**
     * Gets available system color LAF.
     */
    public static String[] getAvailableSystemLAF() {
        return new String [] {SYSTEM};
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

        String lafClass = null;
        try {
            switch(lafName) {
                case LIGHT: 
                	isLightTheme = true;
                    lafClass = FlatLightLaf.class.getName();
                    break;
	
                case SOLARIZED_LIGHT:
                	isLightTheme = true;
                	lafClass = FlatMTSolarizedLightIJTheme.class.getName();
                	FlatMTSolarizedLightIJTheme.setup();
                    break;
                    
                case LIGHT_OWL:
                	isLightTheme = true;
	                lafClass = FlatMTLightOwlIJTheme.class.getName();
	                FlatMTLightOwlIJTheme.setup();
	                break;
                
                case DARK:
                	isLightTheme = false;
                    lafClass = FlatDarkLaf.class.getName();
                    break;
                    
                case HIBERBEE_DARK:
                	isLightTheme = false;
                    lafClass = FlatHiberbeeDarkIJTheme.class.getName();
                    FlatHiberbeeDarkIJTheme.setup();
                    break;

                case MONOKAI_DARK:
                	isLightTheme = false;
	                lafClass = FlatMTMonokaiProIJTheme.class.getName();
	                FlatMTMonokaiProIJTheme.setup();
	                break;
	                
                case NIGHT_OWL:
                	isLightTheme = false;
	                lafClass = FlatMTNightOwlIJTheme.class.getName();
	                FlatMTNightOwlIJTheme.setup();
	                break;
	                
                case SOLARIZED_DARK:
                	isLightTheme = false;
                	lafClass = FlatMTSolarizedDarkIJTheme.class.getName();
                	FlatMTSolarizedDarkIJTheme.setup();
                    break;
                    
                case SYSTEM:
                	isLightTheme = true;
                    lafClass = UIManager.getSystemLookAndFeelClassName();
                    accentColor = null;   // No accent colouring for system
                    break;
                
                default:
                    logger.warning( "Don't know LAF style " + style);
            }

            if (lafClass != null) {
                // Preamble settingfs
                applyAccentColor(accentColor);
                calculateFonts();

                // Apply LAF but clear any previously installed customised settings
                UIManager.getLookAndFeelDefaults().put("Table.alternateRowColor", null);
                UIManager.setLookAndFeel(lafClass);
                styles.get(LAF_STYLE).setProperty(LAF_NAME, style);

                // Adjust colors on JTable
                UIDefaults defaults = UIManager.getLookAndFeelDefaults();
                Color selBackground = (Color) defaults.get("Table.selectionBackground");
                if (defaults.get("Table.alternateRowColor") == null) {
                    Color tabBackground = (Color) defaults.get("Table.background");

                    defaults.put("Table.alternateRowColor", getTableAlternativeColor(selBackground, tabBackground));
                }

                // Table Header is a shade off from the inactive select colour
                if (accentColor != null) {
                    HSLColor baseColor = new HSLColor(selBackground);
                    Color tableHeader = baseColor.adjustShade(20F);
                    defaults.put("TableHeader.background", new ColorUIResource(tableHeader));
                    defaults.put("TableHeader.foreground", new ColorUIResource(Color.WHITE));
                    // Make sure sort icon is noticable
                    defaults.put("Table.sortIconColor", defaults.get("TableHeader.foreground"));
                }
                // Always use a grid on Tables
                defaults.put("Table.showHorizontalLines", true);
                defaults.put("Table.showVerticalLines", true);
            }
        }
        catch (Exception e) {
            // Many things can go wrong so catch all
            logger.log(Level.SEVERE, "Problem setting LAF", e);
            lafClass = null;
        } 
        return (lafClass != null);
	}

    /**
     * Gets the styles used.
     * 
     * @return
     */
    public static Map<String, Properties> getStyles() {
        return styles;
    }

    /**
     * Loads the styles definitions to use in this UI.
     * 
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
     * Calculates the various fonts used in the styling.
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

        // Smaller font is not user-configurable
        Properties smallProps = new Properties();
        smallProps.setProperty(FONT_SIZE, "-2");
        smallFont = createFont(defaultFont, smallProps);
        smallLabelFont = createFont(labelFont, smallProps);

        // Hardcode the default font
        UIManager.put(UIMANAGER_FONT, defaultFont);
    }

    /**
     * Creates a new Font by deriving it from a base font.
     * 
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
     * Copied approach from ZTable.
     * 
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
     * Sets the accent colour.
     * 
     * @param newColour
     */
    private static void applyAccentColor(Color newColour) {
        // Set Accent color. Code taken from the description of the setSyetmColorGetter method
        FlatLaf.setSystemColorGetter( name -> {
            return name.equals( "accent" ) ? newColour : null;
        } );
    }

    /**
     * Looks up a color by name. Only supports the static entries in the Color class.
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
     * Applies styling to make this a SubHeading; normally applied to a JLabel.
     */
    public static void applyHeading(JComponent item) {
        item.setFont(headingFont);
    }

    /**
     * Applies styling to make this a SubHeading; normally applied to a JLabel.
     */
    public static void applySubHeading(JComponent item) {
        item.setFont(subHeadingFont);
    }

    /**
     * Creates a Titled border that uses the Label font.
     * 
     * @param title
     * @return
     */
    public static Border createLabelBorder(String title) {
        return BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
                                                        TitledBorder.DEFAULT_POSITION,
                                                        labelFont, (Color)null);
    }

    /**
     * Gets the Bold default font.
     * 
     * @return
     */
    public static Font getLabelFont() {
        return labelFont;
    }

    /**
     * Gets a smaller version of the main default font.
     */
    public static Font getSmallFont() {
        return smallFont;
    }

    /**
     * Gets a small label font.
     * @return
     */
    public static Font getSmallLabelFont() {
        return smallLabelFont;
    }

    /**
     * Creates a standardized empty border.
     */
    public static Border newEmptyBorder() {
    	return new EmptyBorder(1, 1, 1, 1);
    }

    /**
     * 
     * @param title
     * @param content
     * @return
     */
    public static JScrollPane createScrollBorder(String title, Component content) {
		JScrollPane listScroller = new JScrollPane(content);
		listScroller.setBorder(StyleManager.createLabelBorder(title));

        return listScroller;
    }
}
