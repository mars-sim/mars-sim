/**
 * Mars Simulation Project
 * MalfunctionableBuildingPanel.java
 * @version 2.75 2003-06-02
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard.unit_window.structure.building;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import org.mars_sim.msp.simulation.malfunction.*;
import org.mars_sim.msp.simulation.structure.building.*;
import org.mars_sim.msp.ui.standard.MainDesktopPane;
import org.mars_sim.msp.ui.standard.unit_window.MalfunctionPanel;

/**
 * The MalfunctionableBuildingPanel class is a building function panel representing 
 * the malfunctions of a settlement building.
 */
public class MalfunctionableBuildingPanel extends BuildingFunctionPanel {
    
    private Malfunctionable malfunctionable; // The malfunctionable building.
    private Collection malfunctionPanels; // List of malfunction panels.
    private Collection malfunctionCache; // List of malfunctions in building.
    private JPanel malfunctionListPanel; // Malfunction list panel.
    
    /**
     * Constructor
     *
     * @param malfunctionable the malfunctionable building the panel is for.
     * @param desktop The main desktop.
     */
    public MalfunctionableBuildingPanel(Malfunctionable malfunctionable, MainDesktopPane desktop) {
        
        // Use BuildingFunctionPanel constructor
        super((Building) malfunctionable, desktop);
        
        // Initialize data members.
        this.malfunctionable = malfunctionable;
        
        // Set panel layout
        setLayout(new BorderLayout());
        
        // Create malfunctions label
        JLabel malfunctionsLabel = new JLabel("Malfunctions", JLabel.CENTER);
        add(malfunctionsLabel, BorderLayout.NORTH);
        
        // Create scroll panel for malfunction list
        JScrollPane malfunctionScrollPanel = new JScrollPane();
        malfunctionScrollPanel.setPreferredSize(new Dimension(170, 90));
        add(malfunctionScrollPanel, BorderLayout.CENTER);
        
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
        
        Collection malfunctions = malfunctionable.getMalfunctionManager().getMalfunctions();
        
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
