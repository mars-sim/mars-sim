/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 3.07 2015-01-26
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import org.mars_sim.msp.core.time.EarthClock;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.sound.AngledLinesWindowsCornerIcon;
import org.mars_sim.msp.ui.swing.tool.JStatusBar;
import org.mars_sim.msp.ui.swing.tool.guide.GuideWindow;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * tool bars and main desktop pane.
 */
public class MainWindow {

	public static final String WINDOW_TITLE = Msg.getString(
		"MainWindow.title", //$NON-NLS-1$
		Simulation.VERSION + " build " + Simulation.BUILD
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

	// 2014-12-05 Added mainWindowMenu;
	private final MainWindowMenu mainWindowMenu;
	
	private Thread newSimThread;
	private Thread loadSimThread;
	private Thread saveSimThread;
	
	// 2014-12-27 Added delay timer
	private Timer delayLaunchTimer;
	private Timer autosaveTimer;
	private javax.swing.Timer earthTimer = null;
	private static int AUTOSAVE_MINUTES = 15;
	private static final int TIMEDELAY = 900;

    //protected ShowDateTime showDateTime;
    private JStatusBar statusBar;
    private JLabel leftLabel;
    private JLabel memMaxLabel;
    private JLabel memUsedLabel;
    //private JLabel dateLabel;
    private JLabel timeLabel;
    private JPanel bottomPane;
    
    private int memMax;
    private int memTotal;
    private int memUsed, memUsedCache;
    private int memFree;
    
    private String statusText;
    String earthTimeString;
    //String t = null;
    
    //private SimulationConfig config = SimulationConfig.instance();

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
		// 2014-12-05 Added mainWindowMenu
		mainWindowMenu = new MainWindowMenu(this, desktop);
		frame.setJMenuBar(mainWindowMenu);

		// Prepare content frame
		JPanel mainPane = new JPanel(new BorderLayout());
		frame.setContentPane(mainPane);

		// Prepare tool toolbar
		toolToolbar = new ToolToolBar(this);
		mainPane.add(toolToolbar, BorderLayout.NORTH);

		
		// 2015-01-07 Added bottomPane for holding unitToolbar and statusBar
		bottomPane = new JPanel(new BorderLayout());
		
		// Prepare unit toolbar
		unitToolbar = new UnitToolBar(this) {
	        /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
	        protected JButton createActionComponent(Action a) {
	            JButton jb = super.createActionComponent(a);
	            jb.setOpaque(false);
	            return jb;
	        }
	    };
	    
//	    BasicToolBarUI ui = new BasicToolBarUI();
//	    unitToolbar.setUI(ui);

	    // Set the bgcolor to black
	    //color = java.awt.Color.black;
	    unitToolbar.setOpaque(false);
	    unitToolbar.setBackground(new Color(0,0,0,0));
	    //unitToolbar.getParent.getParent.setBackground(new Color(0,0,0,0));
	     
	    // Remove the toolbar border, to blend into figure contents
	    unitToolbar.setBorderPainted(false);
	     

		mainPane.add(bottomPane, BorderLayout.SOUTH);
		
		bottomPane.add(unitToolbar, BorderLayout.CENTER);
	

		// set the visibility of tool and unit bars from preferences
		unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
		toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

		// 2015-01-07 Added statusBar
        statusBar = new JStatusBar();
        //statusText = "Mars-Sim 3.08 is running";
        leftLabel = new JLabel(statusText);
		statusBar.setLeftComponent(leftLabel);
    
        memMaxLabel = new JLabel();
        memMaxLabel.setHorizontalAlignment(JLabel.CENTER);
        memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
        memMaxLabel.setText("Total Designated Memory : " + memMax +  " MB");
        statusBar.addRightComponent(memMaxLabel, false);

        memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;
        
        memUsedLabel = new JLabel();
        memUsedLabel.setHorizontalAlignment(JLabel.CENTER);
        memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
        memUsed = memTotal - memFree;
        memUsedLabel.setText("Current Used Memory : " + memUsed +  " MB");
        statusBar.addRightComponent(memUsedLabel, false);       
  
        timeLabel = new JLabel();
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        statusBar.addRightComponent(timeLabel, false);

        statusBar.addRightComponent(new JLabel(new AngledLinesWindowsCornerIcon()), true);
   
        bottomPane.add(statusBar, BorderLayout.SOUTH);	        
		
		// 2015-01-19 Added using delayLaunchTimer to launch earthTime 
		if (earthTimer == null) {
			//System.out.println(" constructor : earthTimer == null");			
			delayLaunchTimer = new Timer();
			int seconds = 1;
			// Note: this delayLaunchTimer is non-repeating
			// thus period is N/A
			delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		}
		 
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
		
		// I'm commenting this out for now.  I would like the user guide tutorial
		// to be the only initial tool window open for a new simulation. - Scott
		// 2014-12-27 Added OpenSettlementWindow with delay timer
		//delayLaunchTimer = new Timer();
		//int seconds = 1;
		//delayLaunchTimer.schedule(new OpenSettlementWindow(), seconds * 1000);	

		// 2015-01-07 Added startAutosaveTimer()
		startAutosaveTimer();

	}

	public JPanel getBottomPane() {
		return bottomPane;
	}
	
	// 2015-01-07 Added startAutosaveTimer()	
	public void startAutosaveTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                //System.out.println("calling run()");
                //autosaveUpdate();
                autosaveTimer.cancel();
    			saveSimulation(true,true);
    			startAutosaveTimer();
            }
        };
        autosaveTimer = new Timer();
        autosaveTimer.schedule(timerTask, 1000* 60 * AUTOSAVE_MINUTES);

    }
	
	// 2015-01-13 Added startEarthTimer()
	public void startEarthTimer() { // (final String t) {
	//final String earthTimeString = null;
		earthTimer = new javax.swing.Timer(TIMEDELAY, 
			new ActionListener() {		
			String t = null;
			    @SuppressWarnings("deprecation")
				@Override
			    public void actionPerformed(ActionEvent evt) {
				    try {
		        		//String t = null; 
		        		MasterClock master = Simulation.instance().getMasterClock();
		        		if (master == null) {
		        		  throw new IllegalStateException("master clock is null");
		        		}
		        		EarthClock earthclock = master.getEarthClock();
		        		if (earthclock == null) {
			        		// TODO: why is earthclock null when loading a saved simulation? 
		        			// how should I fix this ?
		        		  throw new IllegalStateException("earthclock is null"); 
		        		}
		        		t = earthclock.getTimeStamp();
				    	} catch (Exception ee) {ee.printStackTrace(System.err);
				    }
					timeLabel.setText("Earth Time : " + t);
					memFree = (int) Math.round(Runtime.getRuntime().freeMemory()) / 1000000;			        
					//memMax = (int) Math.round(Runtime.getRuntime().maxMemory()) / 1000000;
			        //memMaxLabel.setText("Total Designated Memory : " + memMax +  " MB");
					memTotal = (int) Math.round(Runtime.getRuntime().totalMemory()) / 1000000;
	                memUsed = memTotal - memFree;
	                int mem = ( memUsedCache + memUsed ) /2;
	                if (mem > memUsedCache * 1.2 || mem < memUsedCache * 0.8)
	                	memUsedLabel.setText("Current Used Memory : " + mem +  " MB");
	                memUsedCache = mem;
			    }
			});
	
		earthTimer.start();
	}
	
	// 2015-01-19 Added StatusBar
	class StatusBar extends TimerTask { // (final String t) {
		public void run() {		
			startEarthTimer();
			//delayLaunchTimer.cancel();
		}
	}
	
	/*
	// 2014-12-27 Added OpenSettlementWindow
	public class OpenSettlementWindow extends TimerTask {
		public void run() {
			desktop.openToolWindow(SettlementWindow.NAME);
			delayLaunchTimer.cancel(); // Terminate the thread
		}
	}
	*/

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
	 * Gets the Main Window Menu.
	 * @return mainWindowMenu
	 */
	// 2014-12-05 Added getMainWindowMenu()
	public MainWindowMenu getMainWindowMenu() {
		return mainWindowMenu;
	}

	/**
	 * Load a previously saved simulation.
	 */
	// 2015-01-25 Added autosave
	public void loadSimulation(boolean autosave) {	
		final boolean ans = autosave;
        if (earthTimer != null) 
            earthTimer.stop();
        earthTimer = null;
        //logger.info(" loadSimulation() : just set earthTime = null");
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
		
		// 2015-01-19 Added using delayLaunchTimer to launch earthTime 
		if (earthTimer == null) {
			//logger.info("loadSimulation() : earthTimer == null");
			delayLaunchTimer = new Timer();
			int seconds = 1;
			delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		}
		
		//logger.info(" finishing loadSimulation()");
	}

	/**
	 * Performs the process of loading a simulation.
	 */
	private void loadSimulationProcess(boolean autosave) {
		String dir = null;
		String title = null;
		// 2015-01-25 Added autosave
		if (autosave) {			
			dir = Simulation.AUTOSAVE_DIR;
			title = Msg.getString("MainWindow.dialogLoadAutosaveSim");
		}
		else {
			dir = Simulation.DEFAULT_DIR;
			title = Msg.getString("MainWindow.dialogLoadSavedSim");
		}
		JFileChooser chooser= new JFileChooser(dir);
		chooser.setDialogTitle(title); //$NON-NLS-1$
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
			
			try {
                desktop.resetDesktop();
                //logger.info(" loadSimulationProcess() : desktop.resetDesktop()");
            }
            catch (Exception e) {
                // New simulation process should continue even if there's an exception in the UI.
                logger.severe(e.getMessage());
                e.printStackTrace(System.err);
            }
			
			desktop.disposeAnnouncementWindow();
			
			// Open navigator tool after loading.
//			desktop.openToolWindow(NavigatorWindow.NAME);
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
	
		// 2015-01-19 Added using delayLaunchTimer to launch earthTime 
		if (earthTimer == null) {
			//System.out.println(" newSimulation() : earthTimer == null");
			delayLaunchTimer = new Timer();
			int seconds = 1;
			delayLaunchTimer.schedule(new StatusBar(), seconds * 1000);	
		}
		//System.out.println(" finishing newSimulation()");
		
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
			    if (earthTimer != null) {
                    earthTimer.stop();
			    }
                earthTimer = null;
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
			
			startEarthTimer();

			desktop.disposeAnnouncementWindow();
			
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
	public void saveSimulation(final boolean useDefault, final boolean isAutosave) {
		if ((saveSimThread == null) || !saveSimThread.isAlive()) {
			saveSimThread = new Thread(Msg.getString("MainWindow.thread.saveSim")) { //$NON-NLS-1$
				@Override
				public void run() {		
					saveSimulationProcess(useDefault, isAutosave);
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
    // 2015-01-08 Added autosave
	private void saveSimulationProcess(boolean useDefault, boolean isAutosave) {
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

		MasterClock clock = Simulation.instance().getMasterClock();
		
		if (isAutosave) {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.autosavingSim")); //$NON-NLS-1$
			clock.autosaveSimulation(fileLocn);			
		}
		else {
			desktop.openAnnouncementWindow(Msg.getString("MainWindow.savingSim")); //$NON-NLS-1$
			clock.saveSimulation(fileLocn);
		}
		
		while (clock.isSavingSimulation() || clock.isAutosavingSimulation()) {
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
		
		earthTimer = null;
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
			desktop.updateTransportWizardLF();
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