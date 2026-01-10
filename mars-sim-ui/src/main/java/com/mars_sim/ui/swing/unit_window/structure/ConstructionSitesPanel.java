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

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import com.mars_sim.core.EntityEvent;
import com.mars_sim.core.EntityListener;
import com.mars_sim.core.building.construction.ConstructionManager;
import com.mars_sim.core.building.construction.ConstructionSite;
import com.mars_sim.core.building.construction.ConstructionStage;
import com.mars_sim.core.tool.Conversion;
import com.mars_sim.core.tool.Msg;
import com.mars_sim.ui.swing.UIContext;
import com.mars_sim.ui.swing.components.EntityLabel;
import com.mars_sim.ui.swing.utils.ConstructionStageFormat;
import com.mars_sim.ui.swing.utils.SwingHelper;

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

    private UIContext context;
    
    /**
     * Constructor.
     * 
     * @param manager the settlement construction manager.
     */
    public ConstructionSitesPanel(ConstructionManager manager, UIContext context) {
        // Use JPanel constructor.
        super();
        
        this.manager = manager;
        this.context = context;
        
        setLayout(new BorderLayout());

        setBorder(SwingHelper.createLabelBorder(Msg.getString("ConstructionSite.plural")));
        		
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
        var newPanel = new ConstructionPanel(site, context);
        sitesListPane.add(newPanel);
        sitesCache.put(site, newPanel);
    }

    private void removeSitePanel(ConstructionSite site) {
        var oldPanel = sitesCache.remove(site);
        site.removeEntityListener(oldPanel);
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
     * Destroys this panel and remove all the Site Entity listeners
     */
    public void destroy() {
        sitesCache.keySet().forEach(site -> site.removeEntityListener(sitesCache.get(site)));
        sitesCache.clear();
    }

    /**
     * A panel displaying information about a particular construction site.
     */
    private static class ConstructionPanel extends JPanel implements EntityListener{
        
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
        private ConstructionPanel(ConstructionSite site, UIContext context) {
            // Use JPanel constructor
            super();
            
            // Initialize data members.
            this.site = site;
            
            // Set the layout.
            setLayout(new BorderLayout(5, 5));
            setBorder(BorderFactory.createEtchedBorder());

            // Create the status panel.
            var attrPanel = Box.createVerticalBox();
            var siteLabel = new EntityLabel(site, context);
            siteLabel.setAlignmentX(LEFT_ALIGNMENT);
            attrPanel.add(siteLabel);
            statusLabel = new JLabel(" ", SwingConstants.LEFT);
            statusLabel.setAlignmentX(LEFT_ALIGNMENT);
            attrPanel.add(statusLabel);
            add(attrPanel, BorderLayout.NORTH);
            
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

            site.addEntityListener(this);
        }

        /**
         * Updates the panel information.
         */
        private void update() {
            
            // Update status label.
            String statusString = site.getStatusDescription();
            
            // Make sure status label isn't too long.
            statusString = Conversion.trim(statusString, MAX);
            
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
                return ConstructionStageFormat.getTooltip(stage);
            }
            return "";
        }

        @Override
        public void entityUpdate(EntityEvent event) {
            update();
        }

        @Override
        public String toString() {
            return "ConstructionPanel for " + site.getAssociatedSettlement().getName();
        }
    }   
}
