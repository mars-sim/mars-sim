/**
/ * Mars Simulation Project
 * MainWindow.java
 * @version 3.1.0 2017-10-05
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.basic.BasicToolBarUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;

//import com.alee.managers.UIManagers;
import com.alee.laf.WebLookAndFeel;
import com.alee.managers.UIManagers;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * tool bars and main desktop pane.
 */
public class MainWindow extends JComponent {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(MainWindow.class.getName());

	/** Icon image filename for main window. */
	private static final String ICON_IMAGE = "/images/LanderHab.png";
	public static final String OS = System.getProperty("os.name").toLowerCase(); // e.g. 'linux', 'mac os x'

	// Data members

	private String lookAndFeelTheme;

	private static JFrame frame;
	/** The unit tool bar. */
	private UnitToolBar unitToolbar;
	/** The tool bar. */
	private ToolToolBar toolToolbar;
	/** The main desktop. */
	private static MainDesktopPane desktop;

	private MainWindowMenu mainWindowMenu;

//	private TransportWizard transportWizard;
//	private ConstructionWizard constructionWizard;
	private BuildingManager mgr; // mgr is very important for FINISH_BUILDING_PLACEMENT_EVENT

	private static Thread newSimThread;
	private static Thread loadSimThread;
	private static Thread saveSimThread;

	private Timer delayLaunchTimer;
	private Timer autosaveTimer;
	private static javax.swing.Timer earthTimer = null;
	private static int AUTOSAVE_EVERY_X_MINUTE = 15;
	private static final int TIME_DELAY = 960;

	// protected ShowDateTime showDateTime;
	private static JStatusBar statusBar;
	private static JLabel leftLabel;
	private static JLabel memMaxLabel;
	private static JLabel memUsedLabel;
	// private JLabel dateLabel;
	private static JLabel timeLabel;
	private static JPanel bottomPane;
	private static JPanel mainPane;

	private static int memMax;
	private static int memTotal;
	private static int memUsed, memUsedCache;
	private static int memFree;

	private static String earthTimeString;

	// private boolean cleanUI;
	private boolean useDefault;

	private String statusText;
	
	private static Simulation sim = Simulation.instance();
	private static MasterClock masterClock = sim.getMasterClock();
	private static EarthClock earthClock = masterClock.getEarthClock();
	
	/**
	 * Constructor 1.
	 * 
	 * @param cleanUI
	 *            true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI) {
		// this.cleanUI = cleanUI;
		if (OS.contains("linux"))
			setLookAndFeel(false, false);
		else
			setLookAndFeel(false, true);
		
		desktop = new MainDesktopPane(this);
		
		frame = new JFrame();

		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set look and feel of UI.
		useDefault = UIConfig.INSTANCE.useUIDefault();

		// Set the icon image for the frame.
		setIconImage();

		init();

		showStatusBar();
		// Add autosave timer
		startAutosaveTimer();
		// Open all initial windows.
		desktop.openInitialWindows();
		
		// Load the menu choice
		sim.getTerm().loadTerminalMenu();
	}


	public void init() {
		frame.setTitle(Simulation.title);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

		mainPane = new JPanel(new BorderLayout());
		frame.setContentPane(mainPane);

		// Add main pane
		mainPane.add(desktop, BorderLayout.CENTER);

		// Prepare menu
		mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		mainPane.add(toolToolbar, BorderLayout.NORTH);

		// Add bottomPane for holding unitToolbar and statusBar
		bottomPane = new JPanel(new BorderLayout());

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {

			private static final long serialVersionUID = 1L;

			@Override
			protected JButton createActionComponent(Action a) {
				JButton jb = super.createActionComponent(a);
				jb.setOpaque(false);
				return jb;
			}
		};

		BasicToolBarUI ui = new BasicToolBarUI();
		unitToolbar.setUI(ui);
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

		// Add statusBar
		statusBar = new JStatusBar();

		memMaxLabel = new JLabel();
		memMaxLabel.setHorizontalAlignment(JLabel.CENTER);
		memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1_000_000;
		memMaxLabel.setText("Total Designated Memory : " + memMax + " MB");
		statusBar.addRightComponent(memMaxLabel, false);

		memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1_000_000;

		memUsedLabel = new JLabel();
		memUsedLabel.setHorizontalAlignment(JLabel.CENTER);
		memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1_000_000;
		memUsed = memTotal - memFree;
		memUsedLabel.setText("Used Memory : " + memUsed + " MB");
		statusBar.addRightComponent(memUsedLabel, false);

		timeLabel = new JLabel();
		timeLabel.setHorizontalAlignment(JLabel.CENTER);
		statusBar.addRightComponent(timeLabel, false);

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
		frame.setVisible(true);

	}

	/**
	 * Updates time in the status bar
	 */
	public void showStatusBar() {
		// Use delayLaunchTimer to launch earthTime
		if (earthTimer == null) {
			delayLaunchTimer = new Timer();
			int millisec = 500;
			// Note: this delayLaunchTimer is non-repeating
			// thus period is N/A
			delayLaunchTimer.schedule(new StatusBar(), millisec);
		}
	}

	public JPanel getBottomPane() {
		return bottomPane;
	}

	/**
	 * Start the auto save timer
	 */
	public void startAutosaveTimer() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				autosaveTimer.cancel();
				saveSimulation(true, true);
				startAutosaveTimer();
			}
		};
		autosaveTimer = new Timer();
		autosaveTimer.schedule(timerTask, 1000 * 60 * AUTOSAVE_EVERY_X_MINUTE);
	}

	/**
	 * Start the earth timer
	 */
	public static void startEarthTimer() {

		earthTimer = new javax.swing.Timer(TIME_DELAY, new ActionListener() {
			String t = null;

			@Override
			public void actionPerformed(ActionEvent evt) {
				try {
					// Check if new simulation is being created or loaded from file.
					if (!Simulation.isUpdating()) {

//						MasterClock master = sim.getMasterClock();
						if (masterClock == null) {
							throw new IllegalStateException("master clock is null");
						}
//						EarthClock earthclock = master.getEarthClock();
						if (earthClock == null) {
							throw new IllegalStateException("earthclock is null");
						}
						t = earthClock.getTimeStampF0();

					}
				} catch (Exception ee) {
					ee.printStackTrace(System.err);
				}
				if (t != null)
					timeLabel.setText("Earth Date & Time : " + t);
				memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
				memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
				memUsed = memTotal - memFree;

				if (memUsed > memUsedCache * 1.1 || memUsed < memUsedCache * 0.9)
					memUsedLabel.setText("Used Memory : " + memUsed + " MB");
				memUsedCache = memUsed;
			}
		});

		earthTimer.start();
	}

	/**
	 * Defines the StatusBar class for running earth timer
	 */
	class StatusBar extends TimerTask { // (final String t) {
		public void run() {
			startEarthTimer();
		}
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
	public static void loadSimulation(boolean autosave) {
		final boolean ans = autosave;

		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					loadSimulationProcess(ans);
				}
			};
			loadSimThread.start();
		} else {
			loadSimThread.interrupt();
		}

	}

	/**
	 * Performs the process of loading a simulation.
	 */
	public static void loadSimulationProcess(boolean autosave) {
		logger.config("MainWindow's loadSimulationProcess() is on " + Thread.currentThread().getName());

		if (masterClock != null)
			sim.stop();

		String dir = null;
		String title = null;
		
		// Add autosave
		if (autosave) {
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		} else {
			dir = Simulation.DEFAULT_DIR;
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

	/**
	 * Create a new simulation.
	 */
	public void newSimulation() {
		if ((newSimThread == null) || !newSimThread.isAlive()) {
			newSimThread = new Thread(Msg.getString("MainWindow.thread.newSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					newSimulationProcess();
					// Simulation.instance().runStartTask(false);
				}
			};
			newSimThread.start();
		} else {
			newSimThread.interrupt();
		}

	}

	/**
	 * Performs the process of creating a new simulation.
	 */
	void newSimulationProcess() {
		logger.config("newSimulationProces() is on " + Thread.currentThread().getName());

		if (JOptionPane.showConfirmDialog(desktop, Msg.getString("MainWindow.abandonRunningSim"), //$NON-NLS-1$
				UIManager.getString("OptionPane.titleText"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.creatingNewSim")); //$NON-NLS-1$

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
			SimulationConfig.loadConfig();
			new SimulationConfigEditor(SimulationConfig.instance(), null);
			// });
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * 
	 * @param loadingDefault
	 *            Should the user be allowed to override location?
	 */
	public void saveSimulation(boolean loadingDefault, final boolean isAutosave) {
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					saveSimulationProcess(loadingDefault, isAutosave);
				}
			};
			saveSimThread.start();
		} else {
			saveSimThread.interrupt();
		}
	}

	/**
	 * Performs the process of saving a simulation.
	 */
	private void saveSimulationProcess(boolean loadingDefault, boolean isAutosave) {
		File fileLocn = null;

		if (!loadingDefault) {
			JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			} else {
				return;
			}
		}

//		MasterClock clock = Simulation.instance().getMasterClock();

		if (isAutosave) {
			desktop.disposeAnnouncementWindow();
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			masterClock.setSaveSim(Simulation.AUTOSAVE, null);
		} else {
			desktop.disposeAnnouncementWindow();
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			if (fileLocn == null)
				masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
			else
				masterClock.setSaveSim(Simulation.SAVE_AS, fileLocn);
		}

		while (masterClock.isSavingSimulation()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.sleepInterrupt"), e); //$NON-NLS-1$
			}
		}
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Pauses the simulation and opens an announcement window.
	 */
	public void pauseSimulation() {
		desktop.openAnnouncementWindow(Msg.getString("MainWindow.pausingSim")); //$NON-NLS-1$
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
	 * @param unit
	 *            the unit the button is for.
	 */
	public void createUnitButton(Unit unit) {
		unitToolbar.createUnitButton(unit);
	}

	/**
	 * Disposes a unit button in toolbar.
	 * 
	 * @param unit
	 *            the unit to dispose.
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
		try {
			masterClock.setSaveSim(Simulation.SAVE_DEFAULT, null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}

		masterClock.exitProgram();

		earthTimer = null;
	}

	/**
	 * Sets the look and feel of the UI
	 * 
	 * @param nativeLookAndFeel
	 *            true if native look and feel should be used.
	 */
	public void setLookAndFeel(boolean nativeLookAndFeel, boolean nimRODLookAndFeel) {
		boolean changed = false;

		// use the weblaf skin
		WebLookAndFeel.install();
		UIManagers.initialize();
		
		String currentTheme = UIManager.getLookAndFeel().getClass().getName();

		if (nativeLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
				lookAndFeelTheme = "system";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else if (nimRODLookAndFeel) {
			try {
//				UIManager.setLookAndFeel(new NimRODLookAndFeel());
//				changed = true;
//				lookAndFeelTheme = "nimrod";
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.lookAndFeelError"), e); //$NON-NLS-1$
			}
		} else {
			try {
				// Set Nimbus look & feel if found in JVM.
				boolean foundNimbus = false;
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if (info.getName().equals("Nimbus")) { //$NON-NLS-1$
						UIManager.setLookAndFeel(info.getClassName());
						foundNimbus = true;
						lookAndFeelTheme = "nimbus";
						changed = true;
						break;
					}
				}

				// Metal Look & Feel fallback if Nimbus not present.
				if (!foundNimbus) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
					UIManager.setLookAndFeel(new MetalLookAndFeel());
					lookAndFeelTheme = "metal";
					changed = true;
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, Msg.getString("MainWindow.log.nimbusError")); //$NON-NLS-1$
			}
		}

		if (changed) {

			frame.validate();
			frame.repaint();

			if (desktop != null) {
				desktop.updateToolWindowLF();
				desktop.updateAnnouncementWindowLF();

			}

		}
	}

	/**
	 * Sets the icon image for the main window.
	 */
	public void setIconImage() {

		String fullImageName = ICON_IMAGE;
		URL resource = ImageLoader.class.getResource(fullImageName);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Image img = kit.createImage(resource);
		frame.setIconImage(img);
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
}