/*
 * Mars Simulation Project
 * ConstructionSitesPanel.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.List;

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
import com.mars_sim.core.tool.Conversion;
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
    private List<ConstructionSite> sitesCache;
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
        sitesCache = manager.getConstructionSites();
        Iterator<ConstructionSite> i = sitesCache.iterator();
        while (i.hasNext()) sitesListPane.add(new ConstructionPanel(i.next()));
    }
    
    /**
     * Updates the information on this panel.
     */
    public void update() {
        // Update sites is necessary.
        List<ConstructionSite> sites = manager.getConstructionSites();
        if (!sitesCache.equals(sites)) {
            
            // Add site panels for new sites.
            Iterator<ConstructionSite> i = sites.iterator();
            while (i.hasNext()) {
                ConstructionSite site = i.next();
                if (!sitesCache.contains(site)) 
                    sitesListPane.add(new ConstructionPanel(site));
            }
            
            // Remove site panels for old sites.
            Iterator<ConstructionSite> j = sitesCache.iterator();
            while (j.hasNext()) {
                ConstructionSite site = j.next();
                if (!sites.contains(site)) {
                    ConstructionPanel panel = getConstructionSitePanel(site);
                    if (panel != null) sitesListPane.remove(panel);
                }
            }
            
            sitesScrollPane.validate();
            
            // Update sitesCache
            sitesCache.clear();
            sitesCache.addAll(sites);
        }
        
        // Update all site panels.
        Iterator<ConstructionSite> i = sites.iterator();
        while (i.hasNext()) {
            ConstructionPanel panel = getConstructionSitePanel(i.next());
            if (panel != null) panel.update();
        }
    }
    
    /**
     * Gets a construction site panel for a particular construction site.
     * 
     * @param site the construction site.
     * @return construction site panel or null if none found.
     */
    private ConstructionPanel getConstructionSitePanel(ConstructionSite site) {        
        for (int x = 0; x < sitesListPane.getComponentCount(); x++) {
            Component component = sitesListPane.getComponent(x);
            if (component instanceof ConstructionPanel panel && panel.getConstructionSite().equals(site)) {
                return panel;
            }
        }
        
        return null;
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
         * Gets the construction site for this panel.
         * 
         * @return construction site.
         */
        private ConstructionSite getConstructionSite() {
            return site;
        }
        
        /**
         * Updates the panel information.
         */
        private void update() {
            
            // Update status label.
            String statusString = getStatusString();
            
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
         * Gets the status label string.
         * 
         * @return status string.
         */
        private String getStatusString() {
            String statusString = "";
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
            	String name = Conversion.capitalize(stage.getInfo().getName());
                if (site.isUndergoingConstruction()) 
                	statusString = " Constructing " +  name;
                else if (site.isUndergoingSalvage()) 
                	statusString = " Salvaging " + name;
                else if (site.hasUnfinishedStage()) {
                    if (stage.isSalvaging()) 
                    	statusString = " Salvaging " + name + " (Unfinished)";
                    else 
                    	statusString = " Constructing " + name + " (Unfinished)";
                }
                else 
                	statusString = " " + name + " (Completed)";
            }
            else 
            	statusString = "No Construction";
            
            return statusString;
        }
        
        /**
         * Gets a tool tip string for the panel.
         */
        private String getToolTipString() {            
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                return ConstructionStageFormat.getTooltip(stage);
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
