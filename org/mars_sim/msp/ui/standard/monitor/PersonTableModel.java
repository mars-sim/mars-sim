/**
 * Mars Simulation Project
 * PersonTableModel.java
 * @version 2.74 2002-01-13
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.UIProxyManager;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults
 * the source of the lsit is the Unit Manager.
 * It maps key attributes of the Person into Columns.
 */
public class PersonTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;           // Person name column
    private final static int  LOCATION = 1;      // Situation column
    private final static int  COORDS = 2;       // Location column
    private final static int  HUNGER = 3;         // Hunger column
    private final static int  FATIGUE = 4;        // Fatigue column
    private final static int  TASK = 5;           // Task column
    private final static int  MISSION = 6;        // Mission column
    private final static int  HEALTH = 7;         // Health column
    private final static int  COLUMNCOUNT = 8;    // The number of Columns
    private static String columnNames[];          // Names of Columns
    private static Class columnTypes[];           // Types of Columns
    /**
     * The static initialisier creates the name & type arrays.
     */
    static {
        columnNames = new String[COLUMNCOUNT];
        columnTypes = new Class[COLUMNCOUNT];
        columnNames[NAME] = "Name";
        columnTypes[NAME] = String.class;
        columnNames[HUNGER] = "Hunger";
        columnTypes[HUNGER] = Integer.class;
        columnNames[FATIGUE] = "Fatigue";
        columnTypes[FATIGUE] = Integer.class;
        columnNames[COORDS] = "Coordinates";
        columnTypes[COORDS] = Coordinates.class;
        columnNames[LOCATION] = "Location";
        columnTypes[LOCATION] = String.class;
        columnNames[MISSION] = "Mission";
        columnTypes[MISSION] = String.class;
        columnNames[TASK] = "Task";
        columnTypes[TASK] = String.class;
        columnNames[HEALTH] = "Health";
        columnTypes[HEALTH] = MedicalComplaint.class;
    }

    // Data members
    private UIProxyManager proxyManager;        // Source of all Person

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * proxy manager.
     *
     * @param proxyManager Manager containing Person objects.
     */
    public PersonTableModel(UIProxyManager proxyManager) {
        super("Person", columnNames, columnTypes);

        this.proxyManager = proxyManager;
        addAll();
    }

    /**
     * Find all the Person units in the simulation and add them to this
     * model
     */
    public void addAll() {
        add(proxyManager.getOrderedPersonProxies());
    }


    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Person person = (Person)getUnit(rowIndex).getUnit();

        // Invoke the appropriate method, switch is the best solution
        // althought disliked by some
        switch (columnIndex) {
            case NAME : {
                result = person.getName();
            } break;

            case HUNGER : {
                double hunger = person.getPhysicalCondition().getHunger();
                result = new Integer(new Float(hunger).intValue());
            } break;

            case FATIGUE : {
                double fatigue = person.getPhysicalCondition().getFatigue();
                result = new Integer(new Float(fatigue).intValue());;
            } break;

            case COORDS : {
                result = person.getCoordinates();
            } break;

            case HEALTH : {
                result = person.getPhysicalCondition().getHealthSituation();
            } break;

            // Create a composite sdtring containing Vehicle & Settlement
            case LOCATION : {
                Settlement house = person.getSettlement();
                if (house != null) {
                    result = house.getName();
                }
                else {
                    Vehicle vech = person.getVehicle();
                    if (vech != null) {
                        result = vech.getName();
                    }
                }
            } break;

            case TASK : {
                // If the Person is dead, there is no Task Manager
                TaskManager mgr = person.getMind().getTaskManager();
                result = ((mgr != null)? mgr.getTaskDescription() : null);
            } break;

            case MISSION : {
                Mission mission = person.getMind().getMission();
                if (mission != null) {
                    StringBuffer cellValue = new StringBuffer();
                    cellValue.append(mission.getName());
                    cellValue.append(" - ");
                    cellValue.append(mission.getPhase());
                    result = cellValue.toString();
                }
            } break;
        }

        return result;
    }
}
