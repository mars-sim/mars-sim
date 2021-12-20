/*
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @date 2021-09-20
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.TitledBorder;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.MBTIPersonality;
import org.mars_sim.msp.core.person.ai.PersonalityTraitManager;
import org.mars_sim.msp.core.person.ai.PersonalityTraitType;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.tool.SpringUtilities;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;
import com.alee.managers.style.StyleId;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	private static final String TAB_BIRTH_DATE_AGE = "TabPanelGeneral.birthDateAndAge";
	
	private static final Font SERIF_PLAIN_14 = new Font("Serif", Font.PLAIN, 14);
	private static final Font MONOSPACED_PLAIN_12 = new Font("Monospaced", Font.PLAIN, 12);

	
	/** The Person instance. */
	private Person person;
	
	private JTextField birthDateTF;
	
	private String birthDate;
	
	/**
	 * Constructor.
	 * @param unit the unit to display.
	 * @param desktop the main desktop.
	 */
	public TabPanelGeneral(Unit unit, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(
			Msg.getString("TabPanelGeneral.title"), //$NON-NLS-1$
			null,
			Msg.getString("TabPanelGeneral.tooltip"), //$NON-NLS-1$
			unit, desktop
		);

		person = (Person) unit;
	}
	
	@Override
	protected void buildUI(JPanel content) {

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());
		content.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender textfield
		String gender = person.getGender().getName();
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.gender"), Conversion.capitalize(gender), null);
		
		// Prepare birthdate and age textfield
		birthDate = person.getBirthDate();
		String birthTxt = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate,
			Integer.toString(person.getAge())); //$NON-NLS-1$
		birthDateTF = addTextField(infoPanel, Msg.getString("TabPanelGeneral.birthDate"), birthTxt, null);

		// Prepare birth location textfield
		String birthLocation = person.getBirthplace();
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.birthLocation"), Conversion.capitalize(birthLocation), null);		
		
		// Prepare birth location textfield
		String country = person.getCountry();
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.country"), Conversion.capitalize(country), null);

		// Prepare weight textfield
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.weight"),
				  							  DECIMAL_KG.format(person.getBaseMass()), null);
		
		// Prepare height name label
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.height"), 
					 DECIMAL_PLACES1.format(person.getHeight()) + " m", null);

		// Prepare BMI label
		double height = person.getHeight()/100D;
		double heightSquared = height*height;
		double BMI = person.getBaseMass()/heightSquared;
		// categorize according to general weight class
		String weightClass = "";
		if (BMI <= 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.underweight");} //$NON-NLS-1$
		if (BMI > 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.normal");} //$NON-NLS-1$
		if (BMI > 24.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.overweight");} //$NON-NLS-1$
		if (BMI > 29.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese1");} //$NON-NLS-1$
		if (BMI > 34.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese2");} //$NON-NLS-1$
		if (BMI > 39.99) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese3");} //$NON-NLS-1$

		String BMItext = Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
				Math.round(BMI*100.0)/100.0, weightClass);
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.bmi"), BMItext, null);
		
		// Prepare loading cap label
		addTextField(infoPanel, Msg.getString("TabPanelGeneral.loadCap"), DECIMAL_KG.format(person.getCarryingCapacity()), null);
		
		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                8, 2, //rows, cols
		                                50, 10,        //initX, initY
		                                XPAD_DEFAULT, YPAD_DEFAULT);       //xPad, yPad
		
		MBTIPersonality p = person.getMind().getMBTI();

		// Create the text area for displaying the MBTI scores
		createMBTI(p, content);
		// Create the text area for displaying the Big Five scores
		createBigFive(content);
	}
	
	
	/**
	 * Creates the MBTI text area
	 * 
	 * @param p an instance of MBTIPersonality
	 */
	private void createMBTI(MBTIPersonality p, JPanel content) {
		
		int ie = p.getIntrovertExtrovertScore();
		int ns = p.getScores().get(1);
		int ft = p.getScores().get(2);
		int jp = p.getScores().get(3);

		
		String[] types = new String[4];
		int[] scores = new int[4];
		
		// Prepare MBTI text area
		WebTextArea ta = new WebTextArea();
		String tip =  "<html>Introvert (I) / Extrovert  (E) : -50 to 0 / 0 to 50" 
					  + "<br>Intuitive (N) / Sensing    (S) : -50 to 0 / 0 to 50"
					  + "<br>  Feeling (F) / Thinking   (T) : -50 to 0 / 0 to 50"
					  + "<br>  Judging (J) / Perceiving (P) : -50 to 0 / 0 to 50</html>";
		ta.setToolTipText(tip);
		ta.setEditable(false);
		ta.setSelectedTextColor(Color.ORANGE.darker());
		ta.setFont(MONOSPACED_PLAIN_12);
		ta.setColumns(13);
//		specialtyTA.setSize(100, 60);
		ta.setBorder(new MarsPanelBorder());
		
		WebPanel mbtiPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
//		mbtiPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		mbtiPanel.add(ta);
		
		WebPanel listPanel = new WebPanel(new BorderLayout(1, 1));
		listPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		listPanel.setSize(110, 130);
		listPanel.add(mbtiPanel, BorderLayout.CENTER);
		
		String type = p.getTypeString();
		String descriptor = p.getDescriptor();
		WebLabel descriptorLabel = new WebLabel(StyleId.labelShadow, type + " : " + descriptor, JLabel.CENTER);
		descriptorLabel.setToolTipText(Msg.getString("TabPanelGeneral.mbti.descriptor.tip"));//$NON-NLS-1$
		descriptorLabel.setFont(SERIF_PLAIN_14);
		listPanel.add(descriptorLabel, BorderLayout.SOUTH);
		
		content.add(listPanel, BorderLayout.CENTER);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, " MBTI Scores",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				SUBTITLE_FONT, Color.darkGray);
		listPanel.setBorder(titledBorder);
		
		
		if (ie < 0) {
			types[0] = "Introvert (I)";
			scores[0] = ie;
		}
		else {
			types[0] = "Extrovert (E)";
			scores[0] = ie;
		}
		
		if (ns < 0) {
			types[1] = "Intuitive (N)";
			scores[1] = ns;
		}
		else {
			types[1] = "Sensing (S)";
			scores[1] = ns;
		}
		
		if (ft < 0) {
			types[2] = "Feeling (F)";
			scores[2] = ft;
		}
		else {
			types[2] = "Thinking (T)";
			scores[2] = ft;
		}
		
		if (jp < 0) {
			types[3] = "Judging (J)";
			scores[3] = jp;
		}
		else {
			types[3] = "Perceiving (P)";
			scores[3] = jp;
		}
	
		for (int i = 0; i < 4; i++) {
//			StringBuffer sb = new StringBuffer();
			String s = types[i];
			int size = 14 - s.length();
			while (size > 0) {
				ta.append(" ");
				size--;
			}
			ta.append(" " + s + " : " + scores[i] + "  ");
			if (i < 3)
				//if it's NOT the last one
				ta.append("\n");
		}
		
	}

	/**
	 * Create the text area for the Big Five
	 */
	private void createBigFive(JPanel content) {
		PersonalityTraitManager p = person.getMind().getTraitManager();
		
		String[] types = new String[5];
		int[] scores = new int[5];
		
    	for (PersonalityTraitType t : PersonalityTraitType.values()) {
    		types[t.ordinal()] = t.getName();
    		scores[t.ordinal()] = p.getPersonalityTrait(t);
    	}
    	
		// Prepare MBTI text area
		WebTextArea ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(MONOSPACED_PLAIN_12);
		ta.setSelectedTextColor(Color.ORANGE.darker());
		ta.setColumns(14);
//		specialtyTA.setSize(100, 60);
		ta.setBorder(new MarsPanelBorder());
		
		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setToolTipText(Msg.getString("TabPanelGeneral.bigFive.label"));//$NON-NLS-1$
		listPanel.setSize(110, 150);
		listPanel.add(ta);

		content.add(listPanel, BorderLayout.SOUTH);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, " Big Five Scores",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				SUBTITLE_FONT, Color.darkGray);
		listPanel.setBorder(titledBorder);
		
    
		for (int i = 0; i < 5; i++) {
//			StringBuffer sb = new StringBuffer();
			String s = types[i];
			int size = 18 - s.length();
			while (size > 0) {
				ta.append(" ");
				size--;
			}
			ta.append(" " + s + " : " + scores[i] + "  ");
			if (i < 4)
				//if it's NOT the last one
				ta.append("\n");
		}
		
	}
	
	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
		
		// Update the age
		String birthdate = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate,
			Integer.toString(person.getAge())); //$NON-NLS-1$
				
		birthDateTF.setText(birthdate); 
	}
}
