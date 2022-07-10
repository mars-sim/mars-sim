/*
 * Mars Simulation Project
 * TabPanelEVA.java
 * @date 2022-07-09
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.ImageLoader;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitListPanel;

import com.alee.laf.panel.WebPanel;


/**
 * The TabPanelEVA class represents the EVA airlock function of a vehicle.
 */
@SuppressWarnings("serial")
public class TabPanelEVA extends TabPanel {

	private static final String EVA_ICON = Msg.getString("icon.eva"); //$NON-NLS-1$

	private int occupiedCache;
    private int emptyCache;
    private double cycleTimeCache;

    private String operatorCache = "";
    private String airlockStateCache = "";

    private JTextField occupiedLabel;
    private JTextField emptyLabel;
    private JTextField operatorLabel;
    private JTextField airlockStateLabel;
    private JTextField cycleTimeLabel;

    private UnitListPanel<Person> occupants;

    private Airlock airlock;


    /**
     * Constructor.
     * @param vehicle the vehicle.
     * @param desktop The main desktop.
     */
    public TabPanelEVA(Vehicle vehicle, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
        	null,
			ImageLoader.getNewIcon(EVA_ICON),        	
        	Msg.getString("TabPanelEVA.title"), //$NON-NLS-1$
        	vehicle, desktop
        );

        if (vehicle instanceof Rover)
        	airlock = ((Rover) vehicle).getAirlock();
    }

    @Override
    protected void buildUI(JPanel content) {
    	if (airlock == null) {
    		return;
    	}

        // Create top panel
        WebPanel topPanel = new WebPanel(new GridLayout(6, 1, 0, 0)); // new FlowLayout(FlowLayout.CENTER));
        content.add(topPanel, BorderLayout.NORTH);

        // Create occupiedLabel
        occupiedLabel = addTextField(topPanel, Msg.getString("TabPanelEVA.occupied"),
                					 airlock.getNumOccupants(), null);

        // Create emptyLabel
        emptyLabel = addTextField(topPanel, Msg.getString("TabPanelEVA.empty"),
                airlock.getNumEmptied(), null);

        // Create OperatorLabel
        operatorLabel = addTextField(topPanel, Msg.getString("TabPanelEVA.operator"),
                airlock.getOperatorName(), null);

        // Create airlockStateLabel
        airlockStateLabel = addTextField(topPanel, Msg.getString("TabPanelEVA.airlock.state"),
                airlock.getState().toString(), null);

        // Create cycleTimeLabel
        cycleTimeLabel = addTextField(topPanel, Msg.getString("TabPanelEVA.airlock.cycleTime"),
        								DECIMAL_PLACES1.format(airlock.getRemainingCycleTime()), null);

        // Create occupant panel
        WebPanel occupantPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
        addBorder(occupantPanel, Msg.getString("TabPanelEVA.titledBorder")); 
        content.add(occupantPanel, BorderLayout.CENTER);

        // Create occupant list, new Dimension(150, 100)
        occupants = new UnitListPanel<>(getDesktop()) {

			@Override
			protected Collection<Person> getData() {
				return getUnitsFromIds(airlock.getAllInsideOccupants());
			}
        };
        
        occupantPanel.add(occupants);
    }

    @Override
    public void update() {

        // Update occupiedLabel
        if (occupiedCache != airlock.getNumOccupants()) {
            occupiedCache = airlock.getNumOccupants();
            occupiedLabel.setText(Integer.toString(occupiedCache));
        }

        // Update emptyLabel
        if (emptyCache != airlock.getNumEmptied()) {
            emptyCache = airlock.getNumEmptied();
            emptyLabel.setText(Integer.toString(emptyCache));
        }

        // Update operatorLabel
        if (!operatorCache.equals(airlock.getOperatorName())) {
            operatorCache = airlock.getOperatorName();
            operatorLabel.setText(operatorCache);
        }

        // Update airlockStateLabel
        String state = airlock.getState().toString();
        if (!airlockStateCache.equalsIgnoreCase(state)) {
            airlockStateCache = state;
            airlockStateLabel.setText(state);
        }

        // Update cycleTimeLabel
        double time = airlock.getRemainingCycleTime();
        if (cycleTimeCache != time) {
            cycleTimeCache = time;
            cycleTimeLabel.setText(DECIMAL_PLACES1.format(time));
        }

        // Update occupant list
        occupants.update();
    }

    @Override
    public void destroy() {
        super.destroy();
        
        occupiedLabel = null;
        emptyLabel = null;
        operatorLabel = null;
        airlockStateLabel = null;
        cycleTimeLabel = null;

        occupants = null;

        airlock = null;
    }
}