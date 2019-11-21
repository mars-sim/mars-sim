/**
 * Mars Simulation Project
 * MissionTableModel.java
* @version 3.1.0 2017-09-14
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.GameManager;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.GameManager.GameMode;
import org.mars_sim.msp.core.person.ai.mission.BuildingConstructionMission;
import org.mars_sim.msp.core.person.ai.mission.BuildingSalvageMission;
import org.mars_sim.msp.core.person.ai.mission.Mission;
import org.mars_sim.msp.core.person.ai.mission.MissionEvent;
import org.mars_sim.msp.core.person.ai.mission.MissionEventType;
import org.mars_sim.msp.core.person.ai.mission.MissionListener;
import org.mars_sim.msp.core.person.ai.mission.MissionManager;
import org.mars_sim.msp.core.person.ai.mission.MissionManagerListener;
import org.mars_sim.msp.core.person.ai.mission.NavPoint;
import org.mars_sim.msp.core.person.ai.mission.TravelMission;
import org.mars_sim.msp.core.person.ai.mission.VehicleMission;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.vehicle.GroundVehicle;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.tool.Conversion;

@SuppressWarnings("serial")
public class MissionTableModel extends AbstractTableModel
		implements MonitorModel, MissionManagerListener, MissionListener {

	private DecimalFormat decFormatter = new DecimalFormat("#,###,##0.0");
	/** Track the GameMode */
	private GameMode mode;
	
	// Column indexes
	/** Date filed column. */
	private final static int DATE_FILED = 0;
	/** Date Embarked column. */
	private final static int DATE_EMBARKED = 1;
	/** Date Returned column. */
	private final static int DATE_RETURNED = 2;
	/** Starting member column. */
	private final static int STARTING_MEMBER = 3;
	/** Desc column. */
	private final static int DESC = 4;
	/** Description column. */
	private final static int DESIGNATION = 5;
	/** Phase column. */
	private final static int PHASE = 6;
	/** Mission vehicle column. */
	private final static int VEHICLE = 7;
	/** Starting settlement column. */
	private final static int STARTING_SETTLEMENT = 8;
	/** Member number column. */
	private final static int MEMBER_NUM = 9;
	/** Navpoint number column. */
	private final static int NAVPOINT_NUM = 10;
	/** Travelled distance column. */
	private final static int TRAVELLED_DISTANCE = 11;
	/** Remaining distance column. */
	private final static int REMAINING_DISTANCE = 12;
	/** Proposed route distance column. */
	private final static int PROPOSED_ROUTE_DISTANCE = 13;
	/** The number of Columns. */
	private final static int COLUMNCOUNT = 14;
	/** Names of Columns. */
	private static String columnNames[];
	/** Types of Columns. */
	private static Class<?> columnTypes[];

	private List<Mission> missionCache;

	private Settlement commanderSettlement;
	
	private static MissionManager missionManager = Simulation.instance().getMissionManager();

	public MissionTableModel() {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[DATE_FILED] = Msg.getString("MissionTableModel.column.filed"); //$NON-NLS-1$
		columnTypes[DATE_FILED] = String.class;
		columnNames[DATE_EMBARKED] = Msg.getString("MissionTableModel.column.embarked"); //$NON-NLS-1$
		columnTypes[DATE_EMBARKED] = String.class;
		columnNames[DATE_RETURNED] = Msg.getString("MissionTableModel.column.returned"); //$NON-NLS-1$
		columnTypes[DATE_RETURNED] = String.class;
		columnNames[STARTING_MEMBER] = Msg.getString("MissionTableModel.column.name"); //$NON-NLS-1$
		columnTypes[STARTING_MEMBER] = String.class;
		columnNames[DESC] = Msg.getString("MissionTableModel.column.desc"); //$NON-NLS-1$
		columnTypes[DESC] = String.class;
		columnNames[DESIGNATION] = Msg.getString("MissionTableModel.column.designation"); //$NON-NLS-1$
		columnTypes[DESIGNATION] = String.class;
		columnNames[PHASE] = Msg.getString("MissionTableModel.column.phase"); //$NON-NLS-1$
		columnTypes[PHASE] = String.class;
		columnNames[STARTING_SETTLEMENT] = Msg.getString("MissionTableModel.column.startingSettlement"); //$NON-NLS-1$
		columnTypes[STARTING_SETTLEMENT] = String.class;
		columnNames[VEHICLE] = Msg.getString("MissionTableModel.column.vehicle"); //$NON-NLS-1$
		columnTypes[VEHICLE] = String.class;
		columnNames[MEMBER_NUM] = Msg.getString("MissionTableModel.column.members"); //$NON-NLS-1$
		columnTypes[MEMBER_NUM] = Integer.class;
		columnNames[NAVPOINT_NUM] = Msg.getString("MissionTableModel.column.navpoints"); //$NON-NLS-1$
		columnTypes[NAVPOINT_NUM] = Integer.class;
		columnNames[TRAVELLED_DISTANCE] = Msg.getString("MissionTableModel.column.distanceTravelled"); //$NON-NLS-1$
		columnTypes[TRAVELLED_DISTANCE] = Integer.class;
		columnNames[REMAINING_DISTANCE] = Msg.getString("MissionTableModel.column.distanceRemaining"); //$NON-NLS-1$
		columnTypes[REMAINING_DISTANCE] = Integer.class;
		columnNames[PROPOSED_ROUTE_DISTANCE] = Msg.getString("MissionTableModel.column.proposedDistance"); //$NON-NLS-1$
		columnTypes[PROPOSED_ROUTE_DISTANCE] = Integer.class;

		if (GameManager.mode == GameMode.COMMAND) {
			mode = GameMode.COMMAND;
			commanderSettlement = Simulation.instance().getUnitManager().getCommanderSettlement();
			missionCache = missionManager.getMissionsForSettlement(commanderSettlement);
		}
		else {
			missionCache = missionManager.getMissions();
		}
		
		missionManager.addListener(this);
		Iterator<Mission> i = missionCache.iterator();
		while (i.hasNext())
			i.next().addMissionListener(this);
	}

	/**
	 * Get the name of this model. The name will be a description helping the user
	 * understand the contents.
	 *
	 * @return Descriptive name.
	 */
	public String getName() {
		return Msg.getString("MissionTableModel.tabName"); //$NON-NLS-1$
	}

	/**
	 * Adds a new mission.
	 * 
	 * @param mission the new mission.
	 */
	public void addMission(Mission mission) {
		boolean goodToGo = false;
		if (mode == GameMode.COMMAND) {	
			if (mission.getStartingMember().getAssociatedSettlement().getName().equals(commanderSettlement.getName())
				&& !missionCache.contains(mission)) {
				goodToGo = true;
			}
		}
		
		else {
			if (!missionCache.contains(mission)) {
				goodToGo = true;
			}
		}
		
		if (goodToGo) {
			missionCache.add(mission);
			mission.addMissionListener(this);
			
			// Inform listeners of new row
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
		//			fireTableRowsInserted(missionCache.size() - 1, missionCache.size() - 1);
					fireTableRowsInserted(0, missionCache.size() - 1);
				}
			});
		}
	}

	/**
	 * Removes an old mission.
	 * 
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
	 * 
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
	 * 
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
			return columnNames[columnIndex];
		}
		return Msg.getString("unknown"); //$NON-NLS-1$
	}

	/**
	 * Return the object at the specified row indexes.
	 * 
	 * @param row Index of the row object.
	 * @return Object at the specified row.
	 */
	public Object getObject(int row) {
		return missionCache.get(row);
	}

	/**
	 * Has this model got a natural order that the model conforms to. If this value
	 * is true, then it implies that the user should not be allowed to order.
	 */
	public boolean getOrdered() {
		return false;
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return " " + Msg.getString("MissionTableModel.numberOfMissions", //$NON-NLS-2$
//			Integer.toString(missionCache.size())
				missionCache.size());
	}

	/**
	 * Catch mission update event.
	 * 
	 * @param event the mission event.
	 */
	public void missionUpdate(MissionEvent event) {
		int index = missionCache.indexOf(event.getSource());
		
//		MissionEventType eventType = event.getType();
//		Mission mission = (Mission) event.getSource();
//
//		if (mission != null) {
//			List<Mission> list = missionManager.getMissions();
//			int numMissions = list.size();
//			int index = missionCache.indexOf(mission);
//			if (index <= -1 
//					|| index < missionCache.size()
//					|| missionCache.size() != numMissions
//					|| (!missionCache.contains(mission)
//							&& list.contains(mission))
////					|| !missionCache.equals(missionManager.getMissions()) {	
//					){
//				// Update the missionCache
//				missionCache = missionManager.getMissions();
//			}

			if ((index > -1) && (index < missionCache.size())) {
				MissionEventType eventType = event.getType();
				
				int column0 = -1;
				
				if (eventType == MissionEventType.VEHICLE_EVENT)
					column0 = VEHICLE;
				else if (eventType == MissionEventType.STARTING_SETTLEMENT_EVENT)
					column0 = STARTING_SETTLEMENT;
				else if (eventType == MissionEventType.DESCRIPTION_EVENT)
					column0 = DESC;
				else if (eventType == MissionEventType.DESIGNATION_EVENT)
					column0 = DESIGNATION;
				else if (eventType == MissionEventType.ADD_MEMBER_EVENT
						|| eventType == MissionEventType.REMOVE_MEMBER_EVENT)
					column0 = MEMBER_NUM;
				else if (eventType == MissionEventType.DATE_EVENT)
					column0 = DATE_FILED;
				else if (eventType == MissionEventType.NAME_EVENT)
					column0 = STARTING_MEMBER;
				
				if (column0 > -1)
					SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column0));
			
				if (event.getSource() instanceof VehicleMission) {
					
					int column1 = -1;
					int column2 = -1;
					int column3 = -1;
					int column4 = -1;
					int column5 = -1;
					
					if (eventType == MissionEventType.DISTANCE_EVENT) {
						column1 = TRAVELLED_DISTANCE;
						column2 = REMAINING_DISTANCE;
						column3 = PROPOSED_ROUTE_DISTANCE;
					} 
					
					if (eventType == MissionEventType.NAVPOINTS_EVENT)
						column4 = NAVPOINT_NUM;
									
					if (eventType == MissionEventType.PHASE_EVENT
							|| eventType == MissionEventType.PHASE_DESCRIPTION_EVENT)
//							|| eventType == MissionEventType.END_MISSION_EVENT)
						column5 = PHASE;
					
					if (column1 > -1)
						SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column1));
					if (column2 > -1)
						SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column2));
					if (column3 > -1)
						SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column3));
					if (column4 > -1)
						SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column4));
					if (column5 > -1)
						SwingUtilities.invokeLater(new MissionTableCellUpdater(index, column5));
				}
			}
			else 
				// Update the missionCache
				missionCache = missionManager.getMissions();
//		}
	}

	public int getRowCount() {
		return missionCache.size();
	}

	/**
	 * Return the number of columns
	 * 
	 * @return column count.
	 */
	public int getColumnCount() {
		return COLUMNCOUNT;
	}

	/**
	 * Return the value of a Cell
	 * 
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

//		if (rowIndex < missionCache.size()) {
			Mission mission = missionCache.get(rowIndex);

			if (mission != null) {// && mission.getDescription() != null && !mission.getDescription().equals("")) {
				switch (columnIndex) {

				case DATE_FILED: {
					result = mission.getDateFiled();
				}
					break;
					
				case DATE_EMBARKED: {
					result = mission.getDateEmbarked();
				}
					break;
					
				case DATE_RETURNED : {
					result = mission.getDateReturned();
				}
					break;
					
				case STARTING_MEMBER: {
					result = mission.getStartingMember().getName();
				}
					break;

				case DESC: {
					result = mission.getDescription();
				}
					break;
					
				case DESIGNATION: {
					result = mission.getFullMissionDesignation();
				}
					break;

				case PHASE: {
					if (mission.getPlan() == null) {
						result = "Submitting Plan";
					}
					else if (mission.getPhase() != null && VehicleMission.REVIEWING.equals(mission.getPhase())) {
						int percent = (int) mission.getPlan().getPercentComplete();
						if (percent > 100)
							percent = 100;
						int score = (int)mission.getPlan().getScore();
						int min = (int)mission.getStartingMember().getAssociatedSettlement().getMinimumPassingScore();
						result = percent + "%. Score : " + score + " (Min : " + min + ") "
								+ Conversion.capitalize(mission.getPhaseDescription());
					}
					else
						result = Conversion.capitalize(mission.getPhaseDescription());

				}
					break;

				case VEHICLE: {
					result = ""; //$NON-NLS-1$
					if (mission instanceof VehicleMission) {
//						VehicleMission vehicleMission = (VehicleMission) mission;
//						if (vehicleMission.getVehicle() != null)
//							result = vehicleMission.getVehicle().getName();
						result = mission.getReservedVehicle();
					} else if (mission instanceof BuildingConstructionMission) {
						BuildingConstructionMission constructionMission = (BuildingConstructionMission) mission;
						List<GroundVehicle> constVehicles = constructionMission.getConstructionVehicles();
						if (constVehicles.size() > 0) {
							Vehicle vehicle = constVehicles.get(0);
							result = vehicle.getName();
						}
					} else if (mission instanceof BuildingSalvageMission) {
						BuildingSalvageMission salvageMission = (BuildingSalvageMission) mission;
						List<GroundVehicle> constVehicles = salvageMission.getConstructionVehicles();
						if (constVehicles.size() > 0) {
							Vehicle vehicle = constVehicles.get(0);
							result = vehicle.getName();
						}
					}
				}
					break;

				case STARTING_SETTLEMENT: {
					result = ""; //$NON-NLS-1$
					if (mission instanceof TravelMission) {
						NavPoint nav0 = ((TravelMission) mission).getNavpoint(0);
						if ((nav0 != null) && nav0.isSettlementAtNavpoint()) {
							result = nav0.getSettlement().getName();
						}
					}
				}
					break;

				case MEMBER_NUM: {
					result = mission.getMembersNumber();
				}
					break;

				case NAVPOINT_NUM: {
					if (mission instanceof TravelMission) {
						TravelMission travelMission = (TravelMission) mission;
						result = travelMission.getNumberOfNavpoints();
					} else
						result = 0;
				}
					break;

				case TRAVELLED_DISTANCE: {
					if (mission instanceof VehicleMission) {
						VehicleMission vehicleMission = (VehicleMission) mission;
						result = decFormatter.format(vehicleMission.getActualTotalDistanceTravelled());
					} else
						result = 0;
				}
					break;

				case REMAINING_DISTANCE: {
					if (mission instanceof TravelMission) {
						TravelMission travelMission = (TravelMission) mission;
						try {
							result = decFormatter.format(travelMission.getTotalRemainingDistance());
						} catch (Exception e) {
						}
					} else
						result = 0;
				}
				
				break;

				case PROPOSED_ROUTE_DISTANCE: {
					if (mission instanceof TravelMission) {
						TravelMission travelMission = (TravelMission) mission;
						try {
							result = decFormatter.format(travelMission.getProposedRouteTotalDistance());
						} catch (Exception e) {
						}
					} else
						result = 0;
				}

				}
			}
//		}

		return result;
	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		Object[] missions = missionCache.toArray();
		for (int x = 0; x < missions.length; x++) {
			removeMission((Mission) missions[x]);
		}

		missionManager = null;
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
			if (row < getRowCount())
				fireTableRowsDeleted(row, row);
		}
	}
}