/**
 * Mars Simulation Project
 * PersonTableModel.java
 * @version 2.74 2002-02-22
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.monitor;

import org.mars_sim.msp.ui.standard.UIProxyManager;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Vehicle;

import java.util.List;

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
    private Vehicle         vehicle;    // Monitored Vehicle
    private Settlement      settlement; // Monitored Location
    private Mission         mission; // Monitored Mission

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * proxy manager.
     *
     * @param unitManager Manager containing Person objects.
     */
    public PersonTableModel(UnitManager unitManager) {
        super("All People", columnNames, columnTypes);

        PersonIterator iter = unitManager.getPeople().sortByName().iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified Vechile
     *
     * @param vehicle Monitored vehicle Person objects.
     */
    public PersonTableModel(Vehicle vehicle) {
        super(vehicle.getName() + " - People", columnNames, columnTypes);

        this.vehicle = vehicle;

        PersonIterator iter = vehicle.getPassengers().iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified Vehicle
     *
     * @param settlement Monitored settlement Person objects.
     */
    public PersonTableModel(Settlement settlement) {
        super(settlement.getName() + " - People", columnNames, columnTypes);

        this.settlement = settlement;

        PersonIterator iter = settlement.getInhabitants().iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified Mission.
     *
     * @param mission Monitored mission Person objects.
     */
    public PersonTableModel(Mission mission) {
        super(mission.getName() + " - People", columnNames, columnTypes);

	this.mission = mission;

	PersonIterator iter = mission.getPeople().iterator();
	while(iter.hasNext()) {
            add(iter.next());
	}
    }
    
    /**
     * Compare the current contents to the expected. This would be alot easier
     * if pure Collection were used as the comparision methods have to be
     * implemented by hand.
     *
     * @param contents The contents of this model.
     */
    protected void checkContents(List contents) {
        PersonCollection people = null;

        // Find an appropriate source collection,
        if (vehicle != null) {
            people = vehicle.getPassengers();
        }
        else if (settlement != null) {
            people = settlement.getInhabitants();
        }
	else if (mission != null) {
	    people = mission.getPeople();
	}

        // Compare current to actual
        if (people != null) {

            // Rows been deleted ?
            if (contents.size() > people.size()) {
                for( int i = 0; i < contents.size(); i++) {
                    Person person = (Person)contents.get(i);
                    if (people.contains(person)) {
                        i++;
                    }
                    else {
                        contents.remove(i);
                        fireTableRowsDeleted(i,i);
                    }
                }
            }

            // Rows added
            else if (contents.size() < people.size()) {
                PersonIterator iter = people.iterator();
                while(iter.hasNext()) {
                    Person person = iter.next();
                    if (!contents.contains(person)) {
                        contents.add(person);
                        fireTableRowsDeleted(contents.size(), contents.size());
                    }
                }
            }
        }
    }

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        Person person = (Person)getUnit(rowIndex);

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
                result = ((mgr != null)? mgr.getTaskName() : null);
            } break;

            case MISSION : {
                Mission mission = person.getMind().getMission();
                if (mission != null) {
                    StringBuffer cellValue = new StringBuffer();
                    cellValue.append(mission.getName());
                    // cellValue.append(" - ");
                    // cellValue.append(mission.getPhase());
                    result = cellValue.toString();
                }
            } break;
        }

        return result;
    }
}
