/**
 * Mars Simulation Project
 * InsituResourceProcessorPanel.java
 * @version 2.74 2002-03-17
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/** The InsituResourceProcessorPanel class 
 *  is the standard UI representation of a settlement's
 *  INSITU resource processor facility as a panel
 *  in the settlement's info window.
 */
public class InsituResourceProcessorPanel extends FacilityPanel {

    // Data members
    private InsituResourceProcessor processor; // The INSITU processor this panel displays.
    private JLabel fuelValueLabel;             // A label displaying the rate of fuel generation.
    private JLabel oxygenValueLabel;           // A label displaying the rate of oxygen generation.
    private JLabel waterValueLabel;            // A label displaying the rate of water generation.

    // Update date cache
    private double fuel;                       // Fuel generation rate
    private double oxygen;                     // Oxygen generation rate
    private double water;                      // Water generation rate

    /** Constructs a InsituResourceProcessorPanel object.
     *  @param processor the INSITU resource processor
     *  @param desktop the desktop pane
     */
    public InsituResourceProcessorPanel(InsituResourceProcessor processor, MainDesktopPane desktop) {

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
        contentPane.add(nameLabel, "North");

        // Prepare info pane
        JPanel infoPane = new JPanel(new BorderLayout(0, 5));
        infoPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        contentPane.add(infoPane, "Center");

        // Prepare resources label
        JLabel resourcesLabel = new JLabel("Resources Generated:", JLabel.CENTER);
        infoPane.add(resourcesLabel, "North");

        // Prepare rsources pane
        JPanel resourcesPane = new JPanel();
        infoPane.add(resourcesPane, "Center");

        // Prepare label pane
        JPanel labelPane = new JPanel(new GridLayout(3, 2, 5, 3));
        resourcesPane.add(labelPane);

        // Prepare fuel label
        JLabel fuelLabel = new JLabel("Fuel:");
        labelPane.add(fuelLabel);

        // Prepare fuel value label
        fuel = processor.getFuelRate();
	fuelValueLabel = new JLabel("" + roundOneDecimal(fuel * 1000D) + " kg/sol", JLabel.RIGHT);
        labelPane.add(fuelValueLabel);

        // Prepare oxygen label
        JLabel oxygenLabel = new JLabel("Oxygen:");
        labelPane.add(oxygenLabel);

        // Prepare oxygen value label
        oxygen = processor.getOxygenRate();
	oxygenValueLabel = new JLabel("" + roundOneDecimal(oxygen * 1000D) + " kg/sol", JLabel.RIGHT);
        labelPane.add(oxygenValueLabel);

        // Prepare water label
        JLabel waterLabel = new JLabel("Water:");
        labelPane.add(waterLabel);

        // Prepare water value label
        water = processor.getWaterRate();
	waterValueLabel = new JLabel("" + roundOneDecimal(water * 1000D) + " kg/sol", JLabel.RIGHT);
        labelPane.add(waterValueLabel);
    }

    /** Updates the facility panel's information */
    public void updateInfo() {

        if (fuel != processor.getFuelRate()) {
            fuel = processor.getFuelRate();
            fuelValueLabel.setText("" + roundOneDecimal(fuel * 1000D) + " kg/sol");
        }

        if (oxygen != processor.getOxygenRate()) {
            oxygen = processor.getOxygenRate();
            oxygenValueLabel.setText("" + roundOneDecimal(oxygen * 1000D) + " kg/sol");
        }

        if (water != processor.getWaterRate()) {
            water = processor.getWaterRate();
            waterValueLabel.setText("" + roundOneDecimal(water * 1000D) + " kg/sol");
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

