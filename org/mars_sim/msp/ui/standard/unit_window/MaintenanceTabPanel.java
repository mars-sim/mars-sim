/**
 * Mars Simulation Project
 * MaintenanceTabPanel.java
 * @version 2.81 2007-08-27
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import org.mars_sim.msp.simulation.Unit;
import org.mars_sim.msp.simulation.malfunction.Malfunction;
import org.mars_sim.msp.simulation.malfunction.MalfunctionManager;
import org.mars_sim.msp.simulation.malfunction.Malfunctionable;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.MarsPanelBorder;

/** 
 * The MaintenanceTabPanel is a tab panel for unit maintenance information.
 */
public class MaintenanceTabPanel extends TabPanel {
    
    private JLabel lastCompletedLabel; // The last completed label.
    private BoundedRangeModel progressBarModel; // The progress bar model.
    private int lastCompletedTime; // The time since last completed maintenance.
    private Collection<MalfunctionPanel> malfunctionPanels; // List of malfunction panels.
    private Collection<Malfunction> malfunctionCache; // List of malfunctions.
    private JPanel malfunctionListPanel; // Malfunction list panel.
    
    /**
     * Constructor
     *
     * @param unit the unit to display.
     * @param desktop the main desktop.
     */
    public MaintenanceTabPanel(Unit unit, MainDesktopPane desktop) { 
        // Use the TabPanel constructor
        super("Maint", null, "Maintenance", unit, desktop);
        
        Malfunctionable malfunctionable = (Malfunctionable) unit;
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();
        
        // Create maintenance panel
        JPanel maintenancePanel = new JPanel(new GridLayout(3, 1, 0, 0));
        maintenancePanel.setBorder(new MarsPanelBorder());
        topContentPanel.add(maintenancePanel);
        
        // Create maintenance label.
        JLabel maintenanceLabel = new JLabel("Maintenance", JLabel.CENTER);
        maintenancePanel.add(maintenanceLabel);
        
        // Create lastCompletedLabel.
        lastCompletedTime = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        lastCompletedLabel = new JLabel("Last Completed: " + lastCompletedTime + 
            " sols", JLabel.CENTER);
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
        malfunctionPanels = new ArrayList<MalfunctionPanel>();
        Iterator<Malfunction> i = malfunctionCache.iterator();
        while (i.hasNext()) {
            MalfunctionPanel panel = new MalfunctionPanel(i.next());
            malfunctionListPanel.add(panel);
            malfunctionPanels.add(panel);
        }
    }
    
    /**
     * Update this panel
     */
    public void update() {
    
        Malfunctionable malfunctionable = (Malfunctionable) unit;
        MalfunctionManager manager = malfunctionable.getMalfunctionManager();
    
        // Update last completed label.
        int lastComplete = (int) (manager.getTimeSinceLastMaintenance() / 1000D);
        if (lastComplete != lastCompletedTime) {
            lastCompletedTime = lastComplete;
            lastCompletedLabel.setText("Last Completed: " + lastCompletedTime + " sols");
        }
        
        // Update progress bar.
        double completed = manager.getMaintenanceWorkTimeCompleted();
        double total = manager.getMaintenanceWorkTime();
        int percentDone = (int) (100D * (completed / total));
        progressBarModel.setValue(percentDone);
        
        // Get list of malfunctions.
        Collection<Malfunction> malfunctions = manager.getMalfunctions();
        
        // Update malfunction panels if necessary.
        if (!malfunctionCache.equals(malfunctions)) {
            // Add malfunction panels for new malfunctions.
            Iterator<Malfunction> iter1 = malfunctions.iterator();
            while (iter1.hasNext()) {
                Malfunction malfunction = iter1.next();
                if (!malfunctionCache.contains(malfunction)) {
                    MalfunctionPanel panel = new MalfunctionPanel(malfunction);
                    malfunctionPanels.add(panel);
                    malfunctionListPanel.add(panel);
                }
            }
            
            // Remove malfunction panels for repaired malfunctions.
            Iterator<Malfunction> iter2 = malfunctionCache.iterator();
            while (iter2.hasNext()) {
                Malfunction malfunction = iter2.next();
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
        Iterator<MalfunctionPanel> i = malfunctionPanels.iterator();
        while (i.hasNext()) i.next().update();
    }
    
    /**
     * Gets an existing malfunction panel for a given malfunction.
     *
     * @param malfunction the given malfunction
     * @return malfunction panel or null if none.
     */
    private MalfunctionPanel getMalfunctionPanel(Malfunction malfunction) {
        MalfunctionPanel result = null;
        
        Iterator<MalfunctionPanel> i = malfunctionPanels.iterator();
        while (i.hasNext()) {
            MalfunctionPanel panel = i.next();
            if (panel.getMalfunction() == malfunction) result = panel;
        }
        
        return result;
    }
}
