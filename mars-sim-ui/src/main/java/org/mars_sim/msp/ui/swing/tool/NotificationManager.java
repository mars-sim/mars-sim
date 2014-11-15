/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.07 2014-11-15
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;

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
	private String header;
	private Telegraph telegraph;
	private TelegraphConfig telegraphConfig ;
	private TelegraphQueue queue;
	private boolean willNotify= false;
	private String message = "";		
	private String matchedWord1 = "fixed";
	private String matchedWord2 = "recovering";
	private String oldMsgCache = "";

	public NotificationManager() {
		queue = new TelegraphQueue();
	
	}
	
	// 2014-11-15 Created sendAlert()
	public void sendAlert(HistoricalEvent event) {

		String msg = generateMsg(event);	

			if (!oldMsgCache.equals(msg)) {		
				header = "<html><CENTER><FONT COLOR=BLUE><b><h2>" 
						+ header + "</h2></b></FONT></CENTER></html>";
				//System.out.println("sendAlert() : msg is "+ msg);
				//System.out.println("sendAlert() : header is "+ header);
				telegraphConfig = new TelegraphConfig();
				
				telegraphConfig.setBackgroundColor(Color.ORANGE);
				//telegraphConfig.setDescriptionFont();
				telegraphConfig.setTitleColor(Color.BLUE);
				//telegraphConfig.setTitleFont(font);
				//telegraphConfig.setButtonEnabled(true);
				telegraphConfig.setDuration(3000);
				telegraphConfig.setBorderColor(Color.YELLOW);
				telegraphConfig.setBorderThickness(3);
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
		
		message = event.getDescription(); //.toUpperCase();
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
					header = Msg.getString("NotificationBox.message.malfunction"); //$NON-NLS-1$
					willNotify = true;
				}
				
				if (category.equals(HistoricalEventCategory.MEDICAL))	{
					header = Msg.getString("NotificationBox.message.medical"); //$NON-NLS-1$
					willNotify = true;
				}
	
			}
	
		}

		if (willNotify == true)
			sendAlert(event);
	}
	
	//prepare the notification box for displaying the message
	public String generateMsg(HistoricalEvent event)  {
		//count++;

		String name = "";
		//String category = "";
		String buildingName = "";
		String settlementName = "";
		String equipmentName = "";
		String vehicleName = "";
		String labelText = "";
		String personName = "";
		String locationName = "";
		String unitName = "";
		
		message = message.replaceAll("occurred", "");
		message = message.replaceAll("Occurred", "");
		
		message = "<html><b>" + message + "</b></html>";
		
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
	                labelText = "<html><CENTER><FONT COLOR=RED>" + 
	                		personName +
							" had " + message + " " +
							locationName + 
	                		"</FONT></CENTER></html>"; 
				} // for a person
	            else if (container instanceof Settlement) {
	                settlementName = p.getSettlement().getName();
	                BuildingManager bm = p.getSettlement().getBuildingManager();
	                buildingName = bm.getBuilding(p).getNickName();
	                labelText = "<html><CENTER><FONT COLOR=RED>" + 
	                		personName +
							" had " + message + 
							" at " + buildingName + 
							" in " + settlementName + 
							"</FONT></CENTER></html>";
	            } // for a person
	            else if (container instanceof Vehicle) {
	                vehicleName = p.getVehicle().getName();
	                labelText = "<html><CENTER><FONT COLOR=RED>" + 
	                		personName +
							" had " + message + 
							" in " + vehicleName + 
							"</FONT></CENTER></html>";
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
							labelText = "<html><CENTER><FONT COLOR=RED>" +
									equipmentName + 
									" had " + message + 
									" in " + unitName +
									"</FONT></CENTER></html>";
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
							labelText = "<html><CENTER><FONT COLOR=RED>" +
									equipmentName + 
									" had " + message + 
									" " + unitName +
									"</FONT></CENTER></html>";	
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
						labelText = "<html><CENTER><FONT COLOR=RED>" +
								" Just had " + message + 
								" in " + unitName + 
								"</FONT></CENTER></html>";
						
					} catch (Exception e2) { 
						// No, the equipment does NOT have a location container
						System.err.println("Exception Caught Successfully : equipment's container is" + e2.getMessage());
						//e1.printStackTrace();
						unitName = "outside";
						System.out.println("Equipment malfunction : just had " + message + 
							unitName);
						labelText = "<html><CENTER><FONT COLOR=RED>" +
							" Just had " + message + 
							" " + unitName + 
							"</FONT></CENTER></html>";
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
					labelText = "<html><CENTER><FONT COLOR=RED>" +
							 vehicleName + 
							 " had " + message + 
							 " at " + unitName +
							 "</FONT></CENTER></html>";
					}
				} catch (Exception e) { 
					// No, the Vehicle does NOT have a location container
					System.err.println("Exception Caught Successfully : vehicle's container is " + e.getMessage());
					//e.printStackTrace();
				 	unitName = "outside";
				 	System.out.println("Vehicle malfunction : " + vehicleName +
							" had " + message + " " +
							 unitName + "\n");
					labelText = "<html><CENTER><FONT COLOR=RED>" +
							 vehicleName + 
							 " had " + message + 
							 "  " + unitName +
							 "</FONT></CENTER></html>";
				}
				
		} // end of Vehicle
		else if (source instanceof Building) {
				Building b = ((Building) source);
				buildingName = b.getNickName();
				//System.out.println("buildingName is " + buildingName);
				Settlement s = b.getBuildingManager().getSettlement();
				settlementName = s.getName();
				labelText = "<html><CENTER><FONT COLOR=RED>" +
						buildingName + 
						" had " + message + 
						" in " + settlementName + 
						"</FONT></CENTER></html>";
			} // end of Building
		} catch (Exception e) {
				//e.printStackTrace();
				System.err.println("Exception Caught. Check the try catch branch for reasons: " + e.getMessage());
		};
		
		} // end of if (willNotify == true)

		return labelText;
	}
}
