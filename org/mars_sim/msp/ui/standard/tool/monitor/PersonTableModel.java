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
    
    // Valid source types.
    private static final String ALL_PEOPLE = "All People";
    private static final String VEHICLE_CREW = "Vehicle Crew";
    private static final String SETTLEMENT_INHABITANTS = "Settlement Inhabitants";
    private static final String MISSION_PEOPLE = "Mission People";
    
    private String sourceType; // The type of source for the people table.
    
    // List sources.
    private UnitManager unitManager;
    private Crewable vehicle;
    private Settlement settlement;
    private Mission mission;

    /**
     * Constructs a PersonTableModel object that displays all people in the simulation.
     * @param unitManager Manager containing Person objects.
     */
    public PersonTableModel(UnitManager unitManager) {
        super("All People", " people", columnNames, columnTypes);

		sourceType = ALL_PEOPLE;
		this.unitManager = unitManager;
        setSource(unitManager.getPeople());
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified vehicle.
     * @param vehicle Monitored vehicle Person objects.
     */
    public PersonTableModel(Crewable vehicle) {
        super(((Unit) vehicle).getName() + " - People", " people",
              columnNames, columnTypes);

		sourceType = VEHICLE_CREW;
		this.vehicle = vehicle;
        setSource(vehicle.getCrew());
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified settlement.
     * @param settlement Monitored settlement Person objects.
     */
    public PersonTableModel(Settlement settlement) {
        super(settlement.getName() + " - People", " residents",
              columnNames, columnTypes);

		sourceType = SETTLEMENT_INHABITANTS;
		this.settlement = settlement;
        setSource(settlement.getInhabitants());
    }

    /**
     * Constructs a PersonTableModel object that displays all Person from the
     * specified mission.
     * @param mission Monitored mission Person objects.
     */
    public PersonTableModel(Mission mission) {
        super(mission.getName() + " - People", " mission members",
              columnNames, columnTypes);

		sourceType = MISSION_PEOPLE;
		this.mission = mission;
	    setSource(mission.getPeople());
    }

    /**
     * Defines the source data from this table
     */
    private void setSource(PersonCollection source) {
        PersonIterator iter = source.iterator();
        while(iter.hasNext()) {
            add(iter.next());
        }
    }
    
	/**
	 * The Model should be updated to reflect any changes in the underlying
	 * data.
	 * @return A status string for the contents of the model.
	 */
	public String update() {
		String statusString = "";
		
		if (sourceType.equals(ALL_PEOPLE)) statusString = update(unitManager.getPeople());
		else if (sourceType.equals(VEHICLE_CREW)) statusString = update(vehicle.getCrew());
		else if (sourceType.equals(SETTLEMENT_INHABITANTS)) statusString = update(settlement.getInhabitants());
		else if (sourceType.equals(MISSION_PEOPLE)) {
			if (mission != null) statusString = update(mission.getPeople());
			else statusString = update(null);
		} 
		
		return statusString;
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
        // although disliked by some
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