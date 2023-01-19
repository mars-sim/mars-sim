/*
 * Mars Simulation Project
 * PersonWindow.java
 * @date 2022-10-24
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.ShiftSlot;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfo;
import org.mars_sim.msp.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import org.mars_sim.msp.ui.swing.unit_window.InventoryTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.LocationTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.NotesTabPanel;
import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;


/**
 * The PersonWindow is the window for displaying a person.
 */
@SuppressWarnings("serial")
public class PersonWindow extends UnitWindow {

	private static final String TOWN = Msg.getString("icon.colony");
	private static final String JOB = Msg.getString("icon.career");
	private static final String ROLE = Msg.getString("icon.role");
	private static final String SHIFT = Msg.getString("icon.shift");
	
	private static final String TWO_SPACES = "  ";
	private static final String SIX_SPACES = "      ";
	
	/** Is person dead? */
	private boolean deadCache = false;
	
	private String oldRoleString = "";
	private String oldJobString = "";
	private String oldTownString = "";
	
	private JLabel townLabel;
	private JLabel jobLabel;
	private JLabel roleLabel;
	private JLabel shiftLabel;

	private JPanel statusPanel;
	
	private Person person;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param person  the person for this window.
	 */
	public PersonWindow(MainDesktopPane desktop, Person person) {
		// Use UnitWindow constructor
		super(desktop, person, person.getNickName(), true);
		this.person = person;
	
		// Create status panel
		statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		getContentPane().add(statusPanel, BorderLayout.NORTH);	
		
		initTopPanel(person);
		
		initTabPanel(person);
		
		statusUpdate();
	}
	
	
	public void initTopPanel(Person person) {
		statusPanel.setPreferredSize(new Dimension(WIDTH / 8, UnitWindow.STATUS_HEIGHT));

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = SIX_SPACES + unit.getShortenedName() + SIX_SPACES;

		statusPanel.setPreferredSize(new Dimension(WIDTH / 8, UnitWindow.STATUS_HEIGHT));

		JLabel nameLabel = new JLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
		nameLabel.setMinimumSize(new Dimension(120, UnitWindow.STATUS_HEIGHT));
		
		JPanel namePane = new JPanel(new BorderLayout(50, 0));
		namePane.add(nameLabel, BorderLayout.CENTER);
	
		Font font = null;

		if (MainWindow.OS.contains("linux")) {
			font = new Font("DIALOG", Font.BOLD, 8);
		} else {
			font = new Font("DIALOG", Font.BOLD, 10);
		}
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		nameLabel.setFont(font);
		nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
		nameLabel.setHorizontalTextPosition(JLabel.CENTER);

		statusPanel.add(namePane);

		JLabel townIconLabel = new JLabel();
		townIconLabel.setToolTipText("Hometown");
		setImage(TOWN, townIconLabel);

		JLabel jobIconLabel = new JLabel();
		jobIconLabel.setToolTipText("Job");
		setImage(JOB, jobIconLabel);

		JLabel roleIconLabel = new JLabel();
		roleIconLabel.setToolTipText("Role");
		setImage(ROLE, roleIconLabel);

		JLabel shiftIconLabel = new JLabel();
		shiftIconLabel.setToolTipText("Work Shift");
		setImage(SHIFT, shiftIconLabel);

		JPanel townPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel jobPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		JPanel shiftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		townLabel = new JLabel();
		townLabel.setFont(font);

		jobLabel = new JLabel();
		jobLabel.setFont(font);

		roleLabel = new JLabel();
		roleLabel.setFont(font);

		shiftLabel = new JLabel();
		shiftLabel.setFont(font);

		townPanel.add(townIconLabel);
		townPanel.add(townLabel);
		townPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		jobPanel.add(jobIconLabel);
		jobPanel.add(jobLabel);
		jobPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		rolePanel.add(roleIconLabel);
		rolePanel.add(roleLabel);
		rolePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		shiftPanel.add(shiftIconLabel);
		shiftPanel.add(shiftLabel);
		shiftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel rowPanel = new JPanel(new GridLayout(2, 2, 0, 0));
		rowPanel.add(townPanel);
		rowPanel.add(rolePanel);
		rowPanel.add(shiftPanel);
		rowPanel.add(jobPanel);

		statusPanel.add(rowPanel);
		rowPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
	}
	
	public void initTabPanel(Person person) {
		// Add tab panels	
		addTabPanel(new TabPanelActivity(person, desktop));
		
		addTabPanel(new TabPanelAttribute(person, desktop));

		addTabPanel(new TabPanelCareer(person, desktop));

		// Add death tab panel if person is dead.
		if (person.isDeclaredDead()
				|| person.getPhysicalCondition().isDead()) {
			deadCache = true;
			addDeathPanel(new TabPanelDeath(person, desktop));
		}

		addTabPanel(new TabPanelFavorite(person, desktop));

		addTabPanel(new TabPanelHealth(person, desktop));

		addTabPanel(new InventoryTabPanel(person, desktop));

		addTabPanel(new LocationTabPanel(person, desktop));

		addTabPanel(new NotesTabPanel(person, desktop));
		
		addTabPanel(new TabPanelPersonality(person, desktop));
		
		addTabPanel(new TabPanelSchedule(person, desktop));

		addTabPanel(new TabPanelScienceStudy(person, desktop));

		addTabPanel(new TabPanelSkill(person, desktop));

		addTabPanel(new TabPanelSocial(person, desktop));

		addTabPanel(new TabPanelSponsor(person, desktop));
		
		addFirstPanel(new TabPanelGeneral(person, desktop));
		
		// Add to tab panels. 
		addTabIconPanels();
	}

	/**
	 * Updates this window.
	 */
	@Override
	public void update() {
		super.update();

		if (!deadCache 
			&& (person.isDeclaredDead()
			|| person.getPhysicalCondition().isDead())) {
			deadCache = true;
			addDeathPanel(new TabPanelDeath(person, desktop));
		}
		
		statusUpdate();
	}

	/*
	 * Updates the status of the person.
	 */
	public void statusUpdate() {

		String townString = null;

		if (person.getPhysicalCondition().isDead()) {
			if (person.getAssociatedSettlement() != null)
				townString = person.getAssociatedSettlement().getName();
			else if (person.getBuriedSettlement() != null)
				townString = person.getBuriedSettlement().getName();
			else if (person.getPhysicalCondition().getDeathDetails().getPlaceOfDeath() != null)
				townString = person.getPhysicalCondition().getDeathDetails().getPlaceOfDeath();
		}

		else if (person.getAssociatedSettlement() != null)
			townString = person.getAssociatedSettlement().getName();

		if (townString != null && !oldTownString.equals(townString)) {
			oldJobString = townString;
			if (townString.length() > 40)
				townString = townString.substring(0, 40);
			townLabel.setText(TWO_SPACES + townString);
		}

		String jobString = person.getMind().getJob().getName();
		if (!oldJobString.equals(jobString)) {
			oldJobString = jobString;
			jobLabel.setText(TWO_SPACES + jobString);
		}

		String roleString = person.getRole().getType().getName();
		if (!oldRoleString.equals(roleString)) {
			oldRoleString = roleString;
			roleLabel.setText(TWO_SPACES + roleString);
		}

		ShiftSlot newShiftType = person.getShiftSlot();
		String shiftDesc = TabPanelSchedule.getShiftDescription(newShiftType);
		shiftLabel.setText(TWO_SPACES + newShiftType.getShift().getName());
		shiftLabel.setToolTipText(shiftDesc);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		// nothing
	}
	
	/**
	 * Prepares unit window for deletion.
	 */
	public void destroy() {		
		person = null;
		
		statusPanel = null;
		
		townLabel = null;
		jobLabel = null;
		roleLabel = null;
		shiftLabel = null;
	}

}
