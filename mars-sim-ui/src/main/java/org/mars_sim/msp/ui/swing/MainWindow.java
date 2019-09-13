/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MarsClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.configeditor.CrewEditor;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.tool.AngledLinesWindowsCornerIcon;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;

//import com.alee.managers.UIManagers;
import com.alee.laf.WebLookAndFeel;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.nilo.plaf.nimrod.NimRODLookAndFeel;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * main desktop pane window are, status bar and tool bars.
 */
public class MainWindow 
extends JComponent {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MainWindow.class.getName());
//	private static String loggerName = logger.getName();
//	private static String sourceName = loggerName.substring(loggerName.lastIndexOf(".") + 1, loggerName.length());
	
	public static final int WIDTH = 1366;
	public static final int HEIGHT = 768;
	
	/** Icon image filename for frame */
	public static final String ICON_IMAGE = "/icons/landerhab16.png";//"/images/LanderHab.png";
	
	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'
	private static final String SOL = " Sol ";
//	private static final String themeSkin = "nimrod";
	private static final String WHITESPACES = "   ";
	
	private static final int TIME_DELAY = 960;

	public enum ThemeType {
		SYSTEM, NIMBUS, NIMROD, WEBLAF, METAL
	}

	public ThemeType defaultThemeType = ThemeType.WEBLAF;

	private static JFrame frame;

	// Data members
	private boolean useDefault = true;

	private int solCache = 0;

	private String lookAndFeelTheme;
	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private MainDesktopPane desktop;

	private MainWindowMenu mainWindowMenu;

	private final AtomicBoolean sleeping = new AtomicBoolean(false);

	private Timer delayTimer;
	private Timer delayTimer1;
	
	private javax.swing.Timer earthTimer;

	private JStatusBar statusBar;
	
	private JLabel leftLabel;
	private JLabel memMaxLabel;
	private JLabel memUsedLabel;
	
	private JLabel marsTimeLabel;
	private JLabel earthTimeLabel;
	
	private WebPanel bottomPane;
	private WebPanel mainPane;

	private JLayer<JPanel> jlayer;
	private WaitLayerUIPanel layerUI = new WaitLayerUIPanel();

	private int memMax;
	private int memUsed;
	private int memUsedCache;
	private int memFree;

	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock;// = sim.getMasterClock();
	private static EarthClock earthClock;// = masterClock.getEarthClock();
	private static MarsClock marsClock;// = masterClock.getMarsClock();

	/**
	 * Constructor 1.
	 * 
	 * @param cleanUI true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI) {
//		logger.config("MainWindow is on " + Thread.currentThread().getName() + " Thread");
		// this.cleanUI = cleanUI;
		
		// Set up the frame
		frame = new JFrame();
		frame.setSize(new Dimension(WIDTH, HEIGHT));
		frame.setResizable(false);
//		frame.setPreferredSize(new Dimension(1366, 768));
//		frame.setMinimumSize(new Dimension(1024, 600));
		
		// Set up the look and feel library to be used
		setLookAndFeel(defaultThemeType, ThemeType.NIMROD);
		
		// Disable the close button on top right
//		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// Set up MainDesktopPane
		desktop = new MainDesktopPane(this);

		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set look and feel of UI.
		useDefault = UIConfig.INSTANCE.useUIDefault();

		// Set the icon image for the frame.
		ImageIcon icon = new ImageIcon(CrewEditor.class.getResource(MainWindow.ICON_IMAGE));
		frame.setIconImage(iconToImage(icon));
		//		setIconImage();

		// Initialize UI elements for the frame
		init();

		// Set up timers for use on the status bar
		setupDelayTimer();

		// Add autosave timer
//		startAutosaveTimer();

		// Open all initial windows.
		desktop.openInitialWindows();

//		initializeWeblaf();

		// Set up timers for caching the settlement windows
		setupSettlementWindowTimer();
	}

	public static Image iconToImage(Icon icon) {
		   if (icon instanceof ImageIcon) {
		      return ((ImageIcon)icon).getImage();
		   } 
		   else {
		      int w = icon.getIconWidth();
		      int h = icon.getIconHeight();
		      GraphicsEnvironment ge = 
		        GraphicsEnvironment.getLocalGraphicsEnvironment();
		      GraphicsDevice gd = ge.getDefaultScreenDevice();
		      GraphicsConfiguration gc = gd.getDefaultConfiguration();
		      BufferedImage image = gc.createCompatibleImage(w, h);
		      Graphics2D g = image.createGraphics();
		      icon.paintIcon(null, g, 0, 0);
		      g.dispose();
		      return image;
		   }
		 }
	
	/**
	 * Initializes UI elements for the frame
	 */
	public void init() {
//		frame.setTitle(Simulation.title);
		masterClock = sim.getMasterClock();
		desktop.changeTitle(masterClock.isPaused());
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

		// Set up the main pane
		mainPane = new WebPanel(new BorderLayout());

		// Add the main pane to the frame
//		frame.setContentPane(mainPane);

		// Set up the glassy wait layer for pausing
		jlayer = new JLayer<>(mainPane, layerUI);
		frame.add(jlayer);
		
		// Add main pane
		mainPane.add(desktop, BorderLayout.CENTER);

		// Prepare menu
		mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		mainPane.add(toolToolbar, BorderLayout.NORTH);

		// Add bottomPane for holding unitToolbar and statusBar
		bottomPane = new WebPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {
			private static final long serialVersionUID = 1L;

			@Override
			protected JButton createActionComponent(Action a) {
				JButton jb = super.createActionComponent(a);
//				jb.setOpaque(false);
				return jb;
			}
		};

//		BasicToolBarUI ui = new BasicToolBarUI();
//		unitToolbar.setUI(ui);

		// unitToolbar.setOpaque(false);
		// unitToolbar.setBackground(new Color(0,0,0,0));
		unitToolbar.setBorder(new MarsPanelBorder());
		// Remove the toolbar border, to blend into figure contents
		unitToolbar.setBorderPainted(true);

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		bottomPane.add(unitToolbar, BorderLayout.CENTER);

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
		toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

		// Create the status bar
		statusBar = new JStatusBar();

		marsTimeLabel = new JLabel();
		marsTimeLabel.setHorizontalAlignment(JLabel.LEFT);
		TooltipManager.setTooltip(marsTimeLabel, "Mars Timestamp", TooltipWay.up);
		statusBar.setLeftComponent(marsTimeLabel, true);
		
		earthTimeLabel = new JLabel();
		earthTimeLabel.setHorizontalAlignment(JLabel.LEFT);
		TooltipManager.setTooltip(earthTimeLabel, "Earth Timestamp", TooltipWay.up);
		statusBar.setLeftComponent(earthTimeLabel, true);


		leftLabel = new JLabel();
		leftLabel.setText(SOL + "1");
		leftLabel.setHorizontalAlignment(JLabel.CENTER);
		TooltipManager.setTooltip(leftLabel, "# of sols since the beginning of the sim", TooltipWay.up);
		statusBar.add(leftLabel, 0);

		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1_000_000;

		memUsedLabel = new JLabel();
		memUsedLabel.setHorizontalAlignment(JLabel.RIGHT);
		int memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1_000_000;
		memUsed = memTotal - memFree;
		memUsedLabel.setText(memUsed + " MB");// "Used Memory : " + memUsed + " MB");
		TooltipManager.setTooltip(memUsedLabel, "Memory Used", TooltipWay.up);
		statusBar.addRightComponent(memUsedLabel, false);

		memMaxLabel = new JLabel();
		memMaxLabel.setHorizontalAlignment(JLabel.RIGHT);
		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1_000_000;
		memMaxLabel.setText("[ " + memMax + " MB ] ");// "Total Designated Memory : " + memMax + " MB");
		TooltipManager.setTooltip(memMaxLabel, "Memory Designated", TooltipWay.up);
		statusBar.addRightComponent(memMaxLabel, true);
		statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);

		bottomPane.add(statusBar, BorderLayout.SOUTH);

		// Set frame size
		final Dimension frame_size;
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		if (useDefault) {
			// Make frame size 80% of screen size.
			if (screen_size.width > 800) {
				frame_size = new Dimension((int) Math.round(screen_size.getWidth() * .9D),
						(int) Math.round(screen_size.getHeight() * .9D));
			} else {
				frame_size = new Dimension(screen_size);
			}
		} else {
			frame_size = UIConfig.INSTANCE.getMainWindowDimension();
		}
		frame.setSize(frame_size);

		// Set frame location.
		if (useDefault) {
			// Center frame on screen
			frame.setLocation(((screen_size.width - frame_size.width) / 2),
					((screen_size.height - frame_size.height) / 2));
		} else {
			frame.setLocation(UIConfig.INSTANCE.getMainWindowLocation());
		}

		// Show frame
//		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * Set up the timer for status bar
	 */
	public void setupDelayTimer() {
		if (delayTimer == null) {
			delayTimer = new Timer();
			delayTimer.schedule(new DelayTimer(), 300);
		}
	}

	/**
	 * Defines the delay timer class
	 */
	class DelayTimer extends TimerTask {
		public void run() {
			runStatusTimer();
		}
	}

	/**
	 * Set up the timer for caching settlement windows
	 */
	public void setupSettlementWindowTimer() {
		delayTimer1 = new Timer();
		delayTimer1.schedule(new DelayTimer2(), 2000);
	}

	/**
	 * Defines the delay timer class
	 */
	class DelayTimer2 extends TimerTask {
		public void run() {
			// Cache each settlement unit window
			desktop.cacheSettlementUnitWindow();
		}
	}

	public JPanel getBottomPane() {
		return bottomPane;
	}

//	/**
//	 * Start the auto save timer
//	 */
//	public void startAutosaveTimer() {
//		TimerTask timerTask = new TimerTask() {
//			@Override
//			public void run() {
//				autosaveTimer.cancel();
////				saveSimulation(true, true);
////				startAutosaveTimer();
//			}
//		};
//		autosaveTimer = new Timer();
//		autosaveTimer.schedule(timerTask, 1000 * 60 * AUTOSAVE_EVERY_X_MINUTE);
//	}

	/**
	 * Start the earth timer
	 */
	public void runStatusTimer() {
//		logger.config("runStatusTimer()");
		earthTimer = new javax.swing.Timer(TIME_DELAY, new ActionListener() {
//		String earthTime = null;
			@Override
			public void actionPerformed(ActionEvent evt) {
//				logger.config("runStatusTimer()'s actionPerformed()");
//				try {
				// Check if new simulation is being created or loaded from file.
//					if (!Simulation.isUpdating()) {
////						MasterClock master = sim.getMasterClock();
//						if (masterClock == null) {
//							throw new IllegalStateException("master clock is null");
//						}
////						EarthClock earthclock = master.getEarthClock();
//						if (earthClock == null) {
//							throw new IllegalStateException("earthclock is null");
//						}
//						earthTime = earthClock.getTimeStampF0();
//					}
//				} catch (Exception ee) {
//					ee.printStackTrace(System.err);
//				}

				if (earthClock == null) {
					masterClock = sim.getMasterClock();
					earthClock = masterClock.getEarthClock();
					marsClock = masterClock.getMarsClock();
				}

				earthTimeLabel.setText(WHITESPACES + earthClock.getTimeStampF1() + WHITESPACES);

				marsTimeLabel.setText(WHITESPACES + marsClock.getTrucatedDateTimeStamp()+ WHITESPACES);
				
				int memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1_000_000;
				int memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1_000_000;
				int memUsed = memTotal - memFree;

				if (memUsed > memUsedCache * 1.1 && memUsed < memUsedCache * 0.9) {
					memUsedCache = memUsed;
					memUsedLabel.setText(
//							"Used Memory : " + 
							memUsed + " MB" + WHITESPACES);
				}

				int sol = marsClock.getMissionSol();
				if (solCache != sol) {
					solCache = sol;
					leftLabel.setText(SOL + sol);
				}

//				// Check on whether autosave is due
//				if (masterClock.getAutosave()) {
//					// Trigger an autosave instance
//					saveSimulation(true, true);
//					masterClock.setAutosave(false);
//				}
				
				// Check if the music track should be played
				desktop.getSoundPlayer().playRandomMusicTrack();
			}
		});

		earthTimer.start();
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
	 * Gets the Main Window Menu.
	 * 
	 * @return mainWindowMenu
	 */
	public MainWindowMenu getMainWindowMenu() {
		return mainWindowMenu;
	}

	/**
	 * Load a previously saved simulation.
	 */
	public void loadSimulation(boolean autosave) {
//		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
//			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
		loadSimulationProcess(autosave);
//				}
//			};
//			loadSimThread.start();
//		} else {
//			loadSimThread.interrupt();
//		}

	}

	/**
	 * Performs the process of loading a simulation.
	 * 
	 * @param autosave
	 */
	public static void loadSimulationProcess(boolean autosave) {
//		logger.config("MainWindow's loadSimulationProcess() is on " + Thread.currentThread().getName());

//		if (masterClock != null)
		sim.stop();

		String dir = null;
		String title = null;

		// Add autosave
		if (autosave) {
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		} else {
			dir = Simulation.SAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}

		JFileChooser chooser = new JFileChooser(dir);
		chooser.setDialogTitle(title); // $NON-NLS-1$
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
//			desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the
			// single steps.
//			sim.endSimulation();

//			logger.config("Done open annoucement window");

//			try {
//				desktop.clearDesktop();
//
//				if (earthTimer != null) {
//					earthTimer.stop();
//				}
//				earthTimer = null;
//
//			} catch (Exception e) {
//				// New simulation process should continue even if there's an exception in the
//				// UI.
//				logger.severe(e.getMessage());
//				e.printStackTrace(System.err);
//			}

//			logger.config("About to call loadSimulation()");
			sim.loadSimulation(chooser.getSelectedFile());
//			logger.config("Done calling loadSimulation()");

//			while (masterClock != null) {// while (masterClock.isLoadingSimulation()) {
//				try {
//					Thread.sleep(300L);
//				} catch (InterruptedException e) {
//					logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
//				}
//			}
//
//			desktop.disposeAnnouncementWindow();

//			try {
//				desktop.resetDesktop();
//			} catch (Exception e) {
//				// New simulation process should continue even if there's an exception in the
//				// UI.
//				logger.severe(e.getMessage());
//				e.printStackTrace(System.err);
//			}
//
//			if (masterClock != null)
//				sim.start(false);

//			startEarthTimer();
		}
	}

//	/**
//	 * Create a new simulation.
//	 */
//	public void newSimulation() {
//		if ((newSimThread == null) || !newSimThread.isAlive()) {
//			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
//					newSimulationProcess();
//					// Simulation.instance().runStartTask(false);
//				}
//			};
//			newSimThread.start();
//		} else {
//			newSimThread.interrupt();
//		}
//	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	void newSimulationProcess() {
		logger.config("newSimulationProces() is on " + Thread.currentThread().getName());

		if (JOptionPane.showConfirmDialog(desktop, Msg.getString("MainWindow.abandonRunningSim"), //$NON-NLS-1$
				UIManager.getString("OptionPane.titleText"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.creatingNewSim") + "  "); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the
			// single steps.
			sim.endSimulation();
			sim.endMasterClock();

			desktop.closeAllToolWindow();
			desktop.disposeAnnouncementWindow();

			try {
				desktop.clearDesktop();

				if (earthTimer != null) {
					earthTimer.stop();
				}
				earthTimer = null;

			} catch (Exception e) {
				// New simulation process should continue even if there's an exception in the
				// UI.
				logger.severe(e.getMessage());
				e.printStackTrace(System.err);
			}

			try {
				sim.startSimExecutor();
				// sim.runLoadConfigTask();
				sim.getSimExecutor().submit(new SimConfigTask(this));

			} catch (Exception e) {
				logger.warning("error in restarting a new sim.");
				e.printStackTrace();
			}

			try {
				desktop.resetDesktop();
			} catch (Exception e) {
				// New simulation process should continue even if there's an exception in the
				// UI.
				logger.severe(e.getMessage());
				e.printStackTrace(System.err);
			}

		}
	}

	public class SimConfigTask implements Runnable {
		MainWindow win;

		SimConfigTask(MainWindow win) {
			this.win = win;
		}

		public void run() {
			SimulationConfig.instance().loadConfig();
			new SimulationConfigEditor(SimulationConfig.instance(), null);
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * 
	 * @param loadingDefault Should the user be allowed to override location?
	 */
	public void saveSimulation(boolean loadingDefault, final boolean isAutosave) {
//		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
//			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
//				@Override
//				public void run() {
		saveSimulationProcess(loadingDefault, isAutosave);
//				}
//			};
//			saveSimThread.start();
//		} 
//		
//		else {
//			saveSimThread.interrupt();
//			stopSleeping();
//		}
	}

	/**
	 * Performs the process of saving a simulation.
	 */
	private void saveSimulationProcess(boolean loadingDefault, boolean isAutosave) {
//		logger.config("saveSimulationProcess() is on " + Thread.currentThread().getName());
		if (masterClock.isPaused()) {
			logger.config("Cannot save when the simulation is on pause.");
		}

		else {

			if (isAutosave) {
//				SwingUtilities.invokeLater(() -> {
//					desktop.disposeAnnouncementWindow();
					desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.autosavingSim") + "  "); //$NON-NLS-1$
//				});
//				layerUI.start();
				masterClock.setSaveSim(Simulation.AUTOSAVE, null);
//					sim.getSimExecutor().submit(() -> masterClock.setSaveSim(Simulation.AUTOSAVE, null));
			}

			else {
//				File fileLocn = null;
//				SwingUtilities.invokeLater(() -> {
//					desktop.disposeAnnouncementWindow();
					desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.savingSim") + "  "); //$NON-NLS-1$
//				});

				if (!loadingDefault) {
					JFileChooser chooser = new JFileChooser(Simulation.SAVE_DIR);
					chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
					if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
						final File fileLocn = chooser.getSelectedFile();
//						layerUI.start();
						masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn);
//							sim.getSimExecutor().submit(() -> masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn));
					} else {
						return;
					}
				}

				else {
//					layerUI.start();
//					if (fileLocn == null)
					masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
//						sim.getSimExecutor().submit(() -> masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null));
				}

			}

			sleeping.set(true);
			while (sleeping.get() && masterClock.isSavingSimulation()) {
				try {
					// Thread.sleep(interval);
					TimeUnit.MILLISECONDS.sleep(100L);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.log(Level.SEVERE, Msg.getString("MainWindow.log.sleepInterrupt") + ". " + e); //$NON-NLS-1$
					e.printStackTrace(System.err);
				}
				// do something here
			}

//			try {
//				
//				// Save the current main window ui config
//	//			UIConfig.INSTANCE.saveFile(this);
//		
//				while (keepSleeping && masterClock.isSavingSimulation())
//					TimeUnit.MILLISECONDS.sleep(100L);
//	
//			} catch (Exception e) {
//				logger.log(Level.SEVERE, Msg.getString("MainWindow.log.sleepInterrupt") + e); //$NON-NLS-1$
//				e.printStackTrace(System.err);
//			}

//	        

//			SwingUtilities.invokeLater(() -> {
				desktop.disposeAnnouncementWindow();
//			});
			
//			layerUI.stop();
		}
	}

	public void stopSleeping() {
		sleeping.set(false);
	}

//	/**
//	 * Save the current simulation. This displays a FileChooser to select the
//	 * location to save the simulation if the default is not to be used.
//	 * 
//	 * @param type
//	 */
//	public void saveSimulation(int type) {
//		if (!masterClock.isPaused()) {
//			// hideWaitStage(PAUSED);
//			if (type == Simulation.SAVE_DEFAULT || type == Simulation.SAVE_AS) {
//				desktop.disposeAnnouncementWindow();
//				desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.savingSim") + "  "); //$NON-NLS-1$
//			}
//			
//			else {
//				desktop.disposeAnnouncementWindow();
//				desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.autosavingSim") + "  "); //$NON-NLS-1$
//				masterClock.setSaveSim(Simulation.AUTOSAVE, null);
//			}
//
//			saveExecutor.execute(new Task<Void>() {
//				@Override
//				protected Void call() throws Exception {
//					saveSimulationProcess(type);
//					while (masterClock.isSavingSimulation())
//						TimeUnit.MILLISECONDS.sleep(200L);
//					return null;
//				}
//
//				@Override
//				protected void succeeded() {
//					super.succeeded();
//					desktop.disposeAnnouncementWindow();
//				}
//			});
//
//		}
//		// endPause(previous);
//	}
//
//
//	/**
//	 * Performs the process of saving a simulation.
//	 */
//	private void saveSimulationProcess(int type) {
//		// logger.config("MainScene's saveSimulationProcess() is on " +
//		// Thread.currentThread().getName() + " Thread");
//		fileLocn = null;
//		dir = null;
//		title = null;
//
//		hideWaitStage(PAUSED);
//
//		if (type == Simulation.AUTOSAVE) {
//			dir = Simulation.AUTOSAVE_DIR;
//			masterClock.setSaveSim(Simulation.AUTOSAVE, null);
//
//		} else if (type == Simulation.SAVE_DEFAULT) {
//			dir = Simulation.DEFAULT_DIR;
//			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
//
//		} else if (type == Simulation.SAVE_AS) {
//
//			Platform.runLater(() -> {
//				FileChooser chooser = new FileChooser();
//				dir = Simulation.DEFAULT_DIR;
//				File userDirectory = new File(dir);
//				title = Msg.getString("MainScene.dialogSaveSim");
//				chooser.setTitle(title); // $NON-NLS-1$
//				chooser.setInitialDirectory(userDirectory);
//				// Set extension filter
//				FileChooser.ExtensionFilter simFilter = new FileChooser.ExtensionFilter("Simulation files (*.sim)",
//						"*.sim");
//				FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("all files (*.*)", "*.*");
//				chooser.getExtensionFilters().addAll(simFilter, allFilter);
//				File selectedFile = chooser.showSaveDialog(stage);
//				if (selectedFile != null)
//					fileLocn = selectedFile;
//				else {
//					hideWaitStage(PAUSED);
//					return;
//				}
//
//				showWaitStage(SAVING);
//
//				saveExecutor.execute(new Task<Void>() {
//					@Override
//					protected Void call() throws Exception {
//						try {
//							masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn);
//
//							while (masterClock.isSavingSimulation())
//								TimeUnit.MILLISECONDS.sleep(200L);
//
//						} catch (Exception e) {
//							logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
//							e.printStackTrace(System.err);
//						}
//
//						return null;
//					}
//
//					@Override
//					protected void succeeded() {
//						super.succeeded();
//						hideWaitStage(SAVING);
//					}
//				});
//			});
//		}
//	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow("  " + Msg.getString("MainWindow.pausingSim") + "  "); //$NON-NLS-1$
		masterClock.setPaused(true, false);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		masterClock.setPaused(false, false);
		desktop.disposeAnnouncementWindow();
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
		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
//		Simulation sim = Simulation.instance();
//		try {
//			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
//		} catch (Exception e) {
//			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
//			e.printStackTrace(System.err);
//		}
		endSimulationClass();
		masterClock.exitProgram();
		System.exit(0);
		destroy();
	}

	/**
	 * Ends the current simulation, closes the JavaFX stage of MainScene but leaves
	 * the main menu running
	 */
	private void endSimulationClass() {
		sim.endSimulation();
		sim.getSimExecutor().shutdown();// .shutdownNow();
	}

	/*
	 * Sets the theme skin after calling stage.show() at the start of the sim
	 */
	public void initializeTheme() {
//		if (OS.contains("linux"))
//			SwingUtilities.invokeLater(() -> setLookAndFeel(defaultThemeType, ThemeType.NIMROD));
//		else
		SwingUtilities.invokeLater(() -> initializeWeblaf());//setLookAndFeel(defaultThemeType, ThemeType.NIMROD));

	}

	public void initializeWeblaf() {
//		if (choice0 == ThemeType.WEBLAF) {
		try {
			// use the weblaf skin
			UIManager.setLookAndFeel(new WebLookAndFeel());
//				WebLookAndFeel.setForceSingleEventsThread ( true );
//			WebLookAndFeel.install();
//			UIManagers.initialize();
//				changed = true;

//				logger.config(UIManager.getLookAndFeel().getName() + " is used in MainWindow.");

		} catch (Exception e) {
			logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
		}
//		}
	}

	/**
	 * Sets the look and feel of the UI
	 * 
	 * @param choice
	 */
	public void setLookAndFeel(ThemeType choice0, ThemeType choice1) {
		boolean changed = false;

		if (choice1 == ThemeType.METAL) {

			try {
				UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");// UIManager.getCrossPlatformLookAndFeelClassName());//.getSystemLookAndFeelClassName());

//				logger.config(UIManager.getLookAndFeel().getName() + " is used in MainWindow.");

				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeWeblaf();

		}

		else if (choice1 == ThemeType.SYSTEM) {

			try {

				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				changed = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}

			initializeWeblaf();
		}

		else if (choice1 == ThemeType.NIMROD) {

			initializeWeblaf();
			
//			try {
//				NimRODTheme nt = new NimRODTheme(
//						getClass().getClassLoader().getResource("theme/" + themeSkin + ".theme")); //
//				NimRODLookAndFeel.setCurrentTheme(nt); // must be declared non-static or not
//				// working if switching to a brand new .theme file
//				NimRODLookAndFeel nf = new NimRODLookAndFeel();
//				nf.setCurrentTheme(nt); // must be declared non-static or not working if switching to a brand new .theme
//										// // file
//				UIManager.setLookAndFeel(nf);
//				changed = true; //
//	
//			} catch (Exception e) {
//				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$ } }
//			}
			
			try {
				UIManager.setLookAndFeel(new NimRODLookAndFeel());
			} catch (UnsupportedLookAndFeelException e) {
				e.printStackTrace();
			}
		}

		else if (choice1 == ThemeType.NIMBUS) {

			try {
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) {
						// Set Nimbus look & feel if found in JVM.

						// see https://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/color.html
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						// themeSkin = "nimbus";
						changed = true;
						// break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());

					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}

			initializeWeblaf();
		}

		if (changed) {

//			logger.config(UIManager.getLookAndFeel().getName() + " is used in MainWindow.");

			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateUnitWindowLF();
//				SwingUtilities.updateComponentTreeUI(desktop);
				// desktop.updateAnnouncementWindowLF();
				// desktop.updateTransportWizardLF();
			}

			frame.validate();
			frame.repaint();
//			SwingUtilities.updateComponentTreeUI(frame);
			frame.pack();

		}
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

	public String getLookAndFeelTheme() {
		return lookAndFeelTheme;
	}

	public void setupMainWindow() {
		new Timer().schedule(new WindowDelayTimer(), 1000);
	}
	
	/**
	 * Defines the delay timer class
	 */
	class WindowDelayTimer extends TimerTask {
		public void run() {
			// Create main window
			SwingUtilities.invokeLater(() -> new MainWindow(true));
		}
	}
	
	public WebPanel getMainPane() {
		return mainPane;
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
		mainWindowMenu = null;
//		mgr = null;
//		newSimThread = null;
//		loadSimThread = null;
//		saveSimThread = null;
		delayTimer = null;
//		autosaveTimer = null;
		earthTimer = null;
		statusBar = null;
		leftLabel = null;
		memMaxLabel = null;
		memUsedLabel = null;
		earthTimeLabel = null;
		bottomPane = null;
		mainPane = null;
		sim = null;
		masterClock = null;
		earthClock = null;
	}
}

class WaitLayerUIPanel extends LayerUI<JPanel> implements ActionListener {

	private boolean mIsRunning;
	private boolean mIsFadingOut;
	private javax.swing.Timer mTimer;
	private int mAngle;
	private int mFadeCount;
	private int mFadeLimit = 15;

	@Override
	public void paint(Graphics g, JComponent c) {
		int w = c.getWidth();
		int h = c.getHeight();
		super.paint(g, c); // Paint the view.
		if (!mIsRunning) {
			return;
		}
		Graphics2D g2 = (Graphics2D) g.create();
		float fade = (float) mFadeCount / (float) mFadeLimit;
		Composite urComposite = g2.getComposite(); // Gray it out.
		if (.5f * fade < 0.0f) {
			fade = 0;
		}
		else if (.5f * fade > 1.0f) {
			fade = 1;
		}
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .5f * fade));
		g2.fillRect(0, 0, w, h);
		g2.setComposite(urComposite);
		int s = Math.min(w, h) / 5;// Paint the wait indicator.
		int cx = w / 2;
		int cy = h / 2;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setStroke(new BasicStroke(s / 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2.setPaint(Color.white);
		g2.rotate(Math.PI * mAngle / 180, cx, cy);
		for (int i = 0; i < 12; i++) {
			float scale = (11.0f - (float) i) / 11.0f;
			g2.drawLine(cx + s, cy, cx + s * 2, cy);
			g2.rotate(-Math.PI / 6, cx, cy);
			if (scale * fade < 0.0f) {
				fade = 0;
			}
			else if (scale * fade > 1.0f) {
				fade = 1;
			}
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, scale * fade));
		}
		g2.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (mIsRunning) {
			firePropertyChange("tick", 0, 1);
			mAngle += 3;
			if (mAngle >= 360) {
				mAngle = 0;
			}
			if (mIsFadingOut) {
				if (--mFadeCount == 0) {
					mIsRunning = false;
					mTimer.stop();
				}
			} else if (mFadeCount < mFadeLimit) {
				mFadeCount++;
			}
		}
	}
	
	public void start() {
		if (mIsRunning) {
			return;
		}
		mIsRunning = true;// Run a thread for animation.
		mIsFadingOut = false;
		mFadeCount = 0;
		int fps = 24;
		int tick = 1000 / fps;
		mTimer = new javax.swing.Timer(tick, this);
		mTimer.start();
	}

	public void stop() {
		mIsFadingOut = true;
//		mIsRunning = false;
	}

	@Override
	public void applyPropertyChange(PropertyChangeEvent pce, JLayer l) {
		if ("tick".equals(pce.getPropertyName())) {
			l.repaint();
		}
	}
}
