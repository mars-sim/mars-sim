/*
 * Mars Simulation Project
 * MainWindow.java
 * @date 2021-08-15
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mars.sim.console.InteractiveTerm;
import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationFiles;
import org.mars_sim.msp.core.SimulationListener;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.astroarts.OrbitViewer;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.utils.JMemoryMeter;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * main desktop pane window are, status bar and tool bars.
 */
public class MainWindow
extends JComponent implements ClockListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

	/** Icon image filename for frame */
	public static final String LANDER_PNG = "landerhab16.png";

	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

	/** The size of the weather icons */
	public static final int WEATHER_ICON_SIZE = 64;

	private static final Icon PAUSE_ICON = ImageLoader.getIconByName("speed/pause");
	private static final Icon PLAY_ICON = ImageLoader.getIconByName("speed/play");          

	/** The main window frame. */
	private static JFrame frame;

	private UIConfig configs;

	private static SplashWindow splashWindow;

	private static InteractiveTerm interactiveTerm;

	// Data members
	private boolean isIconified = false;

	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private OrbitViewer orbitViewer;

	/** WebSwitch for the control of play or pause the simulation*/
	private JToggleButton pauseSwitch;
	private JCheckBox blockingSwitch;

	private JLabel blockingImage;

	private Dimension selectedSize;

	private Simulation sim;
	private MasterClock masterClock;

	private JMemoryMeter memoryBar;


	/**
	 * Constructor 1.
	 *
	 * @param cleanUI true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI, Simulation sim) {
		this.sim = sim;
				
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			logger.log(Level.CONFIG, "Running mars-sim in Command Mode.");
		} else {
			logger.log(Level.CONFIG, "Running mars-sim in Sandbox Mode.");
		}

		
		// Load a UI Config instance according to the user's choice
		boolean loadConfig = true;
		if (cleanUI) {
			loadConfig = askScreenConfig();
		}
		configs = new UIConfig();
		if (loadConfig) {
			configs.parseFile();
		}

		// Set up the look and feel library to be used
		StyleManager.setLAF(configs.getLAF()); 
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		GraphicsDevice graphicsDevice = null;

		if (gs.length == 1) {
			logger.log(Level.CONFIG, "Detecting only one screen.");
			logger.config("1 screen detected.");	
		}
		else if (gs.length == 0) {
			throw new RuntimeException("No Screens Found.");
			// NOTE: what about the future server version of mars-sim in which no screen is needed.
		}
		else {
			logger.config(gs.length + " screens detected.");	
		}
		
		graphicsDevice = gs[0];
		int screenWidth = graphicsDevice.getDisplayMode().getWidth();
		int screenHeight = graphicsDevice.getDisplayMode().getHeight();


		// Set up the frame
		frame = new JFrame();
		frame.setResizable(true);
		frame.setMinimumSize(new Dimension(640, 640));

		// Set the UI configuration
		boolean useDefault = configs.useUIDefault();
		logger.config("useDefault is: " + useDefault);
	
		if (useDefault) {
			logger.config("Will calculate screen size for default display instead.");
			setUpCalculatedScreen(screenWidth, screenHeight, useDefault);
		}
		else {
			setUpSavedScreen();
		}

		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this, sim);

		// Set up other elements
		masterClock = sim.getMasterClock();
		init();

		// Show frame
		frame.setVisible(true);

		// Dispose the Splash Window
		disposeSplash();

		// Open all initial windows.
		desktop.openInitialWindows();
	}

	/**
	 * Asks if the player wants to use last saved screen configuration.
	 */
	private boolean askScreenConfig() {

		logger.config("Do you want to use the last saved screen configuration ?");
		logger.config("To proceed, please choose 'Yes' or 'No' button in the dialog box.");
		
		int reply = JOptionPane.showConfirmDialog(frame,
				"Do you want to use the last saved screen configuration", 
				"Screen Configuration", 
				JOptionPane.YES_NO_OPTION);
        return (reply == JOptionPane.YES_OPTION);
	}
	
	private void setUpSavedScreen() {
		selectedSize = configs.getMainWindowDimension();
		
		// Set frame size
		frame.setSize(selectedSize);
		logger.config("The last saved window dimension is "	
			+ selectedSize.width
			+ " x "
			+ selectedSize.height
			+ ".");
		
		// Display screen at a certain location
		frame.setLocation(configs.getMainWindowLocation());
		logger.config("The last saved frame starts at (" 
				+ configs.getMainWindowLocation().x
				+ ", "
				+ configs.getMainWindowLocation().y
				+ ").");
	}
	
	private void setUpCalculatedScreen(int screenWidth, int screenHeight, boolean useDefaults) {
		selectedSize = calculatedScreenSize(screenWidth, screenHeight, useDefaults);
		
		// Set frame size
		frame.setSize(selectedSize);
		
		logger.config("The default window dimension is "
				+ selectedSize.width
				+ " x "
				+ selectedSize.height
				+ ".");

		frame.setLocation(
			((screenWidth - selectedSize.width) / 2),
			((screenHeight - selectedSize.height) / 2)
		);
		
		logger.config("Use default configuration to set frame to the center of the screen.");	
		logger.config("The window frame is centered and starts at (" 
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
	private Dimension calculatedScreenSize(int screenWidth, int screenHeight, boolean useDefault) {
		logger.config("Current screen size is " + screenWidth + " x " + screenHeight);
		logger.config("useDefault is: " + useDefault);
		
		Dimension frameSize = null;
		if (useDefault) {
			frameSize = interactiveTerm.getSelectedScreen();
			logger.config("Use default screen configuration.");
			logger.config("Selected screen size is " + frameSize.width + " x " + frameSize.height);
		}
		else {
			// Use any stored size
			frameSize = configs.getMainWindowDimension();
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
//				else {
//					// proceed to the next
//				}
			}
			

			if (frameSize == null) {
				// Make frame size 80% of screen size.
				if (screenSize.width > 800) {
					frameSize = new Dimension(
						(int) Math.round(screenSize.getWidth() * .8),
						(int) Math.round(screenSize.getHeight() * .8)
					);
					logger.config("New window size is " + frameSize.width + " x " + frameSize.height);
				}
				else {
					frameSize = new Dimension(screenSize);
					logger.config("New window size is " + frameSize.width + " x " + frameSize.height);
				}
			}
		}
 
		return frameSize;
	}

	/**
	 * Get the selected screen size for the main window.
	 * @return
	 */
	Dimension getSelectedSize() {
		return selectedSize;
	}

	/**
	 * Initializes UI elements for the frame
	 */
	@SuppressWarnings("serial")
	private void init() {

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

		frame.addWindowStateListener(new WindowStateListener() {
			   public void windowStateChanged(WindowEvent e) {
				   int state = e.getNewState();
                   isIconified = (state == Frame.ICONIFIED);
				   if (state == Frame.MAXIMIZED_HORIZ
						   || state == Frame.MAXIMIZED_VERT)
//					   frame.update(getGraphics());
						logger.log(Level.CONFIG, "MainWindow set to maximum."); //$NON-NLS-1$
					repaint();
			   }
		});

    	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		changeTitle(false);

		frame.setIconImage(getIconImage());

		// Set up the main pane
		JPanel mainPane = new JPanel(new BorderLayout());
		frame.add(mainPane);

		// Set up the overlay pane
		JPanel contentPane = new JPanel(new BorderLayout());

		// Add desktop to the content pane
		// contextpane should be blocked when paused
		contentPane.add(desktop, BorderLayout.CENTER);
		mainPane.add(contentPane, BorderLayout.CENTER);

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);

		// Add toolToolbar to mainPane
		contentPane.add(toolToolbar, BorderLayout.NORTH);

		// Add bottomPane for holding unitToolbar and statusBar
		JPanel bottomPane = new JPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {
			@Override
			protected JButton createActionComponent(Action a) {
				return super.createActionComponent(a);
			}
		};

		unitToolbar.setBorder(new MarsPanelBorder());
		// Remove the toolbar border, to blend into figure contents
		unitToolbar.setBorderPainted(true);

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		bottomPane.add(unitToolbar, BorderLayout.CENTER);

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(configs.showUnitBar());
		toolToolbar.setVisible(configs.showToolBar());

		// Prepare menu
		MainWindowMenu mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Close the unit bar when starting up
		unitToolbar.setVisible(false);

		// Create the status bar
		JStatusBar statusBar = new JStatusBar(1, 1, 28);

		// Create speed buttons
		createSpeedButtons(statusBar);

		// Create overlay button
		blockingSwitch = createOverlayCheckBox();
		statusBar.addLeftComponent(blockingSwitch, false);

		// Create memory bar
		memoryBar = new JMemoryMeter();
		statusBar.addRightComponent(memoryBar, true);

		statusBar.addRightCorner();

		bottomPane.add(statusBar, BorderLayout.SOUTH);

		// Blocking image tht should be displayed in the overlap
		// Icon pauseIcon = getIcon("pause_orange");
        // blockingImage = new JLabel(
        // 		pauseIcon,
        //         SwingConstants.CENTER
        // );

		// Add this class to the master clock's listener
		masterClock.addClockListener(this, 1000L);
	}


	private JCheckBox createOverlayCheckBox() {
		JCheckBox checkBox = new JCheckBox("Pause Overlay On/Off", true);
		checkBox.setToolTipText("Turn on/off pause overlay in desktop");

		checkBox.addItemListener(e -> displayOverlay());

		return checkBox;
	}

	private void displayOverlay() {
		boolean isPaused = pauseSwitch.isSelected();
		boolean isBlocking = blockingSwitch.isSelected();

		// Need to display the blocking image on the content pane
		// if (isPaused && isBlocking) {
		// 	// Checkbox has been selected
		// 	layeredContent.add(blockingImage, 2);
		// } else {
		// 	// Checkbox has been unselected
		// 	if (blockingImage.isShowing()) {
		// 		layeredContent.remove(blockingImage);
		// 	}
		// };
	}

	private void createPauseSwitch() {
		pauseSwitch = new JToggleButton(PAUSE_ICON);
		pauseSwitch.setToolTipText("Pause or Resume the Simulation");
		pauseSwitch.addItemListener(i -> pauseSwitch.setIcon(pauseSwitch.isSelected() ? 
															PLAY_ICON : PAUSE_ICON)
								);
		pauseSwitch.setSelected(false);
					
		pauseSwitch.addActionListener(e -> 
			masterClock.setPaused(pauseSwitch.isSelected(), false)
			);	
	}

	/**
	 * Update the LAF style to a new value.
	 */
	public void updateLAF(String newStyle) {
		// Set up the look and feel library to be used
		if (StyleManager.setLAF(newStyle)) {
			SwingUtilities.updateComponentTreeUI(frame);
		}
	}

	/**
	 * Open orbit viewer
	 */
	public void openOrbitViewer() {
		if (orbitViewer == null) {
			orbitViewer = new OrbitViewer(desktop);
			return;
		}
        orbitViewer.setVisible(!orbitViewer.isVisible());
	}


	public void setOrbitViewer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}
	

	private void createSpeedButtons(JStatusBar statusBar) {
		// Add the decrease speed button
		JButton decreaseSpeed = new JButton();
		decreaseSpeed.setIcon(ImageLoader.getIconByName("speed/decrease"));
		decreaseSpeed.setToolTipText("Decrease the sim speed (aka time ratio)");

		decreaseSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!masterClock.isPaused()) {
					masterClock.decreaseSpeed();
				}
			};
		});

		statusBar.addLeftComponent(decreaseSpeed, false);

		// Create pause switch
		createPauseSwitch();
		statusBar.addLeftComponent(pauseSwitch, false);

		JButton increaseSpeed = new JButton();
		increaseSpeed.setIcon(ImageLoader.getIconByName("speed/increase"));
		increaseSpeed.setToolTipText("Increase the sim speed (aka time ratio)");

		increaseSpeed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!masterClock.isPaused()) {
					masterClock.increaseSpeed();
				}
			};
		});
		// Add the increase speed button
		statusBar.addLeftComponent(increaseSpeed, false);
	}

	/**
	 * Get the window's frame.
	 *
	 * @return the frame.
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Gets the main desktop panel.
	 *
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Performs the process of saving a simulation.
	 * Note: if defaultFile is false, displays a FileChooser to select the
	 * location and new filename to save the simulation.
	 *
	 * @param defaultFile is the default.sim file be used
	 */
	public void saveSimulation(boolean defaultFile) {
		File fileLocn = null;
		if (!defaultFile) {
			JFileChooser chooser = new JFileChooser(SimulationFiles.getSaveDir());
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			}
			else {
				return;
			}
		}

		// Request the save
		sim.requestSave(fileLocn, action -> {
			if (SimulationListener.SAVE_COMPLETED.equals(action)) {
				// Save the current main window ui config
				configs.saveFile(this);
			}
		});

		logger.log(Level.CONFIG, "Save requested"); 
	}

	/**
	 * Create a new unit button in toolbar.
	 *
	 * @param unit the unit the button is for.
	 */
	public void createUnitButton(Unit unit) {
		unitToolbar.createUnitButton(unit);
	}

	/**
	 * Disposes a unit button in toolbar.
	 *
	 * @param unit the unit to dispose.
	 */
	public void disposeUnitButton(Unit unit) {
		unitToolbar.disposeUnitButton(unit);
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		if (!masterClock.isPaused() && !sim.isSavePending()) {
			int reply = JOptionPane.showConfirmDialog(frame,
					"Are you sure you want to exit?", "Exiting the Simulation", JOptionPane.YES_NO_CANCEL_OPTION);
	        if (reply == JOptionPane.YES_OPTION) {

	        	frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	        	endSimulation();
	    		// Save the UI configuration.
	    		configs.saveFile(this);
	    		masterClock.exitProgram();
	    		frame.dispose();
	    		destroy();
	    		System.exit(0);
	        }

	        else {
	        	frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	        }
		}
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves
	 * the main menu running
	 */
	private void endSimulation() {
		sim.endSimulation();
	}


	/**
	 * Gets the unit toolbar.
	 *
	 * @return unit toolbar.
	 */
	public UnitToolBar getUnitToolBar() {
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
	 * Gets the lander hab icon instance
	 *
	 * @return
	 */
	public static Icon getLanderIcon() {
		return ImageLoader.getIcon(ImageLoader.ICON_DIR + LANDER_PNG);
	}

	public static Image getIconImage() {
		return ImageLoader.getImage(LANDER_PNG);
	}


	/**
	 * Starts the splash window frame
	 */
	public static void startSplash() {
        // Create a splash window
		if (splashWindow == null) {
			splashWindow = new SplashWindow();
		}

		splashWindow.setIconImage();
        splashWindow.display();
        splashWindow.getJFrame().setCursor(new Cursor(java.awt.Cursor.WAIT_CURSOR));
	}

	/**
	 * Disposes the splash window frame
	 */
	private static void disposeSplash() {
		if (splashWindow != null) {
			splashWindow.remove();
		}
		splashWindow = null;
	}

	private void changeTitle(boolean isPaused) {
		if (GameManager.getGameMode() == GameMode.COMMAND) {
			if (isPaused) {
				frame.setTitle(Simulation.TITLE + "  -  Command Mode" + "  -  [ P A U S E ]");
			} else {
				frame.setTitle(Simulation.TITLE + "  -  Command Mode");
			}
		} else {
			if (isPaused) {
				frame.setTitle(Simulation.TITLE + "  -  Sandbox Mode" + "  -  [ P A U S E ]");
			} else {
				frame.setTitle(Simulation.TITLE + "  -  Sandbox Mode");
			}
		}
	}

	public boolean isIconified() {
		return isIconified;
	}
	
	/**
	 * Get the UIConfig for this UI.
	 */
	public UIConfig getConfig() {
		return configs;
	}

	/** 
	 * Get the active simualation.
	 */
	public Simulation getSimulation() {
		return sim;
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
		if (pulse.getElapsed() > 0 && !isIconified) {
			// Increments the Earth and Mars clock labels.
			toolToolbar.incrementClocks(pulse.getMasterClock(), pulse.isNewSol());

			memoryBar.refresh();

			// Cascade the pulse
			desktop.clockPulse(pulse);
		}
	}

	/**
	 * Change the pause status. Called by Masterclock's firePauseChange() since
	 * TimeWindow is on clocklistener.
	 *
	 * @param isPaused true if set to pause
	 * @param showPane true if the pane will show up
	 */
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		changeTitle(isPaused);
		// Make sure the Pause button is synch'ed with the MasterClock state.
		if (isPaused != pauseSwitch.isSelected()) {
			pauseSwitch.setSelected(isPaused);
		}
		displayOverlay();
	}

	public static void setInteractiveTerm(InteractiveTerm i) {
		interactiveTerm = i;
	}

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		frame = null;
		unitToolbar = null;
		toolToolbar = null;
		desktop.destroy();
		desktop = null;
	}

}
