/**
 * Mars Simulation Project
 * UIConfig.java
 * @version 2.82 2007-11-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.ui.standard.sound.AudioPlayer;
import org.mars_sim.msp.ui.standard.tool.ToolWindow;
import org.mars_sim.msp.ui.standard.unit_window.UnitWindow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public class UIConfig {
    
    	private static String CLASS_NAME = 
	    "org.mars_sim.msp.ui.standard.UIConfig";
	
    	private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Singleton instance.
	public static final UIConfig INSTANCE = new UIConfig();
	
	// Internal window types.
	public static final String TOOL = "tool";
	public static final String UNIT = "unit";
	
	// Config filename.
	private static final String DIRECTORY = "saved";
	private static final String FILE_NAME = "ui_settings.xml";
	
	// UI config elements and attributes.
	private static final String UI = "ui";
	private static final String USE_DEFAULT = "use-default";
	private static final String LOOK_AND_FEEL = "look-and-feel";
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
	
	private Document configDoc;
	
	// Private singleton constructor.
	private UIConfig() {}
	
	/**
	 * Loads and parses the XML save file.
	 */
	public void parseFile() {
		InputStream stream = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			String path = DIRECTORY + File.separator + FILE_NAME;
			stream = getClass().getClassLoader().getResourceAsStream(path);
			configDoc = builder.parse(stream);
		}
		catch (Exception e) {
			logger.log(Level.SEVERE,"parseFile()",e);
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (Exception e) {};
			}
		}
	}
	
	/**
	 * Creates an XML document for the UI configuration and saves it to a file.
	 * @param window the main window.
	 */
	public void saveFile(MainWindow window) {
		OutputStream stream = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document outputDoc = builder.newDocument();
			
			Element uiElement = outputDoc.createElement(UI);
			outputDoc.appendChild(uiElement);
			
			uiElement.setAttribute(USE_DEFAULT, "false");
			
			if (UIManager.getLookAndFeel() instanceof MetalLookAndFeel) 
				uiElement.setAttribute(LOOK_AND_FEEL, "default");
			else uiElement.setAttribute(LOOK_AND_FEEL, "native");
			
			Element mainWindowElement = outputDoc.createElement(MAIN_WINDOW);
			uiElement.appendChild(mainWindowElement);
			
			mainWindowElement.setAttribute(LOCATION_X, Integer.toString(window.getX()));
			mainWindowElement.setAttribute(LOCATION_Y, Integer.toString(window.getY()));
			mainWindowElement.setAttribute(WIDTH, Integer.toString(window.getWidth()));
			mainWindowElement.setAttribute(HEIGHT, Integer.toString(window.getHeight()));
			
			Element volumeElement = outputDoc.createElement(VOLUME);
			uiElement.appendChild(volumeElement);
			
			AudioPlayer player = window.getDesktop().getSoundPlayer();
			volumeElement.setAttribute(SOUND, Float.toString(player.getVolume()));
			volumeElement.setAttribute(MUTE, Boolean.toString(player.isMute()));
			
			Element internalWindowsElement = outputDoc.createElement(INTERNAL_WINDOWS);
			uiElement.appendChild(internalWindowsElement);
			
			// Add all internal windows.
			MainDesktopPane desktop = window.getDesktop();
			JInternalFrame[] windows = desktop.getAllFrames();
			for (int x = 0; x < windows.length; x++) {
				Element windowElement = outputDoc.createElement(WINDOW);
				internalWindowsElement.appendChild(windowElement);
				
				windowElement.setAttribute(Z_ORDER, Integer.toString(desktop.getComponentZOrder(windows[x])));
				windowElement.setAttribute(LOCATION_X, Integer.toString(windows[x].getX()));
				windowElement.setAttribute(LOCATION_Y, Integer.toString(windows[x].getY()));
				windowElement.setAttribute(WIDTH, Integer.toString(windows[x].getWidth()));
				windowElement.setAttribute(HEIGHT, Integer.toString(windows[x].getHeight()));
				windowElement.setAttribute(DISPLAY, Boolean.toString(!windows[x].isClosed()));
				
				if (windows[x] instanceof ToolWindow) {
					windowElement.setAttribute(TYPE, TOOL);
					windowElement.setAttribute(NAME, ((ToolWindow) windows[x]).getToolName());
				}
				else if (windows[x] instanceof UnitWindow) {
					windowElement.setAttribute(TYPE, UNIT);
					windowElement.setAttribute(NAME, ((UnitWindow) windows[x]).getUnit().getName());
				}
			}
			
			// Check unit toolbar for unit buttons without open windows.
			Unit[] toolBarUnits = window.getUnitToolBar().getUnitsInToolBar();
			for (int x = 0; x < toolBarUnits.length; x++) {
				UnitWindow unitWindow = desktop.findUnitWindow(toolBarUnits[x]);
				
				if ((unitWindow == null) || unitWindow.isIcon()) {
					Element windowElement = outputDoc.createElement(WINDOW);
					internalWindowsElement.appendChild(windowElement);
		
					windowElement.setAttribute(TYPE, UNIT);
					windowElement.setAttribute(NAME, toolBarUnits[x].getName());
					windowElement.setAttribute(DISPLAY, "false");
				}
			}
			
			// Save to file.
			String path = DIRECTORY + File.separator + FILE_NAME;
			stream = new BufferedOutputStream(new FileOutputStream(path));
			XMLSerializer serializer = new XMLSerializer();
			serializer.setOutputByteStream(stream);
			serializer.serialize(outputDoc);
		}
		catch (Exception e) {
			logger.log(Level.SEVERE,e.getMessage());
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (Exception e) {};
			}
		}
	}
	
	/**
	 * Checks if UI should use default configuration.
	 * @return true if default.
	 */
	public boolean useUIDefault() {
		try {
			Element root = configDoc.getDocumentElement();
			return Boolean.parseBoolean(root.getAttribute(USE_DEFAULT));
		}
		catch (Exception e) {
			return true;
		}
	}
	
	/**
	 * Checks if UI should use native or default look & feel.
	 * @return true if native.
	 */
	public boolean useNativeLookAndFeel() {
		try {
			Element root = configDoc.getDocumentElement();
			String lookAndFeel = root.getAttribute(LOOK_AND_FEEL);
			if (lookAndFeel.equals("native")) return true;
			else return false;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Gets the screen location of the main window origin.
	 * @return location.
	 */
	public Point getMainWindowLocation() {
		try {
			Element root = configDoc.getDocumentElement();
			Element mainWindow = (Element) root.getElementsByTagName(MAIN_WINDOW).item(0);
			int x = Integer.parseInt(mainWindow.getAttribute(LOCATION_X));
			int y = Integer.parseInt(mainWindow.getAttribute(LOCATION_Y));
			return new Point(x, y);
		}
		catch (Exception e) {
			return new Point(0, 0);
		}
	}
	
	/**
	 * Gets the size of the main window.
	 * @return size.
	 */
	public Dimension getMainWindowDimension() {
		try {
			Element root = configDoc.getDocumentElement();
			Element mainWindow = (Element) root.getElementsByTagName(MAIN_WINDOW).item(0);
			int width = Integer.parseInt(mainWindow.getAttribute(WIDTH));
			int height = Integer.parseInt(mainWindow.getAttribute(HEIGHT));
			return new Dimension(width, height);
		}
		catch (Exception e) {
			return new Dimension(300, 300);
		}
	}
	
	/**
	 * Gets the sound volume level.
	 * @return volume (0 (silent) to 1 (loud)).
	 */
	public float getVolume() {
		try {
			Element root = configDoc.getDocumentElement();
			Element volume = (Element) root.getElementsByTagName(VOLUME).item(0);
			return Float.parseFloat(volume.getAttribute(SOUND));
		}
		catch (Exception e) {
			return 50F;
		}
	}
	
	/**
	 * Checks if sound volume is set to mute.
	 * @return true if mute.
	 */
	public boolean isMute() {
		try {
			Element root = configDoc.getDocumentElement();
			Element volume = (Element) root.getElementsByTagName(VOLUME).item(0);
			return Boolean.parseBoolean(volume.getAttribute(MUTE));
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Checks if an internal window is displayed.
	 * @param windowName the window name.
	 * @return true if displayed.
	 */
	public boolean isInternalWindowDisplayed(String windowName) {
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			boolean result = false;
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					String name = internalWindow.getAttribute(NAME);
					if (name.equals(windowName)) result = Boolean.parseBoolean(internalWindow.getAttribute(DISPLAY));
				}
			}
			return result;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Gets the origin location of an internal window on the desktop.
	 * @param windowName the window name.
	 * @return location.
	 */
	public Point getInternalWindowLocation(String windowName) {
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			Point result = new Point(0, 0);
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					String name = internalWindow.getAttribute(NAME);
					if (name.equals(windowName)) {
						int locationX = Integer.parseInt(internalWindow.getAttribute(LOCATION_X));
						int locationY = Integer.parseInt(internalWindow.getAttribute(LOCATION_Y));
						result.setLocation(locationX, locationY);
					}
				}
			}
			return result;
		}
		catch (Exception e) {
			return new Point(0, 0);
		}
	}

	/**
	 * Gets the z order of an internal window on the desktop.
	 * @param windowName the window name.
	 * @return z order (lower number represents higher up)
	 */
	public int getInternalWindowZOrder(String windowName) {
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			int result = -1;
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					String name = internalWindow.getAttribute(NAME);
					if (name.equals(windowName)) result = Integer.parseInt(internalWindow.getAttribute(Z_ORDER));
				}
			}
			return result;
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * Gets the size of an internal window.
	 * @param windowName the window name.
	 * @return size.
	 */
	public Dimension getInternalWindowDimension(String windowName) {
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			Dimension result = new Dimension(0, 0);
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					String name = internalWindow.getAttribute(NAME);
					if (name.equals(windowName)) {
						int width = Integer.parseInt(internalWindow.getAttribute(WIDTH));
						int height = Integer.parseInt(internalWindow.getAttribute(HEIGHT));
						result = new Dimension(width, height);
					}
				}
			}
			return result;
		}
		catch (Exception e) {
			return new Dimension(0, 0);
		}
	}
	
	/**
	 * Gets the internal window type.
	 * @param windowName the window name.
	 * @return "unit" or "tool".
	 */
	public String getInternalWindowType(String windowName) {
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			String result = "";
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					String name = internalWindow.getAttribute(NAME);
					if (name.equals(windowName)) result = internalWindow.getAttribute(TYPE);
				}
			}
			return result;
		}
		catch (Exception e) {
			return "";
		}
	}
	
	/**
	 * Checks if internal window is configured.
	 * @param windowName the window name.
	 * @return true if configured.
	 */
	public boolean isInternalWindowConfigured(String windowName) {
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			boolean result = false;
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					String name = internalWindow.getAttribute(NAME);
					if (name.equals(windowName)) result = true;
				}
			}
			return result;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Gets all of the internal window names.
	 * @return list of window names.
	 */
	public List<String> getInternalWindowNames() {
		List<String> result = new ArrayList<String>();
		try {
			Element root = configDoc.getDocumentElement();
			Element internalWindows = (Element) root.getElementsByTagName(INTERNAL_WINDOWS).item(0);
			NodeList internalWindowNodes = internalWindows.getChildNodes();
			for (int x = 0; x < internalWindowNodes.getLength(); x++) {
				if (internalWindowNodes.item(x) instanceof Element) {
					Element internalWindow = (Element) internalWindowNodes.item(x);
					result.add(internalWindow.getAttribute(NAME));
				}
			}
			return result;
		}
		catch (Exception e) {
			return result;
		}
	}
}