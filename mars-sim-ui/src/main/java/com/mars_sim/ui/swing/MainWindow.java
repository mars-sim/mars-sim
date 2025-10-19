/*
 * Mars Simulation Project
 * MainWindow.java
 * @date 2025-10-18
 * @author Scott Davis
 */
package com.mars_sim.ui.swing;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Taskbar;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicToolBarUI;

import com.formdev.flatlaf.util.SystemInfo;
import com.mars_sim.core.GameManager;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.SimulationListener;
import com.mars_sim.core.SimulationRuntime;
import com.mars_sim.core.Unit;
import com.mars_sim.core.time.ClockListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.MasterClock;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.tools.helpgenerator.HelpLibrary;
import com.mars_sim.ui.swing.components.JMemoryMeter;
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.terminal.MarsTerminal;
import com.mars_sim.ui.swing.tool.JStatusBar;
import com.mars_sim.ui.swing.tool.guide.GuideWindow;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * main desktop pane window are, status bar and tool bars.
 */
public class MainWindow
		extends JComponent implements ClockListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

	public static final int HEIGHT_STATUS_BAR = 25;
	
	/** Icon image filename for frame */
    private static final String ICON_DIR = "/icons/";
	public static final String LANDER_91_PNG = "lander_hab91.png";
	public static final String LANDER_64_PNG = "lander_hab64.png";
	public static final String LANDER_16 = "lander16";
	
	public static final String LANDER_91_PATH = ICON_DIR + LANDER_91_PNG;
	
	private static final String SHOW_UNIT_BAR = "show-unit-bar";
	private static final String SHOW_TOOL_BAR = "show-tool-bar";
	private static final String MAIN_PROPS = "main-window";
	private static final String EXTERNAL_BROWSER = "use-external";

	private static final String VOLUME = "volume";
	
	/** The main window frame. */
	private static JFrame frame;

	private transient UIConfig uiconfigs;

	private MarsTerminal terminal;

	// Data members
	private boolean isIconified = false;

	private int millisolIntCache;
	
	private int initX;
	private int initY;
	
	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private Dimension selectedSize;
	
	private Simulation sim;
	
	private MasterClock masterClock;

	private JMemoryMeter memoryBar;

	/** The control of play or pause the simulation. */
	private JToggleButton playPauseSwitch;
	
	private transient HelpLibrary helpLibrary;

	private boolean useExternalBrowser;

	/**
	 * Constructor 1.
	 *
	 * @param cleanUI true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI, Simulation sim) {
		this.sim = sim;

		logger.config("Starting as " + GameManager.getGameMode());

		// Set Apache Batik library system property so that it doesn't output:
		// "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" in system err.
		System.setProperty("org.apache.batik.warn_destination", "false");

		// Load a UI Config instance according to the user's choice
		boolean loadConfig = true;
		if (cleanUI) {
			loadConfig = askScreenConfig();
		}
		uiconfigs = new UIConfig();
		if (loadConfig) {
			uiconfigs.parseFile();
		}

		// Set up the look and feel library to be used
		StyleManager.setStyles(uiconfigs.getPropSets());

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

		// Setup before loading defaults
		terminal = new MarsTerminal(sim);

		// Set the UI configuration
		boolean useDefault = uiconfigs.useUIDefault();

		if (useDefault) {
			logger.config("Will calculate screen size for default display instead.");
			setUpDefaultScreen(graphicsDevice, screenWidth, screenHeight, useDefault);
		} else {
			setUpSavedScreen();
		}

		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this, sim);

		// Set up other elements
		masterClock = sim.getMasterClock();
		
		init();

		// Show frame
		frame.setVisible(true);

		// Open all initial windows.
		desktop.openInitialWindows();
		
		if (desktop.getSoundPlayer() == null)
			return;
		
		// Starts a background sound track.
		desktop.playBackgroundMusic();
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

	/**
	 * Sets up the screen config used from last saved session.
	 */
	private void setUpSavedScreen() {
		// For the Main Window	
		selectedSize = uiconfigs.getMainWindowDimension();

		// Set frame size
		frame.setSize(selectedSize);
		logger.config("Last saved window dimension: "
				+ selectedSize.width
				+ " x "
				+ selectedSize.height
				+ ".");

		// Display screen at a certain location
		frame.setLocation(uiconfigs.getMainWindowLocation());
		logger.config("Last saved main window's frame starts at ("
				+ uiconfigs.getMainWindowLocation().x
				+ ", "
				+ uiconfigs.getMainWindowLocation().y
				+ ").");
		
		
		// For Mars Terminal
		// Set frame size
		terminal.positionTerminal(uiconfigs.getMarsTerminalLocation(), uiconfigs.getMarsTerminalDimension());
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
		
		
		// Set Mars Terminal frame size
		var terminalSize = calculatedScreenSize(gd, screenWidth, screenHeight, useDefaults, uiconfigs.getMarsTerminalDimension());

		terminal.positionTerminal(new Point(((screenWidth - terminalSize.width) / 2),
										((screenHeight - terminalSize.height) / 2)),
				terminalSize);
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
	 */
	private void init() {
	
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

		frame.addWindowStateListener(e -> {
			int state = e.getNewState();
			isIconified = (state == Frame.ICONIFIED);
			if (state == Frame.MAXIMIZED_HORIZ
					|| state == Frame.MAXIMIZED_VERT)
				logger.log(Level.CONFIG, "MainWindow set to maximum."); //$NON-NLS-1$
			repaint();
		});
	
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		changeTitle(false);

		if (SystemInfo.isMacOS) {
			final Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
			Image image = defaultToolkit.getImage(getClass().getResource(LANDER_91_PATH));
			final Taskbar taskbar = Taskbar.getTaskbar();
			taskbar.setIconImage(image);
			
			// Move the menu bar out of the main window to the top of the screen
			System.setProperty( "apple.laf.useScreenMenuBar", "true" );
			
			// Change the appearance of window title bars
	        // 	 Possible values:
	        //   - "system": use current macOS appearance (light or dark)
	        //   - "NSAppearanceNameAqua": use light appearance
	        //   - "NSAppearanceNameDarkAqua": use dark appearance
	        // Note: Must be set on main thread and before AWT/Swing is initialized;
	        //       Setting it on AWT thread does not work)
//	        System.setProperty( "apple.awt.application.appearance", "system" );
			
//			if (SystemInfo.isMacFullWindowContentSupported) {
//			    frame.getRootPane().putClientProperty( "apple.awt.fullWindowContent", true );
//			    frame.getRootPane().putClientProperty( "apple.awt.transparentTitleBar", true );
//			}
		}
		else {
			frame.setIconImage(getIconImage());
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

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		toolToolbar.requestFocusInWindow();
		
		// Add toolToolbar to mainPane
		contentPane.add(toolToolbar, BorderLayout.NORTH);

		// Add a floating tool bar
		JToolBar floatingBar = new JToolBar();
		floatingBar.setFloatable(true);
		JPanel floatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
		floatPanel.add(floatingBar);
		
		floatingBar.add(createSpeedBar());
		
		floatingBar.add(createVolumeSlider());

		contentPane.add(floatPanel, BorderLayout.SOUTH);
		
//		BasicToolBarUI ui = (BasicToolBarUI) floatingBar.getUI();
//		// Sets the toolbar to float at the specified point
//		ui.setFloating(true, new Point(initX, initY));
		
		// Add bottomPane for holding unitToolbar and statusBar
		JPanel bottomPane = new JPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this);

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
		MainWindowMenu mainWindowMenu = new MainWindowMenu(this, desktop);
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

		// Add this class to the master clock's listener
		masterClock.addClockListener(this, 1000L);
	}

	  
		/**
		 * Creates the speed bar and buttons.
		 * 
		 * @return
		 */
		private JPanel createSpeedBar() {
			
			JPanel speedPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			speedPanel.setToolTipText("Speed Control");
			
			// Add the decrease speed button
			JButton decreaseSpeed = new JButton("\u2212");//u23EA");
			decreaseSpeed.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
			decreaseSpeed.setPreferredSize(new Dimension(25, 25));
//			decreaseSpeed.setMaximumSize(new Dimension(25, 25));
			decreaseSpeed.setToolTipText("Decrease the sim speed (aka time ratio)");
			
			decreaseSpeed.addActionListener(e -> {
				if (!masterClock.isPaused()) {
					masterClock.decreaseSpeed();
				}
			});
			
			// Create pause switch
			createPauseSwitch();

			JButton increaseSpeed = new JButton("\u002B");//\u23E9");
			increaseSpeed.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
			increaseSpeed.setPreferredSize(new Dimension(25, 25));
//			increaseSpeed.setMaximumSize(new Dimension(25, 25));
			increaseSpeed.setToolTipText("Increase the sim speed (aka time ratio)");

			increaseSpeed.addActionListener(e -> {
				if (!masterClock.isPaused()) {
					masterClock.increaseSpeed();
				}
			});
			
			// Add the increase speed button
			speedPanel.add(decreaseSpeed);
			speedPanel.add(playPauseSwitch);
			speedPanel.add(increaseSpeed);
			
			return speedPanel;
		}
		
		/**
		 * Creates the pause button.
		 */
		private JToggleButton createPauseSwitch() {
			playPauseSwitch = new JToggleButton("\u23E8");
			playPauseSwitch.setFont(new Font(Font.DIALOG, Font.BOLD, 13));
			playPauseSwitch.setPreferredSize(new Dimension(25, 25));
//			playPauseSwitch.setMaximumSize(new Dimension(25, 25));
			playPauseSwitch.setToolTipText("Pause or Resume the Simulation");
			playPauseSwitch.addActionListener(e -> {
					boolean isSel = playPauseSwitch.isSelected();
					if (isSel) {
						// To show play symbol
						playPauseSwitch.setText("\u23F5");
					}
					else {
						// To show pause symbol 
						playPauseSwitch.setText("\u23F8");
					}		
					masterClock.setPaused(isSel, false);	
				});
			playPauseSwitch.setText("\u23F8");
			
			return playPauseSwitch;
		}
		
		public JToggleButton getPlayPauseSwitch() {
			return playPauseSwitch;
		}
		
		/**
		 * Creates the music volume slider.
		 * 
		 * @return 
		 */
		private JPanel createVolumeSlider() {

			Icon icon = ImageLoader.getIconByName(VOLUME);
			JPanel volPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
			volPanel.setToolTipText("Volume Control");
			volPanel.add(new JLabel(icon));
			
	        JSlider slider = new JSlider();
	        slider.setMajorTickSpacing(50);
	        slider.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 6));
	        slider.setPaintLabels(true);
	        slider.setToolTipText("Music and Sound Volume Slider");
	        volPanel.add(slider);
	        slider.setPreferredSize(new Dimension(160, 25));

	        //this is for setting the value
	        slider.addChangeListener(new ChangeListener() {

	            @Override
	            public void stateChanged(ChangeEvent e) {
	                JSlider src = (JSlider)e.getSource();

	                double value = src.getValue() / 100.0;
	                
	                try {
	                	getAudioPlayer().setMusicVolume(value);

	                } catch (Exception ex) {
	                	logger.severe("Unable to set new music volume: " + ex);
	                }
	            }
	        });

	        try {
	            slider.setValue((int) (AudioPlayer.DEFAULT_VOL * 100));
	        } catch (Exception e) {
	        	logger.severe("Unable to set new music volume: " + e);
	        }
	        
	        return volPanel;
	    }
		
	/**
	 * Updates the LAF style to a new value.
	 */
	public void updateLAF(String newStyle) {
		// Set up the look and feel library to be used
		if (StyleManager.setLAF(newStyle)) {
			SwingUtilities.updateComponentTreeUI(frame);
		}
	}

//	private void createSpeedButtons(ToolToolBar toolBar) {
//		
//		JPanel panel = new JPanel(new BorderLayout());
//		JPanel speedPanel = new JPanel(new GridLayout(1, 3));
//		panel.add(speedPanel, BorderLayout.EAST);
//		
//		// Add the decrease speed button
//		JButton decreaseSpeed = new JButton("\u23EA");
//		decreaseSpeed.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
//		decreaseSpeed.setPreferredSize(new Dimension(30, 30));
//		decreaseSpeed.setMaximumSize(new Dimension(30, 30));
//		decreaseSpeed.setToolTipText("Decrease the sim speed (aka time ratio)");
//		
//		decreaseSpeed.addActionListener(e -> {
//			if (!masterClock.isPaused()) {
//				masterClock.decreaseSpeed();
//			}
//		});
//		
//		// Create pause switch
//		createPauseSwitch();
//
//		JButton increaseSpeed = new JButton("\u23E9");
//		increaseSpeed.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
//		increaseSpeed.setPreferredSize(new Dimension(30, 30));
//		increaseSpeed.setMaximumSize(new Dimension(30, 30));
//		increaseSpeed.setToolTipText("Increase the sim speed (aka time ratio)");
//
//		increaseSpeed.addActionListener(e -> {
//			if (!masterClock.isPaused()) {
//				masterClock.increaseSpeed();
//			}
//		});
//		
//		// Add the increase speed button
//		speedPanel.add(decreaseSpeed);
//		speedPanel.add(playPauseSwitch);
//		speedPanel.add(increaseSpeed);
//		toolBar.add(panel);
//		
//	}
//
//	/**
//	 * Creates the pause button.
//	 */
//	private void createPauseSwitch() {
//		playPauseSwitch = new JToggleButton("\u23E8");
//		playPauseSwitch.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
//		playPauseSwitch.setPreferredSize(new Dimension(30, 30));
//		playPauseSwitch.setMaximumSize(new Dimension(30, 30));
//		playPauseSwitch.setToolTipText("Pause or Resume the Simulation");
//		playPauseSwitch.addActionListener(e -> {
//				boolean isSel = playPauseSwitch.isSelected();
//				if (isSel) {
//					// To show play symbol
//					playPauseSwitch.setText("\u23F5");
//				}
//				else {
//					// To show pause symbol 
//					playPauseSwitch.setText("\u23F8");
//				}		
//				masterClock.setPaused(isSel, false);	
//			});
//		playPauseSwitch.setText("\u23F8");
//	}

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
	 * Gets the audio player.
	 * 
	 * @return
	 */
	public AudioPlayer getAudioPlayer() {
		return desktop.getSoundPlayer();
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
			JFileChooser chooser = new JFileChooser(SimulationRuntime.getSaveDir());
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			} else {
				return;
			}
		}

		// Request the save
		sim.requestSave(fileLocn, action -> {
			if (SimulationListener.SAVE_COMPLETED.equals(action)) {
				// Save the current main window ui config
				uiconfigs.saveFile(this);
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
	 * Exits the running simulation.
	 */
	public void exitSimulation() {
		if (!masterClock.isPaused() && !sim.isSavePending()) {
			int reply = JOptionPane.showConfirmDialog(frame,
					"Are you sure you want to exit?", "Exiting the Simulation", JOptionPane.YES_NO_CANCEL_OPTION);
			if (reply == JOptionPane.YES_OPTION) {

				frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

				endSimulation();
				// Save the UI configuration.
				uiconfigs.saveFile(this);
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
	 * Gets the lander hab icon instance.
	 *
	 * @return
	 */
	public static Icon getLanderIcon() {
		return ImageLoader.getIconByName(LANDER_16);
	}

	/**
	 * Gets the lander hab image icon instance.
	 *
	 * @return
	 */
	public static Image getIconImage() {
		return ImageLoader.getImage(LANDER_91_PNG);
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
	 * Is it iconified ?
	 * 
	 * @return
	 */
	public boolean isIconified() {
		return isIconified;
	}

	/**
	 * Gets the UIConfig for this UI.
	 */
	public UIConfig getConfig() {
		return uiconfigs;
	}

	/**
	 * Gets the UI properties of the application.
	 */
	public Map<String, Properties> getUIProps() {
		Map<String, Properties> result = new HashMap<>();

		// Add the Style manager details
		result.putAll(StyleManager.getStyles());

		// Add any Desktop properties
		result.putAll(desktop.getUIProps());

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
			desktop.clockPulse(pulse);
			
			if (desktop.getSoundPlayer() == null)
				return;
			
			int now = pulse.getMarsTime().getMillisolInt();	
//			logger.info("now: " + now);
			if (now != millisolIntCache && now != 1000 && now % 15 == 2) {

				desktop.getSoundPlayer().loopThruBackgroundMusic();
				
				millisolIntCache = now;
			}
		}
	}

	/**
	 * Changes the pause status.
	 *
	 * @param isPaused true if set to pause
	 * @param showPane true if the pane will show up
	 */
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		changeTitle(isPaused);
		// Make sure the Pause button is synch'ed with the MasterClock state.
		if (isPaused != playPauseSwitch.isSelected()) {
			playPauseSwitch.setSelected(isPaused);
		}
		
		if (desktop.getSoundPlayer() == null)
			return;
		
		if (isPaused) {
			desktop.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			desktop.getSoundPlayer().muteMusic();
		}
		else {
			desktop.getParent().setCursor(Cursor.getDefaultCursor());
			desktop.getSoundPlayer().unmuteMusic();
		}
	}

	/**
	 * Returns the reference of the Mars Terminal.
	 * 
	 * @return
	 */
	public MarsTerminal getMarsTerminal() {
		return terminal;
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
	 * Gets the help library.
	 * 
	 * @param helpPage
	 */
	public HelpLibrary getHelp() {
		if (helpLibrary == null) {
			try {
				helpLibrary = HelpLibrary.createDefault(sim.getConfig());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Problem loading help library", e);
			}
		}

		return helpLibrary;
	}

	/**
	 * Displays a help page.
	 * 
	 * @param helpPage
	 */
	public void showHelp(String helpPage) {
		try {
			var library = getHelp();

			var  helpURI = library.getPage(helpPage);	
			if (useExternalBrowser) {
				Desktop.getDesktop().browse(helpURI);
			}
			else {
				GuideWindow ourGuide = (GuideWindow) desktop.openToolWindow(GuideWindow.NAME);
				ourGuide.displayURI(helpURI);
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Problem showing help page", e);
		}
    }

	/**
	 * Prepares the panel for deletion.
	 */
	public void destroy() {
		
		masterClock.removeClockListener(this);
		masterClock = null;
		
		frame = null;
		
		unitToolbar = null;
		toolToolbar = null;
		
		desktop.destroy();
		desktop = null;
		
		uiconfigs = null;

		selectedSize = null;
		
		sim = null;
		
		memoryBar = null;
	}
}
