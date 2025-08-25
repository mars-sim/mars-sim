/*
 * Mars Simulation Project
 * ConstructionSitesPanel.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.ui.swing.MarsPanelBorder;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;

/**
 * A panel displaying a list of construction sites at a settlement.
 */
@SuppressWarnings("serial")
public class ConstructionSitesPanel extends JPanel {
  
	private static final int MAX = 65;
	
    // Data members
    private ConstructionManager manager;
    private Map<ConstructionSite,ConstructionPanel> sitesCache;
    private JPanel sitesListPane;
    private JScrollPane sitesScrollPane;
    
    /**
     * Constructor.
     * 
     * @param manager the settlement construction manager.
     */
    public ConstructionSitesPanel(ConstructionManager manager) {
        // Use JPanel constructor.
        super();
        
        this.manager = manager;
        
        setLayout(new BorderLayout());

        setBorder(StyleManager.createLabelBorder("Construction Sites"));
        		
        // Create scroll panel for sites list pane.
        sitesScrollPane = new JScrollPane();
        sitesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(sitesScrollPane, BorderLayout.CENTER);  
        
        // Prepare sites outer list pane.
        JPanel sitesOuterListPane = new JPanel(new BorderLayout(0, 0));
        sitesScrollPane.setViewportView(sitesOuterListPane);
        
        // Prepare sites list pane.
        sitesListPane = new JPanel();
        sitesListPane.setLayout(new BoxLayout(sitesListPane, BoxLayout.Y_AXIS));
        sitesOuterListPane.add(sitesListPane, BorderLayout.NORTH);
        
        // Create the site panels.
        sitesCache = new HashMap<>();
        manager.getConstructionSites().forEach(this::addSitePanel);
    }
    
    private void addSitePanel(ConstructionSite site) {
        var newPanel = new ConstructionPanel(site);
        sitesListPane.add(newPanel);
        sitesCache.put(site, newPanel);
    }

    private void removeSitePanel(ConstructionSite site) {
        var oldPanel = sitesCache.remove(site);
        sitesListPane.remove(oldPanel);
    }

    /**
     * Updates the information on this panel.
     */
    public void update() {
        // Update sites is necessary.
        Set<ConstructionSite> activeSites = new HashSet<>(manager.getConstructionSites());
        if (!sitesCache.keySet().equals(activeSites)) {        
            // Add site panels for new sites.
            activeSites.stream()
                .filter(s -> !sitesCache.containsKey(s))
                .forEach(this::addSitePanel);
            
            // Remove site panels for old sites.
            var remove = sitesCache.keySet().stream()
                .filter(s -> !activeSites.contains(s))
                .toList();
            // Must be done seperately as the Map stream is altered
            remove.forEach(this::removeSitePanel);
        }
        
        // Update all site panels.
        sitesCache.values().forEach(p -> p.update());
    }
    
    /**
     * A panel displaying information about a particular construction site.
     */
    private static class ConstructionPanel extends JPanel {
        
        /** default serial id. */
        private static final long serialVersionUID = 1L;
        
        // Data members
        private ConstructionSite site;
        private JLabel statusLabel;
        private BoundedRangeModel workBarModel;
        
        /**
         * Constructor.
         * 
         * @param site the construction site.
         */
        private ConstructionPanel(ConstructionSite site) {
            // Use JPanel constructor
            super();
            
            // Initialize data members.
            this.site = site;
            
            // Set the layout.
            setLayout(new BorderLayout(5, 5));

            // Set border
            setBorder(new MarsPanelBorder());

            // Create the status panel.
            statusLabel = new JLabel(" ", SwingConstants.LEFT);
            add(statusLabel, BorderLayout.NORTH);
            
            // Create the progress bar panel.
            JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            add(progressBarPanel, BorderLayout.CENTER);
                        
            // Prepare work progress bar.
            JProgressBar workBar = new JProgressBar();
            workBarModel = workBar.getModel();
            workBar.setStringPainted(true);
            progressBarPanel.add(workBar);
            
            // Update progress bar.
            update();
            
            // Add tooltip.
            setToolTipText(getToolTipString());
        }

        /**
         * Updates the panel information.
         */
        private void update() {
            
            // Update status label.
            String statusString = site.getStatusDescription();
            
            // Make sure status label isn't too long.
            if (statusString.length() > MAX) statusString = statusString.substring(0, MAX) + "...";
            
            // Set the text in the status label.
            statusLabel.setText(statusString);
            
            // Update work progress bar.
            int workProgress = 0;
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                double completedWork = stage.getCompletedWorkTime();
                double requiredWork = stage.getRequiredWorkTime();
                if (requiredWork > 0D) workProgress = (int) (100D * completedWork / requiredWork);
            }
            workBarModel.setValue(workProgress);
            
            // Update the tool tip string.
            setToolTipText(getToolTipString());
        }
        
        /**
         * Gets a tool tip string for the panel.
         */
        private String getToolTipString() {            
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                return ConstructionStageFormat.getTooltip(stage, true);
            }
            return "";
        }
    }   
    
	/**
	 * Prepares object for garbage collection.
	 */
	public void destroy() {
		manager = null;
	    sitesCache = null;
	    sitesListPane = null;
	    sitesScrollPane = null;
	}
}
