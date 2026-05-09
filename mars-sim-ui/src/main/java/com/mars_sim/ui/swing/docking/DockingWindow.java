/*
 * Mars Simulation Project
 * DockingWindow.java
 * @date 2025-12-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.docking;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.ClockPulseListener;
import com.mars_sim.core.time.CompressedClockListener;
import com.mars_sim.ui.swing.ContentManager;
import com.mars_sim.ui.swing.ConfigurableWindow;
import com.mars_sim.ui.swing.ContentPanel;
import com.mars_sim.ui.swing.ContentPanel.Placement;
import com.mars_sim.ui.swing.MainMenuBar;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.TemporalComponent;
import com.mars_sim.ui.swing.ToolToolBar;
import com.mars_sim.ui.swing.UIConfig;
import com.mars_sim.ui.swing.UIConfig.WindowSpec;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.entitywindow.EntityContentFactory;
import com.mars_sim.ui.swing.entitywindow.EntityContentPanel;
import com.mars_sim.ui.swing.tool.ToolRegistry;
import com.mars_sim.ui.swing.tool.entitybrowser.EntityBrowser;
import com.mars_sim.ui.swing.tool.eventviewer.EventViewer;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SpeedControl;

import io.github.andrewauclair.moderndocking.Dockable;
import io.github.andrewauclair.moderndocking.DockableStyle;
import io.github.andrewauclair.moderndocking.DockingRegion;
import io.github.andrewauclair.moderndocking.app.Docking;
import io.github.andrewauclair.moderndocking.app.RootDockingPanel;
import io.github.andrewauclair.moderndocking.ext.ui.DockingUI;

/**
 * The main window for the Mars Simulation UI that uses a docking approach to the window layout.
 * It implements the UIContext interface to provide access to the simulation and other UI features.
 */
public class DockingWindow extends ContentManager 
        implements ClockPulseListener, UIContext {
    /**
     * A blank panel used as an anchor for docking regions.
     */
    private static class Anchor extends JPanel implements Dockable {
        public Anchor() {
            add(new JLabel());
        }

        @Override
        public String getPersistentID() {
            return "anchor";
        }

        @Override
        public String getTabText() {
            return "";
        }

        @Override
        public DockableStyle getStyle() {
            return DockableStyle.CENTER_ONLY;
        }
    }

    private static SimLogger logger = SimLogger.getLogger(DockingWindow.class.getName());

    private Set<DockingAdapter> windows = new HashSet<>();
    private Map<Placement, Dockable> anchors = new EnumMap<>(Placement.class);
    private ToolToolBar toolToolBar;
    private SpeedControl speed;

    private DockingWindow(Simulation sim, UIConfig config, boolean useAudio) {
        super(sim, config, useAudio);

        // Set up the look and feel library to be used
        StyleManager.setTabPlacement(SwingConstants.TOP);

        // Enable dynamic layout for Docking windows as they are more flexible
        AttributePanel.setUseDynamicLayout(true);

        // Setup the JFrame
        var frame = getTopFrame();
        if (!loadSavedScreen()) {
            frame.setSize(1200, 800);
        }

        // Initialize Docking. Force tabs for single Dockerables
        Docking.initialize(frame);
        DockingUI.initialize();

        RootDockingPanel dockingPanel = new RootDockingPanel(frame);

        frame.setLayout(new BorderLayout());
        frame.add(dockingPanel, BorderLayout.CENTER);

        toolToolBar = new ToolToolBar(this, this);
        var topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolToolBar, BorderLayout.CENTER);

        speed = new SpeedControl(sim.getMasterClock());
        topPanel.add(speed, BorderLayout.WEST);
        frame.add(topPanel, BorderLayout.NORTH);

        frame.setJMenuBar(new MainMenuBar(this, this, getAudio()));

        // Add the blanks panels for docking anchors
        createBlank(Placement.CENTER);

        // Add this class to the master clock's listener but compress the pulses
		// to no more than one per second
		var clockHandler = new CompressedClockListener(this, 1000L);
        sim.getMasterClock().addClockPulseListener(clockHandler);
        
        // Add default tools
        openInitialTools();
    }

    /**
     * Open initial windows based on the UIConfig. If no windows are configured, open a default set of tools.
     */
    private void openInitialTools() {
        List<WindowSpec> startingWindows = getConfig().getConfiguredWindows();

		if (!startingWindows.isEmpty()) {
            var sim = getSimulation();
			for(WindowSpec w : startingWindows) {
				switch(w.type()) {
					case UIConfig.TOOL:
						openToolWindow(w.name());
					break;

					case UIConfig.UNIT:
						var u = EntityContentFactory.getEntity(sim, w.props());
						if (u != null) {
                            createEntityWindow(u, w.props());
						}
					break;
                    default:
				}
 			}
        }
        else {
            // No starting windows configured so open defaults
            openToolWindow(EntityBrowser.NAME);
            openToolWindow(EventViewer.NAME);
            openToolWindow(MonitorWindow.NAME);
        }
    }

    /**
     * This method is called by a DockingAdapter when its content panel is closed. It will deregister the Dockable
     * and destroy the ContentPanel.
     * 
     * @param panel Panel being closed.
     */
    void closeContentPanel(DockingAdapter panel) {
        logger.info("Closing content panel: " + panel.getPersistentID());
        Docking.deregisterDockable(panel);
        windows.remove(panel);
        panel.getContent().destroy();
    }

    /**
     * Add a content panel to the docking window. This will wrap the panel in a DockingAdapter
     * and dock it to the appropriate region based on its placement.
     * @param panel Content to add
     */
    private void addContentPanel(ContentPanel panel) {
        var w = new DockingAdapter(this, panel);
        
        Docking.registerDockable(w);
        logger.info("Content Panel registered: " + w.getPersistentID() + " " + panel.getTitle());

        // Check if there is a target for dockering already in the Placement
        var target = anchors.get(panel.getPlacement());
        if (target != null) {
            // Use it and place at the center relative to the target
            Docking.dock(w, target, DockingRegion.CENTER);
        }
        else {
            // Add direct to the window in the specified region
            var region = getRegion(panel.getPlacement());
            Docking.dock(w, getTopFrame(), region);
        }
        windows.add(w);

    }

    /**
     * Convert the Placement of a Content Panel to a DockingRegion.
     * @param placement
     * @return DockingRegion
     */
    private static DockingRegion getRegion(Placement placement) {
        return switch(placement) {
            case CENTER -> DockingRegion.CENTER;
            case RIGHT -> DockingRegion.EAST;
            case LEFT -> DockingRegion.WEST;
            case BOTTOM -> DockingRegion.SOUTH;
            case TOP -> DockingRegion.NORTH;
            default -> DockingRegion.CENTER;
        };
    }

    /**
     * Create a blank anchor panel for the given placement and register it with Docking.
     * @param placement
     */
    private void createBlank(Placement placement) {
        var anchor = new Anchor();
        Docking.registerDockingAnchor(anchor);
        Docking.dock(anchor, getTopFrame(), getRegion(placement));

        anchors.put(placement, anchor);
    }

    /**
     * Show the details for the given entity in a new content panel.
     * If the entity is already being shown, bring that panel to the front.
     * This uses the EntityContentFactory to create the appropriate panel for the entity type.
     * @param entity The entity to show details for.
     */
    @Override
    public void showDetails(Entity entity) {
        // Is it already open?
		DockingAdapter existing = windows.stream()
						.filter(w -> w.getContent() instanceof EntityContentPanel<?> panel && panel.getEntity().equals(entity))
						.findFirst().orElse(null);
		if (existing != null) {
			Docking.bringToFront(existing);
			return;
		}
				
		// Build a new window
        createEntityWindow(entity, new Properties());
    }

    /**
     * Open an Entity window for an Entity using a set of user properties.
     * @param entity Entity to display
     * @param props User properties.
     */
    private void createEntityWindow(Entity entity, Properties props) {
		var panel = EntityContentFactory.getEntityPanel(entity, this, props);
		if (panel != null) {
            // Cheat to shrink the window size to fit content
            var panelMin = panel.getMinimumSize();
            var newDims = new Dimension(panelMin.width, 400);
            panel.setMinimumSize(newDims);
            panel.setPreferredSize(newDims);
            addContentPanel(panel);
		}
    }

    /**
     * Open a tool window by name. If the tool is already open, bring it to the front.
     * This uses the ToolRegistry to create the tool.
     * @param name The name of the tool to open.
     * @return The ContentPanel for the tool.
     */
    @Override
    public ContentPanel openToolWindow(String name) {
        // Is it already open?
		DockingAdapter existing = windows.stream()
						.filter(w -> w.getContent().getName().equals(name))
						.findFirst().orElse(null);
		if (existing != null) {
			Docking.bringToFront(existing);
			return existing.getContent();
		}

        // Not found so make a new window
        Properties props = new Properties();
        var toolContent = ToolRegistry.getTool(name, this, props);
        addContentPanel(toolContent);

        return toolContent;
    }

	/**
	 * Plays a sound by name.
	 * @param soundName The name of the sound to play
	 */
	@Override
	public void playSound(String soundName) {
        var audio = getAudio();
        if (audio != null) {
            audio.playSound(soundName);
        }
	}

    /**
     * Master clock pulse update. This will forward the pulse to all content panels
     * that implement the TemporalComponent interface.
     */
	@Override
	public void clockPulse(ClockPulse pulse) {
		for(var w : windows) {
            if (w.getContent() instanceof TemporalComponent listener) {
                listener.clockUpdate(pulse);
            }
        }
        toolToolBar.incrementClocks(pulse.getMasterClock());
	}

    /**
     * Factory method to create and show a DockingWindow for the given simulation.
     * @param sim Simulation running.
     * @param config UI configuration to use for the window.
     * @param useAudio Whether to use audio for the window.
     * @return The created DockingWindow.
     */
    public static DockingWindow create(Simulation sim, UIConfig config, boolean useAudio) {
        var dw = new DockingWindow(sim, config, useAudio);

        dw.getTopFrame().setVisible(true);
        return dw;
    }

    /**
     * Get the details of open windows.
     */
    @Override
    public List<WindowSpec> getContentSpecs() {
        var results = new ArrayList<WindowSpec>();
        
		// Add all internal windows.
		for (var window1 : windows) {

            var content = window1.getContent();
            Properties props = null;
            if (content instanceof ConfigurableWindow cw) {
                props = cw.getUIProps();
            }
            else {
                props = new Properties();
            }

            var winName = content.getName();
            var winType = content instanceof EntityContentPanel ? UIConfig.UNIT : UIConfig.TOOL;
            var wp = new WindowSpec(winName, null, null, 0, winType, props);
            results.add(wp);
        }

		return results;
    }

    @Override
    public void shutdown() {
        super.shutdown();

        // Close the window and release resources
        speed.unregister();
    }
}
