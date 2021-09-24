/*
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @date 2021-09-24
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.core.time.ClockPulse;
import org.mars_sim.msp.core.time.MasterClock;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;


/**
 * The BuildingPanelEVA class presents the EVA activities 
 * of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelEVA extends BuildingFunctionPanel implements MouseListener, ClockListener {
	private static final String UNLOCKED = "UNLOCKED";
	private static final String LOCKED = "LOCKED";
	
	private int capCache;
	private int innerDoorCache;
	private int outerDoorCache;
	private int occupiedCache;
	private int emptyCache;
	private double cycleTimeCache;
	
	private String operatorCache = "";
	private String airlockStateCache = "";
	private String innerDoorStateCache = "";
	private String outerDoorStateCache = "";
	
//	private WebLabel capLabel;
	private WebLabel innerDoorLabel;
	private WebLabel outerDoorLabel;
	private WebLabel occupiedLabel;
	private WebLabel emptyLabel;
	private WebLabel operatorLabel;
	private WebLabel airlockStateLabel;
	private WebLabel cycleTimeLabel;
	private WebLabel innerDoorStateLabel;
	private WebLabel outerDoorStateLabel;
	
	private ListModel occupantListModel;
	private ReservationListModel reservationListModel;
	private JList<Person> occupants;
	private JList<Person> reservationList;
	private WebScrollPane scrollPanel;
	private WebScrollPane scrollPanel1;
	
	private EVA eva; 
	private BuildingAirlock buildingAirlock;
	private Airlock airlock;
	
	private static Simulation sim;
	private static UnitManager unitManager;
	private static MasterClock masterClock;
	
	/**
	 * Constructor.
	 * @param eva the eva function of a building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelEVA(EVA eva, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(eva.getBuilding(), desktop);

		// Initialize data members
		this.eva = eva;
		this.airlock = eva.getAirlock();
		this.buildingAirlock = (BuildingAirlock)eva.getAirlock();
		
		
		if (sim == null)
			sim = Simulation.instance();
		
		if (masterClock == null)
			masterClock = sim.getMasterClock();
		
		masterClock.addClockListener(this);
		unitManager = sim.getUnitManager();
		
		// Create occupant list model
		occupantListModel = new ListModel();
		
		// Create reservation list model
		reservationListModel = new ReservationListModel();
		
		// Set panel layout
		setLayout(new BorderLayout());

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(10, 1, 0, 0));
		add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));
	
		// Create medical care label
		WebLabel titleLabel = new WebLabel(Msg.getString("BuildingPanelEVA.title"), WebLabel.CENTER);
		titleLabel.setFont(new Font("Serif", Font.BOLD, 16));
		//medicalCareLabel.setForeground(new Color(102, 51, 0)); // dark brown
		labelPanel.add(titleLabel);

		
		// Create capacity label
//		capLabel = new WebLabel(Msg.getString("BuildingPanelEVA.capacity",
//				eva.getAirlockCapacity()), WebLabel.CENTER);
//		labelPanel.add(capLabel);
		
		// Create outerDoorLabel
		outerDoorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.outerDoor.number",
				eva.getNumAwaitingOuterDoor()), WebLabel.CENTER);
		labelPanel.add(outerDoorLabel);

		// Create innerDoorLabel
		innerDoorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.innerDoor.number",
				eva.getNumAwaitingInnerDoor()), WebLabel.CENTER);
		labelPanel.add(innerDoorLabel);
		
		
		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.innerDoor.state",
				innerDoorStateCache), WebLabel.CENTER);
		labelPanel.add(innerDoorStateLabel);
		
		
		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.outerDoor.state",
				outerDoorStateCache), WebLabel.CENTER);
		labelPanel.add(outerDoorStateLabel);
		
		
		// Create occupiedLabel
		occupiedLabel = new WebLabel(Msg.getString("BuildingPanelEVA.occupied",
				eva.getNumOccupied()), WebLabel.CENTER);
		labelPanel.add(occupiedLabel);
		
		
		// Create emptyLabel
		emptyLabel = new WebLabel(Msg.getString("BuildingPanelEVA.empty",
				eva.getNumEmptied()), WebLabel.CENTER);
		labelPanel.add(emptyLabel);
			
		
		// Create OperatorLabel
		operatorLabel = new WebLabel(Msg.getString("BuildingPanelEVA.operator",
				eva.getOperatorName()), WebLabel.CENTER);
		labelPanel.add(operatorLabel);
		
		
		// Create airlockStateLabel
		airlockStateLabel = new WebLabel(Msg.getString("BuildingPanelEVA.airlock.state",
				eva.getAirlock().getState().toString()), WebLabel.CENTER);
		labelPanel.add(airlockStateLabel);
		

		// Create cycleTimeLabel
		cycleTimeLabel = new WebLabel(Msg.getString("BuildingPanelEVA.airlock.cycleTime",
				Math.round(eva.getAirlock().getRemainingCycleTime()*10.0)/10.0), WebLabel.CENTER);
		labelPanel.add(cycleTimeLabel);
				
		UIManager.getDefaults().put("TitledBorder.titleColor", Color.darkGray);
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
	        		lowerEtched, " " + Msg.getString("BuildingPanelEVA.titledB.occupants") + " ");
//	      title.setTitleJustification(TitledBorder.RIGHT);
		Font titleFont = UIManager.getFont("TitledBorder.font");
		title.setTitleFont(titleFont.deriveFont(Font.ITALIC + Font.BOLD));
	        
		// Create occupant panel
		WebPanel occupantPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		occupantPanel.setBorder(title);
		add(occupantPanel, BorderLayout.CENTER);
		
		// Create scroll panel for occupant list.
		scrollPanel = new WebScrollPane();
		scrollPanel.setPreferredSize(new Dimension(150, 100));
		occupantPanel.add(scrollPanel);

		// Create occupant list
		occupants = new JList<Person>(occupantListModel);
		occupants.addMouseListener(this);
		scrollPanel.setViewportView(occupants);

		TitledBorder title1 = BorderFactory.createTitledBorder(
	        		lowerEtched, " " + Msg.getString("BuildingPanelEVA.titledB.Reserved") + " ");
		title1.setTitleFont(titleFont.deriveFont(Font.ITALIC + Font.BOLD));
		
		// Create reservation panel
		WebPanel reservationPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		reservationPanel.setBorder(title1);
		add(reservationPanel, BorderLayout.SOUTH);
		
		// Create scroll panel for occupant list.
		scrollPanel1 = new WebScrollPane();
		scrollPanel1.setPreferredSize(new Dimension(150, 100));
		reservationPanel.add(scrollPanel1);
		
		// Create reservation list
		reservationList = new JList<Person>(reservationListModel);
		reservationList.addMouseListener(this);
		scrollPanel1.setViewportView(reservationList);

	}

	@Override
	public void update() {
		// Update CapLabel
//		if (capCache != eva.getAirlockCapacity()) {
//			capCache = eva.getAirlockCapacity();
//			capLabel.setText(Msg.getString("BuildingPanelEVA.capacity", capCache));
//		}

		// Update innerDoorLabel
		if (innerDoorCache != eva.getNumAwaitingInnerDoor()) {
			innerDoorCache = eva.getNumAwaitingInnerDoor();
			innerDoorLabel.setText(Msg.getString("BuildingPanelEVA.innerDoor.number", innerDoorCache));
		}
		
		// Update outerDoorLabel
		if (outerDoorCache != eva.getNumAwaitingOuterDoor()) {
			outerDoorCache = eva.getNumAwaitingOuterDoor();
			outerDoorLabel.setText(Msg.getString("BuildingPanelEVA.outerDoor.number", outerDoorCache));
		}
		
		// Update occupiedLabel
		if (occupiedCache != eva.getNumOccupied()) {
			occupiedCache = eva.getNumOccupied();
			occupiedLabel.setText(Msg.getString("BuildingPanelEVA.occupied", occupiedCache));
		}	
		
		// Update emptyLabel
		if (emptyCache != eva.getNumEmptied()) {
			emptyCache = eva.getNumEmptied();
			emptyLabel.setText(Msg.getString("BuildingPanelEVA.empty", emptyCache));
		}	
		
		// Update operatorLabel
		if (!operatorCache.equals(eva.getOperatorName())) {
			operatorCache = eva.getOperatorName();
			operatorLabel.setText(Msg.getString("BuildingPanelEVA.operator", operatorCache));
		}	
		
		// Update airlockStateLabel
		String state = eva.getAirlock().getState().toString();
		if (!airlockStateCache.equalsIgnoreCase(state)) {
			airlockStateCache = state;
			airlockStateLabel.setText(Msg.getString("BuildingPanelEVA.airlock.state", state));
		}
		
		// Update cycleTimeLabel
		double time = Math.round(eva.getAirlock().getRemainingCycleTime()*10.0)/10.0;
		if (cycleTimeCache != time) {
			cycleTimeCache = time;
			cycleTimeLabel.setText(Msg.getString("BuildingPanelEVA.airlock.cycleTime", time));
		}
		
		String innerDoorState = "";
		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorState = LOCKED;
		else {
			innerDoorState = UNLOCKED;
		}
		
		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(Msg.getString("BuildingPanelEVA.innerDoor.state", innerDoorState));
		}
		
		String outerDoorState = "";
		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorState = LOCKED;
		else {
			outerDoorState = UNLOCKED;
		}
		
		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(Msg.getString("BuildingPanelEVA.outerDoor.state", outerDoorState));
		}
		
		// Update occupant list
		if (occupantListModel != null)
			occupantListModel.update();
		if (scrollPanel != null)
			scrollPanel.validate();
		
		// Update reservation list
		if (reservationListModel != null)
			reservationListModel.update();
	}
	
	/**
	 * List model for airlock occupant.
	 */
	private class ListModel extends AbstractListModel<Person> {

		private List<Person> list;
		private List<Integer> intList;
		
		private ListModel() {
	
			intList = new ArrayList<>(buildingAirlock.getAllInsideOccupants());
			list = new ArrayList<>();
			
			for (int i: intList) {
				list.add(airlock.getPersonByID(i));
			}

			Collections.sort(list);
		}

		@Override
		public Person getElementAt(int index) {

			Person result = null;

			if ((index >= 0) && (index < buildingAirlock.getAllInsideOccupants().size())) {
				result = list.get(index);
			}

			return result;
		}

		@Override
		public int getSize() {
			return buildingAirlock.getNumOccupants();
		}

		/**
		 * Update the population list model.
		 */
		public void update() {

			List<Integer> newIntList = new ArrayList<>(buildingAirlock.getAllInsideOccupants());
			
			if (!intList.containsAll(newIntList)
					|| !newIntList.containsAll(intList)) {

				intList = newIntList;
				
				list = new ArrayList<>();
				
				for (int i: newIntList) {
					list.add(airlock.getPersonByID(i));
				}

				Collections.sort(list);
				
				fireContentsChanged(this, 0, getSize());
			}
		}
	}

	/**
	 * Reservation List model for airlock reservation.
	 */
	private class ReservationListModel extends AbstractListModel<Person> {

		private List<Person> list;
		private List<Integer> intList;
		
		private ReservationListModel() {

			intList = new ArrayList<>(airlock.getReserved());
			list = new ArrayList<>();
			
			for (int i: intList) {
				list.add(airlock.getPersonByID(i));
			}

			Collections.sort(list);
		}

		@Override
		public Person getElementAt(int index) {

			Person result = null;

			if ((index >= 0) && (index < getSize())) {
				result = list.get(index);
			}

			return result;
		}

		@Override
		public int getSize() {
			return airlock.getReserved().size();
		}

		/**
		 * Update the population list model.
		 */
		public void update() {

			List<Integer> newIntList = new ArrayList<>(airlock.getReserved());
			
			if (!intList.containsAll(newIntList)
					|| !newIntList.containsAll(intList)) {

				intList = newIntList;
				
				list = new ArrayList<>();
				
				for (int i: newIntList) {
					list.add(unitManager.getPersonByID(i));
				}

				Collections.sort(list);
				
				fireContentsChanged(this, 0, getSize());
			}
		}
	}
	/**
	 * Mouse clicked event occurs.
	 * 
	 * @param event the mouse event
	 */
	public void mouseClicked(MouseEvent event) {
		// If double-click, open person window.
		if (event.getClickCount() >= 2) {
			Person person = (Person) occupants.getSelectedValue();
			if (person != null) {
				desktop.openUnitWindow(person, false);
			}
			
			Person person1 = (Person) reservationList.getSelectedValue();
			if (person1 != null) {
				desktop.openUnitWindow(person1, false);
			}
		}
		
		// Update panel
		update();
	}

	public void mousePressed(MouseEvent arg0) {
		// Update panel
		update();
	}

	public void mouseReleased(MouseEvent arg0) {
	}

	public void mouseEntered(MouseEvent arg0) {
		// Update panel
		update();
	}

	public void mouseExited(MouseEvent arg0) {
		// Update panel
		update();
	}

	@Override
	public void clockPulse(ClockPulse pulse) {
	}

	@Override
	public void uiPulse(double time) {
		// Update panel
		update();
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		// Update panel
		update();	
	}

	public void destroy() {

		innerDoorLabel = null;
		outerDoorLabel = null;
		occupiedLabel = null;
		emptyLabel = null;
		operatorLabel = null;
		airlockStateLabel = null;
		cycleTimeLabel = null;
		innerDoorStateLabel = null;
		outerDoorStateLabel = null;
		
		occupantListModel = null;
		reservationListModel = null;
		
		occupants = null;
		scrollPanel = null;
		scrollPanel1 = null;
		
		eva = null;
		airlock = null;
		buildingAirlock = null;
	}
}
	
