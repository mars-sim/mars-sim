/**
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @version 2.75 2003-07-08
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import java.awt.*;
import javax.swing.*;
import java.util.*;
import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.ui.standard.*;

/** 
 * The MaintenanceTabPanel is a tab panel for unit maintenance information.
 */
public class MaintenanceTabPanel extends TabPanel {
    
    private JLabel lastCompletedLabel; // The last completed label.
    private BoundedRangeModel progressBarModel; // The progress bar model.
    private int lastCompletedTime; // The time since last completed maintenance.
    private Collection malfunctionPanels; // List of malfunction panels.
    private Collection malfunctionCache; // List of malfunctions in building.
    private JPanel malfunctionListPanel; // Malfunction list panel.
    
    /**
     * Constructor
     *
     * @param proxy the UI proxy for the unit.
     * @param desktop the main desktop.
     */
    public MaintenanceTabPanel(UnitUIProxy proxy, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Maint", null, "Maintenance", proxy, desktop);
        
        Malfunctionable malfunctionable = (Malfunctionable) proxy.getUnit();
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();
        
        // Create maintenance panel
        JPanel maintenancePanel = new JPanel(new GridLayout(3, 1, 0, 0));
        maintenancePanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(maintenancePanel);
        
        // Create maintenance label.
        JLabel maintenanceLabel = new JLabel("Maintenance", JLabel.CENTER);
        maintenancePanel.add(maintenanceLabel);
        
        // Create lastCompletedLabel.
        lastCompletedTime = (int) manager.getTimeSinceLastMaintenance();
        lastCompletedLabel = new JLabel("Last Completed: " + lastCompletedTime + 
            " millisols", JLabel.CENTER);
        maintenancePanel.add(lastCompletedLabel);
        
        // Create maintenance progress bar panel.
        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        maintenancePanel.add(progressPanel);
    
        // Prepare progress bar.
        JProgressBar progressBar = new JProgressBar();
        progressBarModel = progressBar.getModel();
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        
        // Set initial value for progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);
        
        // Prepare malfunction panel
        JPanel malfunctionPanel = new JPanel(new BorderLayout(0, 0));
        malfunctionPanel.setBorder(new MarsPanelBorder());
        centerContentPanel.add(malfunctionPanel, BorderLayout.CENTER);
        
        // Create malfunctions label
        JLabel malfunctionsLabel = new JLabel("Malfunctions", JLabel.CENTER);
        malfunctionPanel.add(malfunctionsLabel, BorderLayout.NORTH);
        
        // Create scroll panel for malfunction list
        JScrollPane malfunctionScrollPanel = new JScrollPane();
        malfunctionScrollPanel.setPreferredSize(new Dimension(170, 90));
        malfunctionPanel.add(malfunctionScrollPanel, BorderLayout.CENTER);
        
        // Create malfunction list main panel.
        JPanel malfunctionListMainPanel = new JPanel(new BorderLayout(0, 0));
        malfunctionScrollPanel.setViewportView(malfunctionListMainPanel);
        
        // Create malfunction list panel
        malfunctionListPanel = new JPanel();
        malfunctionListPanel.setLayout(new BoxLayout(malfunctionListPanel, BoxLayout.Y_AXIS));
        malfunctionListMainPanel.add(malfunctionListPanel, BorderLayout.NORTH);
        
        // Create malfunction panels
        malfunctionCache = malfunctionable.getMalfunctionManager().getMalfunctions();
        malfunctionPanels = new ArrayList();
        Iterator i = malfunctionCache.iterator();
        while (i.hasNext()) {
            MalfunctionPanel panel = new MalfunctionPanel((Malfunction) i.next());
            malfunctionListPanel.add(panel);
            malfunctionPanels.add(panel);
        }
    }
    
    /**
     * Update this panel
     */
    public void update() {
    
        Malfunctionable malfunctionable = (Malfunctionable) proxy.getUnit();
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();
    
        // Update last completed label.
        int lastComplete = (int) manager.getTimeSinceLastMaintenance();
        if (lastComplete != lastCompletedTime) {
            lastCompletedTime = lastComplete;
            lastCompletedLabel.setText("Last Completed: " + lastCompletedTime + " millisols");
        }
        
        // Update progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);
        
        // Get list of malfunctions.
        Collection malfunctions = manager.getMalfunctions();
        
        // Update malfunction panels if necessary.
        if (!malfunctionCache.equals(malfunctions)) {
            // Add malfunction panels for new malfunctions.
            Iterator iter1 = malfunctions.iterator();
            while (iter1.hasNext()) {
                Malfunction malfunction = (Malfunction) iter1.next();
                if (!malfunctionCache.contains(malfunction)) {
                    MalfunctionPanel panel = new MalfunctionPanel(malfunction);
                    malfunctionPanels.add(panel);
                    malfunctionListPanel.add(panel);
                }
            }
            
            // Remove malfunction panels for repaired malfunctions.
            Iterator iter2 = malfunctionCache.iterator();
            while (iter2.hasNext()) {
                Malfunction malfunction = (Malfunction) iter2.next();
                if (!malfunctions.contains(malfunction)) {
                    MalfunctionPanel panel = getMalfunctionPanel(malfunction);
                    if (panel != null) {
                        malfunctionPanels.remove(panel);
                        malfunctionListPanel.remove(panel);
                    }
                }
            }
            
            // Update malfunction cache.
            malfunctionCache = malfunctions;
        }
    
        // Have each malfunction panel update.
        Iterator i = malfunctionPanels.iterator();
        while (i.hasNext()) ((MalfunctionPanel) i.next()).update();
    }
    
    /**
     * Gets an existing malfunction panel for a given malfunction.
     *
     * @param malfunction the given malfunction
     * @return malfunction panel or null if none.
     */
    private MalfunctionPanel getMalfunctionPanel(Malfunction malfunction) {
        MalfunctionPanel result = null;
        
        Iterator i = malfunctionPanels.iterator();
        while (i.hasNext()) {
            MalfunctionPanel panel = (MalfunctionPanel) i.next();
            if (panel.getMalfunction() == malfunction) result = panel;
        }
        
        return result;
    }
}
