/*
 * Mars Simulation Project
 * MainDesktopPane.java
 * @date 2025-08-17
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import com.mars_sim.core.Entity;
import com.mars_sim.core.GameManager.GameMode;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.Unit;
import com.mars_sim.core.UnitManager;
import com.mars_sim.core.UnitManagerEvent;
import com.mars_sim.core.UnitManagerListener;
import com.mars_sim.core.UnitType;
import com.mars_sim.core.interplanetary.transport.Transportable;
import com.mars_sim.core.map.location.Coordinates;
import com.mars_sim.core.person.ai.mission.Mission;
import com.mars_sim.core.science.ScientificStudy;
import com.mars_sim.core.structure.Settlement;
import com.mars_sim.core.time.ClockListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.tool.RandomUtil;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.astroarts.OrbitWindow;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.sound.SoundConstants;
import com.mars_sim.ui.swing.tool.commander.CommanderWindow;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.tool.mission.MissionWindow;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.tool.monitor.UnitTableModel;
import com.mars_sim.ui.swing.tool.navigator.NavigatorWindow;
import com.mars_sim.ui.swing.tool.resupply.ResupplyWindow;
import com.mars_sim.ui.swing.tool.science.ScienceWindow;
import com.mars_sim.ui.swing.tool.search.SearchWindow;
import com.mars_sim.ui.swing.tool.settlement.SettlementMapPanel;
import com.mars_sim.ui.swing.tool.settlement.SettlementWindow;
import com.mars_sim.ui.swing.tool.time.TimeWindow;
import com.mars_sim.ui.swing.tool_window.ToolWindow;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import com.mars_sim.ui.swing.unit_window.UnitWindow;
import com.mars_sim.ui.swing.unit_window.UnitWindowFactory;
import com.mars_sim.ui.swing.unit_window.UnitWindowListener;

/**
 * The MainDesktopPane class is the desktop part of the project's UI. It
 * contains all tool and unit windows, and is itself contained, along with the
 * tool bars, by the main window.
 */
@SuppressWarnings("serial")
public class MainDesktopPane extends JDesktopPane
		implements ClockListener, ComponentListener, UnitManagerListener {

	// Properties for UIConfig settings
	private static final String DESKTOP_PROPS = "desktop";
	private static final String PRELOAD_TOOLS = "preload_tools";

	/** Default logger. */
	private static Logger logger = Logger.getLogger(MainDesktopPane.class.getName());

	/** The sound player. */
	private static AudioPlayer soundPlayer;

	/** The game mode of this simulation session. */
	public GameMode mode;
	/** List of open or buttoned unit windows. */
	private Collection<UnitWindow> unitWindows;
	/** List of tool windows. */
	private Collection<ToolWindow> toolWindows;
	/** ImageIcon that contains the tiled background. */
	private ImageIcon backgroundImageIcon;
	/** Label that contains the tiled background. */
	private JLabel backgroundLabel;
	/** The image icon of the tiled background. */
	private Image baseImageIcon = ImageLoader.getImage("background");

	private MainWindow mainWindow;
	
	private SettlementMapPanel settlementMapPanel;
	
	// Preload the Tool windows
	private boolean preloadTools = true;

	// Simulation reference used by the UI windows
	private Simulation sim;

	/**
	 * Constructor 1.
	 *
	 * @param mainWindow the main outer window
	 */
	public MainDesktopPane(MainWindow mainWindow, Simulation sim) {
		super();

		this.mainWindow = mainWindow;
		this.sim = sim;

		// Initialize sound player
		if (!AudioPlayer.isAudioDisabled()) {
			soundPlayer = new AudioPlayer(this);
		}
		
		// Play the splash sound
		String soundFilePath = SoundConstants.SND_SPLASH;
		if (soundFilePath != null && soundFilePath.length() != 0 && soundPlayer != null) {
			soundPlayer.playSound(soundFilePath);
		}
		
		// Prepare unit windows.
		unitWindows = new ArrayList<>();

		// Prepare tool windows. Needs to be thread safe as windows are used by clock pulse
		toolWindows = new CopyOnWriteArrayList<>();

		prepareListeners();

		init();
	}

	private void init() {
		// Set background color to black
		setBackground(Color.black);
		// set desktop manager
		setDesktopManager(new MainDesktopManager());
		// Set component listener
		addComponentListener(this);
		// Create background label and set it to the back layer
		backgroundImageIcon = new ImageIcon();
		// Set up background
		backgroundLabel = new JLabel(backgroundImageIcon);
		// Add background
		add(backgroundLabel, Integer.MIN_VALUE);
		// Set location of background
		backgroundLabel.setLocation(0, 0);
		// Push the background to the back
		moveToBack(backgroundLabel);

		Properties props = mainWindow.getConfig().getPropSet(DESKTOP_PROPS);
		preloadTools = UIConfig.extractBoolean(props, PRELOAD_TOOLS, false);
		if (preloadTools) {
			// Prep tool windows
			prepareToolWindows();
		}
		
		// Prep listeners
		prepareListeners();

		// Set the main window's size
		Dimension selectedSize = mainWindow.getSelectedSize();
		if (selectedSize != null) {
			setSize(selectedSize);
			setPreferredSize(selectedSize);
			logger.config("Main Window initially set to " + selectedSize);
		}
	}

	/**
	 * Starts a background sound track.
	 */
	public void playBackgroundMusic() {
		// Play a random music track
		soundPlayer.playRandomMusicTrack();
	}
	
	/**
	 * Creates background tile when MainDesktopPane is first displayed. Center
	 * logoLabel on MainWindow and set backgroundLabel to the size of
	 * MainDesktopPane.
	 *
	 * @param e the component event
	 */
	@Override
	public void componentResized(ComponentEvent e) {

		Dimension screenSize = getSize();

		if (screenSize == null || screenSize.getWidth() == 0 || screenSize.getHeight() == 0) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			logger.config("Toolkit default screen size is " + screenSize.getWidth() + " x " + screenSize.getHeight());
		}

		Image backgroundImage = createImage((int) screenSize.getWidth(), (int) screenSize.getHeight());
		if (backgroundImage != null) {
			// Not loaded when the window is first building
			Graphics backgroundGraphics = backgroundImage.getGraphics();

			int sourceWidth = baseImageIcon.getWidth(this);
			int sourceHeight = baseImageIcon.getHeight(this);
			int targetWidth = backgroundImage.getWidth(this);
			int targetHeight = backgroundImage.getHeight(this);
			for (int x = 0; x < targetWidth; x += sourceWidth) {
				for (int y = 0; y < targetHeight; y += sourceHeight) {
					backgroundGraphics.drawImage(baseImageIcon, x, y, this);
				}
			}

			backgroundImageIcon.setImage(backgroundImage);
			// Set the backgroundLabel size to the size of the desktop
			backgroundLabel.setSize(getSize());
			
			backgroundGraphics.dispose();
		}
	}

	// Additional Component Listener methods implemented but not used.
	@Override
	public void componentMoved(ComponentEvent e) {
		updateToolWindow();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void updateToolWindow() {
		JInternalFrame[] frames = (JInternalFrame[]) this.getAllFrames();
		for (JInternalFrame f : frames) {
			f.updateUI();
		}
	}

	@Override
	public Component add(Component comp) {
		super.add(comp);
		centerJIF(comp);
		return comp;
	}

	/**
	 * Move a window to the center of the desktop
	 * @param comp
	 */
	void centerJIF(Component comp) {
		Dimension desktopSize = getSize();
		Dimension jInternalFrameSize = comp.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		comp.setLocation(width, height);
		comp.setVisible(true);
	}

	@Override
	public void unitManagerUpdate(UnitManagerEvent event) {
		Object unit = event.getUnit();
		if (unit instanceof Settlement) {
			updateToolWindow();
		}
	}

	/**
	 * Sets up this class with two listeners.
	 */
	private void prepareListeners() {
		// Attach UnitManagerListener to desktop
		UnitManager unitManager = sim.getUnitManager();
		unitManager.addUnitManagerListener(UnitType.SETTLEMENT, this);
	}

	/**
	 * Returns the MainWindow instance.
	 *
	 * @return MainWindow instance
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	/**
	 * Creates tool windows.
	 */
	private void prepareToolWindows() {
		getToolWindow(CommanderWindow.NAME, true);
		getToolWindow(NavigatorWindow.NAME, true);
		getToolWindow(SearchWindow.NAME, true);
		getToolWindow(TimeWindow.NAME, true);
		getToolWindow(SettlementWindow.NAME, true);
		getToolWindow(ScienceWindow.NAME, true);
		getToolWindow(MonitorWindow.NAME, true);
		getToolWindow(MissionWindow.NAME, true);
		getToolWindow(ResupplyWindow.NAME, true);
	}

	/**
	 * Get the list of open Tool windows
	 */
	Collection<ToolWindow> getToolWindows() {
		return toolWindows;
	}

	/**
	 * Get the list of open UnitWindows
	 */
	Collection<UnitWindow> getUnitWindows() {
		return unitWindows;
	}

	/**
	 * Returns a tool window for a given tool name.
	 *
	 * @param toolName      the name of the tool window
	 * @param createWinddow Create a window if it doesn't exist
	 * @return the tool window
	 */
	private ToolWindow getToolWindow(String toolName, boolean createWindow) {
		for (ToolWindow w : toolWindows) {
			if (toolName.equals(w.getToolName()))
				return w;
		}

		if (createWindow) {
			ToolWindow w = switch(toolName) {
				case CommanderWindow.NAME -> new CommanderWindow(this);
				case NavigatorWindow.NAME -> new NavigatorWindow(this);
				case SearchWindow.NAME -> new SearchWindow(this);
				case TimeWindow.NAME -> new TimeWindow(this);
				case SettlementWindow.NAME -> new SettlementWindow(this);
				case ScienceWindow.NAME -> new ScienceWindow(this);
				case GuideWindow.NAME -> new GuideWindow(this);
				case MonitorWindow.NAME -> new MonitorWindow(this);
				case MissionWindow.NAME -> new MissionWindow(this);
				case ResupplyWindow.NAME -> new ResupplyWindow(this);
				case OrbitWindow.NAME -> new OrbitWindow(this);
				default -> null;
			};
			if (w == null) {
				logger.warning("No tool called " + toolName);
				return null;
			}

			// Close it from the start
			try {
				w.setClosed(true);
			} catch (PropertyVetoException e) {
				logger.warning("Problem loading tool window " + e.getMessage());
			}
			toolWindows.add(w);
			return w;
		}
		return null;
	}

	/**
	 * Displays a new Unit model in the monitor window.
	 *
	 * @param model the new model to display
	 */
	public void addModel(UnitTableModel<?> model) {
		((MonitorWindow) openToolWindow(MonitorWindow.NAME)).displayModel(model);
	}

	/**
	 * Centers the map and the globe on given coordinates. Also opens the map tool
	 * if it's closed.
	 *
	 * @param targetLocation the new center location
	 */
	public void centerMapGlobe(Coordinates targetLocation) {
		((NavigatorWindow) openToolWindow(NavigatorWindow.NAME)).updateCoordsMaps(targetLocation);
	}

	/**
	 * Returns true if tool window is open.
	 *
	 * @param toolName the name of the tool window
	 * @return true true if tool window is open
	 */
	public boolean isToolWindowOpen(String toolName) {
		ToolWindow w = getToolWindow(toolName, false);
		if (w != null)
			return !w.isClosed();
		return false;
	}
	
	/**
	 * Opens a tool window if necessary.
	 *
	 * @param toolName the name of the tool window
	 */
	public ToolWindow openToolWindow(String toolName) {
		ToolWindow window = getToolWindow(toolName, true);

		if (window == null) {
			return null;
		}

		if (window.isClosed() && !window.wasOpened()) {
			UIConfig config = mainWindow.getConfig();
			Point location = null;
			WindowSpec previousDetails = config.getInternalWindowDetails(toolName);
			if (previousDetails != null) {
				location = previousDetails.position();
				if (window.isResizable()) {
					window.setSize(previousDetails.size());
				}
			} else if (toolName.equals(TimeWindow.NAME))
				location = computeLocation(window, 0, 2);
			else if (toolName.equals(MonitorWindow.NAME))
				location = computeLocation(window, 1, 0);
			else
				location = getCenterLocation(window);

			// Check is visible
			Dimension currentSize = getSize();
			location = new Point(Math.max(1, Math.min(location.x, (int)currentSize.getWidth() - 20)),
								 Math.max(1, Math.min(location.y, (int)currentSize.getHeight() - 20)));
			window.setLocation(location);
			window.setWasOpened(true);
		}

		// in case of classic swing mode for MainWindow
		add(window, 0);

		try {
			window.setClosed(false);
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.toString());
		}

		window.show();

		// bring to front if it overlaps with other windows
		try {
			window.setSelected(true);
		} catch (PropertyVetoException e) {
			// ignore if setSelected is vetoed
		}

		window.getContentPane().validate();
		window.getContentPane().repaint();

		validate();
		repaint();

		return window;
	}

	/**
	 * Closes a tool window if it is open.
	 *
	 * @param toolName the name of the tool window
	 */
	public void closeToolWindow(String toolName) {
		ToolWindow window = getToolWindow(toolName, false);
		if ((window != null) && !window.isClosed()) {
			try {
				window.setClosed(true);
				window.dispose();
			} catch (java.beans.PropertyVetoException e) {
				// ignore
			}
		}
	}

	/**
	 * Gets the navigator window.
	 * 
	 * @return
	 */
	public NavigatorWindow getNavWin() {
		return ((NavigatorWindow)openToolWindow(NavigatorWindow.NAME));
	}
		
	/**
	 * Creates and opens a window for an Entity if it isn't already in existence and
	 * open. This selects the most appropriate tool window.
	 *
	 * @param entity Entity to display
	 */
    public void showDetails(Entity entity) {
		if (entity instanceof Unit u) {
			openUnitWindow(u, null);
		}
		else if (entity instanceof Mission m) {
			((MissionWindow)openToolWindow(MissionWindow.NAME)).openMission(m);
		}
		else if (entity instanceof Transportable t) {
			((ResupplyWindow)openToolWindow(ResupplyWindow.NAME)).openTransportable(t);
		}
		else if (entity instanceof ScientificStudy s) {
			((ScienceWindow)openToolWindow(ScienceWindow.NAME)).setScientificStudy(s);
		}
    }

	/**
	 * Opens a Unit Window for a specific Unit with a optional set of user properties.
	 * 
	 * @param unit Unit to display
	 * @param initProps Initial properties
     * @return
	 */
	private UnitWindow openUnitWindow(Unit unit, WindowSpec initProps) {

		UnitWindow tempWindow = null;

		// See if the window of this unit has already been opened
		for (UnitWindow window : unitWindows) {
			if (window.getUnit() == unit) {
				tempWindow = window;
				if (tempWindow.isClosed()) {
					add(tempWindow, 0);
				}
			}
		}

		if (tempWindow == null) {
			// Create new window for unit.
			tempWindow = UnitWindowFactory.getUnitWindow(unit, this);
			
			add(tempWindow, 0);
			tempWindow.pack();

			// Set internal frame listener
			tempWindow.addInternalFrameListener(new UnitWindowListener(this));

			Point newPosition = null;
			if (initProps != null) {
				tempWindow.setUIProps(initProps.props());
				newPosition = initProps.position();
				
				// Make sure store position is visible
				Dimension desktopSize = getSize();
				if ((newPosition.getX() >= desktopSize.getWidth())
						|| (newPosition.getY() >= desktopSize.getHeight())) {
					newPosition = null;
				}
			}
			if (newPosition == null) {
				newPosition = getRandomLocation(tempWindow);
			}
			// Put window in random position on desktop.
			tempWindow.setLocation(newPosition);

			// Add unit window to unit windows
			unitWindows.add(tempWindow);

			// Create new unit button in tool bar if necessary
			if (mainWindow != null)
				mainWindow.createUnitButton(unit);
		}

		tempWindow.setVisible(true);

		// Correct window becomes selected
		try {
			tempWindow.setSelected(true);
			tempWindow.moveToFront();
		} catch (java.beans.PropertyVetoException e) {
		}

		// Play sound
		String soundFilePath = UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getSound(unit);
		if (soundFilePath != null && soundFilePath.length() != 0 && soundPlayer != null) {
			soundPlayer.playSound(soundFilePath);
		}
		
		return tempWindow;
	}

	/**
	 * Disposes a unit window and button.
	 *
	 * @param window the unit window to dispose.
	 */
	public void disposeUnitWindow(UnitWindow window) {

		if (window != null) {
			window.setVisible(false);

			unitWindows.remove(window);
			window.dispose();

			// Have main window dispose of unit button
			if (mainWindow != null)
				mainWindow.disposeUnitButton(window.getUnit());

			// Lastly destroy the window
			window.destroy();
		}
	}

	/**
	 * Update the desktop and all of its windows.
	 */
	private void updateWindows(ClockPulse pulse) {
		// Update all unit windows.
		for (UnitWindow u : unitWindows) {
			if (u.isVisible() || u.isShowing())
				u.update();
		}

		// Update all tool windows.
		for (ToolWindow w : toolWindows) {
			if (w.isVisible() || w.isShowing())
				w.update(pulse);
		}
	}

	private Point getCenterLocation(JInternalFrame tempWindow) {

		Dimension desktopSize = getSize();
		Dimension windowSize = tempWindow.getSize();

		int rX = (int) Math.round((desktopSize.width - windowSize.width) / 2D);
		int rY = (int) Math.round((desktopSize.height - windowSize.height - 100) / 2D);

		// Added rX checking
		if (rX < 0) {
			rX = 0;
		}

		// Make sure y position isn't < 0.
		if (rY < 0) {
			rY = 0;
		}

		return new Point(rX, rY);
	}

	/**
	 * Gets a random location on the desktop for a given {@link JInternalFrame}.
	 *
	 * @param tempWindow an internal window
	 * @return random point on the desktop
	 */
	private Point getRandomLocation(JInternalFrame tempWindow) {

		Dimension desktopSize = getSize();
		Dimension windowSize = tempWindow.getSize();

		// Populate windows in grid=like starting position
		int w = desktopSize.width - windowSize.width;
		int rX = RandomUtil.getRandomInt(w / 20) * 20;

		int rY = RandomUtil.getRandomInt(5) * 20;

		return new Point(rX, rY);
	}

	/**
	 * Gets a particular location on the desktop for a given {@link JInternalFrame}.
	 *
	 * @param f
	 * @param position
	 * @return a specific point on the desktop
	 */
	private Point computeLocation(JInternalFrame f, int positionX, int positionY) {
		Dimension desktopSize = getSize();
		Dimension windowSize = f.getSize();

		// Populate windows in grid=like starting position
		int w = desktopSize.width - windowSize.width;
		int h = desktopSize.height - windowSize.height;
		int rX = 0;
		int rY = 0;

		if (positionX == 0)
			rX = 0;
		else if (positionX == 1)
			rX = w / 2;
		else if (positionX == 2)
			rX = w;

		if (positionY == 0)
			rY = 0;
		else if (positionY == 1)
			rY = h / 2;
		else if (positionY == 2)
			rY = h;

		return new Point(rX, rY);
	}

	/**
	 * Gets the sound player used by the desktop.
	 *
	 * @return sound player.
	 */
	public AudioPlayer getSoundPlayer() {
		return soundPlayer;
	}

	/**
	 * Opens all initial windows based on UI configuration.
	 */
	public void openInitialWindows() {
		List<WindowSpec> startingWindows = mainWindow.getConfig().getConfiguredWindows();

		if (!startingWindows.isEmpty()) {
			UnitManager uMgr = mainWindow.getDesktop().getSimulation().getUnitManager();
			for(WindowSpec w : startingWindows) {
				switch(w.type()) {
					case UIConfig.TOOL:
						openToolWindow(w.name());
					break;

					case UIConfig.UNIT:
						Unit u = UnitWindow.getUnit(uMgr, w.props());
						if (u != null) {
							openUnitWindow(u, w);
						}
					break;
				}
 			}
		}
		else {
			if (mode == GameMode.COMMAND) {
				// Open the time window for the Commander Mode
				openToolWindow(TimeWindow.NAME);
				openToolWindow(CommanderWindow.NAME);
			}

			else {
				openToolWindow(MonitorWindow.NAME);
			}
		}
	}

	/**
	 * Gets a reference to the Simulation being displayed.
	 * 
	 * @return
	 */
	public Simulation getSimulation() {
		return sim;
	}

	public void setSettlementMapPanel(SettlementMapPanel panel) {
		settlementMapPanel = panel;
	}
	
	public SettlementMapPanel getSettlementMapPanel() {
		return settlementMapPanel;
	}
	
	@Override
	public void clockPulse(ClockPulse pulse) {
		updateWindows(pulse);
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// Nothing to do
	}

	/**
	 * Gets UI properties of the Desktop.
	 */
	public Map<String, Properties> getUIProps() {
		Map<String, Properties> result = new HashMap<>();

		if (soundPlayer != null) {
			result.put(AudioPlayer.PROPS_NAME, soundPlayer.getUIProps());
		}

		// Add desktop properties
		Properties desktopProps = new Properties();
		desktopProps.setProperty(PRELOAD_TOOLS, Boolean.toString(preloadTools));
		result.put(DESKTOP_PROPS, desktopProps);
		return result;
    }

	/**
	 * Prepares for deletion.
	 */
	public void destroy() {

		removeComponentListener(this);
		
		mode = null;
		if (unitWindows != null) {
			for (UnitWindow u : unitWindows) {
				u.destroy();
			}
			unitWindows = null;
		}
		if (toolWindows != null) {
			for (ToolWindow w : toolWindows) {
				w.destroy();
			}
			toolWindows = null;
		}
	}
}
