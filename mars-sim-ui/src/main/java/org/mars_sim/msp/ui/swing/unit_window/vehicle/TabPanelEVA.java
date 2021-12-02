/*
 * Mars Simulation Project
 * TabPanelEVA.java
 * @date 2021-09-25
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.vehicle;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.core.vehicle.Rover;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;


/**
 * The TabPanelEVA class represents the EVA airlock function of a vehicle.
 */
@SuppressWarnings("serial")
public class TabPanelEVA extends TabPanel implements MouseListener, ClockListener {
    /** Is UI constructed. */
    private boolean uiDone = false;

    private int occupiedCache;
    private int emptyCache;
    private double cycleTimeCache;

    private String operatorCache = "";
    private String airlockStateCache = "";

    private WebLabel occupiedLabel;
    private WebLabel emptyLabel;
    private WebLabel operatorLabel;
    private WebLabel airlockStateLabel;
    private WebLabel cycleTimeLabel;

    private ListModel listModel;
    private JList<Person> occupants;
    private WebScrollPane scrollPanel;

    private Airlock airlock;

    private static Simulation sim;
    private static MasterClock masterClock;

    /**
     * Constructor.
     * @param vehicle the vehicle.
     * @param desktop The main desktop.
     */
    public TabPanelEVA(Vehicle vehicle, MainDesktopPane desktop) {
        // Use the TabPanel constructor
        super(
                Msg.getString("TabPanelEVA.title"), //$NON-NLS-1$
                null,
                Msg.getString("TabPanelEVA.tooltip"), //$NON-NLS-1$
                vehicle, desktop
        );

        if (vehicle instanceof Rover)
        	airlock = ((Rover) vehicle).getAirlock();

    }

    public boolean isUIDone() {
        return uiDone;
    }

    public void initializeUI() {
        uiDone = true;

        // Initialize data members

        if (sim == null)
            sim = Simulation.instance();

        if (masterClock == null)
            masterClock = sim.getMasterClock();

        masterClock.addClockListener(this);

        // Create top panel
        WebPanel topPanel = new WebPanel(new GridLayout(6, 1, 0, 0)); // new FlowLayout(FlowLayout.CENTER));

        topPanel.setOpaque(false);
        topPanel.setBackground(new Color(0,0,0,128));
        topContentPanel.add(topPanel, BorderLayout.NORTH);

        // Create the title label
        WebLabel titleLabel = new WebLabel(Msg.getString("TabPanelEVA.title"), WebLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
        //titleLabel.setForeground(new Color(102, 51, 0)); // dark brown
        topPanel.add(titleLabel);

        // Create occupiedLabel
        occupiedLabel = new WebLabel(Msg.getString("TabPanelEVA.occupied",
                airlock.getNumOccupants()), WebLabel.CENTER);
        topPanel.add(occupiedLabel);


        // Create emptyLabel
        emptyLabel = new WebLabel(Msg.getString("TabPanelEVA.empty",
                airlock.getNumEmptied()), WebLabel.CENTER);
        topPanel.add(emptyLabel);


        // Create OperatorLabel
        operatorLabel = new WebLabel(Msg.getString("TabPanelEVA.operator",
                airlock.getOperatorName()), WebLabel.CENTER);
        topPanel.add(operatorLabel);


        // Create airlockStateLabel
        airlockStateLabel = new WebLabel(Msg.getString("TabPanelEVA.airlock.state",
                airlock.getState().toString()), WebLabel.CENTER);
        topPanel.add(airlockStateLabel);


        // Create cycleTimeLabel
        cycleTimeLabel = new WebLabel(Msg.getString("TabPanelEVA.airlock.cycleTime",
                Math.round(airlock.getRemainingCycleTime()*10.0)/10.0), WebLabel.CENTER);
        topPanel.add(cycleTimeLabel);

        UIManager.getDefaults().put("TitledBorder.titleColor", Color.darkGray);
        Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder title = BorderFactory.createTitledBorder(
                lowerEtched, " " + Msg.getString("TabPanelEVA.titledBorder") + " ");
        Font titleFont = UIManager.getFont("TitledBorder.font");
        title.setTitleFont(titleFont.deriveFont(Font.ITALIC + Font.BOLD));

        // Create occupant panel
        WebPanel occupantPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
        occupantPanel.setBorder(title);
        topContentPanel.add(occupantPanel, BorderLayout.CENTER);

        // Create scroll panel for occupant list.
        scrollPanel = new WebScrollPane();
        scrollPanel.setPreferredSize(new Dimension(150, 100));
        occupantPanel.add(scrollPanel);

        // Create occupant list model
        listModel = new ListModel(airlock);

        // Create occupant list
        occupants = new JList<Person>(listModel);
        occupants.addMouseListener(this);
        scrollPanel.setViewportView(occupants);

    }

    @Override
    public void update() {

        // Update occupiedLabel
        if (occupiedCache != airlock.getNumOccupants()) {
            occupiedCache = airlock.getNumOccupants();
            occupiedLabel.setText(Msg.getString("TabPanelEVA.occupied", occupiedCache));
        }

        // Update emptyLabel
        if (emptyCache != airlock.getNumEmptied()) {
            emptyCache = airlock.getNumEmptied();
            emptyLabel.setText(Msg.getString("TabPanelEVA.empty", emptyCache));
        }

        // Update operatorLabel
        if (!operatorCache.equals(airlock.getOperatorName())) {
            operatorCache = airlock.getOperatorName();
            operatorLabel.setText(Msg.getString("TabPanelEVA.operator", operatorCache));
        }

        // Update airlockStateLabel
        String state = airlock.getState().toString();
        if (!airlockStateCache.equalsIgnoreCase(state)) {
            airlockStateCache = state;
            airlockStateLabel.setText(Msg.getString("TabPanelEVA.airlock.state", state));
        }

        // Update cycleTimeLabel
        double time = Math.round(airlock.getRemainingCycleTime()*10.0)/10.0;
        if (cycleTimeCache != time) {
            cycleTimeCache = time;
            cycleTimeLabel.setText(Msg.getString("TabPanelEVA.airlock.cycleTime", time));
        }

        // Update occupant list
        listModel.update();
        scrollPanel.validate();
    }

    /**
     * List model for airlock occupant.
     */
    private class ListModel extends AbstractListModel<Person> {

        private Airlock airlock;
        private List<Person> list;
        private List<Integer> intList;

        private ListModel(Airlock airlock) {
            this.airlock = airlock;

            intList = new ArrayList<>(airlock.getAllInsideOccupants());
            list = new ArrayList<>();

            for (int i: intList) {
                list.add(airlock.getPersonByID(i));
            }

            Collections.sort(list);
        }

        @Override
        public Person getElementAt(int index) {

            Person result = null;

            if ((index >= 0) && (index < airlock.getAllInsideOccupants().size())) {
                result = list.get(index);
            }

            return result;
        }

        @Override
        public int getSize() {
            return airlock.getNumOccupants();
        }

        /**
         * Update the population list model.
         */
        public void update() {

            List<Integer> newIntList = new ArrayList<>(airlock.getAllInsideOccupants());

            if (!intList.containsAll(newIntList)
                    || !newIntList.containsAll(intList)) {

                intList = newIntList;

                list = new ArrayList<>();

                for (int i: newIntList) {
                    list.add(airlock.getPersonByID(i));
                }

                Collections.sort(list);

                fireContentsChanged(this, 0, getSize());
            }
        }
    }

    /**
     * Mouse clicked event occurs.
     *
     * @param event the mouse event
     */
    public void mouseClicked(MouseEvent event) {
        // If double-click, open person window.
        if (event.getClickCount() >= 2) {
            Person person = (Person) occupants.getSelectedValue();
            if (person != null) {
                desktop.openUnitWindow(person, false);
            }
        }

        // Update panel
        update();
    }

    public void mousePressed(MouseEvent arg0) {
        // Update panel
        update();
    }

    public void mouseReleased(MouseEvent arg0) {
    }

    public void mouseEntered(MouseEvent arg0) {
        // Update panel
        update();
    }

    public void mouseExited(MouseEvent arg0) {
        // Update panel
        update();
    }

    @Override
    public void clockPulse(ClockPulse pulse) {
    }

    @Override
    public void uiPulse(double time) {
        // Update panel
        update();
    }

    @Override
    public void pauseChange(boolean isPaused, boolean showPane) {
        // Update panel
        update();
    }

    public void destroy() {

        occupiedLabel = null;
        emptyLabel = null;
        operatorLabel = null;
        airlockStateLabel = null;
        cycleTimeLabel = null;

        listModel = null;
        occupants = null;
        scrollPanel = null;

        airlock = null;
    }
}
