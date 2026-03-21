/*
 * Mars Simulation Project
 * DockingWindow.java
 * @date 2025-12-30
 * @author Barry Evans
 */
package com.mars_sim.ui.swing.docking;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.mars_sim.core.Entity;
import com.mars_sim.core.Simulation;
import com.mars_sim.core.time.ClockPulseListener;
import com.mars_sim.core.time.ClockPulse;
import com.mars_sim.core.time.CompressedClockListener;
import com.mars_sim.ui.swing.ContentManager;
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
import com.mars_sim.ui.swing.sound.AudioPlayer;
import com.mars_sim.ui.swing.terminal.MarsTerminal;
import com.mars_sim.ui.swing.tool.ToolRegistry;
import com.mars_sim.ui.swing.tool.entitybrowser.EntityBrowser;
import com.mars_sim.ui.swing.tool.monitor.MonitorWindow;
import com.mars_sim.ui.swing.utils.AttributePanel;
import com.mars_sim.ui.swing.utils.SaveDialog;

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
public class DockingWindow extends JFrame 
        implements ClockPulseListener, UIContext, ContentManager {
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

    private Simulation sim;
    private Set<DockingAdapter> windows = new HashSet<>();
    private Map<Placement, Dockable> anchors = new EnumMap<>(Placement.class);
    private ToolToolBar toolToolBar;
    private UIConfig config;
    private AudioPlayer audio;

    private DockingWindow(Simulation sim, UIConfig config, AudioPlayer audio) {
        this.sim = sim;
        this.config = config;
        this.audio = audio;

        // Set up the look and feel library to be used
		StyleManager.setStyles(Collections.emptyMap());
        StyleManager.setTabPlacement(SwingConstants.TOP);

        // Enable dynamic layout for Docking windows as they are more flexible
        AttributePanel.setUseDynamicLayout(true);
        // Setup the JFrame
        setTitle("Mars Simulation");
        setSize(1200, 800);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Initialize Docking. Force tabs for single Dockerables
        Docking.initialize(this);
        DockingUI.initialize();

        RootDockingPanel dockingPanel = new RootDockingPanel(this);

        setLayout(new BorderLayout());
        add(dockingPanel, BorderLayout.CENTER);
        toolToolBar = new ToolToolBar(this, this, audio);
        add(toolToolBar, BorderLayout.NORTH);

        setJMenuBar(new MainMenuBar(this, this, audio));

        // Add the blanks panels for docking anchors
        createBlank(Placement.CENTER);

        // Add this class to the master clock's listener but compress the pulses
		// to no more than one per second
		var clockHandler = new CompressedClockListener(this, 1000L);
        sim.getMasterClock().addClockPulseListener(clockHandler);
        
        // Add default tools
        var emptyProps = new Properties();
        addContentPanel(new MonitorWindow(this, emptyProps));
        addContentPanel(new EntityBrowser(this));

        addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				// Save simulation and UI configuration when window is closed.
				SaveDialog.createEndSimulation(sim, DockingWindow.this);
			}
		});
    }

    /**
     * Add a content panel to the docking window. This will wrap the panel in a DockingAdapter
     * and dock it to the appropriate region based on its placement.
     * @param panel Content to add
     */
    private void addContentPanel(ContentPanel panel) {
        var w = new DockingAdapter(panel);

        // Check if there is a target for dockering already in the Placement
        var target = anchors.get(panel.getPlacement());
        if (target != null) {
            // Use it and place at the center relative to the target
            Docking.dock(w, target, DockingRegion.CENTER);
        }
        else {
            // Add direct to the window in the specified region
            var region = getRegion(panel.getPlacement());
            Docking.dock(w, this, region);
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
        Docking.dock(anchor, this, getRegion(placement));

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
        Properties props = new Properties();
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

    @Override
    public Simulation getSimulation() {
        return sim;
    }

    @Override
    public JFrame getTopFrame() {
        return this;
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
     * @param audio Audio player for the window.
     * @return The created DockingWindow.
     */
    public static DockingWindow create(Simulation sim, UIConfig config, AudioPlayer audio) {
        var dw = new DockingWindow(sim, config, audio);

        dw.setVisible(true);
        return dw;
    }

    @Override
    public MarsTerminal getMarsTerminal() {
        return null;
    }

    /**
     * Saved any ui properties specifc to the Docking windows.
     * @return Map of property sets for the UI elements in the Docking window.
     */
    @Override
    public Map<String, Properties> getUIProps() {
		return Collections.emptyMap();
    }

    @Override
    public List<WindowSpec> getContentSpecs() {
        return Collections.emptyList();
    }

    /**
     * Get the assigned audio player.
     */
    @Override
    public AudioPlayer getAudio() {
        return audio;
    }

    @Override
    public UIConfig getConfig() {
        return config;
    }

    @Override
    public void shutdown() {
        // Close the window and release resources
        dispose();
    }

}
