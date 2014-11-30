/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 3.07 2014-11-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * tool bars and main desktop pane.
 */
public class MainWindow {

	public static final String WINDOW_TITLE = Msg.getString(
		"MainWindow.title", //$NON-NLS-1$
		Simulation.VERSION
	);

	private static Logger logger = Logger.getLogger(MainWindow.class.getName());

	// Data members
	private JFrame frame;
	/** The unit tool bar. */
	private final UnitToolBar unitToolbar;
	/** The tool bar. */
	private final ToolToolBar toolToolbar;
	/** The main desktop. */
	private final MainDesktopPane desktop;

	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;

	/**
	 * Constructor.
	 * @param cleanUI true if window should display a clean UI.
	 */
	public MainWindow(boolean cleanUI) {
		// use JFrame constructor
		frame = new JFrame(WINDOW_TITLE);

		// Load UI configuration.
		if (!cleanUI) {
			UIConfig.INSTANCE.parseFile();
		}

		// Set look and feel of UI.
		boolean useDefault = UIConfig.INSTANCE.useUIDefault();

		// Prepare desktop
		desktop = new MainDesktopPane(this);

		setLookAndFeel(false);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				exitSimulation();
			}
		});

		// Prepare menu
		frame.setJMenuBar(new MainWindowMenu(this));

		// Prepare content frame
		JPanel mainPane = new JPanel(new BorderLayout());
		frame.setContentPane(mainPane);

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		mainPane.add(toolToolbar, BorderLayout.NORTH);

		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this);
		mainPane.add(unitToolbar, BorderLayout.SOUTH);

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
		toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

		
		// add mainpane
		mainPane.add(desktop, BorderLayout.CENTER);

		// Set frame size
		final Dimension frame_size;
		Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
		if (useDefault) {
			// Make frame size 80% of screen size.
			if (screen_size.width > 800) {
				frame_size = new Dimension(
					(int) Math.round(screen_size.getWidth() * .9D),
					(int) Math.round(screen_size.getHeight() * .9D)
				);
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
			frame.setLocation(
				((screen_size.width - frame_size.width) / 2),
				((screen_size.height - frame_size.height) / 2)
			);
		} else {
			frame.setLocation(UIConfig.INSTANCE.getMainWindowLocation());
		}

		// Show frame
		frame.setVisible(true);

		// Open all initial windows.
		desktop.openInitialWindows();
	}

	/**
	 * Get the window's frame.
	 * @return the frame.
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Gets the main desktop panel.
	 * @return desktop
	 */
	public MainDesktopPane getDesktop() {
		return desktop;
	}

	/**
	 * Load a previously saved simulation.
	 */
	public void loadSimulation() {
		if ((loadSimThread == null) || !loadSimThread.isAlive()) {
			loadSimThread = new Thread(Msg.getString("MainWindow.thread.loadSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					loadSimulationProcess();
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
	private void loadSimulationProcess() {
		JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
		chooser.setDialogTitle(Msg.getString("MainWindow.dialogLoadSim")); //$NON-NLS-1$
		if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.loadingSim")); //$NON-NLS-1$
			desktop.clearDesktop();
			MasterClock clock = Simulation.instance().getMasterClock();
			clock.loadSimulation(chooser.getSelectedFile());
			while (clock.isLoadingSimulation()) {
				try {
					Thread.sleep(100L);
				} catch (InterruptedException e) {
					logger.log(Level.WARNING, Msg.getString("MainWindow.log.waitInterrupt"), e); //$NON-NLS-1$
				}
			}

			desktop.resetDesktop();
			desktop.disposeAnnouncementWindow();

			// Open navigator tool after loading.
			desktop.openToolWindow(NavigatorWindow.NAME);
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
	private void newSimulationProcess() {
		if (
			JOptionPane.showConfirmDialog(
				desktop,
				Msg.getString("MainWindow.abandonRunningSim"), //$NON-NLS-1$
				UIManager.getString("OptionPane.titleText"), //$NON-NLS-1$
				JOptionPane.YES_NO_OPTION
			) == JOptionPane.YES_OPTION
		) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.creatingNewSim")); //$NON-NLS-1$

			// Break up the creation of the new simulation, to allow interfering with the single steps.
			Simulation.stopSimulation();

			try {
				desktop.clearDesktop();
			}
			catch (Exception e) {
				// New simulation process should continue even if there's an exception in the UI.
				logger.severe(e.getMessage());
				e.printStackTrace(System.err);
			}

			SimulationConfig.loadConfig();

			SimulationConfigEditor editor = new SimulationConfigEditor(
				frame.getOwner(), 
				SimulationConfig.instance()
			);
			editor.setVisible(true);

			Simulation.createNewSimulation();

			// Start the simulation.
			Simulation.instance().start();

			try {
				desktop.resetDesktop();
			}
			catch (Exception e) {
				// New simulation process should continue even if there's an exception in the UI.
				logger.severe(e.getMessage());
				e.printStackTrace(System.err);
			}

			desktop.disposeAnnouncementWindow();

			// Open navigator tool after creating new simulation.
			desktop.openToolWindow(NavigatorWindow.NAME);
			
			// Open user guide tool.
            desktop.openToolWindow(GuideWindow.NAME);
            GuideWindow ourGuide = (GuideWindow) desktop.getToolWindow(GuideWindow.NAME);
            ourGuide.setURL(Msg.getString("doc.tutorial")); //$NON-NLS-1$
		}
	}

	/**
	 * Save the current simulation. This displays a FileChooser to select the
	 * location to save the simulation if the default is not to be used.
	 * @param useDefault Should the user be allowed to override location?
	 */
	public void saveSimulation(final boolean useDefault) {
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {
					saveSimulationProcess(useDefault);
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
	private void saveSimulationProcess(boolean useDefault) {
		File fileLocn = null;

		if (!useDefault) {
			JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
			chooser.setDialogTitle(Msg.getString("MainWindow.dialogSaveSim")); //$NON-NLS-1$
			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
				fileLocn = chooser.getSelectedFile();
			} else {
				return;
			}
		}

		desktop.openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
		MasterClock clock = Simulation.instance().getMasterClock();
		clock.saveSimulation(fileLocn);
		while (clock.isSavingSimulation()) {
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
		Simulation.instance().getMasterClock().setPaused(true);
	}

	/**
	 * Closes the announcement window and unpauses the simulation.
	 */
	public void unpauseSimulation() {
		Simulation.instance().getMasterClock().setPaused(false);
		desktop.disposeAnnouncementWindow();
	}

	/**
	 * Create a new unit button in toolbar.
	 * @param unit the unit the button is for.
	 */
	public void createUnitButton(Unit unit) {
		unitToolbar.createUnitButton(unit);
	}

	/**
	 * Disposes a unit button in toolbar.
	 * @param unit the unit to dispose.
	 */
	public void disposeUnitButton(Unit unit) {
		unitToolbar.disposeUnitButton(unit);
	}

	/**
	 * Exit the simulation for running and exit.
	 */
	public void exitSimulation() {
		//logger.info("Exiting simulation");

		// Save the UI configuration.
		UIConfig.INSTANCE.saveFile(this);

		// Save the simulation.
		Simulation sim = Simulation.instance();
		try {
			sim.getMasterClock().saveSimulation(null);
		} catch (Exception e) {
			logger.log(Level.SEVERE, Msg.getString("MainWindow.log.saveError") + e); //$NON-NLS-1$
			e.printStackTrace(System.err);
		}

		sim.getMasterClock().exitProgram();
	}

	/**
	 * Sets the look and feel of the UI
	 * @param nativeLookAndFeel true if native look and feel should be used.
	 */
	public void setLookAndFeel(boolean nativeLookAndFeel) {
		boolean changed = false;
		if (nativeLookAndFeel) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				changed = true;
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
						changed = true;
						break;
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
		}

		if (changed) {
			SwingUtilities.updateComponentTreeUI(frame);
			if (desktop != null) {
				desktop.updateToolWindowLF();
			}
			desktop.updateAnnouncementWindowLF();
		}
	}

	/**
	 * Gets the unit toolbar.
	 * @return unit toolbar.
	 */
	public UnitToolBar getUnitToolBar() {
		return unitToolbar;
	}

	/**
	 * Gets the tool toolbar.
	 * @return tool toolbar.
	 */
	public ToolToolBar getToolToolBar() {
		return toolToolbar;
	}
}