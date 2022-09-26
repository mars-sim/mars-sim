/*
 * Mars Simulation Project
 * EventTableModel.java
 * @date 2022-09-24
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.events.SimpleEvent;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.notification.NotificationMenu;

/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an Adapter
 * onto the existing Event Manager.
 */
@SuppressWarnings("serial")
public class EventTableModel extends AbstractTableModel
		implements MonitorModel, HistoricalEventListener{

	/** default logger. */
//	private static final Logger logger = Logger.getLogger(EventTableModel.class.getName());

	private static final int MSG_CACHE = 5;

	// Column names
	private static final int TIMESTAMP = 0;
	private static final int CATEGORY = 1;
	private static final int TYPE = 2;
	private static final int CAUSE = 3;
	private static final int WHILE = 4;
	private static final int WHO = 5;
	private static final int CONTAINER = 6;
	private static final int HOMETOWN = 7;
	private static final int COORDINATES = 8;
	
	private static final int COLUMNCOUNT = 9;

	/** Names of the displayed columns. */
	static private String columnNames[];
	/** Types of the individual columns. */
	static private Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[TIMESTAMP] = Msg.getString("EventTableModel.column.time"); //$NON-NLS-1$
		columnTypes[TIMESTAMP] = String.class;
		columnNames[CATEGORY] = Msg.getString("EventTableModel.column.category"); //$NON-NLS-1$
		columnTypes[CATEGORY] = String.class;
		columnNames[TYPE] = Msg.getString("EventTableModel.column.eventType"); //$NON-NLS-1$
		columnTypes[TYPE] = String.class;	
		columnNames[CAUSE] = Msg.getString("EventTableModel.column.cause"); //$NON-NLS-1$
		columnTypes[CAUSE] = String.class;
		columnNames[WHILE] = Msg.getString("EventTableModel.column.while"); //$NON-NLS-1$
		columnTypes[WHILE] = String.class;
		columnNames[WHO] = Msg.getString("EventTableModel.column.who"); //$NON-NLS-1$
		columnTypes[WHO] = Object.class;
		columnNames[CONTAINER] = Msg.getString("EventTableModel.column.container"); //$NON-NLS-1$
		columnTypes[CONTAINER] = String.class;
		columnNames[HOMETOWN] = Msg.getString("EventTableModel.column.hometown"); //$NON-NLS-1$
		columnTypes[HOMETOWN] = String.class;
		columnNames[COORDINATES] = Msg.getString("EventTableModel.column.coordinates"); //$NON-NLS-1$
		columnTypes[COORDINATES] = String.class;
	}

	private boolean showMedical = true;
	private boolean showMedicalCache = true;
	private boolean showMalfunction = true;
	private boolean showMalfunctionCache = true;

	private boolean noFiring = false;

	// Event categories to be displayed.
	private boolean displayMalfunction = true;
	private boolean displayMedical = true;
	private boolean displayMission = true;
	private boolean displayHazard = true;
	private boolean displayTask = false;
	private boolean displayTransport = false;

	private MainDesktopPane desktop;
	private NotificationMenu nMenu;

	private List<String> messageCache = new ArrayList<>();

	private transient List<SimpleEvent> cachedEvents = new ArrayList<>();
	private UnitManager unitManager;
	private HistoricalEventManager eventManager;

	/**
	 * constructor. Create a new Event model based on the specified event manager.
	 *
	 * @param manager   Manager to extract events from.
	 * @param notifyBox to present notification message to user.
	 */
	public EventTableModel(MainDesktopPane desktop) {

		this.desktop = desktop;

		// Add this model as an event listener.
		Simulation sim = desktop.getSimulation();
		this.unitManager = sim.getUnitManager();
		this.eventManager = sim.getEventManager();
		
		// Update the cached events.
		updateCachedEvents();
		
		// Add listener only when fully constructed
		eventManager.addListener(this);
	}

	private synchronized void updateCachedEvents() {
		List<SimpleEvent> events = null;

		// Clean out existing cached events for the Event Table.
		cachedEvents = new ArrayList<>();

//		if (GameManager.getGameMode() == GameMode.COMMAND) {
//			int id = unitManager.getCommanderSettlement().getIdentifier();
//			events = new ArrayList<>(eventManager.getEvents(id));
//		}
//		else {
			events = new ArrayList<>(eventManager.getEvents());
//		}


		for (SimpleEvent event : events) {
			HistoricalEventCategory category = HistoricalEventCategory.int2enum((int) (event.getCat()));
			EventType eventType = EventType.int2enum((event.getType()));
			if (category.equals(HistoricalEventCategory.HAZARD) && displayHazard) {
				cachedEvents.add(event);
			}

			else if (category.equals(HistoricalEventCategory.MALFUNCTION) && displayMalfunction) {
				cachedEvents.add(event);
			}

			else if (category.equals(HistoricalEventCategory.MEDICAL) && displayMedical
					&& (eventType == EventType.MEDICAL_STARTS
							//|| eventType == EventType.MEDICAL_CURED
							|| eventType == EventType.MEDICAL_TREATED
							|| eventType == EventType.MEDICAL_DEATH)) {
				cachedEvents.add(event);
			}

			else if (category.equals(HistoricalEventCategory.MISSION) && displayMission
					&& (eventType == EventType.MISSION_EMERGENCY_BEACON_ON
						|| eventType == EventType.MISSION_EMERGENCY_BEACON_OFF
							|| eventType == EventType.MISSION_EMERGENCY_DESTINATION
							|| eventType == EventType.MISSION_NOT_ENOUGH_RESOURCES
							|| eventType == EventType.MISSION_MEDICAL_EMERGENCY
							|| eventType == EventType.MISSION_RENDEZVOUS
							|| eventType == EventType.MISSION_RESCUE_PERSON
							|| eventType == EventType.MISSION_SALVAGE_VEHICLE)) {
				cachedEvents.add(event);
			}

//			else if (category.equals(HistoricalEventCategory.TASK) && displayTask)
//				cachedEvents.add(event);

			else if (category.equals(HistoricalEventCategory.TRANSPORT) && displayTransport) {
				cachedEvents.add(event);
			}

		}

		// Update all table listeners.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});

	}

	/**
	 * Return the number of columns
	 *
	 * @return column count.
	 */
	public int getColumnCount() {
		return columnNames.length;
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
	 * Get the name of the model.
	 *
	 * @return model name.
	 */
	public String getName() {
		return Msg.getString("EventTableModel.tabName"); //$NON-NLS-1$
	}

	/**
	 * Get the number of rows in the model.
	 *
	 * @return the number of Events.
	 */
	public int getRowCount() {
		if (cachedEvents != null)
			return cachedEvents.size();
		else
			return 0;
	}

	/**
	 * Get the unit at the specified row.
	 *
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit associated with the Event as the specified position.
	 */
	public Object getObject(int row) {
//		HistoricalEvent event = cachedEvents.get(row);
//		Object result = null;
//		if (event != null) {
//			Object source = event.getSource();
//			if (source instanceof Unit) result = source;
//			else if (source instanceof Building)
//				result = ((Building) source).getBuildingManager().getSettlement();
//		}
//		return result;
//
		return null;
	}

	/**
	 * Is this model already ordered according to some external criteria.
	 *
	 * @return TRUE as the events are time ordered.
	 */
	public boolean getOrdered() {
		return true;
		// return false; // 2015-01-14 if false, events will be missing and # events
		// will be out of sync
	}

	/**
	 * Return the value of a Cell
	 *
	 * @param rowIndex    Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		// if (rowIndex == 0 && columnIndex == 2)
		// check if event.getCategory() == MEDICAL or MALFUNCTION

		if (rowIndex < cachedEvents.size()) {
//			HistoricalEvent event = cachedEvents.get(rowIndex);
			SimpleEvent event = cachedEvents.get(rowIndex);
			if (event != null) {
				switch (columnIndex) {
				
				case TIMESTAMP: {
					result = event.getFullDateTimeString();
				}
					break;

				case CATEGORY: {
					result = HistoricalEventCategory.int2enum(event.getCat()).getName();
				}
					break;

				case TYPE: {
					result = EventType.int2enum(event.getType());
				}
					break;
					
				case CAUSE: {
					result = eventManager.getWhat(event.getWhat());
				}
					break;	

				case WHILE: {
					result = eventManager.getWhileDoing(event.getWhileDoing());
				}
					break;

				case WHO: {
					result = eventManager.getWho(event.getWho());
				}
					break;

				case CONTAINER: {
					result = eventManager.getContainer(event.getContainer());
				}
					break;

				case HOMETOWN: {
					result = eventManager.getHomeTown(event.getHomeTown());
				}
					break;
					

				case COORDINATES: {
					result = eventManager.getCoordinates(event.getCoordinates());
				}
					break;
					
				default: {
					result = null;
				}
					break;
	
				}
			} // end of if event
		}

		return result;
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return "  " + Msg.getString("EventTableModel.numberOfEvents", //$NON-NLS-2$
				cachedEvents.size());
	}

	public synchronized void eventAdded(int index, SimpleEvent se, HistoricalEvent he) {
		eventAdded(index, he);
	}

	/**
	 * Adds a new event.
	 * @param index
	 * @param event {@link SimpleEvent}
	 */
	public synchronized void eventAdded(int index, SimpleEvent event) {

		updateCachedEvents();

		if (!noFiring && index == 0 && event != null) {

			// reset willNotify to false
			boolean willNotify = false;
			int type = -1;

			String header = null;
			String message = null;
			String cause = eventManager.getWhat(event.getWhat());
			String during = (eventManager.getWhileDoing(event.getWhileDoing()));
			String who = eventManager.getWho(event.getWho());
			String container = eventManager.getContainer(event.getContainer());
			String hometown = eventManager.getHomeTown(event.getHomeTown());
			String coordinates = eventManager.getCoordinates(event.getCoordinates());
			
			HistoricalEventCategory category = HistoricalEventCategory.int2enum(event.getCat());
			EventType eventType = EventType.int2enum(event.getType());

			if (category == HistoricalEventCategory.MALFUNCTION) {

				during = during.toLowerCase();

				if (during.equals("n/a"))
					during = "while ";
				else
					during = "while " + during;

				header = Msg.getString("EventTableModel.message.malfunction"); //$NON-NLS-1$

				// Only display notification window when malfunction has occurred, not when
				// fixed.
				if (eventType == EventType.MALFUNCTION_HUMAN_FACTORS) {
					during = during.replace("do ", "doing ");
					message = cause + " in " + container + " at " + coordinates + ". " + who
							+ " reported the malfunction " + during + ".";
					willNotify = true;
				}

				else if (eventType == EventType.MALFUNCTION_PROGRAMMING_ERROR) {
					message = cause + " in " + container + " at " + coordinates + ". " + who
							+ " may have caused the malfunction due to software quality control issues " + during + ".";
					willNotify = true;
				}

				else if (eventType == EventType.MALFUNCTION_PARTS_FAILURE) {
					message = who + " reported " + cause + " in " + container + " at " + coordinates + ".";
					willNotify = true;
				}

				else if (eventType == EventType.MALFUNCTION_ACT_OF_GOD) {
					if (who.toLowerCase().equals("none"))
						message = "No one witnessed " + cause + " in " + container + " at "
								+ coordinates+ ".";
					else
						message = who + " got traumatized by " + cause + during
							+ " in " + container + " at " + coordinates+ ".";
					willNotify = true;
				}

				type = 0;
			}

			else if (category == HistoricalEventCategory.MEDICAL) {

				cause = cause.toLowerCase();
				during = during.toLowerCase();

				header = Msg.getString("EventTableModel.message.medical"); //$NON-NLS-1$

				if (eventType == EventType.MEDICAL_STARTS) {

					String phrase = "";

					if (cause.equalsIgnoreCase("starvation"))
						phrase = " is starving";
					else if (cause.equalsIgnoreCase("cold"))
						phrase = " caught a cold";
					else if (cause.equalsIgnoreCase("flu"))
						phrase = " caught the flu";
					else if (cause.equalsIgnoreCase("fever"))
						phrase = " had a fever";
					else if (cause.equalsIgnoreCase("decompression"))
						phrase = " suffered from decompression";
					else if (cause.equalsIgnoreCase("dehydration"))
						phrase = " suffered from dehydration";
					else if (cause.equalsIgnoreCase("freezing"))
						phrase = " was freezing";
					else if (cause.equalsIgnoreCase("heat stroke"))
						phrase = " suffered from a heat stroke";
					else if (cause.equalsIgnoreCase("suffocation"))
						phrase = " was suffocating";
					else if (cause.equalsIgnoreCase("laceration"))
						phrase = " suffered laceration";
					else if (cause.equalsIgnoreCase("pulled muscle/tendon"))
						phrase = " had a pulled muscle";
					else
						phrase = " complained about the " + cause;//" is suffering from ";

					willNotify = true;

					if (!during.equals("sleeping"))
						during = "falling asleep";
					if (!container.equals("outside on Mars"))
						container = " in " + container;
					message = who + phrase + " while " + during + container + " at " + coordinates;

				} else if (eventType == EventType.MEDICAL_DEATH) {

					willNotify = true;
					if (!container.equals("outside on Mars"))
						container = " in " + container;
					message = who + " died from " + cause + container + " at " + coordinates;

				} else if (eventType == EventType.MEDICAL_TREATED) {

					willNotify = true;
					if (!container.equals("outside on Mars"))
						container = " in " + container;
					message = who + " was being treated for " + cause + container + " at " + coordinates;

//					} else if (eventType == EventType.MEDICAL_CURED) {
//
//						willNotify = true;
//						message = who + " was cured from " + cause + " in " + location0 + " at " + location1;
				}

				type = 1;

			}

			else if (category == HistoricalEventCategory.MISSION) {

				header = Msg.getString("EventTableModel.message.mission"); //$NON-NLS-1$

				// Only display notification window when malfunction has occurred, not when
				// fixed.

				if (eventType == EventType.MISSION_RESCUE_PERSON
						|| eventType == EventType.MISSION_SALVAGE_VEHICLE
						|| eventType == EventType.MISSION_RENDEZVOUS) {
					message = who + " is " + during
						+ " from " + container + " at " + coordinates;
					willNotify = true;
				}
				else if (eventType == EventType.MISSION_EMERGENCY_BEACON_ON
						|| eventType == EventType.MISSION_EMERGENCY_BEACON_OFF
						|| eventType == EventType.MISSION_EMERGENCY_DESTINATION
//							|| eventType == EventType.MISSION_NOT_ENOUGH_RESOURCES
						|| eventType == EventType.MISSION_MEDICAL_EMERGENCY) {
					message = who + " has " + Conversion.setFirstWordLowercase(cause)
						+ " while " + during.toLowerCase() + " in " + container + " at " + coordinates;
					willNotify = true;
				}

				type = 2;

			}

			else if (category == HistoricalEventCategory.HAZARD) {

				if (eventType == EventType.HAZARD_METEORITE_IMPACT) {

					header = Msg.getString("EventType.hazard.meteoriteImpact"); //$NON-NLS-1$

					if (who.toLowerCase().equals("none"))
						message = "There was a " + eventType.getName() + " in " + container + " at " + coordinates
								+ ". Fortunately, no one was hurt.";
					else
						message = who + " was rattled by the " + eventType.getName() + " while "
								+ Conversion.setFirstWordLowercase(during) + " in " + container
								+ " at " + coordinates;
					willNotify = true;
				}

				else if (eventType == EventType.HAZARD_RADIATION_EXPOSURE) {

					header = Msg.getString("EventType.hazard.radiationExposure"); //$NON-NLS-1$
					willNotify = true;
					message = who + " was exposed to " + cause.replace("Dose", "dose")
							+ " radiation while " + during + " in "
							+ container + " at " + coordinates;
				}

				type = 3;

			}

			if (willNotify) {
				if (!messageCache.contains(message)) {
					messageCache.add(0, message);
					if (messageCache.size() > MSG_CACHE)
						messageCache.remove(messageCache.size() - 1);
				}
				else
					willNotify = false;
			}


			// Use controlsfx's notification window for javaFX UI
			if (willNotify);
//					Platform.runLater(new NotifyFXLauncher(header, message, type));
		}
	}

	/**
	 * Add a new event (for using MarsProject only)
	 *
	 * @param index Index of the new event in the manager.
	 * @param event {@link HistoricalEvent}
	 */
	public synchronized void eventAdded(int index, HistoricalEvent event) {

		if (desktop.getMainWindow() != null) {

			// TODO: include historical events and ai.task.TaskEvent, filtered by user's
			updateCachedEvents();

			if (nMenu == null) {
//				try {
//					nMenu = desktop.getMainWindow().getMainWindowMenu().getNotificationMenu();
//				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
//				}
			} else if (nMenu != null) {
				// Boolean noFiring = false;
				showMedical = nMenu.getShowMedical();
				if (showMedical != showMedicalCache) {
					showMedicalCache = showMedical;
				}

				showMalfunction = nMenu.getShowMalfunction();
				if (showMalfunction != showMalfunctionCache) {
					showMalfunctionCache = showMalfunction;
				}

				if (!showMedical && !showMalfunction) {
					//notifyBox.emptyQueue();
					noFiring = true;
				}

				if (!noFiring && index == 0 && event != null) {
					SwingUtilities.invokeLater(new NotifyBoxLauncher(event));
				}
			}
		}
	}

	/**
	 * A consecutive sequence of events have been removed from the manager.
	 *
	 * @param startIndex First exclusive index of the event to be removed.
	 * @param endIndex   Last exclusive index of the event to be removed..
	 */
	public void eventsRemoved(int startIndex, int endIndex) {
		updateCachedEvents();
		// fireTableRowsDeleted(startIndex, endIndex);
	}

	/**
	 * Checks if malfunction events are to be displayed.
	 *
	 * @return true if displayed
	 */
	public boolean getDisplayMalfunction() {
		return displayMalfunction;
	}

	/**
	 * Sets if malfunction events are to be displayed.
	 *
	 * @param display true if displayed
	 */
	public void setDisplayMalfunction(boolean display) {
		displayMalfunction = display;
		updateCachedEvents();
	}

	/**
	 * Checks if medical events are to be displayed.
	 *
	 * @return true if displayed
	 */
	public boolean getDisplayMedical() {
		return displayMedical;
	}

	/**
	 * Sets if medical events are to be displayed.
	 *
	 * @param display true if displayed
	 */
	public void setDisplayMedical(boolean display) {
		displayMedical = display;
		updateCachedEvents();
	}

	/**
	 * Checks if mission events are to be displayed.
	 *
	 * @return true if displayed
	 */
	public boolean getDisplayMission() {
		return displayMission;
	}

	/**
	 * Sets if mission events are to be displayed.
	 *
	 * @param display true if displayed
	 */
	public void setDisplayMission(boolean display) {
		displayMission = display;
		updateCachedEvents();
	}

	/**
	 * Checks if task events are to be displayed.
	 *
	 * @return true if displayed
	 */
	public boolean getDisplayTask() {
		return displayTask;
	}

	/**
	 * Sets if task events are to be displayed.
	 *
	 * @param display true if displayed
	 */
	public void setDisplayTask(boolean display) {
		displayTask = display;
		updateCachedEvents();
	}

	/**
	 * Checks if hazard events are to be displayed.
	 *
	 * @return true if displayed
	 */
	public boolean getDisplayHazard() {
		return displayHazard;
	}

	/**
	 * Sets if hazard events are to be displayed.
	 *
	 * @param display true if displayed
	 */
	public void setDisplayHazard(boolean display) {
		displayHazard = display;
		updateCachedEvents();
	}

	/**
	 * Checks if transport events are to be displayed.
	 *
	 * @return true if displayed
	 */
	public boolean getDisplayTransport() {
		return displayTransport;
	}

	/**
	 * Sets if transport events are to be displayed.
	 *
	 * @param display true if displayed
	 */
	public void setDisplayTransport(boolean display) {
		displayTransport = display;
		updateCachedEvents();
	}

//	/**
//	 * Internal class for launching a notify window.
//	 */
//	private class NotifyFXLauncher implements Runnable {
//		private String header;
//		private String message;
//		private Pos pos = null;
//		private int type = -1;
//
//		private NotifyFXLauncher(String header, String message, int type) {
//			this.header = header;
//			this.message = message;
//			this.type = type;
//
//			if (type == 0) {
//				pos = Pos.BOTTOM_RIGHT;
//			}
//
//			else if (type == 1) {
//				pos = Pos.BOTTOM_LEFT;
//			}
//
//			else if (type == 2) {
//				pos = Pos.TOP_RIGHT;
//			}
//
//			else if (type == 3) {
//				pos = Pos.TOP_LEFT;
//			}
//
//		}
//
//		public void run() {
////			System.out.println("EventTableModel : " + message);
//
//			int theme = 7;//MainScene.getTheme();
//
//			if (theme == 7) {// use dark theme
//				Notifications.create().title(header).text(message).position(pos)
////		    		.onAction(new EventHandler<ActionEvent>() {
////		    			@Override
////		    			public void handle(ActionEvent event){
////		    				logger.config("A notification box titled " + "header" + " with " + message + "' has just been clicked.");
////		    			}
////		    		})
//						.graphic(new ImageView(appIconSet.get(type)))
//						.darkStyle().owner(desktop.getMainScene().getStage()).show();
//			}
//
//			else {// use light theme
//				Notifications.create().title(header).text(message).position(pos)
////		    		.onAction(new EventHandler<ActionEvent>() {
////		    			@Override
////		    			public void handle(ActionEvent event){
////		    				logger.config("A notification box titled " + "header" + " with " + message + "' has just been clicked.");
////		    			}
////		    		})
//						.graphic(new ImageView(appIconSet.get(type)))
//						.owner(desktop.getMainScene().getStage()).show();
////		    		.showWarning();
//			}
//
//			desktop.getMainScene().sendMsg(message);
//		}
//	}

	/**
	 * Internal class for launching a notify window.
	 */
	private class NotifyBoxLauncher implements Runnable {

		private HistoricalEvent event;

		private NotifyBoxLauncher(HistoricalEvent event) {
			this.event = event;
		}

		public void run() {
//			notifyBox.validateMsg(event);
			// Note: adding try-catch can cause UI significant slow down here
		}
	}

	public void setNoFiring(boolean value) {
		noFiring = value;
	}

	public boolean isNoFiring() {
		return noFiring;
	}
	
	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		eventManager.removeListener(this);
		eventManager = null;
		desktop = null;
		nMenu = null;
		messageCache = null;
		cachedEvents.clear();
		cachedEvents = null;
	}

}
