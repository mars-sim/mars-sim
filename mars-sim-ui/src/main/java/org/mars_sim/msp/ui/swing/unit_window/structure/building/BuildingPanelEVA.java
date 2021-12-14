/*
 * Mars Simulation Project
 * BuildingPanelEVA.java
 * @date 2021-11-28
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Airlock;
import org.mars_sim.msp.core.structure.building.function.BuildingAirlock;
import org.mars_sim.msp.core.structure.building.function.EVA;
import org.mars_sim.msp.ui.swing.MainDesktopPane;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;


/**
 * The BuildingPanelEVA class presents the EVA activities
 * of a building.
 */
@SuppressWarnings("serial")
public class BuildingPanelEVA extends BuildingFunctionPanel implements MouseListener {
	private static final String UNLOCKED = "UNLOCKED";
	private static final String LOCKED = "LOCKED";

//	private int capCache;
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
	private JTextField innerDoorLabel;
	private JTextField outerDoorLabel;
	private JTextField occupiedLabel;
	private JTextField emptyLabel;
	private JTextField operatorLabel;
	private JTextField airlockStateLabel;
	private JTextField cycleTimeLabel;
	private JTextField innerDoorStateLabel;
	private JTextField outerDoorStateLabel;

	private ListModel occupantListModel;
	private ReservationListModel reservationListModel;
	private JList<Person> occupants;
	private JList<Person> reservationList;
	private WebScrollPane scrollPanel;
	private WebScrollPane scrollPanel1;

	private EVA eva;
	private BuildingAirlock buildingAirlock;
	private Airlock airlock;

	private UnitManager unitManager;

	/**
	 * Constructor.
	 * @param eva the eva function of a building this panel is for.
	 * @param desktop The main desktop.
	 */
	public BuildingPanelEVA(EVA eva, MainDesktopPane desktop) {

		// Use BuildingFunctionPanel constructor
		super(Msg.getString("BuildingPanelEVA.title"), eva.getBuilding(), desktop);

		// Initialize data members
		this.eva = eva;
		this.airlock = eva.getAirlock();
		this.buildingAirlock = (BuildingAirlock)eva.getAirlock();

		unitManager = desktop.getSimulation().getUnitManager();

		// Create occupant list model
		occupantListModel = new ListModel();

		// Create reservation list model
		reservationListModel = new ReservationListModel();
	}
	
	/**
	 * Build the UI
	 */
	@Override
	protected void buildUI(JPanel center) {

		// Create label panel
		WebPanel labelPanel = new WebPanel(new GridLayout(9, 2, 3, 1));
		center.add(labelPanel, BorderLayout.NORTH);
		labelPanel.setOpaque(false);
		labelPanel.setBackground(new Color(0,0,0,128));

		// Create outerDoorLabel
		outerDoorLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.outerDoor.number"),
									  eva.getNumAwaitingOuterDoor(), null);

		// Create innerDoorLabel
		innerDoorLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.innerDoor.number"),
									  eva.getNumAwaitingInnerDoor(), null);


		if (eva.getAirlock().isInnerDoorLocked())
			innerDoorStateCache = LOCKED;
		else {
			innerDoorStateCache = UNLOCKED;
		}
		// Create innerDoorStateLabel
		innerDoorStateLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.innerDoor.state"),
										   innerDoorStateCache, null);

		if (eva.getAirlock().isOuterDoorLocked())
			outerDoorStateCache = LOCKED;
		else {
			outerDoorStateCache = UNLOCKED;
		}
		// Create outerDoorStateLabel
		outerDoorStateLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.outerDoor.state"),
										   outerDoorStateCache, null);

		// Create occupiedLabel
		occupiedLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.occupied"),
									 eva.getNumOccupied(), null);

		// Create emptyLabel
		emptyLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.empty"),
								  eva.getNumEmptied(), null);

		// Create OperatorLabel
		operatorLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.operator"),
									 eva.getOperatorName(), null);

		// Create airlockStateLabel
		airlockStateLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.airlock.state"),
										 airlock.getState().toString(), null);

		// Create cycleTimeLabel
		cycleTimeLabel = addTextField(labelPanel, Msg.getString("BuildingPanelEVA.airlock.cycleTime"),
									  String.format("%.1f", airlock.getRemainingCycleTime()), null);

		// Detials panels
		UIManager.getDefaults().put("TitledBorder.titleColor", Color.darkGray);
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
	        		lowerEtched, " " + Msg.getString("BuildingPanelEVA.titledB.occupants") + " ");
		Font titleFont = UIManager.getFont("TitledBorder.font");
		title.setTitleFont(titleFont.deriveFont(Font.ITALIC + Font.BOLD));

		// Create occupant panel
		WebPanel occupantPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		occupantPanel.setBorder(title);
		center.add(occupantPanel, BorderLayout.CENTER);

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
		center.add(reservationPanel, BorderLayout.SOUTH);

		// Create scroll panel for occupant list.
		scrollPanel1 = new WebScrollPane();
		scrollPanel1.setPreferredSize(new Dimension(150, 100));
		reservationPanel.add(scrollPanel1);

		// Create reservation list
		reservationList = new JList<>(reservationListModel);
		reservationList.addMouseListener(this);
		scrollPanel1.setViewportView(reservationList);

	}

	@Override
	public void update() {

		// Update innerDoorLabel
		if (innerDoorCache != eva.getNumAwaitingInnerDoor()) {
			innerDoorCache = eva.getNumAwaitingInnerDoor();
			innerDoorLabel.setText(Integer.toString(innerDoorCache));
		}

		// Update outerDoorLabel
		if (outerDoorCache != eva.getNumAwaitingOuterDoor()) {
			outerDoorCache = eva.getNumAwaitingOuterDoor();
			outerDoorLabel.setText(Integer.toString(outerDoorCache));
		}

		// Update occupiedLabel
		if (occupiedCache != eva.getNumOccupied()) {
			occupiedCache = eva.getNumOccupied();
			occupiedLabel.setText(Integer.toString(occupiedCache));
		}

		// Update emptyLabel
		if (emptyCache != eva.getNumEmptied()) {
			emptyCache = eva.getNumEmptied();
			emptyLabel.setText(Integer.toString(emptyCache));
		}

		// Update operatorLabel
		if (!operatorCache.equals(eva.getOperatorName())) {
			operatorCache = eva.getOperatorName();
			operatorLabel.setText(operatorCache);
		}

		// Update airlockStateLabel
		String state = airlock.getState().toString();
		if (!airlockStateCache.equalsIgnoreCase(state)) {
			airlockStateCache = state;
			airlockStateLabel.setText(state);
		}

		// Update cycleTimeLabel
		double time = airlock.getRemainingCycleTime();
		if (cycleTimeCache != time) {
			cycleTimeCache = time;
			cycleTimeLabel.setText(String.format("%.1f",cycleTimeCache));
		}

		String innerDoorState = "";
		if (airlock.isInnerDoorLocked())
			innerDoorState = LOCKED;
		else {
			innerDoorState = UNLOCKED;
		}

		// Update innerDoorStateLabel
		if (!innerDoorStateCache.equalsIgnoreCase(innerDoorState)) {
			innerDoorStateCache = innerDoorState;
			innerDoorStateLabel.setText(innerDoorState);
		}

		String outerDoorState = "";
		if (airlock.isOuterDoorLocked())
			outerDoorState = LOCKED;
		else {
			outerDoorState = UNLOCKED;
		}

		// Update outerDoorStateLabel
		if (!outerDoorStateCache.equalsIgnoreCase(outerDoorState)) {
			outerDoorStateCache = outerDoorState;
			outerDoorStateLabel.setText(outerDoorState);
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


		private List<Integer> intList;

		private ListModel() {

			intList = new ArrayList<>(buildingAirlock.getAllInsideOccupants());
			Collections.sort(intList);
		}

		@Override
		public Person getElementAt(int index) {

			Person result = null;

			int size = getSize(); //buildingAirlock.getAllInsideOccupants().size();

			if (!intList.isEmpty() && index >= 0 && index < size && size > 0) {
				result = unitManager.getPersonByID(intList.get(index));
			}

			return result;
		}

		@Override
		public int getSize() {
			return buildingAirlock.getAllInsideOccupants().size(); //getNumOccupants();
		}

		/**
		 * Update the population list model.
		 */
		public void update() {

			List<Integer> newIntList = new ArrayList<>(buildingAirlock.getAllInsideOccupants());
			Collections.sort(newIntList);

			if (!intList.equals(newIntList)
					|| !intList.containsAll(newIntList)
					|| !newIntList.containsAll(intList)) {

				intList = newIntList;

				fireContentsChanged(this, 0, getSize());
			}
		}
	}

	/**
	 * Reservation List model for airlock reservation.
	 */
	private class ReservationListModel extends AbstractListModel<Person> {

		private List<Integer> intList;

		private ReservationListModel() {
			intList = new ArrayList<>(airlock.getReserved());
			Collections.sort(intList);
		}

		@Override
		public Person getElementAt(int index) {

			Person result = null;

			int size = getSize();

			if (!intList.isEmpty() && index >= 0 && index < size && size > 0) {
				result = unitManager.getPersonByID(intList.get(index));
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
			Collections.sort(newIntList);

			if (!intList.equals(newIntList)
					|| !intList.containsAll(newIntList)
					|| !newIntList.containsAll(intList)) {

				intList = newIntList;

				fireContentsChanged(this, 0, getSize());
			}
		}
	}
	/**
	 * Mouse clicked event occurs.
	 *
	 * @param event the mouse event
	 */
	@Override
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

	@Override
	public void mousePressed(MouseEvent arg0) {
		// Update panel
		update();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// Update panel
		update();
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// Update panel
		update();
	}

	@Override
	public void destroy() {
		super.destroy();
		
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

