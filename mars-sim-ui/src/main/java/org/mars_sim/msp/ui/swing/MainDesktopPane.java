/*
 * Mars Simulation Project
 * MainDesktopPane.java
 * @date 2021-08-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.UnitTableModel;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;
import org.mars_sim.msp.ui.swing.toolwindow.ToolWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindowFactory;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindowListener;

/**
 * The MainDesktopPane class is the desktop part of the project's UI. It
 * contains all tool and unit windows, and is itself contained, along with the
 * tool bars, by the main window.
 */
@SuppressWarnings("serial")
public class MainDesktopPane extends JDesktopPane
		implements ClockListener, ComponentListener, UnitListener, UnitManagerListener {

	/** default logger. */
	private static Logger logger = Logger.getLogger(MainDesktopPane.class.getName());

	public final static String THEME_PATH = "/fxui/css/theme/";

	public final static String ORANGE_CSS_THEME = THEME_PATH + "nimrodskin.css";
	public final static String BLUE_CSS_THEME = THEME_PATH + "snowBlue.css";

	public final static String ORANGE_CSS = ORANGE_CSS_THEME;
	public final static String BLUE_CSS = BLUE_CSS_THEME;

	public GameMode mode;

	// Data members
//	private double timeCache = 0;
	private boolean isTransportingBuilding = false, isConstructingSite = false;
	/** True if this MainDesktopPane hasn't been displayed yet. */
	private boolean firstDisplay;
	/** List of open or buttoned unit windows. */
	private Collection<UnitWindow> unitWindows;
	/** List of tool windows. */
	private Collection<ToolWindow> toolWindows;
	/** ImageIcon that contains the tiled background. */
	private ImageIcon backgroundImageIcon;
	/** Label that contains the tiled background. */
	private JLabel backgroundLabel;

	/** The sound player. */
	private static AudioPlayer soundPlayer;

	/** The desktop popup announcement window. */
	private AnnouncementWindow announcementWindow;
	private SettlementWindow settlementWindow;
	private NavigatorWindow navWindow;
	private TimeWindow timeWindow;
	private CommanderWindow commanderWindow;

	private MainWindow mainWindow;

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

		// Initialize data members
		soundPlayer = new AudioPlayer(this);
		// Play music
		if (!soundPlayer.isVolumeDisabled())
			soundPlayer.playRandomMusicTrack();
		// Prepare unit windows.
		unitWindows = new ArrayList<>();

		// Prepare tool windows.
		toolWindows = new ArrayList<>();

		prepareListeners();
		
		SwingUtilities.invokeLater(() -> init());
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
		// Initialize firstDisplay to true
		firstDisplay = true;
		// Prep listeners
		prepareListeners();

		try {
			// Prep tool windows
			prepareToolWindows();
		} catch (Exception e) {
          	logger.log(Level.SEVERE, "Cannot prepare tool windows: " + e);
		}

		// Setup announcement window
		prepareAnnouncementWindow();
		
		// Add clock listener with a minimum duration of 1s
		sim.getMasterClock().addClockListener(this, 1000L);
		
		// Set background paper size
		Dimension selectedSize = mainWindow.getSelectedSize();
		if (selectedSize != null) {
			int w = selectedSize.width;
			int h = selectedSize.height;
			setSize(new Dimension(w, h));
			setPreferredSize(new Dimension(w, h));
			logger.config("The main desktop pane is initially set to " 
					+ w  
					+ " x "
					+ h
					+ ".");	
		}
	}

	/**
	 * Create background tile when MainDesktopPane is first displayed. Recenter
	 * logoLabel on MainWindow and set backgroundLabel to the size of
	 * MainDesktopPane.
	 *
	 * @param e the component event
	 */
	@Override
	public void componentResized(ComponentEvent e) {

		ImageIcon baseImageIcon = ImageLoader.getIcon(Msg.getString("img.background")); //$NON-NLS-1$
		
		Dimension screen_size = mainWindow.getFrame().getSize();
		
		if (screen_size == null || (int) screen_size.getWidth() == 0 || (int) screen_size.getHeight() == 0) {
			screen_size = Toolkit.getDefaultToolkit().getScreenSize();
//			logger.config("Current toolkit screen size is " + screen_size.width + " x " + screen_size.height);
		}
//		else
//			logger.config("Current main window frame is " + screen_size.width + " x " + screen_size.height);
		
		Image backgroundImage = createImage((int) screen_size.getWidth(), (int) screen_size.getHeight());
		Graphics backgroundGraphics = backgroundImage.getGraphics();

		for (int x = 0; x < backgroundImage.getWidth(this); x += baseImageIcon.getIconWidth()) {
			for (int y = 0; y < backgroundImage.getHeight(this); y += baseImageIcon.getIconHeight()) {
				backgroundGraphics.drawImage(baseImageIcon.getImage(), x, y, this);
			}
		}

		backgroundImageIcon.setImage(backgroundImage);


		// Set the backgroundLabel size to the size of the desktop
		backgroundLabel.setSize(getSize());

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

	public void centerJIF(Component comp) {
		Dimension desktopSize = getSize();
		Dimension jInternalFrameSize = comp.getSize();
		int width = (desktopSize.width - jInternalFrameSize.width) / 2;
		int height = (desktopSize.height - jInternalFrameSize.height) / 2;
		comp.setLocation(width, height);
		comp.setVisible(true);
	}

	public void unitManagerUpdate(UnitManagerEvent event) {
		Object unit = event.getUnit();
		if (unit instanceof Settlement) {

			Settlement settlement = (Settlement) unit;
			UnitManagerEventType eventType = event.getEventType();

			if (eventType == UnitManagerEventType.ADD_UNIT) {
				settlement.addUnitListener(this);
			}

			else if (eventType == UnitManagerEventType.REMOVE_UNIT) {
				settlement.removeUnitListener(this);
			}

			SwingUtilities.invokeLater(() -> updateToolWindow());
		}
	}

	/**
	 * Sets up this class with two listeners
	 */
	public void prepareListeners() {
		// Attach UnitManagerListener to desktop
		UnitManager unitManager = sim.getUnitManager();
		unitManager.addUnitManagerListener(this);

		Collection<Settlement> settlements = unitManager.getSettlements();

		// Attach UnitListener to each settlement
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) {
			i.next().addUnitListener(this);
		}
	}

	/**
	 * Returns the MainWindow instance
	 *
	 * @return MainWindow instance
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	/*
	 * Creates tool windows
	 */
	private void prepareToolWindows() throws Exception {
		synchronized (toolWindows) {
		
		// Prepare Commander Window
//		if (GameManager.getGameMode() == GameMode.COMMAND) {
//			mode = GameMode.COMMAND;
			commanderWindow = new CommanderWindow(this);
			try {
				commanderWindow.setClosed(true);
			} catch (PropertyVetoException e) {
				logger.severe("Commander Window not ready: " + e.getMessage());
			}
			toolWindows.add(commanderWindow);
//			logger.config("toolWindows.add(commanderWindow)");
//		}

		// Prepare navigator window
		navWindow = new NavigatorWindow(this);
		try {
			navWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Navigator Window not ready: " + e.getMessage());
		}
		toolWindows.add(navWindow);
//		logger.config("toolWindows.add(navWindow)");

		// Prepare search tool window
		SearchWindow searchWindow = new SearchWindow(this);
		try {
			searchWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Search Window not ready: " + e.getMessage());
		}
		toolWindows.add(searchWindow);
//		logger.config("toolWindows.add(searchWindow)");

		// Prepare time tool window
		timeWindow = new TimeWindow(this);
		try {
			timeWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Time Window not ready: " + e.getMessage());
		}
		toolWindows.add(timeWindow);
//		logger.config("toolWindows.add(timeWindow)");

		// Prepare settlement tool window
		settlementWindow = new SettlementWindow(this);
		try {
			settlementWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Settlement Window not ready: " + e.getMessage());
		}
		toolWindows.add(settlementWindow);
		setSettlementWindow(settlementWindow);
//		logger.config("toolWindows.add(settlementWindow)");

		// Prepare science tool window
		ScienceWindow scienceWindow = new ScienceWindow(this);
		try {
			scienceWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Science Window not ready: " + e.getMessage());
		}
		toolWindows.add(scienceWindow);
//		logger.config("toolWindows.add(scienceWindow)");

		// Prepare guide tool window
		GuideWindow guideWindow = new GuideWindow(this);
		try {
			guideWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Guide Window not ready: " + e.getMessage());
		}
		toolWindows.add(guideWindow);
//		logger.config("toolWindows.add(guideWindow)");

		// Prepare monitor tool window
		MonitorWindow monitorWindow = new MonitorWindow(this);
		try {
			monitorWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Monitor Window not ready: " + e.getMessage());
		}
		toolWindows.add(monitorWindow);
//		logger.config("toolWindows.add(monitorWindow)");

		// Prepare mission tool window
		MissionWindow missionWindow = new MissionWindow(this);
		try {
			missionWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Mission Window not ready: " + e.getMessage());
		}
		toolWindows.add(missionWindow);
//		logger.config("toolWindows.add(missionWindow)");

		// Prepare resupply tool window
		ResupplyWindow resupplyWindow = new ResupplyWindow(this);
		try {
			resupplyWindow.setClosed(true);
		} catch (PropertyVetoException e) {
			logger.severe("Resupply Window not ready: " + e.getMessage());
		}
		toolWindows.add(resupplyWindow);
//		logger.config("toolWindows.add(resupplyWindow)");

		}
	}
	
	/*
	 * * Creates announcement windows & transportWizard
	 */
	private void prepareAnnouncementWindow() {
		// Prepare announcementWindow.
		announcementWindow = new AnnouncementWindow(this);
		try {
			announcementWindow.setClosed(true);
		} catch (java.beans.PropertyVetoException e) {
			logger.severe("Announcement Window not ready: " + e.getMessage());
		}
	}

	/**
	 * Returns a tool window for a given tool name
	 *
	 * @param toolName the name of the tool window
	 * @return the tool window
	 */
	public ToolWindow getToolWindow(String toolName) {
		synchronized (toolWindows) {
			for (ToolWindow w: toolWindows) {
				if (toolName.equals(w.getToolName()))
					return w;
			}
		}
		return null;
	}

	/**
	 * Displays a new Unit model in the monitor window.
	 *
	 * @param model the new model to display
	 */
	public void addModel(UnitTableModel model) {
		((MonitorWindow) getToolWindow(MonitorWindow.TITLE)).displayModel(model);
		openToolWindow(MonitorWindow.TITLE);
	}

	/**
	 * Centers the map and the globe on given coordinates. Also opens the map tool
	 * if it's closed.
	 *
	 * @param targetLocation the new center location
	 */
	public void centerMapGlobe(Coordinates targetLocation) {
		((NavigatorWindow) getToolWindow(NavigatorWindow.NAME)).updateCoords(targetLocation);
		openToolWindow(NavigatorWindow.NAME);
	}

	/**
	 * Return true if an unit window is open.
	 *
	 * @param unit window
	 * @return true true if the unit window is open
	 */
	public boolean isUnitWindowOpen(UnitWindow w) {
		if (w != null) {
			return !w.isClosed() || !w.isVisible();
		} else {
			return false;
		}
	}

	/**
	 * Return true if tool window is open.
	 *
	 * @param toolName the name of the tool window
	 * @return true true if tool window is open
	 */
	public boolean isToolWindowOpen(String toolName) {
		if (getToolWindow(toolName) != null)
			return !getToolWindow(toolName).isClosed();
		return false;
	}

	/**
	 * Opens a tool window if necessary.
	 *
	 * @param toolName the name of the tool window
	 * @param mission
	 */
	public void openToolWindow(String toolName, Mission mission) {
		ToolWindow window = getToolWindow(toolName);
		if (window != null) {
			if (window.isClosed()) {
				if (!window.wasOpened()) {
					UIConfig config = UIConfig.INSTANCE;
//					if (config.useUIDefault()) {
//						window.setLocation(getCenterLocation(window));
//					} else {
					if (config.isInternalWindowConfigured(toolName)) {
						window.setLocation(config.getInternalWindowLocation(toolName));
						if (window.isResizable()) {
							window.setSize(config.getInternalWindowDimension(toolName));
						}
					} else {
						if (toolName.equals(TimeWindow.NAME))
							window.setLocation(computeLocation(window, 0, 2));
						else if (toolName.equals(MonitorWindow.TITLE))
							window.setLocation(computeLocation(window, 1, 0));
						else
							window.setLocation(getCenterLocation(window));
					}
//					}
					window.setWasOpened(true);
				}

				// in case of classic swing mode for MainWindow
				add(window, 0);

				try {
					window.setClosed(false);
				} catch (Exception e) {
					logger.log(Level.SEVERE, e.toString());
				}
			}

			window.show();

			// bring to front if it overlaps with other windows
			try {
				window.setSelected(true);
			} catch (PropertyVetoException e) {
				// ignore if setSelected is vetoed
			}
		}

		window.getContentPane().validate();
		window.getContentPane().repaint();
		validate();
		repaint();

		if (toolName.equals(MissionWindow.NAME)) {
			((MissionWindow)window).selectSettlement(mission.getAssociatedSettlement());
			((MissionWindow)window).selectMission(mission);
		}
	}

	/**
	 * Opens a tool window if necessary.
	 *
	 * @param toolName the name of the tool window
	 */
	public void openToolWindow(String toolName) {
		ToolWindow window = getToolWindow(toolName);
		if (window != null) {
			if (window.isClosed()) {
				if (!window.wasOpened()) {
					UIConfig config = UIConfig.INSTANCE;
					Point location = null;
					if (config.isInternalWindowConfigured(toolName)) {
						location = config.getInternalWindowLocation(toolName);
						if (window.isResizable()) {
							window.setSize(config.getInternalWindowDimension(toolName));
						}
					}
					else if (toolName.equals(TimeWindow.NAME))
						location = computeLocation(window, 0, 2);
					else if (toolName.equals(MonitorWindow.TITLE))
						location = computeLocation(window, 1, 0);
					else
						location = getCenterLocation(window);

					// Check is visible
					if ((location.x < 0) || (location.y < 0)) {
						location = new Point(1,1);
					}
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
		}

		validate();
		repaint();
	}

	/**
	 * Closes a tool window if it is open
	 *
	 * @param toolName the name of the tool window
	 */
	public void closeToolWindow(String toolName) {
		SwingUtilities.invokeLater(() -> {
			ToolWindow window = getToolWindow(toolName);
			if ((window != null) && !window.isClosed()) {
				try {
					window.setClosed(true);
				} catch (java.beans.PropertyVetoException e) {
				}
			}
		});
	}

	/**
	 * Creates and opens a window for a unit if it isn't already in existence and
	 * open.
	 *
	 * @param unit          the unit the window is for.
	 * @param initialWindow true if window is opened at UI startup.
	 */
	public void openUnitWindow(Unit unit, boolean initialWindow) {
		openUnitWindow(unit, initialWindow, true);
	}

	/**
	 * Creates and opens a window for a unit if it isn't already in existence and
	 * open.
	 *
	 * @param unit          the unit the window is for.
	 * @param initialWindow true if window is opened at UI startup.
	 */
	public void openUnitWindow(Unit unit, boolean initialWindow, boolean toShow) {
		UnitWindow tempWindow = null;

		for (UnitWindow window : unitWindows) {
			if (window.getUnit() == unit) {
				tempWindow = window;
			}
		}

		if (tempWindow != null) {
			if (tempWindow.isClosed()) {
				add(tempWindow, 0);
			}
		}

		else {
			// Create new window for unit.
			tempWindow = UnitWindowFactory.getUnitWindow(unit, this);

			add(tempWindow, 0);
			tempWindow.pack();

			// Set internal frame listener
			tempWindow.addInternalFrameListener(new UnitWindowListener(this));

			if (initialWindow) {
				// Put window in configured position on desktop.
				tempWindow.setLocation(UIConfig.INSTANCE.getInternalWindowLocation(unit.getName()));
			} else {
				// Put window in random position on desktop.
				tempWindow.setLocation(getRandomLocation(tempWindow));
			}

			// Add unit window to unit windows
			unitWindows.add(tempWindow);

			// Create new unit button in tool bar if necessary
			if (mainWindow != null)
				mainWindow.createUnitButton(unit);
		}

		if (toShow) {
			tempWindow.setVisible(true);

			// Correct window becomes selected
			try {
				tempWindow.setSelected(true);
				tempWindow.moveToFront();
			} catch (java.beans.PropertyVetoException e) {
			}

			// Play sound
			String soundFilePath = UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getSound(unit);
			if (soundFilePath != null && soundFilePath.length() != 0) {
				soundPlayer.playSound(soundFilePath);
			}
		}
	}

	/**
	 * Finds an existing unit window for a unit.
	 *
	 * @param unit the unit to search for.
	 * @return existing unit window or null if none.
	 */
	public UnitWindow findUnitWindow(Unit unit) {
		UnitWindow result = null;

		for (UnitWindow window : unitWindows) {
			if (window.getUnit().equals(unit)) {
				result = window;
			}
		}
		return result;
	}

	/**
	 * Disposes a unit window and button.
	 *
	 * @param window the unit window to dispose.
	 */
	public void disposeUnitWindow(UnitWindow window) {

		if (window != null) {
			unitWindows.remove(window);
			window.dispose();

			// Have main window dispose of unit button
			if (mainWindow != null)
				mainWindow.disposeUnitButton(window.getUnit());
			
			// Lastly destory the window
			window.destroy();
		}
	}

	public void makeUnitWindowInvisible(UnitWindow window) {

		if (window != null) {
			window.setVisible(false);

			// Have main window dispose of unit button
			if (mainWindow != null)
				mainWindow.disposeUnitButton(window.getUnit());
		}
	}


	private void updateUnitWindows() {
		// Update all unit windows.
		if (!unitWindows.isEmpty()) {
			for (UnitWindow u : unitWindows) {
				if (u.isVisible() || u.isShowing())
					u.update();
			}
		}
	}

	private void updateToolWindows() {
		// Update all unit windows.
		if (!toolWindows.isEmpty()) {
			for (ToolWindow w : toolWindows) {
				if (w.isVisible() || w.isShowing())
					w.update();
			}
		}
	}

	/**
	 * Update the desktop and all of its windows.
	 */
	private void updateWindows() {
		// Update all unit windows.
		updateUnitWindows();
		// Update all tool windows.
		updateToolWindows();
//		runToolWindowExecutor();
	}


	/**
	 * Resets all windows on the desktop. Disposes of all unit windows and tool
	 * windows, and reconstructs the tool windows.
	 */
	public void resetDesktop() {
		// Prepare tool windows
		SwingUtilities.invokeLater(() -> {
			try {
				prepareToolWindows();
			} catch (Exception e) {
				logger.severe("Reseting desktop. Cannot prepare tool windows: " + e);
			}
		});

		logger.config(Msg.getString("MainDesktopPane.desktop.thread.running")); //$NON-NLS-1$

	}

	private Point getCenterLocation(JInternalFrame tempWindow) {

		Dimension desktop_size = getSize();
		Dimension window_size = tempWindow.getSize();

		int rX = (int) Math.round((desktop_size.width - window_size.width) / 2D);
		int rY = (int) Math.round((desktop_size.height - window_size.height - 100) / 2D);

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

		Dimension desktop_size = getSize();
		Dimension window_size = tempWindow.getSize();

		// Populate windows in grid=like starting position
		int w = desktop_size.width - window_size.width;
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
		Dimension desktop_size = getSize();
		Dimension window_size = f.getSize();

		// Populate windows in grid=like starting position
		int w = desktop_size.width - window_size.width;
		int h = desktop_size.height - window_size.height;
		int rX = 0;
		int rY = 0;

		if (positionX == 0)
			rX = 0;
		else if (positionX == 1)
			rX = w/2;
		else if (positionX == 2)
			rX = w;

		if (positionY == 0)
			rY = 0;
		else if (positionY == 1)
			rY = h/2;
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
	 * Opens a popup announcement window on the desktop.
	 *
	 * @param announcement the announcement text to display.
	 */
	public void openAnnouncementWindow(String announcement) {
		announcementWindow.setAnnouncement(announcement);
		announcementWindow.setSize(new Dimension(200, 100));
		announcementWindow.pack();
		add(announcementWindow, 0);
		int Xloc = (int) ((getWidth() - announcementWindow.getWidth()) * .5D);
		int Yloc = (int) ((getHeight() - announcementWindow.getHeight()) * .15D);
		announcementWindow.setLocation(Xloc, Yloc);
		// Note: second window packing seems necessary to get window
		// to display components correctly.
		announcementWindow.pack();
		announcementWindow.setVisible(true);
		validate();
		repaint();
	}

	/**
	 * Removes the popup announcement window from the desktop.
	 */
	public void disposeAnnouncementWindow() {
		announcementWindow.dispose();
	}

	/**
	 * Updates the look & feel of the announcement window.
	 */
	public void updateAnnouncementWindowLF() {
		if (announcementWindow != null) {
			announcementWindow.validate();
			announcementWindow.repaint();
		}
	}

	/**
	 * Updates the look & feel for all tool windows.
	 */
	public void updateToolWindowLF() {

		for (ToolWindow toolWindow : toolWindows) {
			toolWindow.update();
		}
	}

	public void updateUnitWindowLF() {

		for (UnitWindow window : unitWindows) {
			window.update();
		}
	}

	/**
	 * Closes the look & feel for all tool windows.
	 */
	public void closeAllToolWindow() {

		for (ToolWindow toolWindow : toolWindows) {
			remove(toolWindow);
		}
		disposeAnnouncementWindow();
	}

	/**
	 * Caches the creation of settlements for speeding up loading time
	 */
	public void cacheSettlementUnitWindow() {
		UnitManager unitManager = sim.getUnitManager();
		if (mode == GameMode.COMMAND)
			openUnitWindow(unitManager.getCommanderSettlement(), true, false);
		else {
			for (Settlement s : unitManager.getSettlements()) {
				openUnitWindow((Settlement) s, true, false);
			}
		}
	}

	/**
	 * Opens all initial windows based on UI configuration.
	 */
	public void openInitialWindows() {
		// Note 1: SwingUtilities.invokeLater(()) doesn't allow guide windows to be
		// centered for javaFX mode in Windows PC (but not in other platform)
		
		// Note 2: SwingUtilities.invokeLater allows sufficient time for all tool windows
		// to be created so that when calling openToolWindow, the tool windows
		// would be available by then
		SwingUtilities.invokeLater(() ->
			{
			if (mode == GameMode.COMMAND) {
				// Open the time window for the Commander Mode
				openToolWindow(TimeWindow.NAME);
				openToolWindow(CommanderWindow.NAME);
			}
	
			else {
				openToolWindow(GuideWindow.NAME);
			}
		});
	}

	/**
	 * creates a standardized empty border.
	 */
	public static EmptyBorder newEmptyBorder() {
		return new EmptyBorder(1, 1, 1, 1);
	}

	public void setSettlementWindow(SettlementWindow settlementWindow) {
		this.settlementWindow = settlementWindow;
	}

	public AnnouncementWindow getAnnouncementWindow() {
		return announcementWindow;
	}

	public SettlementWindow getSettlementWindow() {
		return settlementWindow;
	}

	public boolean getIsTransportingBuilding() {
		return isTransportingBuilding;
	}

	public boolean getIsConstructingSite() {
		return isConstructingSite;
	}

	/**
	 * Get a reference to the Simulation being displayed
	 * @return
	 */
	public Simulation getSimulation() {
		return sim;
	}

	@Override // @Override needed for Main window
	public void unitUpdate(UnitEvent event) {
		UnitEventType eventType = event.getType();

		if (eventType == UnitEventType.START_TRANSPORT_WIZARD_EVENT) {

		}

		else if (eventType == UnitEventType.END_TRANSPORT_WIZARD_EVENT) {
			isTransportingBuilding = false;
		}

		else if (eventType == UnitEventType.START_CONSTRUCTION_WIZARD_EVENT) {
			if (!isConstructingSite) {
				isConstructingSite = true;
			}
		}

		else if (eventType == UnitEventType.END_CONSTRUCTION_WIZARD_EVENT) {
			isConstructingSite = false;
		}

	}

	public TimeWindow getTimeWindow() {
		return timeWindow;
	}

	public CommanderWindow getCommanderWindow() {
		return commanderWindow;
	}

	public Collection<ToolWindow> getToolWindowsList() {
		return toolWindows;
	}

	public SettlementMapPanel getSettlementMapPanel() {
		return settlementWindow.getMapPanel();
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (!mainWindow.isIconified()) {
			updateWindows();
		}
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
	}

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		sim.getMasterClock().removeClockListener(this);
		
		logger = null;
		mode = null;
		if (unitWindows != null) {
			for (UnitWindow u : unitWindows) {
				u.destroy();
				u = null;
			}
			unitWindows = null;
		}
		if (toolWindows != null) {
			for (ToolWindow w : toolWindows) {
				w.destroy();
				w = null;
			}
			toolWindows = null;
		}
		backgroundImageIcon = null;
		backgroundLabel = null;
		soundPlayer = null;
		announcementWindow = null;
		settlementWindow = null;
		timeWindow = null;
		commanderWindow = null;
		mainWindow = null;
	}

}
