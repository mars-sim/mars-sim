/*
 * Mars Simulation Project
 * UIConfig.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

import com.google.common.base.Charsets;

/**
 * Static class for saving/loading user interface configuration data.
 */
public class UIConfig {

	private static class WindowSpec {
		Point position;
		Dimension size;
		String name;
		int order;

		public WindowSpec( String name, Point position, Dimension size, int order) {
			this.position = position;
			this.size = size;
			this.name = name;
			this.order = order;
		}
	}

	/** default logger. */
	private static final Logger logger = Logger.getLogger(UIConfig.class.getName());

	/** Internal window types. */
	public static final String TOOL = "tool";
	public static final String UNIT = "unit";
	private static final String FILE_NAME = "ui_settings.xml";

	// UI config elements and attributes.
	private static final String UI = "ui";
	private static final String USE_DEFAULT = "use-default";
	private static final String SHOW_UNIT_BAR = "show-unit-bar";
	private static final String SHOW_TOOL_BAR = "show-tool-bar";
	private static final String MAIN_WINDOW = "main-window";
	private static final String LOCATION_X = "location-x";
	private static final String LOCATION_Y = "location-y";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String VOLUME = "volume";
	private static final String SOUND = "sound";
	private static final String MUTE = "mute";
	private static final String INTERNAL_WINDOWS = "internal-windows";
	private static final String WINDOW = "window";
	private static final String TYPE = "type";
	private static final String NAME = "name";
	private static final String DISPLAY = "display";
	private static final String Z_ORDER = "z-order";
	private static final String LAF = "look-and-feel";

	private Map<String,WindowSpec> windows = new HashMap<>();

	private Point mainWindowPosn = new Point(0,0);
	private Dimension mainWindowSize = new Dimension(1024, 720);
	private String lookAndFeel = StyleManager.DEFAULT_LAF;

	private boolean useDefault;
	private boolean showToolBar = true;
	private boolean showUnitBar;

	private boolean mute;
	private double volume;


	/**
	 * Private singleton constructor.
	 */
	public UIConfig() {
	}

	/**
	 * Loads and parses the XML save file.
	 */
	public void parseFile() {
		File configFile = new File(SimulationFiles.getSaveDir(), FILE_NAME);
		if (configFile.exists()) {

		    SAXBuilder builder = new SAXBuilder();
		    // In order to get rid of the XMLConstants.ACCESS_EXTERNAL_DTD as well
		    // Need to switch to using internal DTD first
		    // Gets rid of ACCESS_EXTERNAL_SCHEMA
		    builder.setProperty(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
		    // Gets rid of ACCESS_EXTERNAL_SCHEMA
		    builder.setProperty(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		    try  {
		    	Document configDoc = builder.build(new File(SimulationFiles.getSaveDir(), FILE_NAME));
		    	Element root = configDoc.getRootElement();

				// Main proprerties
				Element mainWindow = root.getChild(MAIN_WINDOW);
				mainWindowSize = parseSize(mainWindow);
				mainWindowPosn = parsePosition(mainWindow);


				// Global props
				useDefault = parseBoolean(root, USE_DEFAULT);
				showToolBar = parseBoolean(root, SHOW_TOOL_BAR);
				showUnitBar = parseBoolean(root, SHOW_UNIT_BAR);
				lookAndFeel = root.getAttributeValue(LAF);

				// Audo controls
				Element volumeItem = root.getChild(VOLUME);
				if (volumeItem != null) {
					mute = parseBoolean(volumeItem, MUTE);
					volume = Double.parseDouble(volumeItem.getAttributeValue(SOUND));
				}

				// Parse Internal Window
				Element internalWindows = root.getChild(INTERNAL_WINDOWS);
				List<Element> internalWindowNodes = internalWindows.getChildren();
				for (Element internalWindow : internalWindowNodes) {
					String name = internalWindow.getAttributeValue(NAME);
					Point position = parsePosition(internalWindow);
					Dimension size = parseSize(internalWindow);
					int zOrder = Integer.parseInt(internalWindow.getAttributeValue(Z_ORDER));

					windows.put(name, new WindowSpec(name, position, size, zOrder));
				}
		    }
		    catch (Exception e) {
				logger.log(Level.SEVERE, "Cannot parse " + FILE_NAME + " : " + e.getMessage());
		    }
		}
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

		File configFile = new File(SimulationFiles.getSaveDir(), FILE_NAME);

		// Create save directory if it doesn't exist.
		if (!configFile.getParentFile().exists()) {
			configFile.getParentFile().mkdirs();
			logger.config(SimulationFiles.getSaveDir() + " created successfully");
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
		uiElement.setAttribute(LAF, StyleManager.getLAF());
		uiElement.setAttribute(SHOW_TOOL_BAR, Boolean.toString(mainWindow.getToolToolBar().isVisible()));
		uiElement.setAttribute(SHOW_UNIT_BAR, Boolean.toString(mainWindow.getUnitToolBar().isVisible()));

		Element mainWindowElement = new Element(MAIN_WINDOW);
		uiElement.addContent(mainWindowElement);

		JFrame realWindow = mainWindow.getFrame();
		mainWindowElement.setAttribute(LOCATION_X, Integer.toString(realWindow.getX()));
		mainWindowElement.setAttribute(LOCATION_Y, Integer.toString(realWindow.getY()));

		// Get the real display size
		mainWindowElement.setAttribute(WIDTH, Integer.toString(realWindow.getWidth()));
		mainWindowElement.setAttribute(HEIGHT, Integer.toString(realWindow.getHeight()));

		AudioPlayer player = desktop.getSoundPlayer();
		if (player != null) {
			Element volumeElement = new Element(VOLUME);
			uiElement.addContent(volumeElement);
			volumeElement.setAttribute(SOUND, Double.toString(player.getEffectVolume()));
			volumeElement.setAttribute(MUTE, Boolean.toString(AudioPlayer.isEffectMute()));
		}

		Element internalWindowsElement = new Element(INTERNAL_WINDOWS);
		uiElement.addContent(internalWindowsElement);

		// Add all internal windows.
		JInternalFrame[] windows = desktop.getAllFrames();
		for (JInternalFrame window1 : windows) {
			if (window1.isVisible() || window1.isIcon()) {
				Element windowElement = new Element(WINDOW);
				internalWindowsElement.addContent(windowElement);

				windowElement.setAttribute(Z_ORDER, Integer.toString(desktop.getComponentZOrder(window1)));
				windowElement.setAttribute(LOCATION_X, Integer.toString(window1.getX()));
				windowElement.setAttribute(LOCATION_Y, Integer.toString(window1.getY()));
				windowElement.setAttribute(WIDTH, Integer.toString(window1.getWidth()));
				windowElement.setAttribute(HEIGHT, Integer.toString(window1.getHeight()));
				windowElement.setAttribute(DISPLAY, Boolean.toString(!window1.isIcon()));

				if (window1 instanceof ToolWindow) {
					windowElement.setAttribute(TYPE, TOOL);
					windowElement.setAttribute(NAME, ((ToolWindow) window1).getToolName());
				} else if (window1 instanceof UnitWindow) {
					windowElement.setAttribute(TYPE, UNIT);
					windowElement.setAttribute(NAME, ((UnitWindow) window1).getUnit().getName());
				} else {
					windowElement.setAttribute(TYPE, "other");
					windowElement.setAttribute(NAME, "other");
				}
			}
		}

		// Load the DTD scheme from the ui_settings.dtd file
		try (

			OutputStream out = new FileOutputStream(new File(SimulationFiles.getSaveDir(), FILE_NAME));
			OutputStream stream = new FileOutputStream(configFile)) {

			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());

			OutputStreamWriter writer = new OutputStreamWriter(stream, Charsets.UTF_8);

			fmt.output(outputDoc, writer);

		    logger.config("Current window settings saved.");

		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
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
	 * Checks if UI should show the Tool bar.
	 *
	 * @return true if default.
	 */
	public boolean showToolBar() {
		return showToolBar;
	}

	/**
	 * Checks if UI should show the Unit bar.
	 *
	 * @return true if default.
	 */
	public boolean showUnitBar() {
		return showUnitBar;
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
	 * Gets the sound volume level.
	 *
	 * @return volume (0 (silent) to 1 (loud)).
	 */
	public double getVolume() {
		return volume;
	}

	/**
	 * Checks if sound volume is set to mute.
	 *
	 * @return true if mute.
	 */
	public boolean isMute() {
		return mute;
	}

	/**
	 * Gets the origin location of an internal window on the desktop.
	 *
	 * @param windowName the window name.
	 * @return location.
	 */
	public Point getInternalWindowLocation(String windowName) {
		WindowSpec spec = windows.get(windowName);
		Point result;
		if (spec != null) {
			result = spec.position;
		}
		else {
			result = new Point(0, 0);
		}
		return result;
	}

	/**
	 * Gets the z order of an internal window on the desktop.
	 *
	 * @param windowName the window name.
	 * @return z order (lower number represents higher up)
	 */
	public int getInternalWindowZOrder(String windowName) {
		WindowSpec spec = windows.get(windowName);
		int result;
		if (spec != null) {
			result = spec.order;
		}
		else {
			result = -1;
		}
		return result;
	}

	/**
	 * Gets the size of an internal window.
	 *
	 * @param windowName the window name.
	 * @return size.
	 */
	public Dimension getInternalWindowDimension(String windowName) {
		WindowSpec spec = windows.get(windowName);
		Dimension result;
		if (spec != null) {
			result = spec.size;
		}
		else {
			result = new Dimension(0, 0);;
		}
		return result;
	}

	/**
	 * Checks if internal window is configured.
	 *
	 * @param windowName the window name.
	 * @return true if configured.
	 */
	public boolean isInternalWindowConfigured(String windowName) {
		return (windows.get(windowName) != null);
	}

	/**
	 * Can configued LAF.
	 */
	public String getLAF() {
		return lookAndFeel;
	}
}
