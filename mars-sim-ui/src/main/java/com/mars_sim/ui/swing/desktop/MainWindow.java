/*
 * Mars Simulation Project
 * MainWindow.java
 * @date 2025-10-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.desktop;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.formdev.flatlaf.util.SystemInfo;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.time.ClockListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.ClockPulseListener;
import com.mars_sim.core.time.CompressedClockListener;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.ui.swing.ContentManager;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.ToolToolBar;
import com.mars_sim.ui.swing.UIConfig;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.components.JMemoryMeter;
import com.mars_sim.ui.swing.entitywindow.EntityToolBar;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.tool.JStatusBar;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;
import com.mars_sim.ui.swing.utils.SaveDialog;
import com.mars_sim.ui.swing.utils.SpeedControl;
import com.mars_sim.ui.swing.utils.SwingHelper;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * main desktop pane window are, status bar and tool bars.
 */
public class MainWindow
		extends JComponent implements ClockListener, ClockPulseListener, ContentManager {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

	public static final int HEIGHT_STATUS_BAR = 25;
		
	private static final String SHOW_UNIT_BAR = "show-unit-bar";
	private static final String SHOW_TOOL_BAR = "show-tool-bar";
	private static final String MAIN_PROPS = "main-window";

	private static final String EXTERNAL_BROWSER = "use-external";
		
	/** The main window frame. */
	private JFrame frame;

	private transient UIConfig uiconfigs;

	// Data members
	private boolean isIconified = false;
    
	/** The unit tool bar. */
	private EntityToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private Dimension selectedSize;
	
	private Simulation sim;

	private JMemoryMeter memoryBar;

	private boolean useExternalBrowser;

	private ClockPulseListener clockHandler;

	private AudioPlayer soundPlayer;

	private SpeedControl speedControls;

	/**
	 * Constructor 1.
	 *
	 * @param sim Simulation running.
	 * @param config UI configuration to use for the window.
	 * @param audio Audio player for the window.
	 */
	public MainWindow(Simulation sim, UIConfig config, AudioPlayer audio) {
		this.sim = sim;

		logger.config("Starting as " + GameManager.getGameMode());

		// Set Apache Batik library system property so that it doesn't output:
		// "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
		System.setProperty("org.apache.batik.warn_destination", "false");

		this.uiconfigs = config;
		this.soundPlayer = audio;

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gd = ge.getScreenDevices();
		GraphicsDevice graphicsDevice = null;

		if (gd.length == 1) {
			logger.log(Level.CONFIG, "Detecting only one screen.");
			logger.config("1 screen detected.");
		} else if (gd.length == 0) {
			throw new IllegalStateException("No Screens Found.");
			// NOTE: what about the future server version of mars-sim in which no screen is
			// needed.
		} else {
			logger.config(gd.length + " screens detected.");
		}

		graphicsDevice = gd[0];
		logger.config("Use the first screen.");
		
		int screenWidth = graphicsDevice.getDisplayMode().getWidth();
		int screenHeight = graphicsDevice.getDisplayMode().getHeight();

		// Set up the frame
		frame = new JFrame();
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(640, 640));

		// Set the UI configuration
		boolean useDefault = uiconfigs.useUIDefault();

		if (useDefault || !setUpSavedScreen()) {
			logger.config("Will calculate screen size for default display instead.");
			setUpDefaultScreen(graphicsDevice, screenWidth, screenHeight, useDefault);
		}
		
		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this, sim, soundPlayer);
		
		init(soundPlayer, sim.getMasterClock());

		// Show frame
		frame.setVisible(true);

		// Open all initial windows.
		desktop.openInitialWindows();
	}

	/**
	 * Sets up the screen config used from last saved session.
	 */
	private boolean setUpSavedScreen() {
		// Display screen at a certain location
		var location = uiconfigs.getMainWindowLocation();
		if (location == null) {
			return false;
		}
		frame.setLocation(uiconfigs.getMainWindowLocation());

		// For the Main Window	
		selectedSize = uiconfigs.getMainWindowDimension();
		if (selectedSize != null) {
			// Set frame size
			frame.setSize(selectedSize);
			logger.config("Last saved window dimension: " + selectedSize);
		}

		return true;
	}

	/**
	 * Sets up the default screen config.
	 * 
	 * @param gd
	 * @param screenWidth
	 * @param screenHeight
	 * @param useDefaults
	 */
	private void setUpDefaultScreen(GraphicsDevice gd, int screenWidth, int screenHeight, boolean useDefaults) {
		int initY;
		int initX;
		
		// Set main window frame size
		selectedSize = calculatedScreenSize(gd, screenWidth, screenHeight, useDefaults, uiconfigs.getMainWindowDimension());

		frame.setSize(selectedSize);

		logger.config("Default main window dimension: "
				+ selectedSize.width
				+ " x "
				+ selectedSize.height
				+ ".");

		initX = (int)(screenWidth - selectedSize.width) / 2;
		initY = (int)(screenHeight - selectedSize.height) / 2;
		frame.setLocation(initX, initY);

		logger.config("Use default configuration to set main window's frame to the center of the screen.");
		logger.config("The main window frame is centered and starts at ("
				+ (screenWidth - selectedSize.width) / 2
				+ ", "
				+ (screenHeight - selectedSize.height) / 2
				+ ").");
	}


	/**
	 * Calculates the screen size.
	 * 
	 * @param screenWidth
	 * @param screenHeight
	 * @param useDefault
	 * @return
	 */
	private Dimension calculatedScreenSize(GraphicsDevice gd, int screenWidth, int screenHeight, boolean useDefault,
			Dimension dimension) {
		logger.config("Current screen size is " + screenWidth + " x " + screenHeight);

		Dimension frameSize = null;
		if (useDefault) {
			var mode = gd.getDisplayMode();
			frameSize = new Dimension(mode.getWidth(), mode.getHeight());
			logger.config("Use default screen configuration.");
			logger.config("Selected screen size is " + frameSize.width + " x " + frameSize.height);
		} else {
			// Use any stored size
			frameSize = dimension;
			logger.config("Use last saved window size " + frameSize.width + " x " + frameSize.height);
		}

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (screenSize != null) {
			logger.config("Current toolkit screen size is " + screenSize.width + " x " + screenSize.height);

			if (frameSize != null) {
				// Check selected is not bigger than the screen
				if (frameSize.width > screenSize.width
						|| frameSize.height > screenSize.height) {
					logger.warning("Selected screen size cannot be larger than physical screen size.");
					frameSize = null;
				}
			}

			if (frameSize == null) {
				// Make frame size 80% of screen size.
				if (screenSize.width > 800) {
					frameSize = new Dimension(
							(int) Math.round(screenSize.getWidth() * .8),
							(int) Math.round(screenSize.getHeight() * .8));
					logger.config("New window size is " + frameSize.width + " x " + frameSize.height);
				} else {
					frameSize = new Dimension(screenSize);
					logger.config("New window size is " + frameSize.width + " x " + frameSize.height);
				}
			}
		}

		return frameSize;
	}

	/**
	 * Get the selected screen size for the main window.
	 * 
	 * @return
	 */
	Dimension getSelectedSize() {
		return selectedSize;
	}
	
	/**
	 * Initializes UI elements for the frame
	 * @param audio The audio player to use but maybe null if audio is not initialized
	 * @param masterClock Main clock
	 */
	private void init(AudioPlayer audio, MasterClock masterClock) {
	
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				SaveDialog.createEndSimulation(sim, MainWindow.this);
			}
		});

		frame.addWindowStateListener(e -> {
			int state = e.getNewState();
			isIconified = (state == Frame.ICONIFIED);
			if (state == Frame.MAXIMIZED_HORIZ
					|| state == Frame.MAXIMIZED_VERT)
				logger.log(Level.CONFIG, "MainWindow set to maximum.");
			repaint();
		});
	
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		changeTitle(false);

		if (SystemInfo.isMacOS) {
			final Taskbar taskbar = Taskbar.getTaskbar();
			taskbar.setIconImage(StyleManager.getIconImage());
			
			// Move the menu bar out of the main window to the top of the screen
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		}
		else {
			frame.setIconImage(StyleManager.getIconImage());
		}
		

		// Set up the main pane
		JPanel mainPane = new JPanel(new BorderLayout());
		frame.add(mainPane);

		// Set up the overlay pane
		JPanel contentPane = new JPanel(new BorderLayout());

		// Add desktop to the content pane
		// context pane should be blocked when paused
		contentPane.add(desktop, BorderLayout.CENTER);
		mainPane.add(contentPane, BorderLayout.CENTER);

     	// Add toolbar pane
		JPanel toolbarPane = new JPanel(new BorderLayout()); 
		// Note: use BorderLayout for now since it has the advantage of 
		// centering the earth/Mars date stamp on the screen
		// while FlowLayout(FlowLayout.CENTER)) will result in icons clump together
		// Cannot use toolbarPane.setLayout(new BoxLayout(toolbarPane, BoxLayout.X_AXIS)) since 
		// it makes icons not stretching out enough
		toolbarPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		contentPane.add(toolbarPane, BorderLayout.NORTH);
		
		 // Add a floating speed bar
     	JToolBar floatingSpeedBar = new JToolBar(SwingConstants.HORIZONTAL);
     	floatingSpeedBar.setAlignmentX(Component.LEFT_ALIGNMENT);
     	// Note: do NOT use floatingSpeedBar.setFloatable(true).
     	// If user clicks close while it's floating, it does not re-positioning itself
     	// back to top left corner. Rather it creates the unintended consequence 
     	// of having two rows of tool bar
		speedControls = new SpeedControl(masterClock);
     	floatingSpeedBar.add(speedControls);	
	
    	// Add floatingSpeedBar to toolbarPane
		toolbarPane.add(floatingSpeedBar, BorderLayout.WEST);
	
     	// Prepare tool toolbar
     	toolToolbar = new ToolToolBar(this, desktop, audio);
     	toolToolbar.requestFocusInWindow();
     	// Add toolToolbar to toolbarPane
     	toolbarPane.add(toolToolbar, BorderLayout.CENTER);
    
		// Add bottomPane for holding unitToolbar and statusBar
		JPanel bottomPane = new JPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new EntityToolBar(desktop);

		unitToolbar.setBorder(new MarsPanelBorder());
		// Remove the toolbar border, to blend into figure contents
		unitToolbar.setBorderPainted(true);

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		bottomPane.add(unitToolbar, BorderLayout.CENTER);

		// set the visibility of tool and unit bars from preferences
		Properties props = uiconfigs.getPropSet(MAIN_PROPS);
		unitToolbar.setVisible(UIConfig.extractBoolean(props, SHOW_UNIT_BAR, false));
		toolToolbar.setVisible(UIConfig.extractBoolean(props, SHOW_TOOL_BAR, true));
		useExternalBrowser = UIConfig.extractBoolean(props, EXTERNAL_BROWSER, false);

		// Prepare menu
		MainWindowMenu mainWindowMenu = new MainWindowMenu(this, desktop, audio);
		frame.setJMenuBar(mainWindowMenu);
		
		// Close the unit bar when starting up
		unitToolbar.setVisible(false);

		// Create the status bar
		JStatusBar statusBar = new JStatusBar(1, 1, HEIGHT_STATUS_BAR);
		bottomPane.add(statusBar, BorderLayout.SOUTH);

		// Create memory bar
		memoryBar = new JMemoryMeter();
		memoryBar.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		memoryBar.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 5));
		memoryBar.setPreferredSize(new Dimension(235, 22));
		statusBar.addRightComponent(memoryBar, false);

		// Add this class to the master clock's listener but compress the pulses
		// to no more than one per second
		clockHandler = new CompressedClockListener(this, 1000L);
		masterClock.addClockPulseListener(clockHandler);

		// Listen for clock changes
		masterClock.addClockListener(this);
	}
	
	/**
	 * Get the window's frame.
	 *
	 * @return the frame.
	 */
	@Override
	public JFrame getTopFrame() {
		return frame;
	}

	/**
	 * Close the main frame and unregister any listeners or active components.
	 */
	@Override
	public void shutdown() {
		frame.dispose();

		var masterClock = sim.getMasterClock();
		masterClock.removeClockPulseListener(clockHandler);
		masterClock.removeClockListener(this);

		speedControls.unregister();
		desktop.destroy();
	}

	/**
	 * Get the assigned audio player.
	 */
	@Override
	public AudioPlayer getAudio() {
		return soundPlayer;
	}

	/**
	 * Gets the unit toolbar.
	 *
	 * @return unit toolbar.
	 */
	public EntityToolBar getUnitToolBar() {
		return unitToolbar;
	}

	/**
	 * Gets the tool toolbar.
	 *
	 * @return tool toolbar.
	 */
	public ToolToolBar getToolToolBar() {
		return toolToolbar;
	}

	/**
	 * Changes the title.
	 * 
	 * @param isPaused
	 */
	private void changeTitle(boolean isPaused) {
		String suffix = switch (GameManager.getGameMode()) {
			case COMMAND -> "Command Mode";
			case SANDBOX -> "Sandbox Mode";
			case SPONSOR -> "Sponsor Mode";
			case SOCIETY ->  "Society Mode";	
		};
		frame.setTitle(SimulationRuntime.SHORT_TITLE + "  -  " + suffix + (isPaused ? "  -  [ P A U S E ]" : ""));
	}

	/**
	 * Gets the UIConfig for this UI.
	 */
	@Override
	public UIConfig getConfig() {
		return uiconfigs;
	}

	/**
	 * Gets the UI properties of the application.
	 */
	@Override
	public Map<String, Properties> getUIProps() {
		Map<String, Properties> result = new HashMap<>();

		// Local details
		Properties desktopProps = new Properties();
		desktopProps.setProperty(SHOW_TOOL_BAR, Boolean.toString(toolToolbar.isVisible()));
		desktopProps.setProperty(SHOW_UNIT_BAR, Boolean.toString(unitToolbar.isVisible()));
		desktopProps.setProperty(EXTERNAL_BROWSER, Boolean.toString(useExternalBrowser));

		result.put(MAIN_PROPS, desktopProps);
		return result;
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (pulse.getElapsed() > 0 && !isIconified) {
			// Increments the Earth and Mars clock labels.
			toolToolbar.incrementClocks(pulse.getMasterClock());

			memoryBar.refresh();

			// Cascade the pulse
			desktop.updateWindows(pulse);
		}
	}

	/**
	 * Changes the pause status.
	 *
	 * @param isPaused true if set to pause
	 */
	@Override
	public void pauseChange(boolean isPaused) {
		changeTitle(isPaused);
	}

	@Override
	public void desiredTimeRatioChange(int desiredTR) {
		// No need to do anything here since the MasterClock is controlling the time ratio
		// and the MainWindow does not display the time ratio. If we want to display the time ratio in the future, we can implement this method to update the display.
	}
	
	/**
	 * Uses the external browser for help.
	 * 
	 * @param selected
	 */
	public void setExternalBrowser(boolean selected) {
		useExternalBrowser = selected;
	}

	/**
	 * Is the external browser being used ?
	 * 
	 * @return
	 */
	public boolean useExternalBrowser() {
		return useExternalBrowser;
	}

	/**
	 * Displays a help page.
	 * 
	 * @param helpPage
	 */
	public void showHelp(String helpPage) {
		var library = GuideWindow.getHelp(sim.getConfig());

		var  helpURI = library.getPage(helpPage);	
		if (useExternalBrowser) {
			SwingHelper.openBrowser(helpURI);
		}
		else {
			var contentWindow = desktop.openToolWindow(GuideWindow.NAME);
			GuideWindow ourGuide = (GuideWindow) contentWindow;
			ourGuide.displayURI(helpURI);
		}
    }

	@Override
	public List<WindowSpec> getContentSpecs() {
		return desktop.getContentSpecs();
	}
}
