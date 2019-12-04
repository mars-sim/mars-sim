/**
 * Mars Simulation Project
 * MainDesktopPane.java
 * @version 3.1.0 2017-02-03
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.mars.sim.console.InteractiveTerm;
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
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.tool.commander.CommanderWindow;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.EventTableModel;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.UnitTableModel;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;
import org.mars_sim.msp.ui.swing.toolWindow.ToolWindow;
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
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());

//	private static final double PERIOD_IN_MILLISOLS = 10D * 500D / MarsClock.SECONDS_PER_MILLISOL;// 750D / MarsClock.SECONDS_IN_MILLISOL;
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

	private ToolWindowTask toolWindowTask;

	private transient ExecutorService toolWindowExecutor;
//	private transient ExecutorService unitWindowExecutor;

	private List<ToolWindowTask> toolWindowTaskList = new ArrayList<>();

	/** The sound player. */
	private static AudioPlayer soundPlayer;

	/** The desktop popup announcement window. */
	private AnnouncementWindow announcementWindow;
	private SettlementWindow settlementWindow;
	private NavigatorWindow navWindow;
	private TimeWindow timeWindow;
	private CommanderWindow commanderWindow;

//	private Building building;
	private MainWindow mainWindow;
	private OrbitViewer orbitViewer;
	private EventTableModel eventTableModel;

	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock = sim.getMasterClock();
	private static UnitManager unitManager = sim.getUnitManager();

	/**
	 * Constructor 1.
	 * 
	 * @param mainWindow the main outer window
	 */
	public MainDesktopPane(MainWindow mainWindow) {
		super();

		this.mainWindow = mainWindow;
		
		// Initialize data members
		soundPlayer = new AudioPlayer(this);
		// Play music
		if (!soundPlayer.isSoundDisabled())
			soundPlayer.playRandomMusicTrack();
		// Prepare unit windows.
		unitWindows = new CopyOnWriteArrayList<UnitWindow>();
		// Add clock listener
		sim.getMasterClock().addClockListener(this);
		// Prepare tool windows.
		toolWindows = new CopyOnWriteArrayList<ToolWindow>();
		
		prepareListeners();
		
		SwingUtilities.invokeLater(() -> init());
	}		
		
	public void init() {
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
		// Set background paper size
		setPreferredSize(new Dimension(InteractiveTerm.getWidth(), InteractiveTerm.getHeight()- 35));
		// Prep listeners
		prepareListeners();
		// Instantiate BrowserJFX
//		browserJFX = new BrowserJFX(this);
		// Create update thread.
		setupToolWindowTasks();
		// Prep tool windows
		prepareToolWindows();
		// Setup announcement window
		prepareAnnouncementWindow();
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
		// If displayed for the first time, create background image tile.
		// The size of the background tile cannot be determined during construction
		// since it requires the MainDesktopPane be displayed first.
		if (firstDisplay) {
			ImageIcon baseImageIcon = ImageLoader.getIcon(Msg.getString("img.background")); //$NON-NLS-1$
			Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
			Image backgroundImage = createImage((int) screen_size.getWidth(), (int) screen_size.getHeight());
			Graphics backgroundGraphics = backgroundImage.getGraphics();

			for (int x = 0; x < backgroundImage.getWidth(this); x += baseImageIcon.getIconWidth()) {
				for (int y = 0; y < backgroundImage.getHeight(this); y += baseImageIcon.getIconHeight()) {
					backgroundGraphics.drawImage(baseImageIcon.getImage(), x, y, this);
				}
			}

			backgroundImageIcon.setImage(backgroundImage);

			backgroundLabel.setSize(getSize());

			firstDisplay = false;
		}

		// Set the backgroundLabel size to the size of the desktop
		backgroundLabel.setSize(getSize());

	}

	// Additional Component Listener methods implemented but not used.
	@Override
	public void componentMoved(ComponentEvent e) {
//		logger.config("componentMoved()");
		SwingUtilities.invokeLater(() -> updateToolWindow());
	}

	@Override
	public void componentShown(ComponentEvent e) {
//		logger.config("componentShown()");
		SwingUtilities.invokeLater(() -> {
			JInternalFrame[] frames = (JInternalFrame[]) this.getAllFrames();
			for (JInternalFrame f : frames) {
				ToolWindow w = (ToolWindow) f;
				if (this.isVisible() || this.isShowing()) {
					w.update();
//					f.updateUI();
//					 SwingUtilities.updateComponentTreeUI(f);
//					f.validate();
//					f.repaint();
				}
				
				else if (!this.isShowing() && w.getToolName().equals(NavigatorWindow.NAME))
					closeToolWindow(NavigatorWindow.NAME);
			}
		});
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	public void updateToolWindow() {
//		logger.config("updateToolWindow()");
		JInternalFrame[] frames = (JInternalFrame[]) this.getAllFrames();
		for (JInternalFrame f : frames) {
			f.updateUI();
		}
	}

//	public void updateWebToolWindow() {
////		logger.config("updateToolWindow()");
//		JInternalFrame[] frames = (JInternalFrame[]) this.getAllFrames();
//		for (JInternalFrame f : frames) {
//			f.updateUI();
//		}
//	}

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

//		if (event.getUnit() instanceof Settlement) {		  
		// removeAllElements();
//		UnitManager unitManager =
//		sim.getUnitManager(); 
//		List<Settlement> settlements = new ArrayList<Settlement>(unitManager.getSettlements());
//		Collections.sort(settlements);
//		  
//		Iterator<Settlement> i = settlements.iterator(); 
//		while (i.hasNext()) {
//			i.next().removeUnitListener(this);
//		} 
//		Iterator<Settlement> j = settlements.iterator(); 
//		while (j.hasNext()) {
//			j.next().addUnitListener(this);
//		}}

		Object unit = event.getUnit();
		if (unit instanceof Settlement) {

			Settlement settlement = (Settlement) unit;
			UnitManagerEventType eventType = event.getEventType();

			if (eventType == UnitManagerEventType.ADD_UNIT) { // REMOVE_UNIT;
				settlement.addUnitListener(this);
			}

			else if (eventType == UnitManagerEventType.REMOVE_UNIT) { // REMOVE_UNIT;
				settlement.removeUnitListener(this);
			}

			SwingUtilities.invokeLater(() -> updateToolWindow());
		}
	}

	/**
	 * Sets up this class with two listeners
	 */
	public void prepareListeners() {
		// logger.config("MainDesktopPane's prepareListeners() is on " +
		// Thread.currentThread().getName() + " Thread");

		// Attach UnitManagerListener to desktop
		unitManager = sim.getUnitManager();
		unitManager.addUnitManagerListener(this);

		// Add addUnitListener()
		Collection<Settlement> settlements = unitManager.getSettlements();

//		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
//		Settlement settlement = settlementList.get(0);
//		List<Building> buildings = settlement.getBuildingManager().getACopyOfBuildings();
//		building = buildings.get(0);
		// building.addUnitListener(this); // not working

		// Attach UnitListener to each settlement
		Iterator<Settlement> i = settlements.iterator();
		while (i.hasNext()) {
			i.next().addUnitListener(this);
		}

		// logger.config("MainDesktopPane's prepareListeners() is done");
	}

	/**
	 * Returns the MainWindow instance
	 * 
	 * @return MainWindow instance
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

//	/**
//	 * Returns the MainScene instance
//	 * 
//	 * @return MainScene instance
//	 */
//	public MainScene getMainScene() {
//		return mainScene;
//	}

	/*
	 * Creates tool windows
	 */
	private void prepareToolWindows() {
		if (toolWindows != null)
			toolWindows.clear();

		// Prepare Commander Window
		if (GameManager.mode == GameMode.COMMAND) {
			mode = GameMode.COMMAND;
			commanderWindow = new CommanderWindow(this);
			try {
				commanderWindow.setClosed(true);
			} catch (PropertyVetoException e) {
			}
			toolWindows.add(commanderWindow);
		}

		// Prepare navigator window
		navWindow = new NavigatorWindow(this);
		try {
			navWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(navWindow);

//		logger.config("toolWindows.add(navWindow)");

		// Prepare search tool window
		SearchWindow searchWindow = new SearchWindow(this);
		try {
			searchWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(searchWindow);

//		logger.config("toolWindows.add(searchWindow)");

		// Prepare time tool window
		timeWindow = new TimeWindow(this);
		try {
			timeWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(timeWindow);

//		logger.config("Done with TimeWindow()");

		// Prepare settlement tool window
		settlementWindow = new SettlementWindow(this);
		try {
			settlementWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(settlementWindow);
		setSettlementWindow(settlementWindow);

//		logger.config("Done with setSettlementWindow()");

		// Prepare science tool window
		ScienceWindow scienceWindow = new ScienceWindow(this);
		try {
			scienceWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(scienceWindow);

//		logger.config("Done with ScienceWindow()");

		// Prepare guide tool window
		GuideWindow guideWindow = new GuideWindow(this);
		try {
			guideWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(guideWindow);

//		logger.config("Done with GuideWindow()");

		// Prepare monitor tool window
		MonitorWindow monitorWindow = new MonitorWindow(this);
		try {
			monitorWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(monitorWindow);

		// Prepare mission tool window
		MissionWindow missionWindow = new MissionWindow(this);
		try {
			missionWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(missionWindow);

		// Prepare resupply tool window
		ResupplyWindow resupplyWindow = new ResupplyWindow(this);
		try {
			resupplyWindow.setClosed(true);
		} catch (PropertyVetoException e) {
		}
		toolWindows.add(resupplyWindow);

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
		}

	}

	/**
	 * Returns a tool window for a given tool name
	 * 
	 * @param toolName the name of the tool window
	 * @return the tool window
	 */
	public ToolWindow getToolWindow(String toolName) {

		return toolWindows.stream().filter(i -> toolName.equals(i.getToolName())).findAny().orElse(null);

	}

	/**
	 * Displays a new Unit model in the monitor window.
	 * 
	 * @param model the new model to display
	 */
	public void addModel(UnitTableModel model) {
		((MonitorWindow) getToolWindow(MonitorWindow.NAME)).displayModel(model);
		openToolWindow(MonitorWindow.NAME);
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
	 */
	public void openToolWindow(String toolName) {
		// logger.config("openToolWindow() is on " + Thread.currentThread().getName());
		// either on JavaFX Application Thread or on AWT-EventQueue-0 Thread
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
						// System.out.println("MainDesktopPane: TimeWindow opens at whatever location");
						if (toolName.equals(TimeWindow.NAME))
							window.setLocation(getStartingLocation(window));
						else if (toolName.equals(MonitorWindow.NAME))
							window.setLocation(new Point(25, 0));
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

//			try {
//				tempWindow.setIcon(false);
//			} catch (PropertyVetoException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
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
				tempWindow.setLocation(0, 0);//getRandomLocation(tempWindow)); 
			}

			// Add unit window to unit windows
			unitWindows.add(tempWindow);
//			System.out.println(unitWindows : "  + unitWindows.size() + " - " + tempWindow.getName() );

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
		if (soundFilePath != null && soundFilePath.length() != 0) {
			soundPlayer.playSound(soundFilePath);
		}

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
			if (window.getUnit() == unit) {
				result = window;
			}
		}
		return result;
	}

//	/**
//	 * Disposes a unit window and button.
//	 *
//	 * @param unit the unit the window is for.
//	 */
//	public void disposeUnitWindow(Unit unit) {
//
//		// Dispose unit window
//		UnitWindow deadWindow = null;
//
//		for (UnitWindow window : unitWindows) {
//			if (unit == window.getUnit()) {
//				deadWindow = window;
//			}
//		}
//
//		unitWindows.remove(deadWindow);
//
//		if (deadWindow != null) {
//			deadWindow.dispose();
//		}
//
//		// Have main window dispose of unit button
//		if (mainWindow != null)
//			mainWindow.disposeUnitButton(unit);
//	}

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
		}
	}

	public void makeUnitWindowInvisible(UnitWindow window) {

		if (window != null) {
//			unitWindows.remove(window);
			window.setVisible(false);

			// Have main window dispose of unit button
			if (mainWindow != null)
				mainWindow.disposeUnitButton(window.getUnit());
		}
	}

//	class UnitWindowTask implements Runnable {
//		// long SLEEP_TIME = 1000;
//		UnitWindow unitWindow;
//
//		private UnitWindowTask(UnitWindow unitWindow) {
//			this.unitWindow = unitWindow;
//		}
//
//		@Override
//		public void run() {
////			 SwingUtilities.invokeLater(() -> {
////			if (unitWindow.isVisible() && unitWindow.isShowing())
//				unitWindow.update();
////			 });
//		}
//	}

//	private void setupUnitWindowExecutor() {
//		// set up unitWindowExecutor
//		unitWindowExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
//		
////		toolWindows.forEach(t -> {
////			toolWindowTask = new ToolWindowTask(t);
////			toolWindowTaskList.add(toolWindowTask);
////		});
//		
////		unitWindows.forEach(u -> {
////			unitWindowsTask = new UnitWindowTask(u);
////			unitWindows.add(unitWindowsTask);
////		});
//	}

	private void runUnitWindowExecutor() {
//		System.out.println("unitWindows : " + unitWindows.size());
		// set up unitWindowExecutor
//		if (unitWindowExecutor == null)
//			setupUnitWindowExecutor();

		// Update all unit windows.
		if (!unitWindows.isEmpty()) {
			for (UnitWindow u : unitWindows) {
				if (u.isVisible() && u.isShowing())
					u.update();
			}
//			unitWindows.forEach(u -> {
////				System.out.println(u.getName());
//				if (u.isVisible())// && u.isShowing()) { // isUnitWindowOpen(u) &&
//					u.update();
//			});
		}

//			unitWindows.forEach(u -> {
//				if (isUnitWindowOpen(u))
//					if (!unitWindowExecutor.isTerminated() || !unitWindowExecutor.isShutdown()) {	
//						unitWindowExecutor.execute(new UnitWindowTask(u));
//					}
//			});
//		}
	}

	class ToolWindowTask implements Runnable {
		// long SLEEP_TIME = 450;
		ToolWindow toolWindow;

		protected ToolWindow getToolWindow() {
			return toolWindow;
		}

		private ToolWindowTask(ToolWindow toolWindow) {
			this.toolWindow = toolWindow;
		}

		@Override
		public void run() {
			// SwingUtilities.invokeLater(() -> {
			if (toolWindow.isVisible() && toolWindow.isShowing())
				toolWindow.update();
			// });
		}
	}

	private void setupToolWindowTasks() {
		toolWindowTaskList = new ArrayList<>();
		toolWindows.forEach(t -> {
			toolWindowTask = new ToolWindowTask(t);
			toolWindowTaskList.add(toolWindowTask);
		});
	}

	private void setupToolWindowExecutor() {
		// set up toolWindowExecutor even though it is not used right now inside this
		// method
		toolWindowExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); // newCachedThreadPool();
	}

	private void runToolWindowExecutor() {

		if (toolWindowExecutor == null) {
			setupToolWindowExecutor();
		}

		if (toolWindowTaskList.isEmpty()) {
			setupToolWindowTasks();
		}

		else {
			toolWindowTaskList.forEach(t -> {
				// if a tool window is opened, run its executor
				if (isToolWindowOpen(t.getToolWindow().getToolName()))
					if (!toolWindowExecutor.isTerminated() || !toolWindowExecutor.isShutdown())
						toolWindowExecutor.execute(t);
			});
		}
	}

	/**
	 * Update the desktop and all of its windows.
	 */
	private void updateWindows() {
		// long SLEEP_TIME = 450;
//		System.out.println("updateWindows");
		// Update all unit windows.
		runUnitWindowExecutor();
		// Update all tool windows.
		runToolWindowExecutor();

	}

	public void clearDesktop() {

		logger.config(Msg.getString("MainDesktopPane.desktop.thread.shutdown")); //$NON-NLS-1$

		if (toolWindowExecutor != null && !toolWindowExecutor.isShutdown())
			toolWindowExecutor.shutdown();
//		if (unitWindowExecutor != null)
//			if (!unitWindowExecutor.isShutdown())
//				unitWindowExecutor.shutdown();
		// logger.config(Msg.getString("MainDesktopPane.desktop.thread.shutdown"));
		// //$NON-NLS-1$
		toolWindowTaskList.clear();

		for (UnitWindow window : unitWindows) {
			window.dispose();
			if (mainWindow != null)
				mainWindow.disposeUnitButton(window.getUnit());
			window.destroy();
		}
		unitWindows.clear();

		for (ToolWindow window : toolWindows) {
			window.dispose();
			window.destroy();
		}
		toolWindows.clear();
	}

	/**
	 * Resets all windows on the desktop. Disposes of all unit windows and tool
	 * windows, and reconstructs the tool windows.
	 */
	public void resetDesktop() {

		// Prepare tool windows
		prepareToolWindows();

		if (!toolWindowExecutor.isShutdown())
			toolWindowExecutor.shutdown();
//		if (unitWindowExecutor != null)
//			if (!unitWindowExecutor.isShutdown())
//				unitWindowExecutor.shutdown();

		// Restart update threads.
		setupToolWindowTasks();
		// updateThread.setRun(true);
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
		// (int) Math.round(Math.random() *
		// );

		int rY = RandomUtil.getRandomInt(5) * 20;
		// (desktop_size.height - window_size.height));

		return new Point(rX, rY);
	}

	/**
	 * Gets the starting location on the desktop for a given {@link JInternalFrame}.
	 * 
	 * @return a specific point on the desktop
	 */
	private Point getStartingLocation(JInternalFrame f) {
		Dimension desktop_size = getSize();
		Dimension window_size = f.getSize();

		// Populate windows in grid=like starting position
		int w = desktop_size.width - window_size.width;
		int h = desktop_size.height - window_size.height;
		int rX = w;
		int rY = h;
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

//	public static void disableSound() {
//		soundPlayer.disableSound();
//	}

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

			// Note : Call updateComponentTreeUI() below is must-have or else Monitor Tool
			// won't work
			// SwingUtilities.updateComponentTreeUI(toolWindow); // does Weblaf throw
			// Exception in thread "AWT-EventQueue-0" com.alee.managers.style.StyleException
			// ?
		}
	}

	public void updateUnitWindowLF() {

		for (UnitWindow window : unitWindows) {
			window.update();
			// });
			// SwingUtilities.updateComponentTreeUI(window);
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
		
//		UIConfig config = UIConfig.INSTANCE;
//		if (config.useUIDefault()) {
		
			// Note: SwingUtilities.invokeLater(()) doesn't allow guide windows to be
			// centered for javaFX mode in Windows PC (but not in other platform)
		
//		GuideWindow ourGuide = (GuideWindow) getToolWindow(GuideWindow.NAME);
		
		openToolWindow(GuideWindow.NAME);
		((GuideWindow) getToolWindow(GuideWindow.NAME)).setURL(Msg.getString("doc.guide")); //$NON-NLS-1$

		if (mode == GameMode.COMMAND) {
			// Open the time window for the Commander Mode
			openToolWindow(TimeWindow.NAME);
			openToolWindow(CommanderWindow.NAME);
		}
		
//		doneLoading = true;
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

//	public void setMarqueeTicker(MarqueeTicker marqueeTicker) {
//		this.marqueeTicker = marqueeTicker;
//	}

//	public MarqueeTicker getMarqueeTicker() {
//		return marqueeTicker;
//	}

	@Override // @Override needed for Main window
	public void unitUpdate(UnitEvent event) {
		UnitEventType eventType = event.getType();

//		Object target = event.getTarget();
		if (eventType == UnitEventType.START_TRANSPORT_WIZARD_EVENT) {

//			building = (Building) target; // overwrite the dummy building object made by the constructor
//			BuildingManager mgr = building.getBuildingManager();

//			if (!isTransportingBuilding) {
//				isTransportingBuilding = true;
//				if (mainWindow != null)
//					mainWindow.openTransportWizard(mgr);
//				// sim.getTransportManager().setIsTransportingBuilding(false);
//			}

		}

		else if (eventType == UnitEventType.END_TRANSPORT_WIZARD_EVENT) {
			isTransportingBuilding = false;
			// disposeAnnouncementWindow();
		}

		else if (eventType == UnitEventType.START_CONSTRUCTION_WIZARD_EVENT) {
//			BuildingConstructionMission mission = (BuildingConstructionMission) target;
//
			if (!isConstructingSite) {
				isConstructingSite = true;

//				if (mainWindow != null) {
//					mainWindow.openConstructionWizard(mission);
//				}
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

	public boolean isOrbitViewerOn() {
		if (orbitViewer == null)
			return false;
		else
			return true;
	}

	public void setOrbitViewer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}

//	public BrowserJFX getBrowserJFX() {
//		return browserJFX;
//	}

	public void setEventTableModel(EventTableModel eventTableModel) {
		this.eventTableModel = eventTableModel;
	}

	public EventTableModel getEventTableModel() {
		return eventTableModel;
	}

	public void changeTitle(boolean isPaused) {
		if (mode == GameMode.COMMAND) {
			if (isPaused) {
				mainWindow.getFrame().setTitle(Simulation.title + "  -  Command Mode" + "  -  [ P A U S E ]");
			} else {
				mainWindow.getFrame().setTitle(Simulation.title + "  -  Command Mode");
			}
		} else {
			if (isPaused) {
				mainWindow.getFrame().setTitle(Simulation.title + "  -  Sandbox Mode" + "  -  [ P A U S E ]");
			} else {
				mainWindow.getFrame().setTitle(Simulation.title + "  -  Sandbox Mode");
			}
		}
	}

	@Override
	public void clockPulse(double time) {
		if (time > 0) {
			// Increments the Earth and Mars clock labels.
			mainWindow.incrementClocks();
		}
	}

	@Override
	public void uiPulse(double time) {
//		SwingUtilities.invokeLater(() -> super.updateUI());
		if (time > 0) {
			updateWindows();
		}
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		changeTitle(isPaused);
	}

	public boolean isEmpty() {
		if (super.getAllFrames().length == 0)
			return true;
		else
			return false;

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
		toolWindowTask = null;
		toolWindowExecutor = null;
//		unitWindowExecutor = null;
		toolWindowTaskList = null;
		soundPlayer = null;
		announcementWindow = null;
		settlementWindow = null;
		timeWindow = null;
		commanderWindow = null;
//		building = null;
		mainWindow = null;
//		marqueeTicker = null;
		orbitViewer = null;
//		browserJFX = null;
		eventTableModel = null;
	}

}