/**
 * Mars Simulation Project
 * PersonDialog.java
 * @version 2.74 2002-05-13
 * @author Scott Davis
 */

package org.mars_sim.msp.ui.standard;

import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.person.*;
import org.mars_sim.msp.simulation.person.ai.*;
import org.mars_sim.msp.simulation.person.medical.DeathInfo;
import org.mars_sim.msp.simulation.structure.*;
import org.mars_sim.msp.simulation.vehicle.*;
import org.mars_sim.msp.ui.standard.monitor.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/** The PersonDialog class is a detail window for a person.
 *  It displays information about the person and the person's current status.
 */
public class PersonDialog extends UnitDialog {

    // Data members
    private Person person;          // Person detail window is about
    private JButton locationButton; // Location button
    private JPanel locationLabelPane; // Location label panel
    private JLabel outsideLabel;    // Outside label
    private JButton deathLocationButton; // Location button in death pane
    private JButton deathCenterMapButton;
    private JButton deathPositionMapButton;
    private JLabel latitudeLabel;   // Latitude label
    private JLabel longitudeLabel;  // Longitude label
    private JPanel skillListPane;   // Panel containing list of person's skills and their levels.
    private JLabel missionLabel;    // Current mission description label
    private JLabel missionPhaseLabel; // Current mission phase label
    private JLabel taskDescription; // Current task description label
    private JLabel taskPhase;       // Current task phase label
    private JLabel fatigueLabel;    // Fatigue label
    private JLabel hungerLabel;     // Hunger labele
    private JLabel performanceLabel;   // Performance rating
    private JList  illnessList;      // Health state in current phase
    private InventoryPanel inventoryPane; // The inventory panel
    private DefaultListModel problemListModel;

    // Cached person data
    private Coordinates unitCoords;
    private String settlementName;
    private String vehicleName;
    private Hashtable skillList;
    private double fatigue;
    private double hunger;
    private int healthRating;

    /**
     * Constructs a PersonDialog object
     * @param parentDesktop the desktop pane
     * @param personUIProxy the person's UI proxy
     */
    public PersonDialog(MainDesktopPane parentDesktop, PersonUIProxy personUIProxy) {

        // Use UnitDialog constructor
        super(parentDesktop, personUIProxy);
    }

    /** Initialize cached data members */
    protected void initCachedData() {

        unitCoords = new Coordinates(0D, 0D);
        settlementName = "";
        vehicleName = "";
        skillList = new Hashtable();
        fatigue = 0D;
        hunger = 0D;
    }

    /** Complete update (overridden) */
    protected void generalUpdate() {
	updatePosition();
	updateLocation();
	updateSkills();
	updateTask();
        updateCondition();
	inventoryPane.updateInfo();
    }

    /** Update position */
    private void updatePosition() {
	if (!unitCoords.equals(person.getCoordinates())) {
            unitCoords = new Coordinates(person.getCoordinates());
            latitudeLabel.setText("Latitude: " + unitCoords.getFormattedLatitudeString());
            longitudeLabel.setText("Longitude: " + unitCoords.getFormattedLongitudeString());
        }
    }

    /** Update location */
    private void updateLocation() {
        String location = person.getLocationSituation();

	if (location.equals(Person.BURIED)) {
	    if (!outsideLabel.getText().equals("Buried Outside")) 
	        outsideLabel.setText("Buried Outside");
	    addOutsideLabel();
	}
	else if (location.equals(Person.OUTSIDE)) {
            if (!outsideLabel.getText().equals("Outside"))
                outsideLabel.setText("Outside");
	    addOutsideLabel();
	}
	else if (location.equals(Person.INSETTLEMENT)) {
            String tempName = person.getSettlement().getName();
	    if (!settlementName.equals(tempName)) locationButton.setText(tempName);
            addLocationButton();
	}
	else if (location.equals(Person.INVEHICLE)) {
            String tempName = person.getVehicle().getName();
	    if (!vehicleName.equals(tempName)) locationButton.setText(tempName);
            addLocationButton();
	}
    }

    /**
     * Adds the outside label to location label pane if it's not there.
     */
    private void addOutsideLabel() {
        if (!containsComponent(locationLabelPane, outsideLabel)) {
            locationLabelPane.add(outsideLabel);
            locationLabelPane.remove(locationButton);
            locationLabelPane.validate();
        }
    }

    /**
     * Adds the location button to location label pane if it's not there.
     */
    private void addLocationButton() {
        if (!containsComponent(locationLabelPane, locationButton)) {
            locationLabelPane.add(locationButton);
	    locationLabelPane.remove(outsideLabel);
	    locationLabelPane.validate();
	}
    }
   
    /**
     * Checks if a container contains a given component.
     */
    private boolean containsComponent(Container container, Component component) {
        boolean result = false;
	Component[] components = container.getComponents();
	for (int x=0; x < components.length; x++) 
	    if (components[x] == component) result = true;
	return result;
    }
    
    /** Update skill list */
    private void updateSkills() {

        boolean change = false;
        SkillManager skillManager = person.getSkillManager();

        String[] keyNames = skillManager.getKeys();
        for (int x=0; x < keyNames.length; x++) {
            int skillLevel = skillManager.getSkillLevel(keyNames[x]);
            if (skillLevel > 0) {
                if (skillList.containsKey(keyNames[x])) {
                    int cacheSkillLevel = ((Integer) skillList.get(keyNames[x])).intValue();
                    if (skillLevel != cacheSkillLevel) {
                        skillList.put(keyNames[x], new Integer(skillLevel));
                        change = true;
                    }
                }
                else {
                    skillList.put(keyNames[x], new Integer(skillLevel));
                    change = true;
                }
            }
        }

        if (change) {
            skillListPane.removeAll();
            skillListPane.setLayout(new GridLayout(skillList.size(), 2));

            for (int x=0; x < keyNames.length; x++) {
                int skillLevel = skillManager.getSkillLevel(keyNames[x]);
                if (skillLevel > 0) {

                    // Display skill name
                    JLabel skillName = new JLabel(keyNames[x] + ":", JLabel.LEFT);
                    skillName.setVerticalAlignment(JLabel.TOP);
                    skillListPane.add(skillName);

                    // Display skill value
                    JLabel skillValue = new JLabel("" + skillLevel, JLabel.RIGHT);
                    skillValue.setVerticalAlignment(JLabel.TOP);
                    skillListPane.add(skillValue);
                }
            }
            validate();
        }
    }

    /** Update task info */
    private void updateTask() {

        TaskManager taskManager = person.getMind().getTaskManager();
        Mind mind = person.getMind();

        // Update mission
        if (mind.hasActiveMission()) missionLabel.setText("Mission: " + mind.getMission().getName());
        else missionLabel.setText("Mission: None");

        // Update mission phase
        if (mind.hasActiveMission()) missionPhaseLabel.setText("Mission Phase: " + mind.getMission().getPhase());
        else missionPhaseLabel.setText("Mission Phase:");

	    // Update task description
        String cacheDescription = "None";
        if ((taskManager != null) && taskManager.hasTask()) {
            cacheDescription = taskManager.getTaskDescription();
        }
        if (!cacheDescription.equals(taskDescription.getText())) taskDescription.setText("Task: " + cacheDescription);

        // Update task phase
        String cachePhase = "";
        if ((taskManager != null) && taskManager.hasTask()) {
            String phase = taskManager.getPhase();
            if ((phase != null) && !phase.equals("")) cachePhase = phase;
        }
        if (!cachePhase.equals(taskPhase.getText())) taskPhase.setText("Task Phase: " + cachePhase);
    }

    /** Update condition info */
    private void updateCondition() {

        PhysicalCondition condition = person.getPhysicalCondition();
        if (condition.getDeathDetails() == null) {
            // Update fatigue label
            if (fatigue != roundOneDecimal(condition.getFatigue())) {
                fatigue = roundOneDecimal(condition.getFatigue());
                fatigueLabel.setText("" + fatigue + " millisols");
            }

            // Update hunger label
            if (hunger != roundOneDecimal(condition.getHunger())) {
                hunger = roundOneDecimal(condition.getHunger());
                hungerLabel.setText("" + hunger + " millisols");
            }

            double performance = roundOneDecimal(person.getPerformanceRating() * 100D);
            performanceLabel.setText("" + performance + " %");

            // Update complaint list
            boolean match = false;

            // Remove missing conditions first
            Collection currentProblems = condition.getProblems();
            int i = 0;
            while(i < problemListModel.getSize()) {
                if (!currentProblems.contains(problemListModel.elementAt(i))) {
                    problemListModel.remove(i);
                }
                else {
                    i++;
                }
            }

            // Add new one in
	        Iterator iter = currentProblems.iterator();
	        while (iter.hasNext()) {
                Object problem = iter.next();
                if (!problemListModel.contains(problem)) {
                    problemListModel.addElement(problem);
                }
            }

            // This prevents the list from sizing strange due to having no contents
            if (problemListModel.getSize() == 0) problemListModel.addElement(" ");
            illnessList.validate();
        }
    }

    /** ActionListener method overriden */
    public void actionPerformed(ActionEvent event) {
        super.actionPerformed(event);

        Object button = event.getSource();
        Unit display = null;
        // If location button, open window for selected unit
        if (button == locationButton) {
            if (person.getSettlement() != null)
                display = person.getSettlement();
            else if (person.getVehicle() != null)
                display = person.getVehicle();
        }
        else if (button == deathLocationButton) {
            display = person.getPhysicalCondition().getDeathDetails().getLocation();
        }

        // If center map button, center map and globe on unit
        else if (button == deathCenterMapButton) {
            DeathInfo info = person.getPhysicalCondition().getDeathDetails();
            parentDesktop.centerMapGlobe(info.getLocation().getCoordinates());
        }
        // If center map button, center map and globe on unit
        else if (button == deathPositionMapButton) {
            DeathInfo info = person.getPhysicalCondition().getDeathDetails();
            parentDesktop.centerMapGlobe(info.getPosition());
        }
        if (display != null) {
            parentDesktop.openUnitWindow(proxyManager.getUnitUIProxy(display));
        }
    }

    /** Set window size
     *  @return the window's size
     */
    protected Dimension setWindowSize() { return new Dimension(300, 375); }

    /** Prepare components */
    protected void setupComponents() {

        super.setupComponents();

        // Initialize person
        person = (Person) parentUnit;

        // Prepare tab pane
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Task", setupTaskPane());
        tabPane.addTab("Location", setupLocationPane());
        tabPane.addTab("Condition", setupConditionPane());
        tabPane.addTab("Attributes", setupAttributePane());
        tabPane.addTab("Skills", setupSkillPane());
        inventoryPane = new InventoryPanel(person.getInventory());
        tabPane.addTab("Inventory", inventoryPane);
        mainPane.add(tabPane, "Center");
    }

    /** Set up task panel
     *  @return the task pane
     */
    protected JPanel setupTaskPane() {

        // Prepare Task pane
        JPanel taskPane = new JPanel(new BorderLayout());
        taskPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare task label pane
        JPanel taskLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        taskPane.add(taskLabelPane, "North");

        // Prepare task label
        JLabel taskLabel = new JLabel("Task", JLabel.CENTER);
        taskLabelPane.add(taskLabel);

        // Use person's task manager.
        TaskManager taskManager = person.getMind().getTaskManager();
        Mind mind = person.getMind();

        // Prepare task description pane
        JPanel taskDescriptionPane = new JPanel(new GridLayout(5, 1));
        JPanel taskDescriptionTopPane = new JPanel(new BorderLayout());
        taskDescriptionTopPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        taskDescriptionTopPane.add(taskDescriptionPane, "North");
        taskPane.add(new JScrollPane(taskDescriptionTopPane), "Center");

        JPanel missionNamePanel = new JPanel(new BorderLayout());
	taskDescriptionPane.add(missionNamePanel);

        // Display current mission.
        // Display "Mission: None" if person currently has no task.
        missionLabel = new JLabel("Mission: None", JLabel.LEFT);
        if (mind.hasActiveMission()) missionLabel.setText("Mission: " + mind.getMission().getName());
        missionNamePanel.add(missionLabel, "West");

	// Add monitor button.
	JButton monitorButton = new JButton(ImageLoader.getIcon("Monitor"));
	monitorButton.setMargin(new Insets(1, 1, 1, 1));
	monitorButton.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
		    Mind mind = person.getMind();
                    if (mind.hasActiveMission())
	                parentDesktop.addModel(new PersonTableModel(mind.getMission()));
	        }
	    });
	JPanel monitorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
	monitorPanel.add(monitorButton);
	missionNamePanel.add(monitorPanel);

        // Display current mission phase.
        // Display "Mission Phase:" if person doesn't have an active mission phase.
        missionPhaseLabel = new JLabel("Mission Phase:", JLabel.LEFT);
        if (mind.hasActiveMission()) missionPhaseLabel.setText("Mission Phase: " + mind.getMission().getPhase());
        taskDescriptionPane.add(missionPhaseLabel);

        // Add spacer label
        JLabel spacerLabel = new JLabel("  ", JLabel.LEFT);
        taskDescriptionPane.add(spacerLabel);

        // Display description of current task.
        // Display 'None' if person is currently doing nothing.
        taskDescription = new JLabel("Task: None", JLabel.LEFT);
        if ((taskManager != null) && taskManager.hasTask()) {
            taskDescription.setText("Task: " + taskManager.getTaskDescription());
        }
        taskDescriptionPane.add(taskDescription);

        // Display name of current phase.
        // Display "Task Phase:" if current task has no current phase.
	taskPhase = new JLabel("Task Phase:", JLabel.LEFT);
	if ((taskManager != null) && taskManager.hasTask()) {
            taskPhase.setText("Task Phase: " + taskManager.getPhase());
	}
	taskDescriptionPane.add(taskPhase);

	// Return skill panel
	return taskPane;
    }

    /**
     * Prepare the pane about Death.
     */
    private JPanel setupDeathPane(DeathInfo details) {
	    // Prepare death pane
        JPanel deathContainer = new JPanel(new BorderLayout());
        deathContainer.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        JPanel deathPane = new JPanel(new GridLayout(6,1,0,0));
        deathContainer.add(deathPane, "North");

	    // Prepare location sub pane
        deathPane.add(new JLabel("Person is dead", JLabel.LEFT));

        // Illness pane
        deathPane.add(new JLabel("Cause of Death: " +
                                details.getIllness().getName(), JLabel.LEFT));

        // Time pane
        deathPane.add(new JLabel("Time of Death: " +
                            details.getTime().getTimeStamp(), JLabel.LEFT));

        // Prepare location label pane
	    JPanel locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    deathPane.add(locationLabelPane);

        // Prepare center map button
	    deathCenterMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
	    deathCenterMapButton.setMargin(new Insets(1, 1, 1, 1));
	    deathCenterMapButton.addActionListener(this);
	    locationLabelPane.add(deathCenterMapButton);

	    // Prepare location label
	    locationLabelPane.add(new JLabel("Place Of Death: ", JLabel.LEFT));
        Unit locationUnit = details.getLocation();
        if (locationUnit != null) {
            // Prepare location button
	        deathLocationButton = new JButton();
	        deathLocationButton.setMargin(new Insets(1, 1, 1, 1));
            deathLocationButton.setText(details.getLocation().getName());
	        deathLocationButton.addActionListener(this);
	        locationLabelPane.add(deathLocationButton);
        }
        else {
            locationLabelPane.add(new JLabel("EVA"));
        }

        // Prepare position label pane
	    JPanel positionLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    deathPane.add(positionLabelPane);

        // Prepare center map button
	    deathPositionMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
	    deathPositionMapButton.setMargin(new Insets(1, 1, 1, 1));
	    deathPositionMapButton.addActionListener(this);
	    positionLabelPane.add(deathPositionMapButton);
        positionLabelPane.add(new JLabel("Position of Death:"));

	    // Prepare location coordinates pane
	    JPanel positionCoordsPane = new JPanel(new GridLayout(1, 2,  0, 0));
	    deathPane.add(positionCoordsPane);

	    // Prepare latitude label
	    Coordinates coords = details.getPosition();
	    positionCoordsPane.add(new JLabel("  Latitude: " +
                            coords.getFormattedLatitudeString(), JLabel.LEFT));
	    positionCoordsPane.add(new JLabel("Longtitude: " +
                            coords.getFormattedLongitudeString(), JLabel.LEFT));

	    // Return location panel
	    return deathContainer;
    }

    /** Set up location panel
     *  @return location pane
     */
    protected JPanel setupLocationPane() {

	// Prepare location pane
	JPanel locationPane = new JPanel(new BorderLayout());
	locationPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

	// Prepare location sub pane
	JPanel locationSubPane = new JPanel(new GridLayout(2, 1));
	locationPane.add(locationSubPane, "North");

        // Prepare location label pane
	locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
	locationSubPane.add(locationLabelPane);

	// Prepare center map button
	centerMapButton = new JButton(ImageLoader.getIcon("CenterMap"));
	centerMapButton.setMargin(new Insets(1, 1, 1, 1));
	centerMapButton.addActionListener(this);
	locationLabelPane.add(centerMapButton);

	// Prepare location label
	JLabel locationLabel = new JLabel("Location: ", JLabel.LEFT);
	locationLabelPane.add(locationLabel);

        // Prepare outside label
        outsideLabel = new JLabel("", JLabel.LEFT);
	boolean showOutsideLabel = false;
	if (person.getLocationSituation().equals(Person.OUTSIDE)) {
            outsideLabel.setText("Outside");
	    showOutsideLabel = true;
	}
	else if (person.getLocationSituation().equals(Person.BURIED)) {
	    outsideLabel.setText("Buried Outside");
	    showOutsideLabel = true;
	}
        if (showOutsideLabel) locationLabelPane.add(outsideLabel);
	
	// Prepare location button
	locationButton = new JButton();
	locationButton.setMargin(new Insets(1, 1, 1, 1));
	boolean showButton = false;
	if (person.getLocationSituation().equals(Person.INSETTLEMENT)) {
            locationButton.setText(person.getSettlement().getName());
	    showButton = true;
	}
	else if (person.getLocationSituation().equals(Person.INVEHICLE)) { 
            locationButton.setText(person.getVehicle().getName());
	    showButton = true;
	}
	locationButton.addActionListener(this);
	if (showButton) locationLabelPane.add(locationButton);

	// Prepare location coordinates pane
	JPanel locationCoordsPane = new JPanel(new GridLayout(1, 2,  0, 0));
	locationSubPane.add(locationCoordsPane);

	// Prepare latitude label
	latitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
	locationCoordsPane.add(latitudeLabel);

	// Prepare longitude label
	longitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
	locationCoordsPane.add(longitudeLabel);

	// Return location panel
	return locationPane;
    }

    /** Set up condition panel
     *  @return condition pane
     */
    protected JPanel setupConditionPane() {

        PhysicalCondition condition = person.getPhysicalCondition();
        DeathInfo death = condition.getDeathDetails();
        if (death != null) {
            return setupDeathPane(death);
        }

        // Prepare condition pane
        JPanel conditionPane = new JPanel(new BorderLayout());
        conditionPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

        // Prepare condition label pane
        JPanel conditionLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
        conditionPane.add(conditionLabelPane, "North");

        // Prepare condition label
        JLabel conditionLabel = new JLabel("Condition", JLabel.CENTER);
        conditionLabelPane.add(conditionLabel);

        // Prepare condition content pane
        JPanel conditionContentPane = new JPanel(new BorderLayout());
        conditionContentPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        conditionPane.add(conditionContentPane, "Center");

        // Prepare condition list pane
        JPanel conditionListPane = new JPanel(new GridLayout(4, 2));
        conditionContentPane.add(conditionListPane, "North");

        // Prepare fatigue name label
        JLabel fatigueNameLabel = new JLabel("Fatigue", JLabel.LEFT);
        conditionListPane.add(fatigueNameLabel);

        // Prepare fatigue label
        fatigueLabel = new JLabel("" + roundOneDecimal(condition.getFatigue()) + " millisols", JLabel.RIGHT);
        conditionListPane.add(fatigueLabel);

        // Prepare hunger name label
        JLabel hungerNameLabel = new JLabel("Hunger", JLabel.LEFT);
        conditionListPane.add(hungerNameLabel);

        // Prepare hunger label
        hungerLabel = new JLabel("" + roundOneDecimal(condition.getHunger()) + " millisols", JLabel.RIGHT);
        conditionListPane.add(hungerLabel);

        // Prepare performance rating label
        JLabel performanceNameLabel = new JLabel("Performance", JLabel.LEFT);
        conditionListPane.add(performanceNameLabel);

        // Performance rating value
        double performance = roundOneDecimal(person.getPerformanceRating() * 100D);
        performanceLabel = new JLabel("" + performance + " %", JLabel.RIGHT);
        conditionListPane.add(performanceLabel);

        // Prepare problem list
        problemListModel = new DefaultListModel();

	    Iterator i = condition.getProblems().iterator();
	    while (i.hasNext()) {
            problemListModel.addElement(i.next());
        }

        // This prevents the list from sizing strange due to having no contents
        if (condition.getProblems().size() == 0) problemListModel.addElement(" ");

        illnessList = new JList(problemListModel);
        illnessList.setVisibleRowCount(4);
        illnessList.setPreferredSize(
                new Dimension(250, (int) illnessList.getPreferredSize().getHeight()));
        JScrollPane problemScroll = new JScrollPane(illnessList);
        JPanel illnessPane = new JPanel(new BorderLayout());
        illnessPane.setBorder(new EtchedBorder());
        illnessPane.add(problemScroll, "Center");
        JLabel illnessLabel = new JLabel("Illnesses", JLabel.CENTER);
        illnessPane.add(illnessLabel, "North");

	conditionPane.add(illnessPane, "South");

        // Return condition panel
        return conditionPane;
    }

    /** Set up attribute panel
     *  @return attribute pane
     */
    protected JPanel setupAttributePane() {

        // Prepare attribute pane
	JPanel attributePane = new JPanel(new BorderLayout());
	attributePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

	// Prepare attribute label pane
	JPanel attributeLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	attributePane.add(attributeLabelPane, "North");

	// Prepare attribute label
	JLabel attributeLabel = new JLabel("Natural Attributes", JLabel.CENTER);
	attributeLabelPane.add(attributeLabel);

	// Use person's natural attribute manager.
	NaturalAttributeManager attributeManager = person.getNaturalAttributeManager();

	// Prepare attribute list pane
	JPanel attributeListPane = new JPanel(new GridLayout(attributeManager.getAttributeNum(), 2));
	attributeListPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	attributePane.add(new JScrollPane(attributeListPane), "Center");

	// For each natural attribute, display the name and its value.
	String[] keyNames = attributeManager.getKeys();
	for (int x=0; x < keyNames.length; x++) {

	    // Display attribute name
	    JLabel attributeName = new JLabel(keyNames[x] + ":", JLabel.LEFT);
	    attributeListPane.add(attributeName);

	    // Display attribute value
    	    JLabel attributeValue = new JLabel("" + attributeManager.getAttribute(keyNames[x]), JLabel.RIGHT);
	    attributeListPane.add(attributeValue);
	}

	// Return attribute panel
	return attributePane;
    }

    /** Set up skill panel
     *  @return the skill pane
     */
    protected JPanel setupSkillPane() {

	// Prepare skill pane
	JPanel skillPane = new JPanel(new BorderLayout());
	skillPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

	// Prepare skill label pane
	JPanel skillLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
	skillPane.add(skillLabelPane, "North");

	// Prepare skill label
	JLabel skillLabel = new JLabel("Skills", JLabel.CENTER);
	skillLabelPane.add(skillLabel);

	// Populate skill list
	SkillManager skillManager = person.getSkillManager();
	String[] keyNames = skillManager.getKeys();
	skillList = new Hashtable();
	for (int x=0; x < keyNames.length; x++) {
	    int skillLevel = skillManager.getSkillLevel(keyNames[x]);
	    if (skillLevel > 0) skillList.put(keyNames[x], new Integer(skillLevel));
	}

	// Prepare skill list pane
	JPanel skillListTopPane = new JPanel(new BorderLayout());
	skillListTopPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
	skillPane.add(new JScrollPane(skillListTopPane), "Center");
	skillListPane = new JPanel(new GridLayout(skillList.size(), 2));
	skillListTopPane.add(skillListPane, "North");

	// For each skill, display the name and its value.
	for (int x=0; x < keyNames.length; x++) {
	    if (skillList.containsKey(keyNames[x])) {

	        // Display skill name
		JLabel skillName = new JLabel(keyNames[x] + ":", JLabel.LEFT);
		skillName.setVerticalAlignment(JLabel.TOP);
		skillListPane.add(skillName);

		// Display skill value
		JLabel skillValue = new JLabel("" + skillList.get(keyNames[x]), JLabel.RIGHT);
		skillValue.setVerticalAlignment(JLabel.TOP);
		skillListPane.add(skillValue);
	    }
	}

	// Return skill panel
	return skillPane;
    }
}
