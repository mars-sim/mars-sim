/**
 * Mars Simulation Project
 * LaboratoryFacilityPanel.java
 * @version 2.71 2000-10-22
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** The LaboratoryFacilityPanel class displays information about a
 *  settlement's laboratory facility in the user interface.
 */
public class LaboratoryFacilityPanel extends FacilityPanel {

    // Data members
    private LaboratoryFacility laboratory; // The laboratory facility this panel displays.

    /** Constructs a LaboratoryFacilityPanel object 
     *  @param laboratory the laboratory facility
     *  @param desktop the desktop pane
     */
    public LaboratoryFacilityPanel(LaboratoryFacility laboratory,
            MainDesktopPane desktop) {

        // Use FacilityPanel's constructor
        super(desktop);

        // Initialize data members
        this.laboratory = laboratory;
        tabName = "Lab";

        // Set up components
        setLayout(new BorderLayout());

        // Prepare content pane
        JPanel contentPane = new JPanel(new BorderLayout(0, 5));
        add(contentPane, "North");

        // Prepare name label
        JLabel nameLabel = new JLabel("Laboratory", JLabel.CENTER);
        nameLabel.setForeground(Color.black);
        contentPane.add(nameLabel, "North");

        // Prepare info pane
        JPanel infoPane = new JPanel(new BorderLayout(0, 5));
        contentPane.add(infoPane, "Center");

        // Prepare label panel
        JPanel labelPane = new JPanel(new GridLayout(2, 1, 0, 5));
        labelPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        infoPane.add(labelPane, "North");

        // Prepare lab size label
        JLabel labSizeLabel =
                new JLabel("Laboratory Size: " + laboratory.getLaboratorySize(),
                JLabel.CENTER);
        labSizeLabel.setForeground(Color.black);
        labelPane.add(labSizeLabel);

        // Prepare lab tech label
        JLabel labTechLabel = new JLabel("Technology Level: " +
                laboratory.getTechnologyLevel(), JLabel.CENTER);
        labTechLabel.setForeground(Color.black);
        labelPane.add(labTechLabel);

        // Prepare tech pane
        JPanel techPane = new JPanel();
        techPane.setBorder(
                new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        infoPane.add(techPane, "Center");

        // Prepare inner tech pane
        JPanel innerTechPane = new JPanel(new BorderLayout());
        techPane.add(innerTechPane);

        // Prepare tech label
        JLabel techLabel = new JLabel("Research Specialities:", JLabel.CENTER);
        techLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        techLabel.setForeground(Color.black);
        innerTechPane.add(techLabel, "North");

        // Get specialities info
        String[] specialities = laboratory.getTechSpecialities();

        // Prepare speciality pane
        JPanel specialityPane = new JPanel(new GridLayout(specialities.length, 1));
        innerTechPane.add(specialityPane, "Center");

        // Prepare speciality labels
        JLabel[] specialityLabels = new JLabel[specialities.length];
        for (int x = 0; x < specialities.length; x++) {
            specialityLabels[x] = new JLabel(specialities[x], JLabel.CENTER);
            specialityLabels[x].setForeground(Color.black);
            specialityPane.add(specialityLabels[x]);
        }
    }

    /** Updates the facility panel's information */
    public void updateInfo() {
        // Implement later
    }
}
