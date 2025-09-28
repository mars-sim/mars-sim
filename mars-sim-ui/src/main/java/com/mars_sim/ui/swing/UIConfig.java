/*
 * Mars Simulation Project
 * UIConfig.java
 * @date 2025-07-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;


import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.ui.swing.terminal.MarsTerminal;
import com.mars_sim.ui.swing.tool_window.ToolWindow;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
/**
 * Static class for saving/loading user interface configuration data.
 */
public class UIConfig {

	/**
	 * The stored details of a window
	 */
	public record WindowSpec(
		 String name,
		 Point position,
		 Dimension size,
		 int order,
		 String type,
		 Properties props) {};

	/** default logger. */
	private static final Logger logger = Logger.getLogger(UIConfig.class.getName());

	/** Internal window types. */
	public static final String TOOL = "tool";
	public static final String UNIT = "unit";
	private static final String FILE_NAME = "ui_settings.xml";
	
	// Copied from javax.xml.XMLConstants to get around the problem with 2 implementations of 
	// javax.xml classes. The problem JAR is batik-transformer that includes xml-apis
	// see https://www.eclipse.org/forums/index.php/t/1110036/
	private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
	private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
			
	// UI config elements and attributes.
	private static final String UI = "ui";
	private static final String USE_DEFAULT = "use-default";
	
	private static final String MAIN_WINDOW = "main-window";
	private static final String LOCATION_X = "location-x";
	private static final String LOCATION_Y = "location-y";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	
	private static final String MARS_TERMINAL = "mars-terminal";
	
	private static final String INTERNAL_WINDOWS = "internal-windows";
	private static final String WINDOW = "window";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String DISPLAY = "display";
	private static final String Z_ORDER = "z-order";
	private static final String PROP_SETS = "prop-sets";
	private static final String PROP_SET = "prop-set";

	private Map<String,WindowSpec> windows = new HashMap<>();
	private Map<String,Properties> propSets = new HashMap<>();

	private Point mainWindowPosn = new Point(0,0);
	private Dimension mainWindowSize = new Dimension(1024, 720);

	private Point marsTerminalPosn = new Point(0,0);
	private Dimension marsTerminalSize = new Dimension(1024, 720);
	
	private boolean useDefault;

	/**
	 * Private singleton constructor.
	 */
	public UIConfig() {
	}

	/**
	 * Loads and parses the XML save file.
	 */
	public void parseFile() {
		File configFile = new File(SimulationRuntime.getSaveDir(), FILE_NAME);
		if (configFile.exists()) {

		    SAXBuilder builder = new SAXBuilder();
		    builder.setProperty(ACCESS_EXTERNAL_DTD, "");
		    builder.setProperty(ACCESS_EXTERNAL_SCHEMA, "");
		    try  {
		    	Document configDoc = builder.build(new File(SimulationRuntime.getSaveDir(), FILE_NAME));
		    	Element root = configDoc.getRootElement();

				// Main properties
				Element mainWindow = root.getChild(MAIN_WINDOW);
				mainWindowSize = parseSize(mainWindow);
				mainWindowPosn = parsePosition(mainWindow);

				Element terminalWindow = root.getChild(MARS_TERMINAL);
				marsTerminalSize = parseSize(terminalWindow);
				marsTerminalPosn = parsePosition(terminalWindow);
				
				// Global props
				useDefault = parseBoolean(root, USE_DEFAULT);

				// Parse Internal Window
				Element internalWindows = root.getChild(INTERNAL_WINDOWS);
				List<Element> internalWindowNodes = internalWindows.getChildren();
				for (Element internalWindow : internalWindowNodes) {
					String name = internalWindow.getAttributeValue(NAME);
					String type = internalWindow.getAttributeValue(TYPE);
					Point position = parsePosition(internalWindow);
					Dimension size = parseSize(internalWindow);
					int zOrder = Integer.parseInt(internalWindow.getAttributeValue(Z_ORDER));

					Element propElement = internalWindow.getChild(PROP_SET);
					Properties props = null;
					if (propElement != null) {
						props = parseProperties(propElement);
					}

					windows.put(name, new WindowSpec(name, position, size, zOrder, type, props));
				}

				// Parse props sets
				List<Element> propsElement = root.getChild(PROP_SETS).getChildren();
				for (Element propElement : propsElement) {
					String name = propElement.getAttributeValue(NAME);
					propSets.put(name, parseProperties(propElement));
				}
		    }
		    catch (Exception e) {
				logger.log(Level.SEVERE, "Cannot parse " + FILE_NAME + " : " + e.getMessage());
		    }
		}
	}

	private static Properties parseProperties(Element propElement) {
		Properties props = new Properties();
		for (Element valueElement : propElement.getChildren()) {
			String name = valueElement.getAttributeValue(NAME);
			String value = valueElement.getAttributeValue(VALUE);
			props.setProperty(name, value);
		}

		return props;
	}

	private static Dimension parseSize(Element window) {
		int width = Integer.parseInt(window.getAttributeValue(WIDTH));
		int height = Integer.parseInt(window.getAttributeValue(HEIGHT));

		return new Dimension(width, height);
	}

	private static Point parsePosition(Element window) {
		int locationX = Integer.parseInt(window.getAttributeValue(LOCATION_X));
		int locationY = Integer.parseInt(window.getAttributeValue(LOCATION_Y));
		return new Point(locationX, locationY);
	}
	
	private static boolean parseBoolean(Element item, String attrName) {
		return Boolean.parseBoolean(item.getAttributeValue(attrName));
	}

	/**
	 * Creates an XML document for the UI configuration and saves it to a file.
	 *
	 * @param mainWindow the main window.
	 */
	public void saveFile(MainWindow mainWindow) {
		MainDesktopPane desktop = mainWindow.getDesktop();

		MarsTerminal marsTerminal = mainWindow.getMarsTerminal();
		
		File configFile = new File(SimulationRuntime.getSaveDir(), FILE_NAME);

		// Create save directory if it doesn't exist.
		if (!configFile.getParentFile().exists()) {
			configFile.getParentFile().mkdirs();
			logger.config(SimulationRuntime.getSaveDir() + " created successfully");
		}

		else {

			try {
				if (Files.deleteIfExists(configFile.toPath())) {
				    logger.config("Previous ui_settings.xml deleted.");
				}
				else {
					logger.config("Can't delete ui_settings.xml since it's not found.");
				}
			} catch (IOException e) {
				logger.config("Can't delete ui_settings.xml: " + e.getMessage());
			}

		}

		Document outputDoc = new Document();

		Element uiElement = new Element(UI);
		outputDoc.setRootElement(uiElement);

		uiElement.setAttribute(USE_DEFAULT, "false");

		Element mainWindowElement = new Element(MAIN_WINDOW);
		uiElement.addContent(mainWindowElement);

		Element marsTerminalElement = new Element(MARS_TERMINAL);
		uiElement.addContent(marsTerminalElement);
		
		JFrame realWindow = mainWindow.getFrame();
		outputWindowCoords(mainWindowElement, realWindow);

		JFrame realTerminal = marsTerminal.getFrame();
		outputWindowCoords(marsTerminalElement, realTerminal);
		
		Element internalWindowsElement = new Element(INTERNAL_WINDOWS);
		uiElement.addContent(internalWindowsElement);

		// Add all internal windows.
		JInternalFrame[] windows = desktop.getAllFrames();
		for (JInternalFrame window1 : windows) {
			if (window1.isVisible() || window1.isIcon()) {
				Element windowElement = new Element(WINDOW);
				internalWindowsElement.addContent(windowElement);

				outputWindowCoords(windowElement, window1);
				windowElement.setAttribute(Z_ORDER, Integer.toString(desktop.getComponentZOrder(window1)));
				windowElement.setAttribute(DISPLAY, Boolean.toString(!window1.isIcon()));

				if (window1 instanceof ConfigurableWindow cw) {
					outputProperties(windowElement, "props", cw.getUIProps());
				}

				if (window1 instanceof ToolWindow tw) {
					windowElement.setAttribute(TYPE, TOOL);
					windowElement.setAttribute(NAME, tw.getToolName());
				} else if (window1 instanceof UnitWindow uw) {
					windowElement.setAttribute(TYPE, UNIT);
					windowElement.setAttribute(NAME, uw.getUnit().getName());
				} else {
					windowElement.setAttribute(TYPE, "other");
					windowElement.setAttribute(NAME, "other");
				}
			}
		}

		// Output the extra properties
		Element propsElement = new Element(PROP_SETS);
		uiElement.addContent(propsElement);
		for (Entry<String,Properties> entry : mainWindow.getUIProps().entrySet()) {
			outputProperties(propsElement, entry.getKey(), entry.getValue());
		}

		// Load the DTD scheme from the ui_settings.dtd file
		try (
			OutputStream out = new FileOutputStream(new File(SimulationRuntime.getSaveDir(), FILE_NAME));
			OutputStream stream = new FileOutputStream(configFile)) {

			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());

			OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

			fmt.output(outputDoc, writer);

		    logger.config("Current window settings saved.");

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}


	private void outputWindowCoords(Element windowElement, Component realWindow) {			
		windowElement.setAttribute(LOCATION_X, Integer.toString(realWindow.getX()));
		windowElement.setAttribute(LOCATION_Y, Integer.toString(realWindow.getY()));
		windowElement.setAttribute(WIDTH, Integer.toString(realWindow.getWidth()));
		windowElement.setAttribute(HEIGHT, Integer.toString(realWindow.getHeight()));
	}
	
	private void outputProperties(Element parent, String name, Properties values) {
		Element propParent = new Element(PROP_SET);
		parent.addContent(propParent);
		if (name != null) {
			propParent.setAttribute(new Attribute(NAME, name));
		}
		for(Object key : values.keySet()) {
			Element valueElement = new Element(VALUE);
			valueElement.setAttribute(new Attribute(NAME, (String) key));
			valueElement.setAttribute(new Attribute(VALUE, values.getProperty((String) key)));
			propParent.addContent(valueElement);
		}
	}

	/**
	 * Checks if UI should use default configuration.
	 *
	 * @return true if default.
	 */
	public boolean useUIDefault() {
		return useDefault;
	}

	/**
	 * Gets the screen location of the main window origin.
	 *
	 * @return location.
	 */
	public Point getMainWindowLocation() {
		return mainWindowPosn;
	}

	/**
	 * Gets the size of the main window.
	 *
	 * @return size.
	 */
	public Dimension getMainWindowDimension() {
		return mainWindowSize;
	}

	/**
	 * Gets the screen location of the Mars Terminal origin.
	 *
	 * @return location.
	 */
	public Point getMarsTerminalLocation() {
		return marsTerminalPosn;
	}

	/**
	 * Gets the size of the Mars Terminal.
	 *
	 * @return size.
	 */
	public Dimension getMarsTerminalDimension() {
		return marsTerminalSize;
	}
	
	/**
	 * Gets any saved properties of the internal window on the desktop.
	 *
	 * @param windowName the window name.
	 * @return properties maybe null.
	 */
	public Properties getInternalWindowProps(String windowName) {
		WindowSpec spec = windows.get(windowName);
		if (spec != null) {
			return spec.props;
		}
		return null;
	}

	/**
	 * Gets the details of a previously stored window.
	 *
	 * @param windowName the window name.
	 * @return Known details; may return null
	 */
	public WindowSpec getInternalWindowDetails(String windowName) {
		return windows.get(windowName);
	}

	/**
	 * Gets the property sets defined in the config.
	 * 
	 * @return
	 */
	public Map<String, Properties> getPropSets() {
		return propSets;
	}

	/**
	 * Gets the property set for a particular name.
	 * If a match is not found an empty property set is returned.
	 * 
	 * @return
	 */
	public Properties getPropSet(String name) {
		return propSets.getOrDefault(name, new Properties());
	}


	/**
	 * Gets the details of the stored windows.
	 * 
	 * @return
	 */
	public List<WindowSpec> getConfiguredWindows() {
		return windows.entrySet().stream().sorted((f1, f2) -> Integer.compare(f2.getValue().order, f1.getValue().order))
										  .map(v -> v.getValue())
										  .toList();
	}

	/**
	 * Helper method to extract a Boolean out of a user properties.
	 * 
	 * @param setting
	 * @param name
	 * @param defaultValue
	 * @return
	 */
    public static boolean extractBoolean(Properties settings, String name, boolean defaultValue) {
    	boolean result = defaultValue;
    	if (settings != null && settings.containsKey(name)) {
    		result = Boolean.parseBoolean(settings.getProperty(name));
    	}
    	return result;
    }

	/**
	 * Helper method to extract a Double out of a user properties.
	 * 
	 * @param setting
	 * @param name
	 * @param defaultValue
	 * @return
	 */
    public static double extractDouble(Properties settings, String name, double defaultValue) {
		double result = defaultValue;
    	if (settings != null && settings.containsKey(name)) {
			try {
    			result = Double.parseDouble(settings.getProperty(name));
			}
			catch(NumberFormatException nfe) {
				logger.warning("Cannot parse double property of " + name + ", value=" + settings.getProperty(name));
			}
    	}
    	return result;
    }
}
