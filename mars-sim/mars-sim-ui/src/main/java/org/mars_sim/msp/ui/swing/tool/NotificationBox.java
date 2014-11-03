/**
 * Mars Simulation Project
 * MainDesktopManager.java
 * @version 3.07 2014-11-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.tool;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.equipment.Equipment;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;
import org.mars_sim.msp.core.vehicle.Vehicle;
import org.mars_sim.msp.ui.swing.MainDesktopPane;


/**
 * The NotificationBox class creates notification messages on the desktop
 * Events are based from HistoricalEvent.java
 */
public class NotificationBox extends JDialog {

	/** The name of the tool the window is for. */
	protected String name;
	
	public static final String MSGTITLE = Msg.getString("NotificationBox.message.title"); //$NON-NLS-1$
	//public static final String MSG = Msg.getString("NotificationBox.msg"); //$NON-NLS-1$

	/** The main desktop. */
	//protected MainDesktopPane desktop;
	//private final JFrame frame = new JFrame();
	private static int count;
	private Timer timer;
	private String msg;
	
	//private JLabel l;
	
	//public NotificationBox(MainDesktopPane desktop, HistoricalEvent event) {
	//	super(NAME, desktop);
	//desktop = Simulation.instance.getMars().getSettlement().	;
	//	this.desktop = desktop;
	public NotificationBox(HistoricalEvent event) {
		super();
		
		String oldMsgCache = "";
		String msg = pushNotification(event);
		if (!msg.equals("")) {
			if (!oldMsgCache.equals(msg)) {
				popUp(msg);
				oldMsgCache = msg;
			}
		}
	}

	public String pushNotification(HistoricalEvent event)  {
		count++;	
		setUndecorated(true);
		setVisible(false);

		String message = "";
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
		
		HistoricalEventCategory category = event.getCategory();

		if (category.equals(HistoricalEventCategory.MALFUNCTION) 
				 ||	category.equals(HistoricalEventCategory.MEDICAL) )	{
			// if the condition is true,
			//prepare the notification box for displaying the message		
			message = message + event.getDescription(); //.toUpperCase();	
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
							e1.printStackTrace();
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
					e1.printStackTrace();
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
						e1.printStackTrace();
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
					e.printStackTrace();
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
				e.printStackTrace();
				System.err.println("Exception Caught. Check the try catch branch for reasons: " + e.getMessage());
		};
		
		} // end of if (category.equals(HistoricalEventCategory.MALFUNCTION)
		
		return labelText;
	}
	
	public void popUp(String labelText) {
		
		String header = "<html><h2>" + MSGTITLE + "</h2></html>";
	
			setSize(220,100);
			//setUndecorated(true);
			setLayout(new GridBagLayout());
			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.weightx = 1.0f;
			constraints.weighty = 1.0f;
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			JLabel headingLabel = new JLabel(header, JLabel.CENTER);
			headingLabel.setFont(new Font("Courier New", Font.BOLD, 14));
			headingLabel.setForeground(Color.BLUE);
			//headingLabel.setIcon(); 
			//headingLabel.setOpaque(false);
			add(headingLabel, constraints);
	
			constraints.gridx = 0;
			constraints.gridy++;
			constraints.weightx = 1.0f;
			constraints.weighty = 1.0f;
			constraints.insets = new Insets(5, 5, 5, 5);
			constraints.fill = GridBagConstraints.BOTH;
			//System.out.println(labelText);
			JLabel messageLabel = new JLabel(labelText);
			messageLabel.setForeground(Color.RED);
			messageLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
			add(messageLabel, constraints);
			
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			//getContentPane().setBackground(Color.YELLOW);

			Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen				
			Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());// height of the task bar
			setLocation(scrSize.width - getWidth(), scrSize.height - toolHeight.bottom - getHeight());								
			//setLocation(scrSize.width - getWidth(), scrSize.height - toolHeight.bottom - (getHeight() * (n+1)));	

			Color paleYellow = new Color(0xff, 0xff, 0x84);
			Container c = getContentPane();
			c.setBackground(paleYellow); 
			//closeButton = new JButton(new AbstractAction("x") {
			//	@Override
		    //    public void actionPerformed(ActionEvent e) {
		        	//timer.stop();
			//		setVisible(false);
		    //           dispose();
		    //    }
			//});	
			/*
			// Create the GUI on the event-dispatching thread
	        SwingUtilities.invokeLater(new Runnable() {
	            @Override
	            public void run() {
	                setVisible(false);
			            dispose();
	            }
	        });		
			timer = new Timer(3000, new ActionListener() {
				@Override
			    public void actionPerformed(ActionEvent e) {
		            setVisible(false);
		            dispose();
		        }
		    });
			timer.setRepeats(false);
			timer.start();
			getContentPane().add(closeButton);
			closeButton.addActionListener(new ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        timer.stop();
		        setVisible(false);
	            dispose();
		        closeButton.setBackground(Color.red);
		      }
		    });
		    */
					new Thread(){
					@Override
			      	public void run() {
						// if (frame == null ) { return; } // working? 
			           try {
			                  Thread.sleep(5000); // time after which pop up will be disappeared.
			                  setVisible(false);
			                  dispose();
			           } catch (InterruptedException e) {
			        	   System.err.println("Exception Caught: " + e.getMessage());
			        	   e.printStackTrace();
			           }
				      };
					}.start();			
			setShape(new java.awt.geom.RoundRectangle2D.Double(0,0,getWidth(),getHeight(),20,20));  
			setAlwaysOnTop(true);
		    setResizable(false);
			setVisible(true);
		}
			
	
}
