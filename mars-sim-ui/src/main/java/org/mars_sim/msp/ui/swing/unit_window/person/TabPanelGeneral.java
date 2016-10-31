/**
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @version 3.07 2014-12-06

 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.PersonalityType;
import org.mars_sim.msp.ui.steelseries.gauges.Radial2Top;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.Conversion;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
public class TabPanelGeneral
extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

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

		Person person = (Person) unit;

		// Create general label panel.
		JPanel generalLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(generalLabelPanel);

		// Prepare general label
		JLabel generalLabel = new JLabel(Msg.getString("TabPanelGeneral.label"), JLabel.CENTER); //$NON-NLS-1$
		generalLabel.setFont(new Font("Serif", Font.BOLD, 16));
		generalLabelPanel.add(generalLabel);

		// Prepare info panel.
		JPanel infoPanel = new JPanel(new GridLayout(8, 2, 0, 0));
		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// Prepare gender name label
		JLabel genderNameLabel = new JLabel(Msg.getString("TabPanelGeneral.gender"), JLabel.RIGHT); //$NON-NLS-1$
		genderNameLabel.setSize(5, 2);
		infoPanel.add(genderNameLabel);

		// Prepare gender label
		String gender = person.getGender().getName();
		JTextField genderTF = new JTextField(Conversion.capitalize(gender));
		genderTF.setEditable(false);
		genderTF.setColumns(12);
		//JLabel genderLabel = new JLabel(gender, JLabel.RIGHT);
		infoPanel.add(genderTF);

		// Prepare birthdate and age name label
		JLabel birthNameLabel = new JLabel(Msg.getString("TabPanelGeneral.birthDate"), JLabel.RIGHT); //$NON-NLS-1$
		birthNameLabel.setSize(5, 2);
		infoPanel.add(birthNameLabel);

		// Prepare birthdate and age label
		String birthdate = Msg.getString(
			"TabPanelGeneral.birthDateAndAge",
			person.getBirthDate(),
			Integer.toString(person.getAge())
		); //$NON-NLS-1$
		//JLabel birthDateLabel = new JLabel(birthdate, JLabel.RIGHT);
		JTextField birthDateTF = new JTextField(birthdate);
		birthDateTF.setEditable(false);
		birthDateTF.setColumns(12);
		infoPanel.add(birthDateTF);

		// Prepare birth location name label
		JLabel birthLocationNameLabel = new JLabel(Msg.getString("TabPanelGeneral.birthLocation"), JLabel.RIGHT); //$NON-NLS-1$
		birthLocationNameLabel.setSize(5, 2);
		infoPanel.add(birthLocationNameLabel);

		// Prepare birth location label
		String birthLocation = person.getBirthplace();
		//JLabel birthLocationLabel = new JLabel(birthLocation, JLabel.RIGHT);
		JTextField birthLocationTF = new JTextField(Conversion.capitalize(birthLocation));
		birthLocationTF.setEditable(false);
		birthLocationTF.setColumns(12);
		infoPanel.add(birthLocationTF);

		// Prepare weight name label
		JLabel weightNameLabel = new JLabel(Msg.getString("TabPanelGeneral.weight"), JLabel.RIGHT); //$NON-NLS-1$
		weightNameLabel.setSize(5, 2);
		infoPanel.add(weightNameLabel);

		// Prepare weight label
		double baseMass = Math.round(person.getBaseMass()*10.0)/10.0;
		//JLabel weightLabel = new JLabel(Msg.getString("TabPanelGeneral.kilograms",baseMass), JLabel.RIGHT); //$NON-NLS-1$
		JTextField weightTF = new JTextField(Msg.getString("TabPanelGeneral.kilograms", baseMass));
		weightTF.setEditable(false);
		weightTF.setColumns(12);
		infoPanel.add(weightTF);

		// Prepare height name label
		JLabel heightNameLabel = new JLabel(Msg.getString("TabPanelGeneral.height"), JLabel.RIGHT); //$NON-NLS-1$
		heightNameLabel.setSize(5, 2);
		infoPanel.add(heightNameLabel);

		// Prepare height label
		double baseHeight = Math.round(person.getHeight()*10.0)/10.0;
		//JLabel heightLabel = new JLabel(Msg.getString("TabPanelGeneral.centimeters", baseHeight), JLabel.RIGHT); //$NON-NLS-1$
		JTextField heightTF = new JTextField(Msg.getString("TabPanelGeneral.centimeters", baseHeight));
		heightTF.setEditable(false);
		heightTF.setColumns(12);
		infoPanel.add(heightTF);

		// Prepare BMI name label
		JLabel BMINameLabel = new JLabel(Msg.getString("TabPanelGeneral.bmi"), JLabel.RIGHT); //$NON-NLS-1$
		BMINameLabel.setSize(5, 2);
		infoPanel.add(BMINameLabel);

		// Prepare BMI label
		double heightInCmSquared = (person.getHeight()/100D)*(person.getHeight()/100D);
		double BMI = (person.getBaseMass()/heightInCmSquared);
		// categorize according to general weight class
		String weightClass = Msg.getString("TabPanelGeneral.bmi.underweight"); //$NON-NLS-1$
		if (BMI > 18.5) {weightClass = Msg.getString("TabPanelGeneral.bmi.normal");} //$NON-NLS-1$
		if (BMI > 25) {weightClass = Msg.getString("TabPanelGeneral.bmi.overweight");} //$NON-NLS-1$
		if (BMI > 30) {weightClass = Msg.getString("TabPanelGeneral.bmi.obese");} //$NON-NLS-1$
		//JLabel BMILabel = new JLabel(Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
		//		Integer.toString((int)BMI),	weightClass), JLabel.RIGHT);
		JTextField BMITF = new JTextField(Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
				Integer.toString((int)BMI),	weightClass));
		BMITF.setEditable(false);
		BMITF.setColumns(12);
		infoPanel.add(BMITF);


		// Prepare personality name label
		JLabel personalityNameLabel = new JLabel(Msg.getString("TabPanelGeneral.personalityMBTI"), JLabel.RIGHT); //$NON-NLS-1$
		personalityNameLabel.setSize(5, 2);
		personalityNameLabel.setToolTipText("<html>Myers-Briggs Type Indicator (MBTI) <br> as a metric for personality type</html>");
		infoPanel.add(personalityNameLabel);

		// Prepare personality label
		String personality = person.getMind().getMBTIType().getTypeString();
		//JLabel personalityLabel = new JLabel(personality, JLabel.RIGHT);
		JTextField personalityTF = new JTextField(personality);
		personalityTF.setEditable(false);
		personalityTF.setColumns(12);
		
		String type1 = personality.substring(0,1);
		if (type1.equals("E"))
			type1 = "Extrovert (E)";
		else
			type1 = "Introvert (I)";

		String type2 = personality.substring(1,2);
		if (type2.equals("N"))
			type2 = "Intuitive (N)";
		else
			type2 = "Sensing (S)";

		String type3 = personality.substring(2,3);
		if (type3.equals("F"))
			type3 = "Feeler (F)";
		else
			type3 = "Thinker (T)";

		String type4 = personality.substring(3,4);
		if (type4.equals("J"))
			type4 = "Judger (J)";
		else
			type4 = "Perceiver (P)";

		personalityTF.setToolTipText("<html>" + type1 + " : " + "<br>" + type2 + "<br>" + type3 + "<br>" + type4 
				+ "<br>" + "Note: see the 4 scores below"+ "<br>" + "</html>");
		infoPanel.add(personalityTF);


		infoPanel.add(new JLabel(" "));
		
		// Prepare gauge panel.
		JPanel gaugePanel = new JPanel(new GridLayout(4, 1, 0, 0));
		//gaugePanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(gaugePanel, BorderLayout.CENTER);		

		Map<Integer, Integer> scores = person.getMind().getMBTIType().getScores();
		
		List<Radial2Top> radials = new ArrayList<Radial2Top>();
		for (int i=0; i< 4; i++) {
			Radial2Top r = new Radial2Top();
			radials.add(r);
			gaugePanel.add(r);
		}
		
		radials.get(0).setTitle("Introversion vs. Extravsersion");
		radials.get(0).setToolTipText("Introversion vs. Extravsersion");
		radials.get(0).setValue(scores.get(PersonalityType.INTROVERSION_EXTRAVERSION));

		radials.get(1).setTitle("Intuition vs. Sensation");
		radials.get(1).setToolTipText("Intuition vs. Sensation");
		radials.get(1).setValue(scores.get(PersonalityType.INTUITION_SENSATION));
		
		radials.get(2).setTitle("Feeling vs. Thinking");		
		radials.get(2).setToolTipText("Feeling vs. Thinking");
		radials.get(2).setValue(scores.get(PersonalityType.FEELING_THINKING));

		radials.get(3).setTitle("Judging vs. Perceiving");
		radials.get(3).setToolTipText("Judging vs. Perceiving");
		radials.get(3).setValue(scores.get(PersonalityType.JUDGING_PERCEIVING));

		
		for (Radial2Top r : radials) {
			r.setUnitString("");
			r.setLedBlinking(false);
			r.setMajorTickSpacing(20);
			r.setMinorTickmarkVisible(false);
			r.setMinValue(0);
			r.setMaxValue(100);
			r.setSize(new Dimension(350, 350));
			r.setMaximumSize(new Dimension(350, 350));
			r.setPreferredSize(new Dimension(350, 350));
			r.setVisible(true);
		}
	}

	/**
	 * Updates the info on this panel.
	 */
	@Override
	public void update() {
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}