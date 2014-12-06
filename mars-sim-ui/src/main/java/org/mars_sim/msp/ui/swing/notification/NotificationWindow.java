/**
 * Mars Simulation Project
 * NotificationWindow.java
 * @version 3.07 2014-12-05
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.notification;

import java.awt.Color;
import java.util.Iterator;
import java.util.Queue;
import java.util.logging.Logger;

import javax.swing.JDialog;

import org.mars_sim.msp.ui.swing.notification.Telegraph;
import org.mars_sim.msp.ui.swing.notification.TelegraphConfig;
import org.mars_sim.msp.ui.swing.notification.TelegraphPosition;
import org.mars_sim.msp.ui.swing.notification.TelegraphQueue;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

/**
 * The NotificationWindow class creates notification messages on the desktop
 * Events are based from HistoricalEvent.java
 */
// TODO: Does subclassing JDialog have slower performance than not? 
// 2014-11-29 Renamed NotificationManager to NotificationWindow 
// 2014-11-29 Relocated its instantiation from EventTableModel to MonitorWindow
// 2014-12-05 Added notification settings to msp
public class NotificationWindow extends JDialog {

	private static final long serialVersionUID = 1L;
	/** The name of the tool the window is for. */
	
	// default logger.
	private static Logger logger = Logger.getLogger(NotificationWindow.class.getName());

	private boolean showMedical = true;
	private boolean showMedicalCache = true;
	private boolean showMalfunction = true;
	private boolean showMalfunctionCache = true;	
	private int maxNumMsg = 99;
	private int maxNumMsgCache = 99;
	private int displayTime = 2;
	private int displayTimeCache = 2;	
	private int messageCounter = 0;
	
	protected String name;
	//TODO: need to create an array of two element with name 
	// as array[0] and header as array[1]
	//private String header;
	private Telegraph telegraph;
	private TelegraphConfig telegraphConfig ;
	private TelegraphQueue telegraphQueue;
	private boolean willNotify= false;
	//private String message = "";		
	private String matchedWord1 = "fixed";
	private String matchedWord2 = "recovering";
	private String oldMsgCache = "";
	private boolean isSettingChange = false;
	
	private MainDesktopPane desktop;
	
	public NotificationWindow(MainDesktopPane desktop) {
		this.desktop = desktop;
		telegraphQueue = new TelegraphQueue();
	}

	public void checkSetting() {
		NotificationMenu nMenu = desktop.getMainWindow().getMainWindowMenu().getNotificationMenu();
		showMedical = nMenu.getShowMedical();	
		if (showMedical != showMedicalCache ) {
			isSettingChange = true;
			showMedicalCache = showMedical;
		}

		showMalfunction = nMenu.getShowMalfunction();
		if (showMalfunction != showMalfunctionCache ) {
			isSettingChange = true;
			showMalfunctionCache = showMalfunction;
		}
		
		maxNumMsg = nMenu.getMaxNumMsg();
		//System.out.println("maxNumMsgCache : "+ maxNumMsgCache);
		//System.out.println("maxNumMsg : "+ maxNumMsg);
		if (maxNumMsg != maxNumMsgCache ) {
			isSettingChange = true;		
			//maxNumMsgCache = 0;	
			//emptyQueue();
			// call run() 
			// set maxNumMsgCache = 0
			// sleep(5000);
			maxNumMsgCache = maxNumMsg;
		}
		//System.out.println("maxNumMsgCache : "+ maxNumMsgCache);
		//System.out.println("maxNumMsg : "+ maxNumMsg);
		
		displayTime = nMenu.getDisplayTime();
		if (displayTime != displayTimeCache ) {
			isSettingChange = true;
			displayTimeCache = displayTime;
		}
		
	}
	
	
	public void emptyQueue() {
		
		Queue<Telegraph> telegraphList = telegraphQueue.getQueue();
		
		telegraphQueue.getTimer().stop();		
		//telegraph.getTimelineStay().suspend();
		
		//System.out.println("entering emptyQueue() : " + telegraphList.size());// + telegraphList.element());
		
		Iterator i = telegraphList.iterator();
		
		int temp = maxNumMsg;
		
		maxNumMsg = 0;
		
		while(i.hasNext()){
		  Telegraph oneTelegraph = (Telegraph) i.next();
		  oneTelegraph.dispose();
		  oneTelegraph = telegraphList.remove();
		  telegraphList.remove(oneTelegraph);
		}
		
		maxNumMsg = temp;
		
		
		//telegraph.getTimelineStay().resume();
		//telegraphQueue.getTimer().start();		
		
		//System.out.println("leaving emptyQueue() : " + telegraphList.size());// + telegraphList.element());

	}

	// 2014-11-15 Created sendAlert()
	public void sendAlert(HistoricalEvent event, 
			String message,
			String header) {

		checkSetting();
		if (isSettingChange) {
			// start a new queue
			//queue = null;

			emptyQueue();
			
			//telegraph.dispose();

			//queue = new TelegraphQueue();
			messageCounter = 0;
		}

    	//System.out.println("validateMsg() : showMedical is " + showMedical);
		//System.out.println("validateMsg() : showMalfunction is " + showMalfunction);
		//System.out.println("validateMsg() : displayTime is " + displayTime);
		//System.out.println("validateMsg() : maxNumMsg is " + maxNumMsg);
		//System.out.println("validateMsg() : messageCounter is " + messageCounter);

		Color GREEN_CYAN = new Color(0, 255, 128);
		Color PURPLE_BLUE = new Color(39, 16, 167);		
		Color BLUE_CYAN = new Color(0, 128, 255);
		Color BURGANDY = new Color(148, 28, 10);
		//Color ORANGE = new Color(255, 128, 0);
		Color ORANGE = new Color(255, 176, 13);
		
		String msg = generateMsg(event, message);	
		
		
			if (!oldMsgCache.equals(msg)
					&& (messageCounter <= maxNumMsg) ) {		

				telegraphConfig = new TelegraphConfig();
				messageCounter++;

				header = "<html><CENTER><i><b><h3>" 
						+ header + "</h3></b></i></CENTER></html>";
				//System.out.println("sendAlert() : msg is "+ msg);
				//System.out.println("sendAlert() : header is "+ header);
				//telegraphConfig.setButtonIcon();
				telegraphConfig.setWindowWidth(80);
				telegraphConfig.setWindowHeight(80);

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
		
				telegraph = new Telegraph(
						header, 
						msg,
						telegraphConfig) ;

				telegraphQueue.add(telegraph);

				oldMsgCache = msg;
				//messageCounter--;
			}
		}

	public void validateMsg(HistoricalEvent event) {
			
		String header = "";
		String message = event.getDescription(); //.toUpperCase();
		// reset willNotify to false
		willNotify = false;
		
		if ( message != null && message != "" ) {
			//System.out.println("validateMsg() : message is "+ message);
			if ( message.toLowerCase().contains(matchedWord1.toLowerCase()) ||
					message.toLowerCase().contains(matchedWord2.toLowerCase())	) {
					willNotify = false; // do not send 
				}
			else {
				// first reset willNotify to false
				willNotify = false;

				HistoricalEventCategory category = event.getCategory();

				if (category.equals(HistoricalEventCategory.MALFUNCTION)
						&& showMalfunction ) {
					header = Msg.getString("NotificationManager.message.malfunction"); //$NON-NLS-1$
					willNotify = true;
				}
				
				else if (category.equals(HistoricalEventCategory.MEDICAL)
						&& showMedical )	{
					header = Msg.getString("NotificationManager.message.medical"); //$NON-NLS-1$
					willNotify = true;
				}
	
			}
	
		}
		if (willNotify) sendAlert(event, message, header);
	}
	
	// 2014-11-16 Added modifyMsg()
	//TODO: 
	public String parseMsg(String msg) {
		
		// or use String replaced = string.replace("abcd", "dddd");
		msg = msg.toUpperCase();
		msg = msg.replaceAll("OCCURRED", "");

		/*
		message = message.replaceAll("COLD", "a COLD");
		message = message.replaceAll("FLU", "a FLU");
		message = message.replaceAll("NAVIGATION", "a NAVIGATION");
		message = message.replaceAll("MAJOR", "a MAJOR");
		message = message.replaceAll("MINOR", "a MINOR");
		message = message.replaceAll("FUEL", "a FUEL");
		 */

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
		
		return msg;
	}
	
	//prepare the notification box for displaying the message
	public String generateMsg(HistoricalEvent event, String msg)  {
		//count++;

		String name = "";
		//String category = "";
		String buildingName = "";
		String settlementName = "";
		String equipmentName = "";
		String vehicleName = "";
		String message = parseMsg(msg);
		String personName = "";
		String locationName = "";
		String unitName = "";
	
		if (willNotify == true) {
			// if the condition is true,
			//category = category + event.getCategory();		
			Object source = event.getSource();
			try {
			if (source instanceof Person) {
				Person p =((Person) source);
				personName = p.getName();
				Unit container = p.getContainerUnit();
				// test if the person has a valid container
				if (container == null) {
	                locationName = "outside";
	                logger.info("Someone had an accident : " +
	                		personName +
							" had " + message + " " +
							locationName);
	    		 	message = formatMsg(message);
	                message = personName +
							" had " + message + " " +
							locationName ; 
				} // for a person
	            else if (container instanceof Settlement) {
	                settlementName = p.getSettlement().getName();
	                BuildingManager bm = p.getSettlement().getBuildingManager();
	                buildingName = bm.getBuilding(p).getNickName();
	                logger.info("Someone had an accident : " +
	                		personName +
							" had " + message + 
							" at " + buildingName + 
							" in " + settlementName);
	    		 	message = formatMsg(message);
	                message = personName +
							" had " + message + 
							" at " + buildingName + 
							" in " + settlementName;
	            } // for a person
	            else if (container instanceof Vehicle) {
	                vehicleName = p.getVehicle().getName();
	                logger.info("Someone had an accident : " 
	                		+ personName +
							" had " + message + 
							" in " + vehicleName);
	    		 	message = formatMsg(message);
	                message = personName +
							" had " + message + 
							" in " + vehicleName ;
				} // for a person
				
		}// end of Person
		else if (source instanceof Equipment) {
				Equipment e = ((Equipment) source);
				
				try { // test if the equipment has a name
					equipmentName = (String)e.getName();
					
					if (!equipmentName.equals(null)) {
					
						try { // yes the equipment does have a name
						  // test if the equipment has a location container
							Unit u = e.getContainerUnit();
							unitName = u.getName();
							// yes the equipment does have a location container
							// TODO: in future test if the equipment belongs to a person
							// Person p = e.getUnitManager().getPerson();
							logger.info("Equipment malfunction : " + equipmentName +
									" had " + message + 
									" in " + unitName);
						 	message = formatMsg(message);
							message = equipmentName + 
									" had " + message + 
									" in " + unitName ;
						} catch (Exception e1) { 
							// No, the equipment does NOT have a location container
							System.err.println("Exception Caught Successfully: equipment's container is" + e1.getMessage());
							//e1.printStackTrace();
							unitName = "outside";
							// TODO: in future test if the equipment belongs to a person
							// Person p = e.getUnitManager().getPerson();
							logger.info("Equipment malfunction : " + equipmentName +
									" had " + message + " " +
									unitName);
						 	message = formatMsg(message);
							message = equipmentName + 
									" had " + message + 
									" " + unitName ;	
							} // end of 2nd try for Equipment
					} // end of if  (equipmentName != "null")

				} catch (Exception e1) { 
					// No, the equipment does NOT have a name
					System.err.println("Exception Caught Successfully: equipment's name is " + e1.getMessage());
					//e1.printStackTrace();
					equipmentName = "";
					try { // test if the equipment has a location/person container
						Unit u = e.getContainerUnit();
						unitName = u.getName();
						// yes the equipment does have a location/person container
						logger.info("Equipment malfunction : just had " + message + 
								" in " + unitName);
					 	message = formatMsg(message);
						message = "Just had " + message + 
								" in " + unitName;
					} catch (Exception e2) { 
						// No, the equipment does NOT have a location container
						System.err.println("Exception Caught Successfully : equipment's container is" + e2.getMessage());
						//e1.printStackTrace();
						unitName = "outside";
						logger.info("Equipment malfunction : just had " + message + 
							unitName);
					 	message = formatMsg(message);
						message = "Just had " + message + 
							" " + unitName;
					} // end of test if the equipment has a location/person container
				} // end of catch for No the equipment does NOT have a name
		} // end of Equipment
		else if (source instanceof Vehicle) {
				Vehicle v =((Vehicle) source);
				vehicleName = v.getName();		
				try {  // test if the Vehicle has a location container
					Unit u = v.getTopContainerUnit();
					//if (!u.equals(null))	
					unitName = u.getName();
					
					if (!unitName.equals(null)) {
					// Yes, the Vehicle does have a location container
					logger.info("Vehicle malfunction : " + vehicleName +
							" had " + message + 
							 " at " + unitName + "\n");
				 	message = formatMsg(message);
					message = vehicleName + 
							 " had " + message + 
							 " at " + unitName ;
					}
				} catch (Exception e) { 
					// No, the Vehicle does NOT have a location container
					System.err.println("Exception Caught Successfully : vehicle's container is " + e.getMessage());
					//e.printStackTrace();
				 	unitName = "outside";
				 	logger.info("Vehicle malfunction : " + vehicleName +
							" had " + message + " " +
							 unitName + "\n");
				 	message = formatMsg(message);
				 	message = vehicleName + 
							 " had " + message + 
							 "  " + unitName;
				}
				
		} // end of Vehicle
		else if (source instanceof Building) {
				Building b = ((Building) source);
				buildingName = b.getNickName();
				//System.out.println("buildingName is " + buildingName);
				Settlement s = b.getBuildingManager().getSettlement();
				settlementName = s.getName();
			 	logger.info("Building malfunction : " + buildingName + 
						" had " + message + 
						" in " + settlementName) ;
			 	message = formatMsg(message);
				message = buildingName + 
						" had " + message + 
						" in " + settlementName ;
			} // end of Building
		} catch (Exception e) {
				//e.printStackTrace();
				System.err.println("Exception Caught. Reasons: " + e.getMessage());
		};
		
		} // end of if (willNotify == true)

		//msg = "<html><i><b>" + msg + "</b></i></html>";
		
		message = "<html><CENTER><FONT COLOR=RED>" + message + "</FONT COLOR=RED></CENTER></html>";

		
		
		// 2014-11-16 Added modifyMsg()
		return message;
	}
	
	public String formatMsg(String msg) {
		return 	"<html><i><b>" 
				+ msg + "</b></i></html>";
	};
}
