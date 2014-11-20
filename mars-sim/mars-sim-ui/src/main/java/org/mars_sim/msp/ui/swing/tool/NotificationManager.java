/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.07 2014-11-16
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JDialog;

import org.jtelegraph.Telegraph;
import org.jtelegraph.TelegraphConfig;
import org.jtelegraph.TelegraphPosition;
import org.jtelegraph.TelegraphQueue;
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

/**
 * The NotificationManager class creates notification messages on the desktop
 * Events are based from HistoricalEvent.java
 */
 //TODO: replace subclassing JDialog. load up in MainDesktopPane
//2014-11-15 Renamed NotificationBox to NotificationManager
public class NotificationManager extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The name of the tool the window is for. */
	protected String name;
	//TODO: need to create an array of two element with name 
	// as array[0] and header as array[1]
	//private String header;
	private Telegraph telegraph;
	private TelegraphConfig telegraphConfig ;
	private TelegraphQueue queue;
	private boolean willNotify= false;
	//private String message = "";		
	private String matchedWord1 = "fixed";
	private String matchedWord2 = "recovering";
	private String oldMsgCache = "";

	public NotificationManager() {
		queue = new TelegraphQueue();
	
	}
	
	// 2014-11-15 Created sendAlert()
	public void sendAlert(HistoricalEvent event, String message, String header) {

		Color GREEN_CYAN = new Color(0, 255, 128);
		Color PURPLE_BLUE = new Color(39, 16, 167);		
		Color BLUE_CYAN = new Color(0, 128, 255);
		Color BURGANDY = new Color(148, 28, 10);
		//Color ORANGE = new Color(255, 128, 0);
		Color ORANGE = new Color(255, 176, 13);
		String msg = generateMsg(event, message);	

			if (!oldMsgCache.equals(msg)) {		
				header = "<html><CENTER><I><b><h1>" 
						+ header + "</h1></b></I></CENTER></html>";
				//System.out.println("sendAlert() : msg is "+ msg);
				//System.out.println("sendAlert() : header is "+ header);
				telegraphConfig = new TelegraphConfig();
				
				telegraphConfig.setBackgroundColor(ORANGE);
				telegraphConfig.setTitleColor(BURGANDY);
				//telegraphConfig.setTitleFont(font);
				telegraphConfig.setDescriptionColor(Color.DARK_GRAY);
				//telegraphConfig.setDescriptionFont(Font.SERIF);
				telegraphConfig.setAudioEnabled(false);
				//telegraphConfig.setAudioInputStream(null);
				//telegraphConfig.setButtonEnabled(true);
				telegraphConfig.setDuration(3000);
				telegraphConfig.setBorderColor(Color.YELLOW);
				telegraphConfig.setBorderThickness(5);
				telegraphConfig.setTelegraphPosition(TelegraphPosition.BOTTOM_RIGHT);
		
				telegraph = new Telegraph(
						header, 
						msg,
						telegraphConfig) ;

				queue.add(telegraph);	
				oldMsgCache = msg;
			}
		}
	
	
	public void validateMsg(HistoricalEvent event)  {
		
		String header = "";
		String message = event.getDescription(); //.toUpperCase();
		// reset willNotify to false
		willNotify = false;
		
		if ( message != null && message != "" ) {
			//System.out.println("validateMsg() : message is "+ message);
			if ( message.toLowerCase().contains(matchedWord1.toLowerCase()) ||
					message.toLowerCase().contains(matchedWord2.toLowerCase())	) {
					willNotify = false; // it ends here
				}
			else {
				// reset willNotify to false
				willNotify = false;

				HistoricalEventCategory category = event.getCategory();

				if (category.equals(HistoricalEventCategory.MALFUNCTION)) {
					header = Msg.getString("NotificationManager.message.malfunction"); //$NON-NLS-1$
					willNotify = true;
				}
				
				if (category.equals(HistoricalEventCategory.MEDICAL))	{
					header = Msg.getString("NotificationManager.message.medical"); //$NON-NLS-1$
					willNotify = true;
				}
	
			}
	
		}

		if (willNotify == true)
			sendAlert(event, message, header);
		
	}
	
	// 2014-11-16 Added modifyMsg()
	//TODO: 
	public String parseMsg(String msg) {
		
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
		
		msg = msg.replaceAll("a AIRLEAK", "an AIRLEAK");
		msg = msg.replaceAll("a ELECTRICAL", "an ELECTRICAL");
		
		msg = msg.replaceAll("a FOOD", "FOOD");
		msg = msg.replaceAll("a WATER", "WATER");
		msg = msg.replaceAll("a NAVIGATION", "NAVIGATION");
		msg = msg.replaceAll("a BROKEN", "BROKEN");
		msg = msg.replaceAll("a MINOR BURNS", "MINOR BURNS");
		msg = msg.replaceAll("a GENERATOR", "GENERATOR");
		msg = msg.replaceAll("a STARVATION", "STARVATION");
		msg = msg.replaceAll("a MENINGITIS", "MENINGITIS");
		msg = msg.replaceAll("a SUFFOCATION", "SUFFOCATION");
		msg = msg.replaceAll("a DEHYDRATION", "DEHYDRATION");

		msg = "<html><i><b>" 
				+ msg + "</b></i></html>";

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
	                message = "<html><CENTER><FONT COLOR=RED>" + 
	                		personName +
							" had " + message + " " +
							locationName + 
	                		"</FONT></CENTER></html>"; 
				} // for a person
	            else if (container instanceof Settlement) {
	                settlementName = p.getSettlement().getName();
	                BuildingManager bm = p.getSettlement().getBuildingManager();
	                buildingName = bm.getBuilding(p).getNickName();
	                message = personName +
							" had " + message + 
							" at " + buildingName + 
							" in " + settlementName;
	            } // for a person
	            else if (container instanceof Vehicle) {
	                vehicleName = p.getVehicle().getName();
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
							System.out.println("Equipment malfunction : " + equipmentName +
									" had " + message + 
									" in " + unitName);
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
							System.out.println("Equipment malfunction : " + equipmentName +
									" had " + message + " " +
									unitName);
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
						System.out.println("Equipment malfunction : just had " + message + 
								" in " + unitName);
						message = "Just had " + message + 
								" in " + unitName;
						
					} catch (Exception e2) { 
						// No, the equipment does NOT have a location container
						System.err.println("Exception Caught Successfully : equipment's container is" + e2.getMessage());
						//e1.printStackTrace();
						unitName = "outside";
						System.out.println("Equipment malfunction : just had " + message + 
							unitName);
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
					System.out.println("Vehicle malfunction : " + vehicleName +
							" had " + message + 
							 " at " + unitName + "\n");
					message = vehicleName + 
							 " had " + message + 
							 " at " + unitName ;
					}
				} catch (Exception e) { 
					// No, the Vehicle does NOT have a location container
					System.err.println("Exception Caught Successfully : vehicle's container is " + e.getMessage());
					//e.printStackTrace();
				 	unitName = "outside";
				 	System.out.println("Vehicle malfunction : " + vehicleName +
							" had " + message + " " +
							 unitName + "\n");
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
				message = buildingName + 
						" had " + message + 
						" in " + settlementName ;
			} // end of Building
		} catch (Exception e) {
				//e.printStackTrace();
				System.err.println("Exception Caught. Reasons: " + e.getMessage());
		};
		
		} // end of if (willNotify == true)

		message = "<html><CENTER><FONT COLOR=RED>" + message + "</FONT COLOR=RED></CENTER></html>";

		// 2014-11-16 Added modifyMsg()
		return message;
	}
}
