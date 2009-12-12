/**
 * Mars Simulation Project
 * MissionTableModel.java
 * @version 2.86 2009-05-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManagerListener;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.RoverMission;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;

public class MissionTableModel extends AbstractTableModel implements
		MonitorModel, MissionManagerListener, MissionListener {

	// Column indexes
	private final static int TYPE = 0;               // Type column
	private final static int DESCRIPTION = 1;        // Description column
	private final static int PHASE = 2;              // Phase column
	private final static int VEHICLE = 3;            // Mission vehicle column
	private final static int STARTING_SETTLEMENT = 4;// Starting settlement column
	private final static int MEMBER_NUM = 5;         // Member number column
	private final static int NAVPOINT_NUM = 6;       // Navpoint number column
	private final static int TRAVELLED_DISTANCE = 7; // Travelled distance column
	private final static int REMAINING_DISTANCE = 8; // Remaining distance column
	private final static int COLUMNCOUNT = 9;        // The number of Columns
	private static String columnNames[];             // Names of Columns
    private static Class columnTypes[];              // Types of Columns
	
    private List<Mission> missionCache;
    
    public MissionTableModel() {
    	columnNames = new String[COLUMNCOUNT];
        columnTypes = new Class[COLUMNCOUNT];
        columnNames[TYPE] = "Type";
        columnTypes[TYPE] = String.class;
        columnNames[DESCRIPTION] = "Description";
        columnTypes[DESCRIPTION] = String.class;
        columnNames[PHASE] = "Phase";
        columnTypes[PHASE] = String.class; 
        columnNames[STARTING_SETTLEMENT] = "Starting Settlement";
        columnTypes[STARTING_SETTLEMENT] = String.class;
        columnNames[VEHICLE] = "Vehicle";
        columnTypes[VEHICLE] = String.class;
        columnNames[MEMBER_NUM] = "Member Num.";
        columnTypes[MEMBER_NUM] = Integer.class;
        columnNames[NAVPOINT_NUM] = "Navpoint Num.";
        columnTypes[NAVPOINT_NUM] = Integer.class;
        columnNames[TRAVELLED_DISTANCE] = "Travelled Dist. (km)";
        columnTypes[TRAVELLED_DISTANCE] = Integer.class;
        columnNames[REMAINING_DISTANCE] = "Remaining Dist. (km)";
        columnTypes[REMAINING_DISTANCE] = Integer.class;
        
        MissionManager manager = Simulation.instance().getMissionManager();
        missionCache = manager.getMissions();
        manager.addListener(this);
        Iterator<Mission> i = missionCache.iterator();
        while (i.hasNext()) i.next().addMissionListener(this);
    }
    
	/**
     * Get the name of this model. The name will be a description helping
     * the user understand the contents.
     *
     * @return Descriptive name.
     */
	public String getName() {
		return "Missions";
	}

	/**
	 * Adds a new mission.
	 * @param mission the new mission.
	 */
	public void addMission(Mission mission) {
		if (!missionCache.contains(mission)) {
			missionCache.add(mission);
			mission.addMissionListener(this);
			
			// Inform listeners of new row
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireTableRowsInserted(missionCache.size() - 1, missionCache.size() - 1);
				}
			});
		}
	}
	
	/**
	 * Removes an old mission.
	 * @param mission the old mission.
	 */
	public void removeMission(Mission mission) {
		if (missionCache.contains(mission)) {
			int index = missionCache.indexOf(mission);
			missionCache.remove(mission);
			mission.removeMissionListener(this);
			
			// Inform listeners of new row
			SwingUtilities.invokeLater(new MissionTableRowDeleter(index));
		}
	}
	
    /**
     * Return the type of the column requested.
     * @param columnIndex Index of column.
     * @return Class of specified column.
     */
    public Class<?> getColumnClass(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < columnTypes.length)) {
            return columnTypes[columnIndex];
        }
        return Object.class;
    }
    
    /**
     * Return the name of the column requested.
     * @param columnIndex Index of column.
     * @return name of specified column.
     */
    public String getColumnName(int columnIndex) {
        if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
            return columnNames[columnIndex];
        }
        return "Unknown";
    }
	
	/**
     * Return the object at the specified row indexes.
     * @param row Index of the row object.
     * @return Object at the specified row.
     */
	public Object getObject(int row) {
		return missionCache.get(row);
	}

	/**
     * Has this model got a natural order that the model conforms to. If this
     * value is true, then it implies that the user should not be allowed to
     * order.
     */
	public boolean getOrdered() {
		return false;
	}
	
    /**
     * Gets the model count string.
     */
    public String getCountString() {
    	return missionCache.size() + " missions";
    }
	
	/**
	 * Catch mission update event.
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event) {
		int index = missionCache.indexOf(event.getSource());
		if ((index > -1) && (index < missionCache.size())) {
			int column1 = -1;
			int column2 = -1;
			
			String eventType = event.getType();
			if (eventType.equals(Mission.NAME_EVENT)) column1 = TYPE;
			else if (eventType.equals(Mission.DESCRIPTION_EVENT)) column1 = DESCRIPTION;
			else if (eventType.equals(Mission.PHASE_EVENT)) column1 = PHASE;
			else if (eventType.equals(Mission.ADD_MEMBER_EVENT) || 
					eventType.equals(Mission.REMOVE_MEMBER_EVENT)) column1 = MEMBER_NUM;
			else if (eventType.equals(TravelMission.NAVPOINTS_EVENT)) column1 = NAVPOINT_NUM;
			else if (eventType.equals(VehicleMission.VEHICLE_EVENT)) column1 = VEHICLE;
			else if (eventType.equals(RoverMission.STARTING_SETTLEMENT_EVENT)) column1 = STARTING_SETTLEMENT;
			else if (eventType.equals(VehicleMission.DISTANCE_EVENT)) {
				column1 = TRAVELLED_DISTANCE;
				column2 = REMAINING_DISTANCE;
			}
			
			if (column1 > -1) SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column1));
			if (column2 > -1) SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column2));
		}
	}

	public int getRowCount() {
        return missionCache.size();
	}

    /**
     * Return the number of columns
     * @return column count.
     */
	public int getColumnCount() {
		return COLUMNCOUNT;
	}

    /**
     * Return the value of a Cell
     * @param rowIndex Row index of the cell.
     * @param columnIndex Column index of the cell.
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;
		
		if (rowIndex < missionCache.size()) {
			Mission mission = missionCache.get(rowIndex);
			
			switch (columnIndex) {
			
				case TYPE : {
					result = mission.getName();
				} break;
				
				case DESCRIPTION : {
					result = mission.getDescription();
				} break;
				
				case PHASE : {
					result = mission.getPhaseDescription();
				} break;
				
				case VEHICLE : {
					result = "";
					if (mission instanceof VehicleMission) {
						VehicleMission vehicleMission = (VehicleMission) mission;
						if (vehicleMission.getVehicle() != null) result = vehicleMission.getVehicle().getName();
					}
				} break;
				
				case STARTING_SETTLEMENT : {
					result = "";
					if (mission instanceof TravelMission) {
						NavPoint nav0 = ((TravelMission) mission).getNavpoint(0);
						if ((nav0 != null) && nav0.isSettlementAtNavpoint()) {
							result = nav0.getSettlement().getName();
						}
					}
				} break;
				 
				case MEMBER_NUM : {
					result = new Integer(mission.getPeopleNumber());
				} break;
				
				case NAVPOINT_NUM : {
					if (mission instanceof TravelMission) {
						TravelMission travelMission = (TravelMission) mission;
						result = new Integer(travelMission.getNumberOfNavpoints());
					}
					else result = new Integer(0);
				} break;
				
				case TRAVELLED_DISTANCE : {
					if (mission instanceof TravelMission) {
						TravelMission travelMission = (TravelMission) mission;
						result = new Integer((int) travelMission.getTotalDistanceTravelled());
					}
					else result = new Integer(0);
				} break;
				
				case REMAINING_DISTANCE : {
					if (mission instanceof TravelMission) {
						TravelMission travelMission = (TravelMission) mission;
						try {
							result = new Integer((int) travelMission.getTotalRemainingDistance());
						}
						catch (Exception e) {}
					}
					else result = new Integer(0);
				}
			}
		}
		
		return result;
	}
    
    /**
     * Prepares the model for deletion.
     */
    public void destroy() {
    	for (int x = 0; x < missionCache.size(); x++) {
    		removeMission(missionCache.get(0));
    	}
        Simulation.instance().getMissionManager().removeListener(this);
    	//missionCache = null;
    }
    
    /**
     * Inner class for updating mission table cell.
     */
    private class MissionTableCellUpdater implements Runnable {
    	
    	private int row;
    	private int column;
    	
    	private MissionTableCellUpdater(int row, int column) {
    		this.row = row;
    		this.column = column;
    	}
    	
    	public void run() {
            if ((row < getRowCount()) && (column < getColumnCount()))
                fireTableCellUpdated(row, column);
    	}
    }
    
    private class MissionTableRowDeleter implements Runnable {
    	
    	private int row;
    	
    	private MissionTableRowDeleter(int row) {
    		this.row = row;
    	}
    	
    	public void run() {
            if (row < getRowCount()) fireTableRowsDeleted(row, row);
    	}
    }
}