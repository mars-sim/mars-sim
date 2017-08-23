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
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.SingleSelectionModel;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.UnitEvent;
import org.mars_sim.msp.core.UnitEventType;
import org.mars_sim.msp.core.UnitListener;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.UnitManagerEvent;
import org.mars_sim.msp.core.UnitManagerEventType;
import org.mars_sim.msp.core.UnitManagerListener;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.javafx.BrowserJFX;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;
import org.mars_sim.msp.ui.swing.tool.MarqueeTicker;
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
 * The MainDesktopPane class is the desktop part of the project's UI.
 * It contains all tool and unit windows, and is itself contained,
 * along with the tool bars, by the main window.
 */
public class MainDesktopPane
extends JDesktopPane
implements ClockListener, ComponentListener, UnitListener, UnitManagerListener {

	/** default serial id. */
	private static final long serialVersionUID = 1L;
	/** default logger. */
	private static Logger logger = Logger.getLogger(MainDesktopPane.class.getName());

	private static final double PERIOD_IN_MILLISOLS = 750D / MarsClock.SECONDS_IN_MILLISOL;

	// Data members
	private double timeCache = 0;
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
	private ThreadPoolExecutor toolWindowExecutor;
	private ThreadPoolExecutor unitWindowExecutor;

	private List<ToolWindowTask> toolWindowTaskList = new ArrayList<>();

	/** The sound player. */
	private AudioPlayer soundPlayer;
	/** The desktop popup announcement window. */
	private AnnouncementWindow announcementWindow;
	private SettlementWindow settlementWindow;
	private NavigatorWindow navWindow;
	private TimeWindow timeWindow;
	private Building building;
	private MainWindow mainWindow;
	private MainScene mainScene;
	private MarqueeTicker marqueeTicker;
	private OrbitViewer orbitViewer;
	private BrowserJFX browserJFX;
	private EventTableModel eventTableModel;
	private SingleSelectionModel ssm;
	//private final ReentrantLock transportLock = new ReentrantLock();
    //private int transportCount = 0;
	//private final ReentrantLock constructionLock = new ReentrantLock();
    //private int constructionCount = 0;

	/**
	 * Constructor 1.
	 * @param mainWindow the main outer window
	 */
	public MainDesktopPane(MainWindow mainWindow) {
		super();
	   	//logger.info("MainDesktopPane's constructor is on " + Thread.currentThread().getName() + " Thread");
		this.mainWindow = mainWindow;

		init();
	}

	/**
	 * Constructor 2.
	 * @param mainScene the main scene
	 */
	public MainDesktopPane(MainScene mainScene) {
		//super(mainScene);
	   	//logger.info("MainDesktopPane's constructor is on " + Thread.currentThread().getName() + " Thread");
		this.mainScene = mainScene;

		init();
	}

	// 2015-02-04 Added init()
	public void init() {
	   	//logger.info("init() is on " + Thread.currentThread().getName() + " Thread");

		// Set background color to black
		setBackground(Color.black);

		// set desktop manager
		setDesktopManager(new MainDesktopManager());

		// Set component listener
		addComponentListener(this);

		// Create background label and set it to the back layer
		backgroundImageIcon = new ImageIcon();
		backgroundLabel = new JLabel(backgroundImageIcon);
		add(backgroundLabel, Integer.MIN_VALUE);
		backgroundLabel.setLocation(0, 0);
		moveToBack(backgroundLabel);

		// Initialize firstDisplay to true
		firstDisplay = true;

		if (mainScene != null)
			setPreferredSize(new Dimension(MainScene.DEFAULT_WIDTH, MainScene.DEFAULT_HEIGHT));
		else
			setPreferredSize(new Dimension(1280, 768));

		prepareListeners();

		// Initialize data members
		soundPlayer = new AudioPlayer(this);
		soundPlayer.playInBackground(SoundConstants.SOUNDS_ROOT_PATH + SoundConstants.SND_MUSIC1); // play our intro music

		// Prepare tool windows.
		toolWindows = new ArrayList<ToolWindow>();

		browserJFX = new BrowserJFX(this);

		prepareToolWindows();
		// Prepare unit windows.
		unitWindows = new ArrayList<UnitWindow>();
		// Create update thread.
		setupToolWindowTasks();

		Simulation.instance().getMasterClock().addClockListener(this);

		if (mainScene == null)
			prepareAnnouncementWindow();

	   	//logger.info("MainDesktopPane's init() is done ");
	}

/*
	public BufferedImage loadPartBufferedImage() {

		//Read Image from File
		//File myJPegFile=new File("ImageAsJPeg.j2k");
		//BufferedImage image = ImageIO.read(myJPegFile);

		Rectangle sourceRegion = new Rectangle(0, 0, 512, 512); // The region you want to extract
		BufferedImage image = null;
		ImageInputStream stream = null;
		try {
			stream = ImageIO.createImageInputStream(new File("..."));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} // File or input stream
		final Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);

		if (readers.hasNext()) {
			ImageReader reader = (ImageReader)readers.next();

			reader.setInput(stream, true, true);

			try {
				if ( reader.isImageTiled(0) == false)
					System.out.println("is not tiled");
				else
					System.out.println("is tiled");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			ImageReadParam param = reader.getDefaultReadParam();
			param.setSourceRegion(sourceRegion); // Set region


			try {
				image = reader.read(0, param);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Will read only the region specified
		}
		return image;
	}
*/



	/**
	 * Create background tile when MainDesktopPane is first
	 * displayed. Recenter logoLabel on MainWindow and set
	 * backgroundLabel to the size of MainDesktopPane.
	 * @param e the component event
	 */
	@Override
	public void componentResized(ComponentEvent e) {

		// If displayed for the first time, create background image tile.
		// The size of the background tile cannot be determined during construction
		// since it requires the MainDesktopPane be displayed first.
		if (firstDisplay) {
			ImageIcon baseImageIcon = ImageLoader.getIcon(Msg.getString("img.background")); //$NON-NLS-1$
			Dimension screen_size =
				Toolkit.getDefaultToolkit().getScreenSize();
			Image backgroundImage =
				createImage((int) screen_size.getWidth(),
						(int) screen_size.getHeight());
			Graphics backgroundGraphics = backgroundImage.getGraphics();

			for (int x = 0; x < backgroundImage.getWidth(this);
			x += baseImageIcon.getIconWidth()) {
				for (int y = 0; y < backgroundImage.getHeight(this);
				y += baseImageIcon.getIconHeight()) {
					backgroundGraphics.drawImage(
							baseImageIcon.getImage(), x, y, this);
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
		logger.info("DesktopPane : componentMoved()");
		updateToolWindow();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		logger.info("DesktopPane : componentShown()");
		JInternalFrame[] frames = this.getAllFrames();
		for (JInternalFrame f : frames) {
			//((ToolWindow)f).update();
			f.updateUI();
			SwingUtilities.updateComponentTreeUI(f);
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {}

	public void updateToolWindow() {
		logger.info("DesktopPane : updateToolWindow()");
		JInternalFrame[] frames = this.getAllFrames();
		for (JInternalFrame f : frames) {
			f.updateUI();
			//SwingUtilities.updateComponentTreeUI(f);
			//((ToolWindow)f).update(); //  java.lang.ClassCastException: org.mars_sim.msp.ui.swing.unit_window.structure.SettlementUnitWindow cannot be cast to org.mars_sim.msp.ui.swing.toolWindow.ToolWindow
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
/*
		if (event.getUnit() instanceof Settlement) {

			//removeAllElements();
			UnitManager unitManager = Simulation.instance().getUnitManager();
			List<Settlement> settlements = new ArrayList<Settlement>(unitManager.getSettlements());
			Collections.sort(settlements);

			Iterator<Settlement> i = settlements.iterator();
			while (i.hasNext()) {
				i.next().removeUnitListener(this);
			}
			Iterator<Settlement> j = settlements.iterator();
			while (j.hasNext()) {
				j.next().addUnitListener(this);
			}
		}
*/
		Object unit = event.getUnit();
		if (unit instanceof Settlement) {

			Settlement settlement = (Settlement) unit;
			UnitManagerEventType eventType = event.getEventType();

			if (eventType == UnitManagerEventType.ADD_UNIT) { // REMOVE_UNIT;
				//System.out.println("MainDesktopPane : " + settlement.getName() + " just added");
				settlement.addUnitListener(this);

				if (mainScene != null) {
					mainScene.changeSBox();
				}

			}
			else if (eventType == UnitManagerEventType.REMOVE_UNIT) { // REMOVE_UNIT;
				//System.out.println("MainDesktopPane : " + settlement.getName() + " just deleted");
				settlement.removeUnitListener(this);

				if (mainScene != null) {
					mainScene.changeSBox();
				}
			}

			updateToolWindow();
		}
	}

	/**
	 * sets up this class with two listeners
	 */
	// 2014-12-19 Added prepareListeners()
	public void prepareListeners() {
	   	//logger.info("MainDesktopPane's prepareListeners() is on " + Thread.currentThread().getName() + " Thread");

		// Add addUnitManagerListener()
		UnitManager unitManager = Simulation.instance().getUnitManager();
		unitManager.addUnitManagerListener(this);

		// Add addUnitListener()
		Collection<Settlement> settlements = unitManager.getSettlements();
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
		Settlement settlement = settlementList.get(0);
		List<Building> buildings = settlement.getBuildingManager().getACopyOfBuildings();
		building = buildings.get(0);
		//building.addUnitListener(this); // not working
		Iterator<Settlement> i = settlementList.iterator();
		while (i.hasNext()) {
			i.next().addUnitListener(this);
		}

	   	//logger.info("MainDesktopPane's prepareListeners() is done");
	}

	/** Returns the MainWindow instance
	 *  @return MainWindow instance
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	/** Returns the MainScene instance
	 *  @return MainScene instance
	 */
	public MainScene getMainScene() {
		return mainScene;
	}


	/**
	 * Create background tile when MainDesktopPane is first
	 * displayed. Recenter logoLabel on MainWindow and set
	 * backgroundLabel to the size of MainDesktopPane.
	 * @param e the component event

	@Override
	public void componentResized(ComponentEvent e) {

		// If displayed for the first time, create background image tile.
		// The size of the background tile cannot be determined during construction
		// since it requires the MainDesktopPane be displayed first.
		if (firstDisplay) {
			ImageIcon baseImageIcon = ImageLoader.getIcon(Msg.getString("img.background")); //$NON-NLS-1$
			Dimension screen_size =
				Toolkit.getDefaultToolkit().getScreenSize();
			Image backgroundImage =
				createImage((int) screen_size.getWidth(),
						(int) screen_size.getHeight());
			Graphics backgroundGraphics = backgroundImage.getGraphics();

			for (int x = 0; x < backgroundImage.getWidth(this);
			x += baseImageIcon.getIconWidth()) {
				for (int y = 0; y < backgroundImage.getHeight(this);
				y += baseImageIcon.getIconHeight()) {
					backgroundGraphics.drawImage(
							baseImageIcon.getImage(), x, y, this);
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
	public void componentMoved(ComponentEvent e) {}
	@Override
	public void componentShown(ComponentEvent e) {}
	@Override
	public void componentHidden(ComponentEvent e) {}
*/

	/*
	 * Creates tool windows
	 */
	private void prepareToolWindows() {
	   	//logger.info("MainDesktopPane's prepareToolWindows() is on " + Thread.currentThread().getName() + " Thread");

		if (toolWindows != null)
			toolWindows.clear();

	   	//logger.info("toolWindows.clear()");

		// Prepare navigator window
		navWindow = new NavigatorWindow(this);
		try { navWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(navWindow);

	   	//logger.info("toolWindows.add(navWindow)");

		// Prepare search tool window
		SearchWindow searchWindow = new SearchWindow(this);
		try { searchWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(searchWindow);

	   	//logger.info("toolWindows.add(searchWindow)");

		// Prepare time tool window
		timeWindow = new TimeWindow(this);
		try { timeWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(timeWindow);

	   	//logger.info("toolWindows.add(timeWindow)");

		// Prepare settlement tool window
		settlementWindow = new SettlementWindow(this);
		try { settlementWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(settlementWindow);
		setSettlementWindow(settlementWindow);

	   	//logger.info("toolWindows.add(settlementWindow)");

		// Prepare science tool window
		ScienceWindow scienceWindow = new ScienceWindow(this);
		try { scienceWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(scienceWindow);

	   	//logger.info("toolWindows.add(scienceWindow)");

		// Prepare guide tool window
		GuideWindow guideWindow = new GuideWindow(this);
		try { guideWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(guideWindow);

//		if (mainScene != null) {

			// Prepare monitor tool window
			MonitorWindow monitorWindow = new MonitorWindow(this);
			try { monitorWindow.setClosed(true); }
			catch (PropertyVetoException e) { }
			toolWindows.add(monitorWindow);

		   	//logger.info("toolWindows.add(monitorWindow)");

			// Prepare mission tool window
			MissionWindow missionWindow = new MissionWindow(this);
			try { missionWindow.setClosed(true); }
			catch (PropertyVetoException e) { }
			toolWindows.add(missionWindow);

		   	//logger.info("toolWindows.add(missionWindow)");

			// Prepare resupply tool window
			ResupplyWindow resupplyWindow = new ResupplyWindow(this);
			try { resupplyWindow.setClosed(true); }
			catch (PropertyVetoException e) { }
			toolWindows.add(resupplyWindow);

		   	//logger.info("toolWindows.add(resupplyWindow)");
/*
		}
		else {

			// Prepare monitor tool window
			MonitorWindow monitorWindow = new MonitorWindow(this);
			try { monitorWindow.setClosed(true); }
			catch (PropertyVetoException e) { }
			toolWindows.add(monitorWindow);

		   	//logger.info("toolWindows.add(monitorWindow)");

			// Prepare mission tool window
			MissionWindow missionWindow = new MissionWindow(this);
			try { missionWindow.setClosed(true); }
			catch (PropertyVetoException e) { }
			toolWindows.add(missionWindow);

		   	//logger.info("toolWindows.add(missionWindow)");

			// Prepare resupply tool window
			ResupplyWindow resupplyWindow = new ResupplyWindow(this);
			try { resupplyWindow.setClosed(true); }
			catch (PropertyVetoException e) { }
			toolWindows.add(resupplyWindow);

		   	//logger.info("toolWindows.add(resupplyWindow)");

		   	//logger.info("toolWindows.add(guideWindow)");
		}
*/

		// Prepare Mars Viewer window
		//MarsViewer marsViewer = new MarsViewer(this);
		//try { marsViewer.setClosed(true); }
		//catch (PropertyVetoException e) { }
		//toolWindows.add(marsViewer);

	   	//logger.info("MainDesktopPane's prepareToolWindows() is done ");
	}

	/*
	 * * Creates announcement windows & transportWizard
	 */
	private void prepareAnnouncementWindow() {
	   	//logger.info("MainDesktopPane's prepareWindows() is on " + Thread.currentThread().getName() + " Thread");
		// Prepare announcementWindow.
		announcementWindow = new AnnouncementWindow(this);
		try { announcementWindow.setClosed(true); }
		catch (java.beans.PropertyVetoException e) { }

	}

	/** Returns a tool window for a given tool name
	 *  @param toolName the name of the tool window
	 *  @return the tool window
	 */
	public ToolWindow getToolWindow(String toolName) {
/*
		ToolWindow result = null;
		Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
			ToolWindow window = i.next();
			if (toolName.equals(window.getToolName())) {
				result = window;
			}
		}
		return result;
*/
		return toolWindows
				.stream()
				.filter(i -> toolName.equals(i.getToolName()))
				.findAny()
				.orElse(null);

	}

	/**
	 * Displays a new Unit model in the monitor window.
	 * @param model the new model to display
	 */
	public void addModel(UnitTableModel model) {
		((MonitorWindow) getToolWindow(MonitorWindow.NAME)).displayModel(model);
		openToolWindow(MonitorWindow.NAME);
	}

	/**
	 * Centers the map and the globe on given coordinates.
	 * Also opens the map tool if it's closed.
	 * @param targetLocation the new center location
	 */
	public void centerMapGlobe(Coordinates targetLocation) {
		((NavigatorWindow) getToolWindow(NavigatorWindow.NAME)).
		updateCoords(targetLocation);
		openToolWindow(NavigatorWindow.NAME);
	}

	/**
	 * Return true if an unit window is open.
	 * @param unit window
	 * @return true true if the unit window is open
	 */
	public boolean isUnitWindowOpen(UnitWindow w) {
		if (w != null) {
			return !w.isClosed();
		} else {
			return false;
		}
	}

	/**
	 * Return true if tool window is open.
	 * @param toolName the name of the tool window
	 * @return true true if tool window is open
	 */
	public boolean isToolWindowOpen(String toolName) {
		ToolWindow window = getToolWindow(toolName);
		if (window != null) {
			return !window.isClosed();
		} else {
			return false;
		}
	}

	/**
	 * Opens a tool window if necessary.
	 * @param toolName the name of the tool window
	 */
	@SuppressWarnings("restriction")
	public void openToolWindow(String toolName) {
        //logger.info("openToolWindow() is on " + Thread.currentThread().getName());
		// either on JavaFX Application Thread or on AWT-EventQueue-0 Thread
		ToolWindow window = getToolWindow(toolName);
		if (window != null) {
			if (window.isClosed()) {
				if (!window.wasOpened()) {
					UIConfig config = UIConfig.INSTANCE;
					if (config.useUIDefault()) {
							window.setLocation(getCenterLocation(window));
					} else {
						if (config.isInternalWindowConfigured(toolName)) {
							window.setLocation(config.getInternalWindowLocation(toolName));
							if (window.isResizable()) {
								window.setSize(config.getInternalWindowDimension(toolName));
							}
						} else {
							//System.out.println("MainDesktopPane: TimeWindow opens at whatever location");
							if (toolName.equals(TimeWindow.NAME))
								window.setLocation(getStartingLocation(window));
							else
								window.setLocation(getRandomLocation(window));
						}
					}
					window.setWasOpened(true);
				}

				if (mainScene != null) {
					// 2016-10-22 These 2 tools are in the Main Tab
					if (toolName.equals(SearchWindow.NAME)
							|| toolName.equals(TimeWindow.NAME)
							|| toolName.equals(MonitorWindow.NAME)
							|| toolName.equals(MissionWindow.NAME)
							|| toolName.equals(ResupplyWindow.NAME)
							|| toolName.equals(ScienceWindow.NAME) ){

						add(window, 0);
					}
					//else if (toolName.equals(MonitorWindow.NAME)) {
					//	mainScene.getDesktops().get(0).add(window, 0);
					//}
					//else if (toolName.equals(MissionWindow.NAME)) {
					//	mainScene.getDesktops().get(1).add(window, 0);
					//}
					//else if (toolName.equals(SettlementWindow.NAME)) {
					//	mainScene.getDesktops().get(2).add(window, 0);
					//}
					//else if (toolName.equals(ResupplyWindow.NAME)) {
					//	mainScene.getDesktops().get(2).add(window, 0);
					//}
					//else if (toolName.equals(ScienceWindow.NAME)) {
					//	mainScene.getDesktops().get(4).add(window, 0);
					//}
					//else if (toolName.equals(GuideWindow.NAME)) {
					//	mainScene.getDesktops().get(5).add(window, 0);
					//}

				}
				else { // in case of classic swing mode for MainWindow
					add(window, 0);
				}

				try {
					window.setClosed(false);
				}
				catch (Exception e) { logger.log(Level.SEVERE,e.toString()); }
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

	    // 2015-12-07 Added below to check the corresponding menu item
		if (mainScene != null) {
			if (ssm == null)
				ssm = mainScene.getJFXTabPane().getSelectionModel();
			//System.out.println(toolName + " is running openToolWindow().");
			Platform.runLater(() -> {

				// 2016-10-22 Opening the first 3 tools will switch to the Desktop Tab
				if (toolName.equals(NavigatorWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAP_TAB))
							ssm.select(MainScene.MAP_TAB);
					mainScene.getMainSceneMenu().getMarsNavigatorItem().setSelected(true);
				}

				else if (toolName.equals(SettlementWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAP_TAB))
							ssm.select(MainScene.MAP_TAB);
					mainScene.getMainSceneMenu().getSettlementMapToolItem().setSelected(true);
				}

				else if (toolName.equals(SearchWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAIN_TAB))
							ssm.select(MainScene.MAIN_TAB);//.MAIN_TAB);
					mainScene.getMainSceneMenu().getSearchToolItem().setSelected(true);
				}

				else if (toolName.equals(TimeWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAIN_TAB))
							ssm.select(MainScene.MAIN_TAB);//.MAIN_TAB);
					mainScene.getMainSceneMenu().getTimeToolItem().setSelected(true);
				}

				else if (toolName.equals(MonitorWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAIN_TAB))
							ssm.select(MainScene.MAIN_TAB);//MONITOR_TAB);
					mainScene.getMainSceneMenu().getMonitorToolItem().setSelected(true);
				}

				else if (toolName.equals(MissionWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAIN_TAB))
							ssm.select(MainScene.MAIN_TAB);//.MISSION_TAB);
					mainScene.getMainSceneMenu().getMissionToolItem().setSelected(true);
				}

				else if (toolName.equals(ResupplyWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAIN_TAB))
							ssm.select(MainScene.MAIN_TAB);//.RESUPPLY_TAB);
					mainScene.getMainSceneMenu().getResupplyToolItem().setSelected(true);
				}

				else if (toolName.equals(ScienceWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.MAIN_TAB))
							ssm.select(MainScene.MAIN_TAB);//.SCIENCE_TAB);
					mainScene.getMainSceneMenu().getScienceToolItem().setSelected(true);
				}

				else if (toolName.equals(GuideWindow.NAME)) {
					if (mainScene.isMainSceneDone())
						if (!ssm.isSelected(MainScene.HELP_TAB))
							ssm.select(MainScene.HELP_TAB);
					mainScene.getMainSceneMenu().getHelpBrowserItem().setSelected(true);
				}
			});
		}
	}

	/**
	 * Recreates the Mars Navigator Tool
	 */
	public void recreateNavWin() {
		System.out.println("recreateNavWin()");//. toolWindows : " + toolWindows.size());
		SwingUtilities.invokeLater(() -> {
			ToolWindow window = getToolWindow(NavigatorWindow.NAME);
			if ((window != null) && !window.isClosed()) {
				//window.dispose();
				try { window.setClosed(true); }
				catch (java.beans.PropertyVetoException e) {}
			}

			window.dispose();
			toolWindows.remove(window);

			navWindow = new NavigatorWindow(this);
			try { navWindow.setClosed(true); }
			catch (PropertyVetoException e) { }

			toolWindows.add(navWindow);
			setupToolWindowTasks();
	        if (!toolWindowExecutor.isShutdown())
	        	toolWindowExecutor.shutdown();
			openToolWindow(NavigatorWindow.NAME);
	        //System.out.println("toolWindows : " + toolWindows.size());
		});
	}

	/**
	 * Closes a tool window if it is open
	 * @param toolName the name of the tool window
	 */
	@SuppressWarnings("restriction")
	public void closeToolWindow(String toolName) {
		SwingUtilities.invokeLater(() -> {
			ToolWindow window = getToolWindow(toolName);
			if ((window != null) && !window.isClosed()) {
				try { window.setClosed(true); }
				catch (java.beans.PropertyVetoException e) {}
			}

/*
		// 2015-10-01 Added Platform.runLater()
		if (mainScene != null) {
			//System.out.println(toolName + " is running closeToolWindow().");
			Platform.runLater(() -> {

				if (toolName.equals(NavigatorWindow.NAME)) {
					//mainScene.getMainSceneMenu().getMarsNavigatorItem().setSelected(false);
				}

				else if (toolName.equals(SearchWindow.NAME)) {
					mainScene.getMainSceneMenu().getSearchToolItem().setSelected(false);
				}

				else if (toolName.equals(SettlementWindow.NAME)) {
					mainScene.getMainSceneMenu().getSettlementMapToolItem().setSelected(false);
				}


				else if (toolName.equals(MonitorWindow.NAME)) {
					mainScene.getMainSceneMenu().getMonitorToolItem().setSelected(false);
				}

				else if (toolName.equals(MissionWindow.NAME)) {
					mainScene.getMainSceneMenu().getMissionToolItem().setSelected(false);
				}

				else if (toolName.equals(ScienceWindow.NAME)) {
					mainScene.getMainSceneMenu().getScienceToolItem().setSelected(false);
				}

				else if (toolName.equals(ResupplyWindow.NAME)) {
					mainScene.getMainSceneMenu().getResupplyToolItem().setSelected(false);
				}
			});
		}
*/
		});
	}

	public void refreshTheme() {
		Unit unit = Simulation.instance().getUnitManager().getFirstSettlement();
		openUnitWindow(unit, true);
		UnitWindow w = findUnitWindow(unit);
		disposeUnitWindow(w);
		logger.info("done with refreshTheme()");
	}

	/**
	 * Creates and opens a window for a unit if it isn't
	 * already in existence and open.
	 * @param unit the unit the window is for.
	 * @param initialWindow true if window is opened at UI startup.
	 */
	public void openUnitWindow(Unit unit, boolean initialWindow) {
		//logger.info("openUnitWindow is on " + Thread.currentThread().getName());

		UnitWindow tempWindow = null;

		Iterator<UnitWindow> i = unitWindows.iterator();
		while (i.hasNext()) {
			UnitWindow window = i.next();
			if (window.getUnit() == unit) {
				tempWindow = window;
			}
		}

		if (tempWindow != null) {
			if (tempWindow.isClosed()) {
				add(tempWindow, 0);
			}

			try {
				tempWindow.setIcon(false);
			} catch (java.beans.PropertyVetoException e) {
				logger.log(Level.SEVERE,Msg.getString("MainDesktopPane.log.problemReopening") + e); //$NON-NLS-1$
			}
		} else {
			// Create new window for unit.
			tempWindow = UnitWindowFactory.getUnitWindow(unit, this);

			add(tempWindow, 0);
			tempWindow.pack();

			// Set internal frame listener
			tempWindow.addInternalFrameListener(new UnitWindowListener(this));

			if (initialWindow) {
				// Put window in configured position on desktop.
				tempWindow.setLocation(UIConfig.INSTANCE.getInternalWindowLocation(unit.getName()));
			}
			else {
				// Put window in random position on desktop.
				tempWindow.setLocation(getRandomLocation(tempWindow));
			}

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
		} catch (java.beans.PropertyVetoException e) {}


		// go to the main tab
		if (mainScene != null) {
			if (ssm == null)
				ssm = mainScene.getJFXTabPane().getSelectionModel();
			Platform.runLater(() -> ssm.select(MainScene.MAIN_TAB));
		}

		playSound(unit);

	}

	/**
	 * Set up playing a sound clip for the unit
	 * @param unit the unit the window is for.
	 */
	// 2016-09-28 Added playSound()
	@SuppressWarnings("restriction")
	public void playSound(Unit unit) {

		if (mainScene != null) {
			Platform.runLater(() -> {
				//logger.info("playSound() is on " + Thread.currentThread().getName());
				// Play sound for window.
				String soundFilePath = UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getSound(unit);
				if ((soundFilePath != null) && soundFilePath.length() != 0) {
					soundFilePath = SoundConstants.SOUNDS_ROOT_PATH + soundFilePath;
				}
				soundPlayer.play(soundFilePath);
			});
		}

		else {
			SwingUtilities.invokeLater(() -> {
				//logger.info("playSound() is on " + Thread.currentThread().getName());
				// Play sound for window.
				String soundFilePath = UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getSound(unit);
				if ((soundFilePath != null) && soundFilePath.length() != 0) {
					soundFilePath = SoundConstants.SOUNDS_ROOT_PATH + soundFilePath;
				}
				soundPlayer.play(soundFilePath);
			});
		}


	}

	/**
	 * Finds an existing unit window for a unit.
	 * @param unit the unit to search for.
	 * @return existing unit window or null if none.
	 */
	public UnitWindow findUnitWindow(Unit unit) {
		UnitWindow result = null;
		Iterator<UnitWindow> i = unitWindows.iterator();
		while (i.hasNext()) {
			UnitWindow window = i.next();
			if (window.getUnit() == unit) {
				result = window;
			}
		}
		return result;
	}

	/**
	 * Disposes a unit window and button.
	 *
	 * @param unit the unit the window is for.
	 */
	public void disposeUnitWindow(Unit unit) {

		// Dispose unit window
		UnitWindow deadWindow = null;
		Iterator<UnitWindow> i = unitWindows.iterator();
		while (i.hasNext()) {
			UnitWindow window = i.next();
			if (unit == window.getUnit()) {
				deadWindow = window;
			}
		}

		unitWindows.remove(deadWindow);

		if (deadWindow != null) {
			deadWindow.dispose();
		}

		// Have main window dispose of unit button
		if (mainWindow != null)
			mainWindow.disposeUnitButton(unit);
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
		}
	}

	/**
	 * Internal class thread for update.

	private class UpdateThread extends Thread { // implements Runnable { //

		public static final long SLEEP_TIME = 1000; // 1 second.
		MainDesktopPane desktop;
		boolean run = false;

		private UpdateThread(MainDesktopPane desktop) {
			//super(Msg.getString("MainDesktopPane.desktop.thread.running")); //$NON-NLS-1$
			this.desktop = desktop;
		}

		private void setRun(boolean run) {
			this.run = run;
		}

		@Override
		public void run() {
			while (true) {
				if (run) {
					desktop.update();
				}
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {}
			}
		}
	}
*/
/*
	class UpdateThreadTask implements Runnable {
		public static final long SLEEP_TIME = 1; // 1 second.
		MainDesktopPane desktop;
		private UpdateThreadTask(MainDesktopPane desktop) {
			logger.info(Msg.getString("MainDesktopPane.desktop.thread.running")); //$NON-NLS-1$
			this.desktop = desktop;
		}

		@Override
		public void run() {

			while (!threadPoolExecutor.isTerminated()){
				desktop.update();
				repaint();
			}

			try {
				TimeUnit.SECONDS.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {}
		}
	}
*/
	class UnitWindowTask implements Runnable {
		//long SLEEP_TIME = 1000;
		UnitWindow unitWindow;

		private UnitWindowTask(UnitWindow unitWindow) {
			//logger.info(Msg.getString("MainDesktopPane.unitWindow.thread.running")); //$NON-NLS-1$
			this.unitWindow = unitWindow;
		}

	@Override
		public void run() {
	   	//SwingUtilities.invokeLater(() -> {
			unitWindow.update();
    	//});
		}
	}

	private void setupUnitWindowExecutor() {
		// set up unitWindowExecutor
		unitWindowExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); //newCachedThreadPool();

	}

	private void runUnitWindowExecutor() {

		if (!unitWindows.isEmpty()) {

			// set up unitWindowExecutor
			setupUnitWindowExecutor();

			// Update all unit windows.
			unitWindows.forEach(u -> {
				if (isUnitWindowOpen(u))
					if ( !unitWindowExecutor.isTerminated() || !unitWindowExecutor.isShutdown() )
						unitWindowExecutor.execute(new UnitWindowTask(u));
			});

			if (!unitWindowExecutor.isShutdown())
				unitWindowExecutor.shutdown();

		}
	}


	class ToolWindowTask implements Runnable {
		//long SLEEP_TIME = 450;
		ToolWindow toolWindow;

		protected ToolWindow getToolWindow() {
			return toolWindow;
		}

		private ToolWindowTask(ToolWindow toolWindow) {
			//logger.info(Msg.getString("MainDesktopPane.toolWindow.thread.running")); //$NON-NLS-1$
			this.toolWindow = toolWindow;
		}

		@Override
		public void run() {
		   	//SwingUtilities.invokeLater(() -> {
		   		toolWindow.update();
		   	//});
		}
	}

	private void setupToolWindowTasks() {
		// set up toolWindowExecutor even though it is not used right now inside this method
		toolWindowExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1); //newCachedThreadPool();

		toolWindowTaskList = new ArrayList<>();
		toolWindows.forEach(t -> {
			toolWindowTask = new ToolWindowTask(t);
			toolWindowTaskList.add(toolWindowTask);
		});
	}

	private void runToolWindowExecutor() {

		if (toolWindowTaskList.isEmpty())
			setupToolWindowTasks();

		toolWindowTaskList.forEach(t -> {
			// if a tool window is opened, run its executor
			if (isToolWindowOpen(t.getToolWindow().getToolName()))
				if ( !toolWindowExecutor.isTerminated() || !toolWindowExecutor.isShutdown() )
					toolWindowExecutor.execute(t);
		});
	}


	/**
	 * Update the desktop and all of its windows.
	 */
	private void update() {
		//long SLEEP_TIME = 450;

		// Update all unit windows.
		runUnitWindowExecutor();

		// Update all tool windows.
		runToolWindowExecutor();

	}


	public void clearDesktop() {
	   	//logger.info("MainDesktopPane's clearDesktop() is on " + Thread.currentThread().getName() + " Thread");
		// Stop update thread.
//		updateThread.setRun(false);
		logger.info(Msg.getString("MainDesktopPane.desktop.thread.shutdown")); //$NON-NLS-1$

        if (!toolWindowExecutor.isShutdown())
        	toolWindowExecutor.shutdown();
        if (unitWindowExecutor != null)
        	if (!unitWindowExecutor.isShutdown())
        		unitWindowExecutor.shutdown();
		//logger.info(Msg.getString("MainDesktopPane.desktop.thread.shutdown")); //$NON-NLS-1$
		toolWindowTaskList.clear();

		// Give some time for the update thread to finish updating.
		//try {
		//	Thread.sleep(100L);
		//} catch (InterruptedException e) {};

		// Dispose unit windows
		Iterator<UnitWindow> i1 = unitWindows.iterator();
		while (i1.hasNext()) {
			UnitWindow window = i1.next();
			window.dispose();
			if (mainWindow !=null) mainWindow.disposeUnitButton(window.getUnit());
			window.destroy();
		}
		unitWindows.clear();

		// Dispose tool windows
		Iterator<ToolWindow> i2 = toolWindows.iterator();
		while (i2.hasNext()) {
			ToolWindow window = i2.next();
			window.dispose();
			window.destroy();
		}
		toolWindows.clear();
	}

	/**
	 * Resets all windows on the desktop.  Disposes of all unit windows
	 * and tool windows, and reconstructs the tool windows.
	 */
	public void resetDesktop() {
	   	//logger.info("MainDesktopPane's resetDesktop() is on " + Thread.currentThread().getName() + " Thread");

		// Prepare tool windows
		prepareToolWindows();

		// Shut down update threads
//		updateThread.setRun(false);
		logger.info(Msg.getString("MainDesktopPane.desktop.thread.shutdown")); //$NON-NLS-1$
        //threadPoolExecutor.shutdown();

        if (!toolWindowExecutor.isShutdown())
        	toolWindowExecutor.shutdown();
        if (unitWindowExecutor != null)
        	if (!unitWindowExecutor.isShutdown())
        		unitWindowExecutor.shutdown();

        // Restart update threads.
        setupToolWindowTasks();
//		updateThread.setRun(true);
		logger.info(Msg.getString("MainDesktopPane.desktop.thread.running")); //$NON-NLS-1$

	}

	private Point getCenterLocation(JInternalFrame tempWindow) {

		Dimension desktop_size = getSize();
		Dimension window_size = tempWindow.getSize();

		int rX = (int) Math.round((desktop_size.width - window_size.width) / 2D);
		int rY = (int) Math.round((desktop_size.height - window_size.height) / 2D);

		// 2014-12-25 Added rX checking
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
	 * @param tempWindow an internal window
	 * @return random point on the desktop
	 */
	private Point getRandomLocation(JInternalFrame tempWindow) {

		Dimension desktop_size = getSize();
		Dimension window_size = tempWindow.getSize();

		// 2016-11-24 populate windows in grid=like starting position
		int w = desktop_size.width - window_size.width;
		int rX = RandomUtil.getRandomInt(w/20) * 20;
				//(int) Math.round(Math.random() *
				//);

		int rY = 55 + RandomUtil.getRandomInt(5) * 20;
				//(desktop_size.height - window_size.height));

		// Make sure y position isn't < 0.
		//if (rY < 50) {
		//	rY = 50;
		//}

		// 2014-12-25 Added rX checking
		//if (rX < 0) {
		//	rX = 0;
		//}
		return new Point(rX, rY);
	}


	/**
	 * Gets the starting location on the desktop for a given {@link JInternalFrame}.
	 * @return a specific point on the desktop
	 */
	// 2016-11-26 getStartingLocation()
	private Point getStartingLocation(JInternalFrame f) {
		Dimension desktop_size = getSize();
		Dimension f_size = f.getSize();

		// 2016-11-24 populate windows in grid=like starting position
		int w = desktop_size.width - f_size.width;
		int rX = 5;
		int rY = 10;
		return new Point(rX, rY);
	}


	/**
	 * Gets the sound player used by the desktop.
	 * @return sound player.
	 */
	public AudioPlayer getSoundPlayer() {
		return soundPlayer;
	}

	/**
	 * Opens a popup announcement window on the desktop.
	 * @param announcement the announcement text to display.
	 */
	public void openAnnouncementWindow(String announcement) {
		announcementWindow.setAnnouncement(announcement);

		if (mainScene != null) {

		} else {
			announcementWindow.pack();
			add(announcementWindow, 0);
			int Xloc = (int)((getWidth() - announcementWindow.getWidth()) * .5D);
			int Yloc = (int)((getHeight() - announcementWindow.getHeight()) * .15D);
			announcementWindow.setLocation(Xloc, Yloc);
			// Note: second window packing seems necessary to get window
			// to display components correctly.
			announcementWindow.pack();
			announcementWindow.setVisible(true);
			validate();
			repaint();
		}
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
	        SwingUtilities.updateComponentTreeUI(announcementWindow);
	    }
	}


	/**
	 * Updates the look & feel for all tool windows.
	 */
	public void updateToolWindowLF() {
		Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
		    ToolWindow toolWindow = i.next();
			SwingUtilities.updateComponentTreeUI(toolWindow);
		   	//SwingUtilities.invokeLater(() -> {
		   		toolWindow.update();
		   	//});
			//toolWindow.pack(); // why will it cause help window to stretch?
		}
	}


	public void updateUnitWindowLF() {
		Iterator<UnitWindow> i = unitWindows.iterator();
		while (i.hasNext()) {
			UnitWindow window = i.next();
			SwingUtilities.updateComponentTreeUI(window);
		   	//SwingUtilities.invokeLater(() -> {
	            window.update();
		   	//});
		}
	}

	/**
	 * Closes the look & feel for all tool windows.
	 */
	public void closeAllToolWindow() {
		Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
		    ToolWindow toolWindow = i.next();
			remove(toolWindow);
		}
		disposeAnnouncementWindow();
	}

	/**
	 * Opens all initial windows based on UI configuration.
	 */
	public void openInitialWindows() {
		//logger.info("openInitialWindows() is on " + Thread.currentThread().getName() );
		// on JavaFX Application Thread
/*
		JFXPannableView v = new JFXPannableView(this);
		if (mainScene != null ) {
			SwingUtilities.invokeLater(new Runnable(){
		            @Override
		            public void run() {
		        		v.createJFX();
		        		//try {
		        			//v.setMaximum(true);
		        		//} catch (PropertyVetoException e) {
		        		//	e.printStackTrace();
		        		//}
			    		//v.toBack();
		            }
			});
		}
*/
		UIConfig config = UIConfig.INSTANCE;
		if (config.useUIDefault()) {

			// Note: SwingUtilities.invokeLater(()) doesn't allow guide windows to be centered for javaFX mode in Windows PC (but not in other platform)

			GuideWindow ourGuide = (GuideWindow) getToolWindow(GuideWindow.NAME);
			openToolWindow(GuideWindow.NAME);

			if (mainScene != null) {

				//int Xloc = (int)((mainScene.getStage().getScene().getWidth() - ourGuide.getWidth()) * .5D);
				//int Yloc = (int)((mainScene.getStage().getScene().getHeight() - ourGuide.getHeight()) * .5D);
				int Xloc = (int)((mainScene.getWidth() - ourGuide.getWidth()) * .5D);
				int Yloc = (int)((mainScene.getHeight() - ourGuide.getHeight()) * .5D);

				ourGuide.setLocation(Xloc, Yloc);
				ourGuide.toFront();
			}

			ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$

		} else {
			// Open windows in Z-order.
			List<String> windowNames = config.getInternalWindowNames();
			int num = windowNames.size();
			for (int x = 0; x < num; x++) {
				String highestZName = null;
				int highestZ = Integer.MIN_VALUE;
				Iterator<String> i = windowNames.iterator();
				while (i.hasNext()) {
					String name = i.next();
					boolean display = config.isInternalWindowDisplayed(name);
					String type = config.getInternalWindowType(name);
					if (UIConfig.UNIT.equals(type) && !Simulation.instance().isDefaultLoad()) {
						display = false;
					}
					if (display) {
						int zOrder = config.getInternalWindowZOrder(name);
						if (zOrder > highestZ) {
							highestZName = name;
							highestZ = zOrder;
						}
					}
				}
				if (highestZName != null)  {
					String type = config.getInternalWindowType(highestZName);
					if (UIConfig.TOOL.equals(type)) {
						openToolWindow(highestZName);
					} else if (UIConfig.UNIT.equals(type)) {
						Unit unit = Simulation.instance().getUnitManager().findUnit(highestZName);
						if (unit != null) {
							openUnitWindow(unit, true);
						}
					}
					windowNames.remove(highestZName);
				}
			}

			if (mainWindow != null) {
				// Create unit bar buttons for closed unit windows.
				if (Simulation.instance().isDefaultLoad()) {
					Iterator<String> i = config.getInternalWindowNames().iterator();
					while (i.hasNext()) {
						String name = i.next();
						if (UIConfig.UNIT.equals(config.getInternalWindowType(name))) {
							if (!config.isInternalWindowDisplayed(name)) {
								Unit unit = Simulation.instance().getUnitManager().findUnit(name);
								if (unit != null) {
									mainWindow.createUnitButton(unit);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * creates a standardized empty border.
	 */
	public static EmptyBorder newEmptyBorder() {
		return new EmptyBorder(1,1,1,1);
	}

	public void setSettlementWindow(SettlementWindow settlementWindow) {
		this.settlementWindow = settlementWindow;
	}

	//public TransportWizard getTransportWizard() {
	//	return transportWizard;
	//}

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

	public void setMarqueeTicker(MarqueeTicker marqueeTicker) {
		this.marqueeTicker = marqueeTicker;
	}

	public MarqueeTicker getMarqueeTicker() {
		return marqueeTicker;
	}


	// 2014-12-19 Added unitUpdate()
	@SuppressWarnings("restriction")
	@Override // @Override needed for Main window
	public void unitUpdate(UnitEvent event) {
		UnitEventType eventType = event.getType();
		//System.out.println("MainDesktopPane : unitUpdate() " + eventType);
		Object target = event.getTarget();
		if (eventType == UnitEventType.START_TRANSPORT_WIZARD_EVENT) {

			building = (Building) target; // overwrite the dummy building object made by the constructor
			BuildingManager mgr = building.getBuildingManager();

			if (!isTransportingBuilding) {
				isTransportingBuilding = true;
				if (mainWindow != null)
					mainWindow.openTransportWizard(mgr);
				else if (mainScene != null)
					mainScene.openTransportWizard(mgr);
				//Simulation.instance().getTransportManager().setIsTransportingBuilding(false);
			}

		}

		else if (eventType == UnitEventType.END_TRANSPORT_WIZARD_EVENT) {
			isTransportingBuilding = false;
            //disposeAnnouncementWindow();
		}

		else if (eventType == UnitEventType.START_CONSTRUCTION_WIZARD_EVENT) {
			BuildingConstructionMission mission = (BuildingConstructionMission) target;
		   	//logger.info("MainDesktopPane's unitUpdate() is on " + Thread.currentThread().getName() + " Thread");
		   	// it's on pool-4-thread-1

			//constructionLock.lock();
			//constructionCount++;
			//try {// access the resource protected by this lock
			//} catch(Exception ex) {// restore invariants
			//} finally {
				//constructionLock.unlock();
				//constructionCount--;
			//}

			if (!isConstructingSite) {
				isConstructingSite = true;

				if (mainWindow != null)  {
					mainWindow.openConstructionWizard(mission);
				}
				else if (mainScene != null) {
					mainScene.openConstructionWizard(mission);
				}

			}
		}

		else if (eventType == UnitEventType.END_CONSTRUCTION_WIZARD_EVENT) {
			isConstructingSite = false;
		}

		// repaint(); // raise cpu util% way too much for putting it here
	}


	public TimeWindow getTimeWindow() {
		return timeWindow;
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

	public BrowserJFX getBrowserJFX() {
		return browserJFX;
	}

	public void setEventTableModel(EventTableModel eventTableModel) {
		this.eventTableModel = eventTableModel;
	}

	public EventTableModel getEventTableModel() {
		return eventTableModel;
	}


	@Override
	public void clockPulse(double time) {
		timeCache = timeCache + time;
		if (timeCache > PERIOD_IN_MILLISOLS) {
			//logger.info("time : " + time);//calling update()");
			update();
			timeCache = 0;
		}
	}

	@Override
	public void pauseChange(boolean isPaused) {
		// TODO Auto-generated method stub

	}

	public void destroy() {
		unitWindows = null;
		toolWindows = null;
		backgroundImageIcon = null;
		backgroundLabel = null;
		toolWindowTask = null;
		toolWindowExecutor = null;
		unitWindowExecutor = null;
		toolWindowTaskList = null;
		soundPlayer = null;
		announcementWindow = null;
		settlementWindow = null;
		timeWindow = null;
		building = null;
		mainWindow = null;
		mainScene = null;
		marqueeTicker = null;
		orbitViewer = null;
		browserJFX = null;
		eventTableModel = null;
		ssm = null;
	}

}