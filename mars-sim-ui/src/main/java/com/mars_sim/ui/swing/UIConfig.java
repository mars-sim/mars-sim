/*
 * Mars Simulation Project
 * UIConfig.java
 * @date 2025-07-30
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.configuration.ConfigHelper;

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
		 Properties props) {}

	/** default logger. */
	private static final Logger logger = Logger.getLogger(UIConfig.class.getName());

	/** Internal window types. */
	public static final String TOOL = "tool";
	public static final String UNIT = "unit";
	public static final String AUDIO_PROPS = "audio";

	private static final String FILE_NAME = "ui_settings.xml";
	
	// Copied from javax.xml.XMLConstants to get around the problem with 2 implementations of 
	// javax.xml classes. The problem JAR is batik-transformer that includes xml-apis
	// see https://www.eclipse.org/forums/index.php/t/1110036/
	private static final String ACCESS_EXTERNAL_DTD = "http://javax.xml.XMLConstants/property/accessExternalDTD";
	private static final String ACCESS_EXTERNAL_SCHEMA = "http://javax.xml.XMLConstants/property/accessExternalSchema";
			
	// UI config elements and attributes.
	private static final String UI = "ui";
	private static final String USE_DEFAULT = "use-default";
	private static final String USE_DOCKING = "use-docking";

	private static final String MAIN_WINDOW = "main-window";
	private static final String LOCATION_X = "location-x";
	private static final String LOCATION_Y = "location-y";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
		
	private static final String INTERNAL_WINDOWS = "internal-windows";
	private static final String WINDOW = "window";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String VALUE = "value";
	private static final String Z_ORDER = "z-order";
	private static final String PROP_SETS = "prop-sets";
	private static final String PROP_SET = "prop-set";

	private Map<String,WindowSpec> loadedSpecs = new HashMap<>();
	private Map<String,Properties> propSets = new HashMap<>();

	private Point mainWindowPosn = new Point(0,0);
	private Dimension mainWindowSize = new Dimension(1024, 720);
	
	private boolean useDefault;

	private boolean useDockingUI = false;

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
				
				// Global props
				useDefault = parseBoolean(root, USE_DEFAULT);
				useDockingUI = parseBoolean(root, USE_DOCKING);

				// Parse Internal Window
				Element internalWindows = root.getChild(INTERNAL_WINDOWS);
				List<Element> internalWindowNodes = internalWindows.getChildren();
				for (Element internalWindow : internalWindowNodes) {
					WindowSpec spec = parseWindowSpec(internalWindow);
					loadedSpecs.put(spec.name(), spec);
				}

				// Parse props sets
				List<Element> propsElement = root.getChild(PROP_SETS).getChildren();
				for (Element propElement : propsElement) {
					String name = propElement.getAttributeValue(NAME);
					propSets.put(name, parseProperties(propElement));
				}
		    }
		    catch (Exception e) {
				logger.log(Level.SEVERE, "Cannot parse {0} : {1}", new Object[] {FILE_NAME, e.getMessage()});
		    }
		}
	}

	private WindowSpec parseWindowSpec(Element internalWindow) {
		String name = internalWindow.getAttributeValue(NAME);
		String type = internalWindow.getAttributeValue(TYPE);
		Point position = parsePosition(internalWindow);
		Dimension size = parseSize(internalWindow);
		int zOrder = Integer.parseInt(internalWindow.getAttributeValue(Z_ORDER));

		Element propElement = internalWindow.getChild(PROP_SET);
		Properties props;
		if (propElement != null) {
			props = parseProperties(propElement);
		}
		else {
			props = new Properties();
		}
		return new WindowSpec(name, position, size, zOrder, type, props);
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
		int width = ConfigHelper.getOptionalAttributeInt(window, WIDTH, -1);
		int height = ConfigHelper.getOptionalAttributeInt(window, HEIGHT, -1);

		if (width >= 0 && height >= 0) {
			return new Dimension(width, height);
		}
		return null;
	}

	private static Point parsePosition(Element window) {
		int locationX = ConfigHelper.getOptionalAttributeInt(window, LOCATION_X, -1);
		int locationY = ConfigHelper.getOptionalAttributeInt(window, LOCATION_Y, -1);

		if (locationX >= 0 && locationY >= 0) {
			return new Point(locationX, locationY);
		}
		return null;
	}
	
	private static boolean parseBoolean(Element item, String attrName) {
		return Boolean.parseBoolean(item.getAttributeValue(attrName));
	}

	/**
	 * Creates an XML document for the UI configuration and saves it to a file.
	 *
	 * @param mainWindow the main window.
	 */
	public void saveFile(ContentManager mainWindow) {
		
		File configFile = new File(SimulationRuntime.getSaveDir(), FILE_NAME);

		// Create save directory if it doesn't exist.
		configFile.getParentFile().mkdirs();
		try {
			Files.deleteIfExists(configFile.toPath());
		} catch (IOException e) {
			logger.config("Can't delete ui_settings.xml: " + e.getMessage());
		}

		Document outputDoc = new Document();

		Element uiElement = new Element(UI);
		outputDoc.setRootElement(uiElement);

		uiElement.setAttribute(USE_DEFAULT, "false");
		uiElement.setAttribute(USE_DOCKING, Boolean.toString(useDockingUI));

		outputTopLevelWindow(uiElement, MAIN_WINDOW, mainWindow.getTopFrame());
		
		Element internalWindowsElement = new Element(INTERNAL_WINDOWS);
		uiElement.addContent(internalWindowsElement);

		// Add all internal windows.
		for (var window1 : mainWindow.getContentSpecs()) {
			internalWindowsElement.addContent(outputWindowSpec(WINDOW, window1));
		}

		// Output the extra properties
		Map<String, Properties> extraProps = new HashMap<>();
		extraProps.putAll(StyleManager.getStyles());
		extraProps.putAll(mainWindow.getUIProps());

		Element propsElement = new Element(PROP_SETS);
		uiElement.addContent(propsElement);
		for (Entry<String,Properties> entry : extraProps.entrySet()) {
			outputProperties(propsElement, entry.getKey(), entry.getValue());
		}
	
		// Output the audio properties
		var audio = mainWindow.getAudio();
		if (audio != null) {
			outputProperties(propsElement, UIConfig.AUDIO_PROPS, audio.getUIProps());
		}

		saveDocumentToXMLFile(outputDoc, new File(SimulationRuntime.getSaveDir(), FILE_NAME));
	}

	private Element outputWindowSpec(String elemName, WindowSpec window1) {
		Element windowElement = new Element(elemName);
		
		var posn = window1.position();
		if (posn != null) {
			windowElement.setAttribute(LOCATION_X, Integer.toString(posn.x));
			windowElement.setAttribute(LOCATION_Y, Integer.toString(posn.y));
		}

		var size = window1.size();
		if (size != null) {
			windowElement.setAttribute(WIDTH, Integer.toString(size.width));
			windowElement.setAttribute(HEIGHT, Integer.toString(size.height));
		}
		windowElement.setAttribute(Z_ORDER, Integer.toString(window1.order()));
		outputProperties(windowElement, "props", window1.props());

		windowElement.setAttribute(NAME, window1.name());
		windowElement.setAttribute(TYPE, window1.type());

		return windowElement;
	}

	/**
	 * Write a Document to a XML file on disk.
	 * This should be shared code for all XML saving.
	 *  
	 * @param outputDoc
	 * @param targetFile
	 */
	private static void saveDocumentToXMLFile(Document outputDoc, File targetFile) {
		try (OutputStream stream = new FileOutputStream(targetFile)) {

			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());

			OutputStreamWriter writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);

			fmt.output(outputDoc, writer);

		    logger.config("Current window settings saved.");

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	private void outputTopLevelWindow(Element uiElement, String winName, JFrame realWindow) {
		Element mainWindowElement = new Element(winName);
		uiElement.addContent(mainWindowElement);
		
		outputWindowCoords(mainWindowElement, realWindow.getLocation(), realWindow.getSize());
	}

	private void outputWindowCoords(Element windowElement, Point position, Dimension size) {			
		windowElement.setAttribute(LOCATION_X, Integer.toString(position.x));
		windowElement.setAttribute(LOCATION_Y, Integer.toString(position.y));
		windowElement.setAttribute(WIDTH, Integer.toString(size.width));
		windowElement.setAttribute(HEIGHT, Integer.toString(size.height));
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
	 * Checks if the UI should use the docking framework.
	 * @return true if the docking framework should be used.
	 */
	public boolean useDockingUI() {
		return useDockingUI;
	}

	/**
	 * Sets whether the UI should use the docking framework.
	 * @param useDocking New value for using the docking framework.
	 */
    public void setUseDockingUI(boolean useDocking) {
		this.useDockingUI = useDocking;
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
	 * Gets any saved properties of the internal window on the desktop.
	 *
	 * @param windowName the window name.
	 * @return properties maybe null.
	 */
	public Properties getInternalWindowProps(String windowName) {
		WindowSpec spec = loadedSpecs.get(windowName);
		if (spec != null) {
			return spec.props;
		}
		return new Properties();
	}

	/**
	 * Gets the details of a previously stored window.
	 *
	 * @param windowName the window name.
	 * @return Known details; may return null
	 */
	public WindowSpec getInternalWindowDetails(String windowName) {
		return loadedSpecs.get(windowName);
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
		return loadedSpecs.entrySet().stream().sorted((f1, f2) -> Integer.compare(f2.getValue().order, f1.getValue().order))
										  .map(Entry::getValue)
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

	/**
	 * Add a window spec to the loaded specs. This is used by tools to add their window details to the config for saving.
	 * @param windowSpec Spec to load.
	 */
	public void addWindowSpec(WindowSpec windowSpec) {
		loadedSpecs.put(windowSpec.name(), windowSpec);
	}
}
