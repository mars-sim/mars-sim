/**
 * Mars Simulation Project
 * MainWindow.java
 * @version 3.00 2011-02-14
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.swing;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.configeditor.SimulationConfigEditor;
import org.mars_sim.msp.ui.swing.configeditor.TempSimulationConfigEditor;
import org.mars_sim.msp.ui.swing.tool.navigator.NavigatorWindow;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalLookAndFeel;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The MainWindow class is the primary UI frame for the project. It contains the
 * tool bars and main desktop pane.
 */
public class MainWindow {

    public static final String WINDOW_TITLE = "Mars Simulation Project (version " + Simulation.VERSION + ")";
    private JFrame frame;

//	/** DOCME: documentation is missing */
////	private static final long serialVersionUID = 1L;

    private static String CLASS_NAME = "org.mars_sim.msp.ui.standard.MainWindow";

    private static Logger logger = Logger.getLogger(CLASS_NAME);

    // Data members
    private final UnitToolBar unitToolbar; // The unit tool bar
    private final ToolToolBar toolToolbar; // The tool bar
    private final MainDesktopPane desktop; // The main desktop

    private Thread newSimThread;
    private Thread loadSimThread;
    private Thread saveSimThread;

    public static void main(String[] args) {
        MainWindow w = new MainWindow();
        w.show();
    }

    /**
     * Constructor
     */
    public MainWindow() {

        // use JFrame constructor
        frame = new JFrame(WINDOW_TITLE);
        
        // Note: this setting causes the application to close before it
        // saves the default.sim file.
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Load UI configuration.
        UIConfig.INSTANCE.parseFile();

        // Set look and feel of UI.
        boolean useDefault = UIConfig.INSTANCE.useUIDefault();

        if (!useDefault) {
            setLookAndFeel(UIConfig.INSTANCE.useNativeLookAndFeel());
        } else {
            setLookAndFeel(false);
        }


//		// Prepare frame
//		setVisible(false);

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
        mainPane.add(toolToolbar, "North");

        // Prepare unit toolbar
        unitToolbar = new UnitToolBar(this);
        mainPane.add(unitToolbar, "South");

        // set the visibility of tool and unit bars from preferences

        unitToolbar.setVisible(UIConfig.INSTANCE.showUnitBar());
        toolToolbar.setVisible(UIConfig.INSTANCE.showToolBar());

        // Prepare desktop
        desktop = new MainDesktopPane(this);
        mainPane.add(desktop, "Center");

        // Set frame size
        final Dimension frame_size;
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        if (useDefault) {
            // Make frame size 80% of screen size.
            if (screen_size.width > 800) {
                frame_size = new Dimension((int) Math.round(screen_size
                        .getWidth() * .9D), (int) Math.round(screen_size
                        .getHeight() * .9D));
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

//		// Show frame
//		setVisible(true);

        // Open all initial windows.
        desktop.openInitialWindows();

        //this.notifySimStartOK(true);
    }

    public void show() {
        frame.setVisible(true);
    }

    public void hide() {
        frame.setVisible(false);
    }

    public int getX() {
        return frame.getX();
    }

    public int getY() {
        return frame.getY();
    }

    public JFrame getFrame() {
        return frame;
    }

    public int getWidth() {
        return frame.getWidth();
    }

    public int getHeight() {
        return frame.getHeight();
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
     * Load a previously saved simulation.
     */
    public void loadSimulation() {
        if ((loadSimThread == null) || !loadSimThread.isAlive()) {
            loadSimThread = new Thread() {
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
//		try {
        JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
        chooser.setDialogTitle("Select stored simulation");
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            desktop.openAnnouncementWindow("Loading simulation...");
            MasterClock clock = Simulation.instance().getMasterClock();
            clock.loadSimulation(chooser.getSelectedFile());
            while (clock.isLoadingSimulation()) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "the wait while loading sleep was interrupted", e);
                }
            }

            desktop.clearDesktop();
            desktop.resetDesktop();
            desktop.disposeAnnouncementWindow();

            // Open navigator tool after loading.
            desktop.openToolWindow(NavigatorWindow.NAME);
        }
//		} catch (Exception e) {
//			JOptionPane.showMessageDialog(null, "Problem loading simulation",
//					e.toString(), JOptionPane.ERROR_MESSAGE);
//			logger.log(Level.SEVERE, "Problem loading simulation: " + e);
//			e.printStackTrace();
//		}
    }

    /**
     * Create a new simulation.
     */
    public void newSimulation() {
        if ((newSimThread == null) || !newSimThread.isAlive()) {
            newSimThread = new Thread() {
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

        if (JOptionPane.showInternalConfirmDialog(desktop,
                "Do you really want to create a new simulation and abandon the current running?",
                UIManager.getString("OptionPane.titleText"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            desktop.openAnnouncementWindow("Creating new simulation...");
            /* Break up the creation of the new simulation, to allow interfering with the single steps.*/
            desktop.clearDesktop();
            Simulation.stopSimulation();
            SimulationConfig.loadConfig();
            //SimulationConfigEditor editor = new SimulationConfigEditor(frame.getOwner(), SimulationConfig.instance());
            TempSimulationConfigEditor editor = new TempSimulationConfigEditor(frame.getOwner(), 
                    SimulationConfig.instance());
            editor.setVisible(true);
            Simulation.createNewSimulation();
            
            // Start the simulation.
            Simulation.instance().start();
            
            desktop.resetDesktop();
            desktop.disposeAnnouncementWindow();
            /* Open navigator tool after creating new simulation. */
            desktop.openToolWindow(NavigatorWindow.NAME);
        }
    }

    /**
     * Save the current simulation. This displays a FileChooser to select the
     * location to save the simulation if the default is not to be used.
     *
     * @param useDefault Should the user be allowed to override location?
     */
    public void saveSimulation(final boolean useDefault) {
        if ((saveSimThread == null) || !saveSimThread.isAlive()) {
            saveSimThread = new Thread() {
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
//		try {
        File fileLocn = null;

        if (!useDefault) {
            JFileChooser chooser = new JFileChooser(Simulation.DEFAULT_DIR);
            chooser.setDialogTitle("Select save location");
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                fileLocn = chooser.getSelectedFile();
            } else {
                return;
            }
        }

        desktop.openAnnouncementWindow("Saving simulation...");
        MasterClock clock = Simulation.instance().getMasterClock();
        clock.saveSimulation(fileLocn);
        while (clock.isSavingSimulation()) {
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Sleep while saving simulation was interrupted", e);
            }
        }
        desktop.disposeAnnouncementWindow();
//		} catch (Exception e) {
//			JOptionPane.showMessageDialog(null, "Problem saving simulation",
//					e.toString(), JOptionPane.ERROR_MESSAGE);
//			logger.log(Level.SEVERE, "Problem saving simulation: " + e);
//			e.printStackTrace();
//		}
    }

    /**
     * Pauses the simulation and opens an announcement window.
     */
    public void pauseSimulation() {
        desktop.openAnnouncementWindow("Pausing simulation");
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
        //logger.info("Exiting simulation");
    	
        // Save the UI configuration.
        UIConfig.INSTANCE.saveFile(this);

        // Save the simulation.
        Simulation sim = Simulation.instance();
        try {
            sim.getMasterClock().saveSimulation(null);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Problem saving simulation " + e);
            e.printStackTrace(System.err);
        }

        sim.getMasterClock().exitProgram();
    }
/*
	public void notifySimStartOK(boolean itsokaytostart) {
		//System.out.println("mainWindow: simulation notified it can start");
		Simulation sim = Simulation.instance();
		try {
			sim.mainWindowSimStartOK(itsokaytostart);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MainWindow: Problem notifying sim it was OK to start simulation " + e);
			e.printStackTrace(System.err);
		}
	}
	
*/

    /**
     * Sets the look and feel of the UI
     *
     * @param nativeLookAndFeel true if native look and feel should be used.
     */
    public void setLookAndFeel(boolean nativeLookAndFeel) {
        
        boolean changed = false;
        if (nativeLookAndFeel) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                changed = true;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not load system look&feel", e);
            }
        } else {
            try {
                // Set Nimbus look & feel if found in JVM.
                boolean foundNimbus = false;
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().equals("Nimbus")) {
                        UIManager.setLookAndFeel(info.getClassName());
                        foundNimbus = true;
                        changed = true;
                        break;
                    }
                }
                
                // Set old Mars theme and metal look & feel if Nimbus not found.
                if (!foundNimbus) {
                    logger.log(Level.WARNING, "Could not set Nimbus look&feel, make sure you have a recent JRE 1.6 update or 1.7");
                    MetalLookAndFeel.setCurrentTheme(new MarsTheme());
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                    changed = true;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Could not set Nimbus look&feel, make sure you have a recent JRE 1.6 update or 1.7", e);
            }
        }
        
        if (changed) {
            SwingUtilities.updateComponentTreeUI(frame);
            if (desktop != null) {
                desktop.updateToolWindowLF();
            }
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

}