/**
 * Mars Simulation Project
 * PersonTableModel.java
 * @version 2.76 2004-06-10
 * @author Barry Evans
 */

package org.mars_sim.msp.ui.standard.tool.monitor;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.mission.Mission;
import org.mars_sim.msp.simulation.person.ai.task.TaskManager;
import org.mars_sim.msp.simulation.structure.Settlement;
import org.mars_sim.msp.simulation.vehicle.Crewable;

/**
 * The PersonTableModel that maintains a list of Person objects. By defaults
 * the source of the lsit is the Unit Manager.
 * It maps key attributes of the Person into Columns.
 */
public class PersonTableModel extends UnitTableModel {

    // Column indexes
    private final static int  NAME = 0;           // Person name column
    private final static int  LOCATION = 1;       // Location column
    private final static int  HUNGER = 2;         // Hunger column
    private final static int  FATIGUE = 3;        // Fatigue column
	private final static int  STRESS = 4;         // Stress column
    private final static int  PERFORMANCE = 5;    // Performance conlumn
    private final static int  JOB = 6;            // Job column
    private final static int  TASK = 7;           // Task column
    private final static int  MISSION = 8;        // Mission column
    private final static int  HEALTH = 9;         // Health column
    private final static int  COLUMNCOUNT = 10;   // The number of Columns
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
        columnNames[STRESS] = "Stress %";
        columnTypes[STRESS] = Integer.class;
        columnNames[PERFORMANCE] = "Performance %";
        columnTypes[PERFORMANCE] = Integer.class;
        columnNames[LOCATION] = "Location";
        columnTypes[LOCATION] = String.class;
        columnNames[JOB] = "Job";
        columnTypes[JOB] = String.class;
        columnNames[MISSION] = "Mission";
        columnTypes[MISSION] = String.class;
        columnNames[TASK] = "Task";
        columnTypes[TASK] = String.class;
        columnNames[HEALTH] = "Health";
        columnTypes[HEALTH] = String.class;
    }

    // Data members
    private Crewable vehicle;      // Monitored Crewable Vehicle
    private Settlement settlement; // Monitored Location
    private Mission mission;       // Monitored Mission

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * proxy manager.
     *
     * @param unitManager Manager containing Person objects.
     */
    public PersonTableModel(UnitManager unitManager) {
        super("All People", " people", columnNames, columnTypes);

        setSource(unitManager.getPeople());
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified Vechile
     *
     * @param vehicle Monitored vehicle Person objects.
     */
    public PersonTableModel(Crewable vehicle) {
        super(((Unit) vehicle).getName() + " - People", " people",
              columnNames, columnTypes);

        this.vehicle = vehicle;
        setSource(vehicle.getCrew());
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified Vehicle
     *
     * @param settlement Monitored settlement Person objects.
     */
    public PersonTableModel(Settlement settlement) {
        super(settlement.getName() + " - People", " residents",
              columnNames, columnTypes);

        this.settlement = settlement;

        setSource(settlement.getInhabitants());
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified Mission.
     *
     * @param mission Monitored mission Person objects.
     */
    public PersonTableModel(Mission mission) {
        super(mission.getName() + " - People", " mission members",
              columnNames, columnTypes);

	    this.mission = mission;

	    setSource(mission.getPeople());
    }

    /**
     * Defines the soruce data from this table
     */
    private void setSource(PersonCollection source) {
        PersonIterator iter = source.iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }

        source.addMspCollectionEventListener(this);
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
                result = new Integer(new Float(fatigue).intValue());
            } break;
            
            case STRESS : {
            	double stress = person.getPhysicalCondition().getStress();
            	result = new Integer(new Double(stress).intValue()); 
            } break;

            case PERFORMANCE : {
                double performance = person.getPhysicalCondition().getPerformanceFactor();
                result = new Integer(new Float(performance * 100D).intValue());
            } break;

            case HEALTH : {
                result = person.getPhysicalCondition().getHealthSituation();
            } break;

            // Create a diplay vehicle, settlement, outside or buried.
            case LOCATION : {
                String locationSituation = person.getLocationSituation();
                if (locationSituation.equals(Person.INSETTLEMENT)) {
                    if (person.getSettlement() != null) result = person.getSettlement().getName();
                }
                else if (locationSituation.equals(Person.INVEHICLE)) {
                    if (person.getVehicle() != null) result = person.getVehicle().getName();
                }
                else result = locationSituation;
            } break;
            
            case JOB : {
            	// If person is dead, get job from deathinfo.
            	if (person.getPhysicalCondition().isDead()) 
            		result = person.getPhysicalCondition().getDeathDetails().getJob();
     			else {
     				if (person.getMind().getJob() != null) result = person.getMind().getJob().getName();
     				else result = null;
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