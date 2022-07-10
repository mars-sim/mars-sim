/*
 * Mars Simulation Project
 * ConstructionSitesPanel.java
 * @date 2022-07-09
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.resource.ItemResourceUtil;
import org.mars_sim.msp.core.resource.ResourceUtil;
import org.mars_sim.msp.core.structure.construction.ConstructionManager;
import org.mars_sim.msp.core.structure.construction.ConstructionSite;
import org.mars_sim.msp.core.structure.construction.ConstructionStage;
import org.mars_sim.msp.core.structure.construction.ConstructionStageInfo;
import org.mars_sim.msp.core.structure.construction.ConstructionVehicleType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;

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
     * Constructor
     * @param manager the settlement construction manager.
     */
    public ConstructionSitesPanel(ConstructionManager manager) {
        // Use JPanel constructor.
        super();
        
        this.manager = manager;
        
        setLayout(new BorderLayout());

        TitledBorder dessertBorder = BorderFactory.createTitledBorder(null, "Construction Sites",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new Font("Serif", Font.BOLD, 14), java.awt.Color.darkGray);
        setBorder(dessertBorder);
        		
        // Create scroll panel for sites list pane.
        sitesScrollPane = new JScrollPane();
        sitesScrollPane.setPreferredSize(new Dimension(UnitWindow.WIDTH - 168, 250));
        sitesScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
     * Update the information on this panel.
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
     * @param site the construction site.
     * @return construction site panel or null if none found.
     */
    private ConstructionPanel getConstructionSitePanel(ConstructionSite site) {
        ConstructionPanel result = null;
        
        for (int x = 0; x < sitesListPane.getComponentCount(); x++) {
            Component component = sitesListPane.getComponent(x);
            if (component instanceof ConstructionPanel) {
                ConstructionPanel panel = (ConstructionPanel) component;
                if (panel.getConstructionSite().equals(site)) result = panel;
            }
        }
        
        return result;
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
            statusLabel = new JLabel(" ", JLabel.LEFT);
            add(statusLabel, BorderLayout.NORTH);
            
            // Create the progress bar panel.
            JPanel progressBarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
//            progressBarPanel.setSize(200, 25);
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
            StringBuilder result = new StringBuilder("<html>");
            result.append(getStatusString()).append("<br>");
            
            ConstructionStage stage = site.getCurrentConstructionStage();
            if (stage != null) {
                ConstructionStageInfo info = stage.getInfo();
                result.append("Stage Type: ").append(info.getType()).append("<br>");
                if (stage.isSalvaging()) result.append("Work Type: salvage<br>");
                else result.append("Work Type: Construction<br>");
                DecimalFormat formatter = new DecimalFormat("0.0");
                String requiredWorkTime = formatter.format(stage.getRequiredWorkTime() / 1000D);
                result.append("Work Time Required: ").append(requiredWorkTime).append(" Sols<br>");
                String completedWorkTime = formatter.format(stage.getCompletedWorkTime() / 1000D);
                result.append("Work Time Completed: ").append(completedWorkTime).append(" Sols<br>");
                result.append("Architect Construction Skill Required: ").append(info.getArchitectConstructionSkill()).append("<br>");
                
                // Add remaining construction resources.
                if ((stage.getRemainingResources().size() > 0) && !stage.isSalvaging()) {
                    result.append("<br>Remaining Construction Resources:<br>");
                    Iterator<Integer> i = stage.getRemainingResources().keySet().iterator();
                    while (i.hasNext()) {
                    	Integer resource = i.next();
                        double amount = stage.getRemainingResources().get(resource);
                        result.append("&nbsp;&nbsp;").append(ResourceUtil.findAmountResource(resource)
                        		.getName()).append(": ").append(amount).append(" kg<br>");
                    }
                }
                
                // Add remaining construction parts.
                if (stage.getRemainingParts().size() > 0) {
                    result.append("<br>Remaining Construction Parts:<br>");
                    Iterator<Integer> j = stage.getRemainingParts().keySet().iterator();
                    while (j.hasNext()) {
                    	Integer part = j.next();
                        int number = stage.getRemainingParts().get(part);
                        result.append("&nbsp;&nbsp;").append(ItemResourceUtil.findItemResource(part)
                        		.getName()).append(": ").append(number).append("<br>");
                    }
                }
                
                // Add salvage parts.
                if (!stage.isSalvaging() && (info.getParts().size() > 0)) {
                    result.append("<br>Salvagable Parts:<br>");
                    Iterator<Integer> j = info.getParts().keySet().iterator();
                    while (j.hasNext()) {
                    	Integer part = j.next();
                        int number = info.getParts().get(part);
                        result.append("&nbsp;&nbsp;").append(ItemResourceUtil.findItemResource(part)
                        		.getName()).append(": ").append(number).append("<br>");
                    }
                }
                
                // Add construction vehicles.
                if (info.getVehicles().size() > 0) {
                    if (stage.isSalvaging()) result.append("<br>Salvage Vehicles:<br>");
                    else result.append("<br>Construction Vehicles:<br>");
                    Iterator<ConstructionVehicleType> k = info.getVehicles().iterator();
                    while (k.hasNext()) {
                        ConstructionVehicleType vehicle = k.next();
                        result.append("&nbsp;&nbsp;Vehicle Type: ").append(vehicle.getVehicleType()).append("<br>");
                        result.append("&nbsp;&nbsp;Attachment Parts:<br>");
                        Iterator<Integer> l = vehicle.getAttachmentParts().iterator();
                        while (l.hasNext()) {
                            result.append("&nbsp;&nbsp;&nbsp;&nbsp;").append(
                            		ItemResourceUtil.findItemResource(l.next()).getName()).append("<br>");
                        }
                    }
                }
            }
            
            result.append("</html>");
            
            return result.toString();
        }
    }   
    
	/**
	 * Prepare object for garbage collection.
	 */
	public void destroy() {
		manager = null;
	    sitesCache = null;
	    sitesListPane = null;
	    sitesScrollPane = null;
	}
}
