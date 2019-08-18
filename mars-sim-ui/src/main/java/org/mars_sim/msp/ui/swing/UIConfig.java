/**
 * Mars Simulation Project
 * UIConfig.java
 * @version 3.1.0 2017-03-04
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;

import org.apache.commons.io.IOUtils;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

/**
 * Static class for saving/loading user interface configuration data.
 */
public class UIConfig {

	/** default logger. */
	private static Logger logger = Logger.getLogger(UIConfig.class.getName());

	/** Singleton instance. */
	public static final UIConfig INSTANCE = new UIConfig();

	/** Internal window types. */
	public static final String TOOL = "tool";

	public static final String UNIT = "unit";

	/** Config filename. */
//	private static final String DIRECTORY = System.getProperty("user.home") + File.separator + ".mars-sim"
//			+ File.separator + "saved";

	private static final String FILE_NAME = "ui_settings.xml";

	private static final String FILE_NAME_DTD = "ui_settings.dtd";

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

	private Document configDoc;

	private MainDesktopPane desktop;

	/**
	 * Private singleton constructor.
	 */
	private UIConfig() {

	}

	/**
	 * Loads and parses the XML save file.
	 */
//	public void parseFile() {
//		FileInputStream stream = null;
//
//		try {
//			
////			 [landrus, 27.11.09]: Hard paths are a pain with webstart, so we will use the
////			 users home dir, because this will work properly.
//			 
//			stream = new FileInputStream(new File(DIRECTORY, FILE_NAME));
//			
////			 bug 2909888: read the inputstream with a specific encoding instead of the
////			 system default.
//			 
//			InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
//			SAXBuilder saxBuilder = new SAXBuilder(true);
//			configDoc = saxBuilder.build(reader);
//		} catch (Exception e) {
//			if (!(e instanceof FileNotFoundException))
//				logger.log(Level.SEVERE, "parseFile()", e);
//		} finally {
//			IOUtils.closeQuietly(stream);
//		}
//	}

	/**
	 * Loads and parses the XML save file.
	 */
	public void parseFile() {
	    SAXBuilder builder = new SAXBuilder();

	    try  {
	    	configDoc = builder.build(new File(Simulation.SAVE_DIR, FILE_NAME));
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/**
	 * Creates an XML document for the UI configuration and saves it to a file.
	 * 
	 * @param mainWindow the main window.
	 */
	public void saveFile(MainWindow mainWindow) {
		desktop = mainWindow.getDesktop();
		FileOutputStream stream = null;

		try {
			Document outputDoc = new Document();
			DocType dtd = new DocType(UI, Simulation.SAVE_DIR + File.separator + FILE_NAME_DTD);
			Element uiElement = new Element(UI);
			outputDoc.setDocType(dtd);
			outputDoc.addContent(uiElement);
			outputDoc.setRootElement(uiElement);

			uiElement.setAttribute(USE_DEFAULT, "false"); // FIXME lechimp 10/9/13: why is this always set to false upon
															// save?
			uiElement.setAttribute(SHOW_TOOL_BAR, Boolean.toString(mainWindow.getToolToolBar().isVisible()));
			uiElement.setAttribute(SHOW_UNIT_BAR, Boolean.toString(mainWindow.getUnitToolBar().isVisible()));

			Element mainWindowElement = new Element(MAIN_WINDOW);
			uiElement.addContent(mainWindowElement);

			mainWindowElement.setAttribute(LOCATION_X, Integer.toString(mainWindow.getFrame().getX()));
			mainWindowElement.setAttribute(LOCATION_Y, Integer.toString(mainWindow.getFrame().getY()));
			mainWindowElement.setAttribute(WIDTH, Integer.toString(mainWindow.getFrame().getWidth()));
			mainWindowElement.setAttribute(HEIGHT, Integer.toString(mainWindow.getFrame().getHeight()));

			Element volumeElement = new Element(VOLUME);
			uiElement.addContent(volumeElement);

			AudioPlayer player = desktop.getSoundPlayer();
			volumeElement.setAttribute(SOUND, Double.toString(player.getMusicVolume()));
			volumeElement.setAttribute(SOUND, Double.toString(player.getEffectVolume()));
			volumeElement.setAttribute(MUTE, Boolean.toString(player.isMusicMute()));
			volumeElement.setAttribute(MUTE, Boolean.toString(player.isSoundMute()));

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

			// Save to file.
	
//			 [landrus, 27.11.09]: Hard paths are a pain with webstart, so we will use the
//			 users home dir, because this will work properly. Also we will have to copy
//			 the ui_settings.dtd to this folder because in a webstart environment, the
//			 user has no initial data in his dirs.
			 
			File configFile = new File(Simulation.SAVE_DIR, FILE_NAME);

			// Create save directory if it doesn't exist.
			if (!configFile.getParentFile().exists()) {
				configFile.getParentFile().mkdirs();
			}

			// Copy /dtd/ui_settings.dtd resource to save directory.
			// Always do this as we don't know when the local saved dtd file is out of date.
			InputStream in = getClass().getResourceAsStream("/dtd/" + FILE_NAME_DTD);
			IOUtils.copy(in, new FileOutputStream(new File(Simulation.SAVE_DIR, FILE_NAME_DTD)));

			XMLOutputter fmt = new XMLOutputter();
			fmt.setFormat(Format.getPrettyFormat());
			stream = new FileOutputStream(configFile);
			
//			 bug 2909888: read the inputstream with a specific encoding instead of the
//			 system default.
			 
			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
			fmt.output(outputDoc, writer);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

//	/**
//	 * Creates an XML document for the UI configuration and saves it to a file.
//	 * 
//	 * @param mainScene the Main Scene.
//	 */
//	public void saveFile(MainScene mainScene) {
//		desktop = mainScene.getDesktop();
//		FileOutputStream stream = null;
//
//		try {
//			Document outputDoc = new Document();
//			DocType dtd = new DocType(UI, DIRECTORY + File.separator + FILE_NAME_DTD);
//			Element uiElement = new Element(UI);
//			outputDoc.setDocType(dtd);
//			outputDoc.addContent(uiElement);
//			outputDoc.setRootElement(uiElement);
//
//			uiElement.setAttribute(USE_DEFAULT, "false"); // FIXME lechimp 10/9/13: why is this always set to false upon
//															// save?
//
//			Element mainWindowElement = new Element(MAIN_WINDOW);
//			uiElement.addContent(mainWindowElement);
//
//			Element volumeElement = new Element(VOLUME);
//			uiElement.addContent(volumeElement);
//
//			AudioPlayer player = desktop.getSoundPlayer();
//			volumeElement.setAttribute(SOUND, Double.toString(player.getMusicVolume()));
//			volumeElement.setAttribute(SOUND, Double.toString(player.getEffectVolume()));
//			volumeElement.setAttribute(MUTE, Boolean.toString(player.isMusicMute()));
//			volumeElement.setAttribute(MUTE, Boolean.toString(player.isSoundMute()));
//
//			Element internalWindowsElement = new Element(INTERNAL_WINDOWS);
//			uiElement.addContent(internalWindowsElement);
//
//			// Add all internal windows.
//			JInternalFrame[] windows = desktop.getAllFrames();
//			for (JInternalFrame window1 : windows) {
//				Element windowElement = new Element(WINDOW);
//				internalWindowsElement.addContent(windowElement);
//
//				windowElement.setAttribute(Z_ORDER, Integer.toString(desktop.getComponentZOrder(window1)));
//				windowElement.setAttribute(LOCATION_X, Integer.toString(window1.getX()));
//				windowElement.setAttribute(LOCATION_Y, Integer.toString(window1.getY()));
//				windowElement.setAttribute(WIDTH, Integer.toString(window1.getWidth()));
//				windowElement.setAttribute(HEIGHT, Integer.toString(window1.getHeight()));
//				windowElement.setAttribute(DISPLAY, Boolean.toString(!window1.isIcon()));
//
//				if (window1 instanceof ToolWindow) {
//					windowElement.setAttribute(TYPE, TOOL);
//					windowElement.setAttribute(NAME, ((ToolWindow) window1).getToolName());
//				} else if (window1 instanceof UnitWindow) {
//					windowElement.setAttribute(TYPE, UNIT);
//					windowElement.setAttribute(NAME, ((UnitWindow) window1).getUnit().getName());
//				} else {
//					windowElement.setAttribute(TYPE, "other");
//					windowElement.setAttribute(NAME, "other");
//				}
//			}
//
//			// Save to file.
//			
////			 [landrus, 27.11.09]: Hard paths are a pain with webstart, so we will use the
////			 users home dir, because this will work properly. Also we will have to copy
////			 the ui_settings.dtd to this folder because in a webstart environment, the
////			 user has no initial data in his dirs.
//			 
//			File configFile = new File(DIRECTORY, FILE_NAME);
//
//			// Create save directory if it doesn't exist.
//			if (!configFile.getParentFile().exists()) {
//				configFile.getParentFile().mkdirs();
//			}
//
//			// Copy /dtd/ui_settings.dtd resource to save directory.
//			// Always do this as we don't know when the local saved dtd file is out of date.
//			InputStream in = getClass().getResourceAsStream("/dtd/ui_settings.dtd");
//			IOUtils.copy(in, new FileOutputStream(new File(DIRECTORY, "ui_settings.dtd")));
//
//			XMLOutputter fmt = new XMLOutputter();
//			fmt.setFormat(Format.getPrettyFormat());
//			stream = new FileOutputStream(configFile);
//			
////			 // bug 2909888: read the inputstream with a specific encoding instead of the
////			 system default.
//			 
//			OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
//			fmt.output(outputDoc, writer);
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, e.getMessage());
//		} finally {
//			IOUtils.closeQuietly(stream);
//		}
//	}

	/**
	 * Checks if UI should use default configuration.
	 *
	 * @return true if default.
	 */
	public boolean useUIDefault() {
		try {
			Element root = configDoc.getRootElement();
			return Boolean.parseBoolean(root.getAttributeValue(USE_DEFAULT));
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Checks if UI should show the Tool bar.
	 *
	 * @return true if default.
	 */
	public boolean showToolBar() {
		try {
			Element root = configDoc.getRootElement();
			return Boolean.parseBoolean(root.getAttributeValue(SHOW_TOOL_BAR));
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Checks if UI should show the Unit bar.
	 *
	 * @return true if default.
	 */
	public boolean showUnitBar() {
		try {
			Element root = configDoc.getRootElement();
			return Boolean.parseBoolean(root.getAttributeValue(SHOW_UNIT_BAR));
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Gets the screen location of the main window origin.
	 *
	 * @return location.
	 */
	public Point getMainWindowLocation() {
		try {
			Element root = configDoc.getRootElement();
			Element mainWindow = root.getChild(MAIN_WINDOW);
			int x = Integer.parseInt(mainWindow.getAttributeValue(LOCATION_X));
			int y = Integer.parseInt(mainWindow.getAttributeValue(LOCATION_Y));
			return new Point(x, y);
		} catch (Exception e) {
			return new Point(0, 0);
		}
	}

	/**
	 * Gets the size of the main window.
	 *
	 * @return size.
	 */
	public Dimension getMainWindowDimension() {
		try {
			Element root = configDoc.getRootElement();
			Element mainWindow = root.getChild(MAIN_WINDOW);
			int width = Integer.parseInt(mainWindow.getAttributeValue(WIDTH));
			int height = Integer.parseInt(mainWindow.getAttributeValue(HEIGHT));
			return new Dimension(width, height);
		} catch (Exception e) {
			return new Dimension(300, 300);
		}
	}

	/**
	 * Gets the sound volume level.
	 *
	 * @return volume (0 (silent) to 1 (loud)).
	 */
	public float getVolume() {
		try {
			Element root = configDoc.getRootElement();
			Element volume = root.getChild(VOLUME);
			return Float.parseFloat(volume.getAttributeValue(SOUND));
		} catch (Exception e) {
			return 50F;
		}
	}

	/**
	 * Checks if sound volume is set to mute.
	 *
	 * @return true if mute.
	 */
	public boolean isMute() {
		try {
			Element root = configDoc.getRootElement();
			Element volume = root.getChild(VOLUME);
			return Boolean.parseBoolean(volume.getAttributeValue(MUTE));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Checks if an internal window is displayed.
	 *
	 * @param windowName the window name.
	 * @return true if displayed.
	 */
	public boolean isInternalWindowDisplayed(String windowName) {
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			boolean result = false;
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					String name = internalWindow.getAttributeValue(NAME);
					if (name.equals(windowName)) {
						result = Boolean.parseBoolean(internalWindow.getAttributeValue(DISPLAY));
						break;
					}
				}
			}
			return result;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Gets the origin location of an internal window on the desktop.
	 *
	 * @param windowName the window name.
	 * @return location.
	 */
	public Point getInternalWindowLocation(String windowName) {
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			Point result = new Point(0, 0);
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					String name = internalWindow.getAttributeValue(NAME);
					if (name.equals(windowName)) {
						int locationX = Integer.parseInt(internalWindow.getAttributeValue(LOCATION_X));
						int locationY = Integer.parseInt(internalWindow.getAttributeValue(LOCATION_Y));
						result.setLocation(locationX, locationY);
					}
				}
			}
			return result;
		} catch (Exception e) {
			return new Point(0, 0);
		}
	}

	/**
	 * Gets the z order of an internal window on the desktop.
	 *
	 * @param windowName the window name.
	 * @return z order (lower number represents higher up)
	 */
	public int getInternalWindowZOrder(String windowName) {
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			int result = -1;
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					String name = internalWindow.getAttributeValue(NAME);
					if (name.equals(windowName))
						result = Integer.parseInt(internalWindow.getAttributeValue(Z_ORDER));
				}
			}
			return result;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 * Gets the size of an internal window.
	 *
	 * @param windowName the window name.
	 * @return size.
	 */
	public Dimension getInternalWindowDimension(String windowName) {
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			Dimension result = new Dimension(0, 0);
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					String name = internalWindow.getAttributeValue(NAME);
					if (name.equals(windowName)) {
						int width = Integer.parseInt(internalWindow.getAttributeValue(WIDTH));
						int height = Integer.parseInt(internalWindow.getAttributeValue(HEIGHT));
						result = new Dimension(width, height);
					}
				}
			}
			return result;
		} catch (Exception e) {
			return new Dimension(0, 0);
		}
	}

	/**
	 * Gets the internal window type.
	 *
	 * @param windowName the window name.
	 * @return "unit" or "tool".
	 */
	public String getInternalWindowType(String windowName) {
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			String result = "";
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					String name = internalWindow.getAttributeValue(NAME);
					if (name.equals(windowName))
						result = internalWindow.getAttributeValue(TYPE);
				}
			}
			return result;
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Checks if internal window is configured.
	 *
	 * @param windowName the window name.
	 * @return true if configured.
	 */
	public boolean isInternalWindowConfigured(String windowName) {
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			boolean result = false;
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					String name = internalWindow.getAttributeValue(NAME);
					if (name.equals(windowName))
						result = true;
				}
			}
			return result;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Gets all of the internal window names.
	 *
	 * @return list of window names.
	 */
	public List<String> getInternalWindowNames() {
		List<String> result = new ArrayList<String>();
		try {
			Element root = configDoc.getRootElement();
			Element internalWindows = root.getChild(INTERNAL_WINDOWS);
			List<Element> internalWindowNodes = internalWindows.getChildren();
			for (Object element : internalWindowNodes) {
				if (element instanceof Element) {
					Element internalWindow = (Element) element;
					result.add(internalWindow.getAttributeValue(NAME));
				}
			}
			return result;
		} catch (Exception e) {
			return result;
		}
	}
}
