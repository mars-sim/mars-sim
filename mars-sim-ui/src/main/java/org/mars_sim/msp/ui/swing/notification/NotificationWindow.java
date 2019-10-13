/**
 * Mars Simulation Project
 * NotificationWindow.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.notification;

import java.awt.Color;
import java.util.Iterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.swing.JDialog;

import org.mars_sim.msp.ui.swing.notification.Telegraph;
import org.mars_sim.msp.ui.swing.notification.TelegraphConfig;
import org.mars_sim.msp.ui.swing.notification.TelegraphPosition;
import org.mars_sim.msp.ui.swing.notification.TelegraphQueue;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.mars.MarsSurface;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The NotificationWindow class creates notification messages on the desktop
 * Events are based from HistoricalEvent.java
 */
// TODO: Does subclassing JDialog have slower performance than not? 
public class NotificationWindow extends JDialog implements ClockListener {

	private static final long serialVersionUID = 1L;
	/** The name of the tool the window is for. */
	
	// default logger.
	private static Logger logger = Logger.getLogger(NotificationWindow.class.getName());

	private boolean isSetQueueEmpty = false;
	private boolean isSetQueueEmptyCache = false;
	private boolean showMedical = true;
	private boolean showMedicalCache = true;
	private boolean showMalfunction = true;
	private boolean showMalfunctionCache = true;	
	private boolean willNotify= false;
	private boolean areAnySettingsChanged = false;

	private boolean isPaused = false;
	
	private int maxNumMsg = 99;
	private int maxNumMsgCache = 99;
	private int displayTime = 2;
	private int displayTimeCache = 2;	

	protected String name;
	private String oldMsgCache = "";
	
	private Telegraph telegraph;
	private TelegraphConfig telegraphConfig ;
	private TelegraphQueue telegraphQueue;
	
	private Timer timer;
	private MainDesktopPane desktop;
	private NotificationMenu nMenu;
	
	
	public NotificationWindow(MainDesktopPane desktop) {
		this.desktop = desktop;
		telegraphQueue = new TelegraphQueue();
		Simulation.instance().getMasterClock().addClockListener(this);
		//nMenu = desktop.getMainWindow().getMainWindowMenu().getNotificationMenu();
	}

	public void checkSetting() {

		if (nMenu == null) 
			nMenu = desktop.getMainWindow().getMainWindowMenu().getNotificationMenu();

		isSetQueueEmpty = nMenu.getIsSetQueueEmpty();	
		if (isSetQueueEmpty != isSetQueueEmptyCache ) {
			areAnySettingsChanged = true;
			emptyQueue();
			isSetQueueEmptyCache = isSetQueueEmpty;
		}
			
		showMedical = getShowMedical();
		if (showMedical != showMedicalCache ) {
			areAnySettingsChanged = true;
			emptyQueue();
			showMedicalCache = showMedical;
		}

		showMalfunction = getShowMalfunction();
		if (showMalfunction != showMalfunctionCache ) {
			areAnySettingsChanged = true;
			emptyQueue();
			showMalfunctionCache = showMalfunction;
		}
		
		maxNumMsg = nMenu.getMaxNumMsg();
		if (maxNumMsg != maxNumMsgCache ) {
			areAnySettingsChanged = true;	
			
			maxNumMsg = 0;

			timer = new Timer();
			// 2014-12-10 Hold off for 3 seconds. 
			int seconds = 3;
			timer.schedule(new CancelTimer(), seconds * 1000);		

			maxNumMsgCache = maxNumMsg;
		}
		
		displayTime = nMenu.getDisplayTime();
		if (displayTime != displayTimeCache ) {
			areAnySettingsChanged = true;
			displayTimeCache = displayTime;
		}
		
	}
	
	public class CancelTimer extends TimerTask {
		@Override
		public void run() {
			//System.out.println("Terminated the Timer Thread!");
			timer.cancel(); // Terminate the thread
		}
	}
	
	public void emptyQueue() {
		if (telegraphQueue != null) {
			int qSize = 0;
			Queue<Telegraph> telegraphList = telegraphQueue.getQueue();
			//if (telegraphList != null)
			//	qSize = telegraphList.size();
			//System.out.println("emptyQueue(); queue size was " + telegraphList.size());
	
			Iterator<Telegraph> i = telegraphList.iterator();
			while(i.hasNext()){
			  Telegraph oneTelegraph = i.next();
			  telegraphList.remove(oneTelegraph);
			}
			//if (telegraphList != null)
			//	qSize = telegraphList.size();
			//System.out.println("emptyQueue(); queue size is now " + telegraphList.size());
		}	
	}

	public void setupTelegraph(HistoricalEvent event, 
			String message, String header) {

		int sizeOfQueue = 0;
		Queue<Telegraph> telegraphList = telegraphQueue.getQueue();
		if (telegraphList != null)
			sizeOfQueue = telegraphList.size();
		//System.out.println("sendAlert(); queue size was " + telegraphList.size());// + telegraphList.element());
		checkSetting();
		
		if (areAnySettingsChanged) {
			//emptyQueue();
			//messageCounter = 0;
		}
		//Color GREEN_CYAN = new Color(0, 255, 128);
		//Color PURPLE_BLUE = new Color(39, 16, 167);		
		//Color BLUE_CYAN = new Color(0, 128, 255);
		//Color ORANGE = new Color(255, 128, 0);
		Color BURGANDY = new Color(148, 28, 10);
		Color ORANGE = new Color(255, 176, 13);
		
		String msg = generateMsg(event, message);	

			if (!oldMsgCache.equals(msg)
					&& (sizeOfQueue <= maxNumMsg) ) {		
				
				telegraphConfig = new TelegraphConfig();

				header = "<html><CENTER><i><b><h3>" 
						+ header + "</h3></b></i></CENTER></html>";
				//telegraphConfig.setButtonIcon();
				telegraphConfig.setWindowWidth(80);
				telegraphConfig.setWindowHeight(80);
				
				// 2014-12-17 Added if clause 
				if (nMenu.getIsConfirmEachEnabled()) 
					telegraphConfig.setButtonEnabled(true);
				
				telegraphConfig.setBackgroundColor(ORANGE);
				telegraphConfig.setTitleColor(BURGANDY);
				//telegraphConfig.setTitleFont(font);
				telegraphConfig.setDescriptionColor(Color.DARK_GRAY);
				//telegraphConfig.setDescriptionFont(Font.SERIF);
				telegraphConfig.setAudioEnabled(false);
				//telegraphConfig.setAudioInputStream(null);
				//telegraphConfig.setButtonEnabled(true);
				telegraphConfig.setDuration(displayTime*1000);
				telegraphConfig.setBorderColor(Color.YELLOW);
				telegraphConfig.setBorderThickness(5);
				telegraphConfig.setTelegraphPosition(TelegraphPosition.BOTTOM_RIGHT);
			
				sendTelegraph(telegraphConfig, msg, header);
			}	
	}
	
	public void sendTelegraph(TelegraphConfig telegraphConfig, 
			String msg, String header) {
				
		//boolean isPaused = Simulation.instance().getMasterClock().isPaused();
		if (isPaused) {
			System.out.println("NotificationWindow.java : sendTelegraph() : isPaused is true");
			timer = new Timer();
			// Hold off 3 seconds
			int seconds = 3;
			timer.schedule(new CancelTimer(), seconds * 1000);	
		}
		else {
			telegraph = new Telegraph(header, msg, telegraphConfig) ;
			telegraphQueue.add(telegraph);
			oldMsgCache = msg;
			//messageCounter++;	
		}
	}

	public void validateMsg(HistoricalEvent event) {
			
		String header = "";
		String message = event.getWhatCause(); //.toUpperCase();
		// reset willNotify to false
		willNotify = false;
		
		if ( message != null && message != "" ) {

		    HistoricalEventCategory category = event.getCategory();

		    if (category.equals(HistoricalEventCategory.MALFUNCTION)
		            && showMalfunction ) {
		        header = Msg.getString("EventTableModel.message.malfunction"); //$NON-NLS-1$

		        // Only display notification window when malfunction has occurred, not when fixed.
		        if (event.getType() == EventType.MALFUNCTION_ACT_OF_GOD
		        	||event.getType() == EventType.MALFUNCTION_HUMAN_FACTORS
				    ||event.getType() == EventType.MALFUNCTION_PROGRAMMING_ERROR
		        	||event.getType() == EventType.MALFUNCTION_PARTS_FAILURE
		        		) {
		            willNotify = true;
		        }
		    }

		    else if (category.equals(HistoricalEventCategory.MEDICAL)
		            && showMedical )	{

		        header = Msg.getString("EventTableModel.message.medical"); //$NON-NLS-1$

		        // Only display notification windows when medical problems are starting or person has died.
		        if ((event.getType() == EventType.MEDICAL_STARTS) || 
		                (event.getType() == EventType.MEDICAL_DEATH)) {
		            willNotify = true;
		        }
		    }
		}
		
//		if (willNotify) setupTelegraph(event, message, header);
	}
	

	public String parseMsg(String msg) {
		
		// or use String replaced = string.replace("abcd", "dddd");
		msg = msg.toUpperCase();
		msg = msg.replaceAll("OCCURRED", "");

//		message = message.replaceAll("COLD", "a COLD");
//		message = message.replaceAll("FLU", "a FLU");
//		message = message.replaceAll("NAVIGATION", "a NAVIGATION");
//		message = message.replaceAll("MAJOR", "a MAJOR");
//		message = message.replaceAll("MINOR", "a MINOR");
//		message = message.replaceAll("FUEL", "a FUEL");

		msg = "a " + msg;
		
		if (showMedical) {
			msg = msg.replaceAll("had a STARVATION", "was STARVING");
			msg = msg.replaceAll("a MENINGITIS", "MENINGITIS");
			msg = msg.replaceAll("had a SUFFOCATION", "was SUFFOCATING");
			msg = msg.replaceAll("had a DEHYDRATION", "was DEHYDRATING");
			//msg = msg.replaceAll("a BROKEN", "BROKEN");
			msg = msg.replaceAll("a MINOR BURNS", "MINOR BURNS");
		}
		
		if (showMalfunction) {
			msg = msg.replaceAll("a EVA", "an EVA");
			msg = msg.replaceAll("a AIRLEAK", "an AIRLEAK");
			msg = msg.replaceAll("a ELECTRICAL", "an ELECTRICAL");
			
			msg = msg.replaceAll("a FOOD", "FOOD");
			//msg = msg.replaceAll("a WATER", "WATER");
			
			msg = msg.replaceAll("a NAVIGATION", "NAVIGATION");
			msg = msg.replaceAll("a GENERATOR", "GENERATOR");
		}
		
		// Remove any white space at the start or end of message.
		msg = msg.trim();
		
		return msg;
	}
	
	/**
	 * Prepare the notification box for displaying the message
	 * 
	 * @param event
	 * @param msg
	 * @return
	 */
	public String generateMsg(HistoricalEvent event, String msg)  {
		//count++;

//		String name = "";
		//String category = "";
//		String buildingName = "";
//		String settlementName = "";
//		String equipmentName = "";
//		String vehicleName = "";
		String message = parseMsg(msg);
//		String personName = "";
		String locationName = "";
		String action = "";
//		String unitName = "";
	
		if (willNotify == true) {
			// if the condition is true,
			//category = category + event.getCategory();		
			Object source = event.getSource();
			
			if (event.getType() == EventType.MEDICAL_DEATH) {
			    action = "died from";
			}
			else {
			    action = "had";
			}
			
			StringBuffer locationBuff = new StringBuffer("");
			
			if (source instanceof Unit) {
			    Unit unit = (Unit) source;
			    
			    // Loop through the hierarchy of container units for this source unit.
			    Unit tempUnit = unit;
			    Unit containerUnit = unit.getContainerUnit();
			    while (!(containerUnit instanceof MarsSurface)) {
			        
			        if (containerUnit instanceof Person) {
			            locationBuff.append(" carried by " + containerUnit.getName());
			        }
			        else if (containerUnit instanceof Vehicle) {
			            locationBuff.append(" in " + containerUnit.getName());
			        }
			        else if (containerUnit instanceof Settlement) {
			            
			            // Determine the building the source is in, if a person or vehicle.
			            Building building = null;
			            if (tempUnit instanceof Person) {
			                building = BuildingManager.getBuilding((Person) tempUnit);
			            }
			            else if (tempUnit instanceof Vehicle) {
			                building = BuildingManager.getBuilding((Vehicle) tempUnit);
			            }
			            
			            if (building != null) {
			                locationBuff.append(" at " + building.getNickName() + " in " + containerUnit.getName());
			            }
			            else {
			                locationBuff.append(" in " + containerUnit.getName());
			            }
			        }
			        
			        tempUnit = containerUnit;
			        containerUnit = tempUnit.getContainerUnit();
			    }
			    
			    // If top container unit is a person, add that he/she is outside.
			    Unit topContainerUnit = unit.getTopContainerUnit();
			    if (topContainerUnit instanceof MarsSurface && unit instanceof Person) {
			        locationBuff.append(" outside");
			    }
			    
			    locationName = locationBuff.toString().trim();
			    
			    message = unit.getName() + " " + action + " " + formatMsg(message) + " " + locationName;
			}
			else if (source instanceof Building) {
			    
			    Building building = (Building) source;
			    Settlement settlement = building.getSettlement();
			    locationName = "in " + settlement.getName();
			    
			    message = building.getNickName() + " " + action + " " + formatMsg(message) + " " + locationName;
			}
			
			
//			try {
//			if (source instanceof Person) {
//				Person p =((Person) source);
//				personName = p.getName();
//				Unit container = p.getContainerUnit();
//				// test if the person has a valid container
//				if (container == null) {
//	                locationName = "outside";
//	                logger.info("Someone had an accident : " +
//	                		personName +
//							" had " + message + " " +
//							locationName);
//	    		 	message = formatMsg(message);
//	                message = personName +
//							" had " + message + " " +
//							locationName ; 
//				} // for a person
//	            else if (container instanceof Settlement) {
//	                settlementName = p.getSettlement().getName();
//	                BuildingManager bm = p.getSettlement().getBuildingManager();
//	                buildingName = bm.getBuilding(p).getNickName();
//	                logger.info("Someone had an accident : " +
//	                		personName +
//							" had " + message + 
//							" at " + buildingName + 
//							" in " + settlementName);
//	    		 	message = formatMsg(message);
//	                message = personName +
//							" had " + message + 
//							" at " + buildingName + 
//							" in " + settlementName;
//	            } // for a person
//	            else if (container instanceof Vehicle) {
//	                vehicleName = p.getVehicle().getName();
//	                logger.info("Someone had an accident : " 
//	                		+ personName +
//							" had " + message + 
//							" in " + vehicleName);
//	    		 	message = formatMsg(message);
//	                message = personName +
//							" had " + message + 
//							" in " + vehicleName ;
//				} // for a person
//				
//		}// end of Person
//		else if (source instanceof Equipment) {
//				Equipment e = ((Equipment) source);
//				
//				try { // test if the equipment has a name
//					equipmentName = (String)e.getName();
//					
//					if (!equipmentName.equals(null)) {
//					
//						try { // yes the equipment does have a name
//						  // test if the equipment has a location container
//							Unit u = e.getContainerUnit();
//							unitName = u.getName();
//							// yes the equipment does have a location container
//							// TODO: in future test if the equipment belongs to a person
//							// Person p = e.getUnitManager().getPerson();
//							logger.info("Equipment malfunction : " + equipmentName +
//									" had " + message + 
//									" in " + unitName);
//						 	message = formatMsg(message);
//							message = equipmentName + 
//									" had " + message + 
//									" in " + unitName ;
//						} catch (Exception e1) { 
//							// No, the equipment does NOT have a location container
//							System.err.println("Exception Caught Successfully: equipment's container is" + e1.getMessage());
//							//e1.printStackTrace();
//							unitName = "outside";
//							// TODO: in future test if the equipment belongs to a person
//							// Person p = e.getUnitManager().getPerson();
//							logger.info("Equipment malfunction : " + equipmentName +
//									" had " + message + " " +
//									unitName);
//						 	message = formatMsg(message);
//							message = equipmentName + 
//									" had " + message + 
//									" " + unitName ;	
//							} // end of 2nd try for Equipment
//					} // end of if  (equipmentName != "null")
//
//				} catch (Exception e1) { 
//					// No, the equipment does NOT have a name
//					System.err.println("Exception Caught Successfully: equipment's name is " + e1.getMessage());
//					//e1.printStackTrace();
//					equipmentName = "";
//					try { // test if the equipment has a location/person container
//						Unit u = e.getContainerUnit();
//						unitName = u.getName();
//						// yes the equipment does have a location/person container
//						logger.info("Equipment malfunction : just had " + message + 
//								" in " + unitName);
//					 	message = formatMsg(message);
//						message = "Just had " + message + 
//								" in " + unitName;
//					} catch (Exception e2) { 
//						// No, the equipment does NOT have a location container
//						System.err.println("Exception Caught Successfully : equipment's container is" + e2.getMessage());
//						//e1.printStackTrace();
//						unitName = "outside";
//						logger.info("Equipment malfunction : just had " + message + 
//							unitName);
//					 	message = formatMsg(message);
//						message = "Just had " + message + 
//							" " + unitName;
//					} // end of test if the equipment has a location/person container
//				} // end of catch for No the equipment does NOT have a name
//		} // end of Equipment
//		else if (source instanceof Vehicle) {
//				Vehicle v =((Vehicle) source);
//				vehicleName = v.getName();		
//				try {  // test if the Vehicle has a location container
//					Unit u = v.getTopContainerUnit();
//					//if (!u.equals(null))	
//					unitName = u.getName();
//					
//					if (!unitName.equals(null)) {
//					// Yes, the Vehicle does have a location container
//					logger.info("Vehicle malfunction : " + vehicleName +
//							" had " + message + 
//							 " at " + unitName + "\n");
//				 	message = formatMsg(message);
//					message = vehicleName + 
//							 " had " + message + 
//							 " at " + unitName ;
//					}
//				} catch (Exception e) { 
//					// No, the Vehicle does NOT have a location container
//					System.err.println("Exception Caught Successfully : vehicle's container is " + e.getMessage());
//					//e.printStackTrace();
//				 	unitName = "outside";
//				 	logger.info("Vehicle malfunction : " + vehicleName +
//							" had " + message + " " +
//							 unitName + "\n");
//				 	message = formatMsg(message);
//				 	message = vehicleName + 
//							 " had " + message + 
//							 "  " + unitName;
//				}
//				
//		} // end of Vehicle
//		else if (source instanceof Building) {
//				Building b = ((Building) source);
//				buildingName = b.getNickName();
//				//System.out.println("buildingName is " + buildingName);
//				Settlement s = b.getBuildingManager().getSettlement();
//				settlementName = s.getName();
//			 	logger.info("Building malfunction : " + buildingName + 
//						" had " + message + 
//						" in " + settlementName) ;
//			 	message = formatMsg(message);
//				message = buildingName + 
//						" had " + message + 
//						" in " + settlementName ;
//			} // end of Building
//		} catch (Exception e) {
//				//e.printStackTrace();
//				System.err.println("Exception Caught. Reasons: " + e.getMessage());
//		};
		
		} // end of if (willNotify == true)

		//msg = "<html><i><b>" + msg + "</b></i></html>";
		
		logger.info(message);
		message = "<html><CENTER><FONT COLOR=RED>" + message + "</FONT COLOR=RED></CENTER></html>";

		return message;
	}
	
	public String formatMsg(String msg) {
//		return 	"<html><i><b>" 
//				+ msg + "</b></i></html>";
	    return "<i><b>" + msg + "</b></i>";
	}

	public boolean getShowMedical() {
		//return showMedical;
		return nMenu.getShowMedical();
	}

	public boolean getShowMalfunction() {
		//return showMalfunction;
		return nMenu.getShowMalfunction();
	}
	
	@Override
	public void clockPulse(double time) {
		// TODO Auto-generated method stub
	}

	@Override
	public void uiPulse(double time) {
		// TODO Auto-generated method stub
	};
	
	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		this.isPaused = isPaused;
	}	
	
	public void destroy() {
		telegraph = null;
		telegraphConfig = null;
		telegraphQueue = null;	
		timer = null;
		desktop = null;
		nMenu = null;
	}
	
}
