/**
 * Mars Simulation Project
 * InsituResourceProcessorFacilityPanel.java
 * @version 2.71 2001-1-28
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** The InsituResourceProcessorFacilityPanel class 
 *  is the standard UI representation of a settlement's
 *  INSITU resource processor facility as a panel
 *  in the settlement's info window.
 */
public class InsituResourceProcessorFacilityPanel extends FacilityPanel {

    // Data members
    private InsituResourceProcessorFacility processor;  // The INSITU processor this panel displays.
    private JLabel fuelValueLabel;                      // A label displaying the rate of fuel generation.
    private JLabel oxygenValueLabel;                    // A label displaying the rate of oxygen generation.
    private JLabel waterValueLabel;                     // A label displaying the rate of water generation.

    // Update date cache
    private double fuel;                                // Fuel generation rate
    private double oxygen;                              // Oxygen generation rate
    private double water;                               // Water generation rate

    /** Constructs a InsituResourceProcessorFacilityPanel object.
     *  @param processor the INSITU resource processor
     *  @param desktop the desktop pane
     */
    public InsituResourceProcessorFacilityPanel(InsituResourceProcessorFacility processor, MainDesktopPane desktop) {

        // Use FacilityPanel's constructor
        super(desktop);

        // Initialize data members
        this.processor = processor;
        tabName = "INSITU";

        // Set up components
        setLayout(new BorderLayout());

        // Prepare main pane
        JPanel contentPane = new JPanel(new BorderLayout(0, 5));
        add(contentPane, "North");

        // Prepare processor label
        JLabel nameLabel = new JLabel("INSITU Resource Processor", JLabel.CENTER);
        nameLabel.setForeground(Color.black);
        contentPane.add(nameLabel, "North");

        // Prepare info pane
        JPanel infoPane = new JPanel(new BorderLayout(0, 5));
        infoPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        contentPane.add(infoPane, "Center");

        // Prepare resources label
        JLabel resourcesLabel = new JLabel("Resources Generated:", JLabel.CENTER);
        resourcesLabel.setForeground(Color.black);
        infoPane.add(resourcesLabel, "North");

        // Prepare rsources pane
        JPanel resourcesPane = new JPanel();
        infoPane.add(resourcesPane, "Center");

        // Prepare label pane
        JPanel labelPane = new JPanel(new GridLayout(3, 2, 5, 3));
        resourcesPane.add(labelPane);

        // Prepare fuel label
        JLabel fuelLabel = new JLabel("Fuel:");
        fuelLabel.setForeground(Color.black);
        labelPane.add(fuelLabel);

        // Prepare fuel value label
        fuel = processor.getFuelRate();
	fuelValueLabel = new JLabel("" + roundOneDecimal(fuel) + " units/day", JLabel.RIGHT);
        fuelValueLabel.setForeground(Color.black);
        labelPane.add(fuelValueLabel);

        // Prepare oxygen label
        JLabel oxygenLabel = new JLabel("Oxygen:");
        oxygenLabel.setForeground(Color.black);
        labelPane.add(oxygenLabel);

        // Prepare oxygen value label
        oxygen = processor.getOxygenRate();
	oxygenValueLabel = new JLabel("" + roundOneDecimal(oxygen) + " units/day", JLabel.RIGHT);
        oxygenValueLabel.setForeground(Color.black);
        labelPane.add(oxygenValueLabel);

        // Prepare water label
        JLabel waterLabel = new JLabel("Water:");
        waterLabel.setForeground(Color.black);
        labelPane.add(waterLabel);

        // Prepare water value label
        water = processor.getWaterRate();
	waterValueLabel = new JLabel("" + roundOneDecimal(water) + " units/day", JLabel.RIGHT);
        waterValueLabel.setForeground(Color.black);
        labelPane.add(waterValueLabel);
    }

    /** Updates the facility panel's information */
    public void updateInfo() {
    
        if (fuel != processor.getFuelRate()) {
            fuel = processor.getFuelRate();
            fuelValueLabel.setText("" + roundOneDecimal(fuel) + " units/day");
        }

        if (oxygen != processor.getOxygenRate()) {
            oxygen = processor.getOxygenRate();
            oxygenValueLabel.setText("" + roundOneDecimal(oxygen) + " units/day");
        }

        if (water != processor.getWaterRate()) {
            water = processor.getWaterRate();
            waterValueLabel.setText("" + roundOneDecimal(water) + " units/day");
        }
    } 

    /** Returns a double value rounded to one decimal point
     *  @param initial the initial double value
     *  @return the rounded value
     */
    public double roundOneDecimal(double initial) {
        return (double) (Math.round(initial * 10D) / 10D);
    }
}

