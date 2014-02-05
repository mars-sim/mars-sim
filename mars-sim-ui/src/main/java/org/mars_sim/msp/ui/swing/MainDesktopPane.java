/**
 * Mars Simulation Project
 * MainDesktopPane.java
 * @version 3.06 2014-01-29
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.Color;
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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.ui.swing.sound.AudioPlayer;
import org.mars_sim.msp.ui.swing.sound.SoundConstants;
import org.mars_sim.msp.ui.swing.tool.ToolWindow;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.mission.MissionWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.MonitorWindow;
import org.mars_sim.msp.ui.swing.tool.monitor.UnitTableModel;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;
import org.mars_sim.msp.ui.swing.tool.preferences.PreferencesWindow;
import org.mars_sim.msp.ui.swing.tool.resupply.ResupplyWindow;
import org.mars_sim.msp.ui.swing.tool.science.ScienceWindow;
import org.mars_sim.msp.ui.swing.tool.search.SearchWindow;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementWindow;
import org.mars_sim.msp.ui.swing.tool.time.TimeWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindowFactory;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindowListener;

/** 
 * The MainDesktopPane class is the desktop part of the project's UI.
 * It contains all tool and unit windows, and is itself contained,
 * along with the tool bars, by the main window.
 */
public class MainDesktopPane extends JDesktopPane implements ComponentListener {

	private static String CLASS_NAME = MainDesktopPane.class.getName();

	private static Logger logger = Logger.getLogger(CLASS_NAME);

	// Data members
	private final Collection<UnitWindow> unitWindows; // List of open or buttoned unit windows.
	private final Collection<ToolWindow> toolWindows; // List of tool windows.
	private final MainWindow mainWindow; // The main window frame.
	private final ImageIcon backgroundImageIcon; // ImageIcon that contains the tiled background.
	private final JLabel backgroundLabel; // Label that contains the tiled background.
	private boolean firstDisplay; // True if this MainDesktopPane hasn't been displayed yet.
	private final UpdateThread updateThread; // The desktop update thread.
	private final AudioPlayer soundPlayer; // The sound player
	private final AnnouncementWindow announcementWindow; // The desktop popup announcement window.

	/** 
	 * Constructor
	 *
	 * @param mainWindow the main outer window
	 */
	public MainDesktopPane(MainWindow mainWindow) {

		// Initialize data members
		soundPlayer = new AudioPlayer();
		soundPlayer.play(SoundConstants.SOUNDS_ROOT_PATH + SoundConstants.SND_SPLASH); // play our splash sound

		this.mainWindow = mainWindow;
		unitWindows = new ArrayList<UnitWindow>();
		toolWindows = new ArrayList<ToolWindow>();

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

		// Prepare tool windows.
		prepareToolWindows();

		// Prepare announcementWindow.
		announcementWindow = new AnnouncementWindow(this);
		try { announcementWindow.setClosed(true); }
		catch (java.beans.PropertyVetoException e) { }

		// Create update thread.
		updateThread = new UpdateThread(this);
		updateThread.setRun(true);
		updateThread.start();
	}

	/** Returns the MainWindow instance
	 *  @return MainWindow instance
	 */
	public MainWindow getMainWindow() {
		return mainWindow;
	}

	/** Create background tile when MainDesktopPane is first
	 *  displayed. Recenter logoLabel on MainWindow and set
	 *  backgroundLabel to the size of MainDesktopPane.
	 *  @param e the component event
	 */
	@Override
	public void componentResized(ComponentEvent e) {

		// If displayed for the first time, create background image tile.
		// The size of the background tile cannot be determined during construction
		// since it requires the MainDesktopPane be displayed first.
		if (firstDisplay) {
			ImageIcon baseImageIcon = ImageLoader.getIcon("background");
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

	/** Creates tool windows */
	private void prepareToolWindows() {

		toolWindows.clear();

		// Prepare navigator window
		NavigatorWindow navWindow = new NavigatorWindow(this);
		try { navWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(navWindow);

		// Prepare search tool window
		SearchWindow searchWindow = new SearchWindow(this);
		try { searchWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(searchWindow);

		// Prepare time tool window
		TimeWindow timeWindow = new TimeWindow(this);
		try { timeWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(timeWindow);

		// Prepare monitor tool window
		MonitorWindow monitorWindow = new MonitorWindow(this);
		try { monitorWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(monitorWindow);

		// Prepare preferences tool window
		PreferencesWindow prefsWindow = new PreferencesWindow(this);
		try { prefsWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(prefsWindow);

		// Prepare mission tool window
		MissionWindow missionWindow = new MissionWindow(this);
		try { missionWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(missionWindow);

		// Prepare settlement tool window
		SettlementWindow settlementWindow = new SettlementWindow(this);
		try { settlementWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(settlementWindow);

		// Prepare science tool window
		ScienceWindow scienceWindow = new ScienceWindow(this);
		try { scienceWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(scienceWindow);
		
		// Prepare resupply tool window
		ResupplyWindow resupplyWindow = new ResupplyWindow(this);
		try { resupplyWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(resupplyWindow);

		// Prepare guide tool window
		GuideWindow guideWindow = new GuideWindow(this);
		try { guideWindow.setClosed(true); }
		catch (PropertyVetoException e) { }
		toolWindows.add(guideWindow);
	}

	/** Returns a tool window for a given tool name
	 *  @param toolName the name of the tool window
	 *  @return the tool window
	 */
	public ToolWindow getToolWindow(String toolName) {
		ToolWindow result = null;
		Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
			ToolWindow window = i.next();
			if (toolName.equals(window.getToolName())) {
				result = window;
			}
		}

		return result;
	}

	/** Displays a new Unit model in the monitor window
	 *  @param model the new model to display
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

	/** Return true if tool window is open
	 *  @param toolName the name of the tool window
	 *  @return true true if tool window is open
	 */
	public boolean isToolWindowOpen(String toolName) {
		ToolWindow window = getToolWindow(toolName);
		if (window != null) {
			return !window.isClosed();
		} else {
			return false;
		}
	}

	/** Opens a tool window if necessary
	 *  @param toolName the name of the tool window
	 */
	public void openToolWindow(String toolName) {
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
							window.setLocation(getRandomLocation(window));
						}
					}
					window.setWasOpened(true);
				}
				add(window, 0);
				try { 
					window.setClosed(false); 
				}
				catch (Exception e) { logger.log(Level.SEVERE,e.toString()); }
			}
			window.show();
			//bring to front if it overlaps with other windows
			try {
				window.setSelected(true);
			} catch (PropertyVetoException e) {
				// ignore if setSelected is vetoed	
			}
		}
	}

	/** Closes a tool window if it is open
	 *  @param toolName the name of the tool window
	 */
	public void closeToolWindow(String toolName) {
		ToolWindow window = getToolWindow(toolName);
		if ((window != null) && !window.isClosed()) {
			try { window.setClosed(true); }
			catch (java.beans.PropertyVetoException e) {}
		}
	}

	/** 
	 * Creates and opens a window for a unit if it isn't 
	 * already in existence and open.
	 * @param unit the unit the window is for.
	 * @param initialWindow true if window is opened at UI startup.
	 */
	public void openUnitWindow(Unit unit, boolean initialWindow) {

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
			}
			catch(java.beans.PropertyVetoException e) {
				logger.log(Level.SEVERE,"Problem reopening " + e);
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
			}
			else {
				// Put window in random position on desktop.
				tempWindow.setLocation(getRandomLocation(tempWindow));
			}

			// Add unit window to unit windows
			unitWindows.add(tempWindow);

			// Create new unit button in tool bar if necessary
			mainWindow.createUnitButton(unit);
		}

		tempWindow.setVisible(true);

		// Correct window becomes selected
		try {
			tempWindow.setSelected(true);
			tempWindow.moveToFront();
		}
		catch (java.beans.PropertyVetoException e) {}

		// Play sound for window.
		String soundFilePath = UnitDisplayInfoFactory.getUnitDisplayInfo(unit).getSound(unit);
		if ((soundFilePath != null) && soundFilePath.length() != 0) {
			soundFilePath = SoundConstants.SOUNDS_ROOT_PATH + soundFilePath;
		}
		soundPlayer.play(soundFilePath);
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
			mainWindow.disposeUnitButton(window.getUnit());
		}
	}

	/**
	 * Update the desktop and all of its windows.
	 */
	private void update() {

		// Update all unit windows.
		Iterator<UnitWindow> i1 = unitWindows.iterator();
		try {
			while (i1.hasNext()) {
				i1.next().update();
			}
		}
		catch (ConcurrentModificationException e) {
			// Concurrent modifications exceptions may occur as 
			// unit windows are opened.
		}

		// Update all tool windows.
		Iterator<ToolWindow> i2 = toolWindows.iterator();
		try {
			while (i2.hasNext()) {
				i2.next().update();
			}
		}
		catch (ConcurrentModificationException e) {
			// Concurrent modifications exceptions may occur as
			// unit windows are opened.
		}
	}


	void clearDesktop() {
		// Stop update thread.
		updateThread.setRun(false);

		// Give some time for the update thread to finish updating.
		try {
		    Thread.sleep(100L);
		}
		catch (InterruptedException e) {};
		
		// Dispose unit windows
		Iterator<UnitWindow> i1 = unitWindows.iterator();
		while (i1.hasNext()) {
			UnitWindow window = i1.next();
			window.dispose();
			mainWindow.disposeUnitButton(window.getUnit());
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
	void resetDesktop() {
		// Prepare tool windows
		prepareToolWindows();

		// Restart update thread.
		updateThread.setRun(true);
	}

	private Point getCenterLocation(JInternalFrame tempWindow) {
	    
	    Dimension desktop_size = getSize();
        Dimension window_size = tempWindow.getSize();

        int rX = (int) Math.round((desktop_size.width - window_size.width) / 2D);
        int rY = (int) Math.round((desktop_size.height - window_size.height) / 2D);

        // Make sure y position isn't < 0.
        if (rY < 0) {
            rY = 0;
        }

        return new Point(rX, rY);
	}
	
	/** 
	 * Gets a random location on the desktop for a given JInternalFrame.
	 *
	 * @param tempWindow an internal window
	 * @return random point on the desktop
	 */
	private Point getRandomLocation(JInternalFrame tempWindow) {

		Dimension desktop_size = getSize();
		Dimension window_size = tempWindow.getSize();

		int rX = (int) Math.round(Math.random() *
				(desktop_size.width - window_size.width));
		int rY = (int) Math.round(Math.random() *
				(desktop_size.height - window_size.height));

		// Make sure y position isn't < 0.
		if (rY < 0) {
			rY = 0;
		}

		return new Point(rX, rY);
	}

	/** 
	 * Internal class thread for update.
	 */
	private class UpdateThread extends Thread {

		public static final long SLEEP_TIME = 1000; // 1 second.
		MainDesktopPane desktop;
		boolean run = false;

		private UpdateThread(MainDesktopPane desktop) {
			super("Desktop update thread");

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
				} 
				catch (InterruptedException e) {}
			}
		}
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
		announcementWindow.pack();
		add(announcementWindow, 0);
		int Xloc = (getWidth() - announcementWindow.getWidth()) / 2;
		int Yloc = (getHeight() - announcementWindow.getHeight()) / 2;
		announcementWindow.setLocation(Xloc, Yloc);
		// Note: second window packing seems necessary to get window
		// to display components correctly.
		announcementWindow.pack();
		announcementWindow.setVisible(true);
	}

	/**
	 * Removes the popup announcement window from the desktop.
	 */
	public void disposeAnnouncementWindow() {
		announcementWindow.dispose();
	}

	/**
	 * Updates the look & feel for all tool windows.
	 */
	void updateToolWindowLF() {
		Iterator<ToolWindow> i = toolWindows.iterator();
		while (i.hasNext()) {
		    ToolWindow toolWindow = i.next();
			SwingUtilities.updateComponentTreeUI(toolWindow);
			toolWindow.pack();
		}
	}

	/**
	 * Opens all initial windows based on UI configuration.
	 */
	void openInitialWindows() {
		UIConfig config = UIConfig.INSTANCE;
		if (config.useUIDefault()) {
		    
		    // Open default windows on desktop.
		    
		    // Open mars navigator tool.
		    openToolWindow(NavigatorWindow.NAME);
		    // Move mars navigator tool to upper left corner of desktop.
		    getToolWindow(NavigatorWindow.NAME).setLocation(0, 0);
		    
		    // Open user guide tool.
		    openToolWindow(GuideWindow.NAME);
		    GuideWindow ourGuide = (GuideWindow) getToolWindow(GuideWindow.NAME);
            ourGuide.setURL("/docs/help/tutorial1.html");
		}
		else {
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

	/**
	 * creates a standardized empty border.
	 */
	public static EmptyBorder newEmptyBorder() {
		return new EmptyBorder(1,1,1,1);
	}
}