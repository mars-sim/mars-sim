/*
 * Mars Simulation Project
 * PersonUnitWindow.java
 * @date 2023-06-04
 * @author Scott Davis
 */
package com.mars_sim.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.mars_sim.core.person.Person;
import com.mars_sim.core.person.ai.shift.ShiftSlot;
import com.mars_sim.ui.swing.MainDesktopPane;
import com.mars_sim.ui.swing.StyleManager;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfo;
import com.mars_sim.ui.swing.unit_display_info.UnitDisplayInfoFactory;
import com.mars_sim.ui.swing.unit_window.InventoryTabPanel;
import com.mars_sim.ui.swing.unit_window.LocationTabPanel;
import com.mars_sim.ui.swing.unit_window.NotesTabPanel;
import com.mars_sim.ui.swing.unit_window.SponsorTabPanel;
import com.mars_sim.ui.swing.unit_window.UnitWindow;


/**
 * The PersonWindow is the window for displaying a person.
 */
@SuppressWarnings("serial")
public class PersonUnitWindow extends UnitWindow {

	private static final String TOWN = "settlement";
	private static final String JOB = "career";
	private static final String ROLE = "role";
	private static final String SHIFT = "shift";
	
	private static final String TWO_SPACES = "  ";
	private static final String SIX_SPACES = "      ";
	
	private static final Font font = StyleManager.getSmallLabelFont();
	
	/** Is person dead? */
	private boolean deadCache = false;
	
	private String oldRole = "";
	private String oldJob = "";
	private String oldTown = "";
	private String oldShift = "";
	
	private JLabel townLabel = new JLabel();
	private JLabel jobLabel = new JLabel();
	private JLabel roleLabel = new JLabel();
	private JLabel shiftLabel = new JLabel();

	private JPanel statusPanel;
	
	private TabPanelDeath tabPanelDeath;
	
	private Person person;

	/**
	 * Constructor.
	 * 
	 * @param desktop the main desktop panel.
	 * @param person  the person for this window.
	 */
	public PersonUnitWindow(MainDesktopPane desktop, Person person) {
		// Use UnitWindow constructor
		
		// Note : variable = (condition) ? expressionTrue : expressionFalse
		
		super(desktop, person, person.getName() 
				+ " of " + 
				((person.getAssociatedSettlement() != null) ? person.getAssociatedSettlement() : person.getBuriedSettlement())
				+ " (" + (person.getLocationStateType().getName()) + ")"
				, false);
		this.person = person;

		initTopPanel();
		
		initTabPanel(person);
		
		statusUpdate();
	}
	
	/**
	 * Initializes the top panel.
	 */
	private void initTopPanel() {
		
		// Create status panel
		statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		statusPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		getContentPane().add(statusPanel, BorderLayout.NORTH);	
		
		statusPanel.setPreferredSize(new Dimension(-1, UnitWindow.STATUS_HEIGHT));

		// Create name label
		UnitDisplayInfo displayInfo = UnitDisplayInfoFactory.getUnitDisplayInfo(unit);
		String name = SIX_SPACES + getShortenedName(unit.getName()) + SIX_SPACES;

		JLabel nameLabel = new JLabel(name, displayInfo.getButtonIcon(unit), SwingConstants.CENTER);
		nameLabel.setMinimumSize(new Dimension(120, UnitWindow.STATUS_HEIGHT));
		
		JPanel namePane = new JPanel(new BorderLayout(0, 0));
		namePane.setAlignmentX(Component.CENTER_ALIGNMENT);
		namePane.setAlignmentY(Component.CENTER_ALIGNMENT);
		namePane.add(nameLabel, BorderLayout.CENTER);
		
		nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		nameLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		nameLabel.setFont(font);
		nameLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		nameLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		statusPanel.add(namePane, BorderLayout.WEST);

		JPanel gridPanel = new JPanel(new GridLayout(2, 2, 5, 1));		
		gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		gridPanel.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		gridPanel.add(createTile(TOWN, "Hometown", townLabel));
		gridPanel.add(createTile(JOB, "Job", jobLabel));
		gridPanel.add(createTile(ROLE, "Role", roleLabel));
		gridPanel.add(createTile(SHIFT, "Shift", shiftLabel));

		statusPanel.add(gridPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a tile panel.
	 * 
	 * @param title
	 * @param tooltip
	 * @param label
	 * @return
	 */
	public JPanel createTile(String title, String tooltip, JLabel label) {
		JPanel tilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		JLabel iconLabel = new JLabel();
		iconLabel.setToolTipText(tooltip);
		setImage(title, iconLabel);

		label.setFont(font);
		
		tilePanel.add(iconLabel);
		tilePanel.add(label);
		tilePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		return tilePanel;
	}
	
	/**
	 * Initializes the tab panels.
	 * 
	 * @param person
	 */
	public void initTabPanel(Person person) {
		// Add tab panels	
		addTabPanel(new TabPanelActivity(person, desktop));
		
		addTabPanel(new TabPanelAttribute(person, desktop));

		addTabPanel(new TabPanelCareer(person, desktop));

		// Add death tab panel if person is dead.
		if (person.isDeclaredDead()
				|| person.getPhysicalCondition().isDead()) {
			deadCache = true;
			
			tabPanelDeath = new TabPanelDeath(person, desktop);
			addTabPanel(tabPanelDeath);
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

		addTabPanel(new SponsorTabPanel(person.getReportingAuthority(), desktop));
		
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
		
		String title = person.getName() 
				+ " of " + 
				((person.getAssociatedSettlement() != null) ? person.getAssociatedSettlement() : person.getBuriedSettlement())
				+ " (" + (person.getLocationStateType().getName()) + ")";
		super.setTitle(title);
		
		if (!deadCache 
			&& (person.isDeclaredDead()
			|| person.getPhysicalCondition().isDead())) {
			deadCache = true;
			addTabPanel(new TabPanelDeath(person, desktop));
		}
		
		if (deadCache && !person.getPhysicalCondition().isDead()) {
			deadCache = false;
			removeTabPanel(tabPanelDeath);
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

		if (townString != null && !oldTown.equals(townString)) {
			oldJob = townString;
			if (townString.length() > 40)
				townString = townString.substring(0, 40);
			townLabel.setText(TWO_SPACES + townString);
		}

		String jobString = person.getMind().getJob().getName();
		if (!oldJob.equals(jobString)) {
			oldJob = jobString;
			jobLabel.setText(TWO_SPACES + jobString);
		}

		String roleString = person.getRole().getType().getName();
		if (!oldRole.equals(roleString)) {
			oldRole = roleString;
			roleLabel.setText(TWO_SPACES + roleString);
		}

		ShiftSlot shiftSlot = person.getShiftSlot();
		String shiftDesc = TabPanelSchedule.getShiftNote(person.getShiftSlot());
		String newShift = shiftSlot.getStatusDescription();
		if (!oldShift.equalsIgnoreCase(newShift)) {
			oldShift = newShift;
			shiftLabel.setText(TWO_SPACES + newShift);
			shiftLabel.setToolTipText(shiftDesc);
		}
	}
	
	/**
	 * Prepares unit window for deletion.
	 */
	@Override
	public void destroy() {		
		person = null;
		statusPanel = null;
		townLabel = null;
		jobLabel = null;
		roleLabel = null;
		shiftLabel = null;

		super.destroy();
	}

}
