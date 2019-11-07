/**
1
 * Mars Simulation Project
 * TabPanelGeneral.java
 * @version 3.1.0 2017-01-21
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.person;

import java.awt.BorderLayout;
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

import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextArea;

/**
 * The TabPanelGeneral is a tab panel for general information about a person.
 */
@SuppressWarnings("serial")
public class TabPanelGeneral extends TabPanel {

	/** Is UI constructed. */
	private boolean uiDone = false;
	
	/** The Person instance. */
	private Person person = null;
	
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
	
	public boolean isUIDone() {
		return uiDone;
	}
	
	public void initializeUI() {
		uiDone = true;
		// Create general panel.
		JPanel generalLabelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		topContentPanel.add(generalLabelPanel);

		// Prepare general label
		JLabel generalLabel = new JLabel(Msg.getString("TabPanelGeneral.label"), JLabel.CENTER); //$NON-NLS-1$
		generalLabel.setFont(new Font("Serif", Font.BOLD, 16));
		generalLabelPanel.add(generalLabel);

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());//GridLayout(7, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// 1. Prepare gender name label
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

		// 2. Prepare birthdate and age name label
		JLabel birthNameLabel = new JLabel(Msg.getString("TabPanelGeneral.birthDate"), JLabel.RIGHT); //$NON-NLS-1$
		birthNameLabel.setSize(5, 2);
		infoPanel.add(birthNameLabel);

		// Prepare birthdate and age label
		String birthdate = Msg.getString(
			"TabPanelGeneral.birthDateAndAge",
			person.getBirthDate(),
			Integer.toString(person.updateAge())
		); //$NON-NLS-1$

		JTextField birthDateTF = new JTextField(birthdate);
		birthDateTF.setEditable(false);
		birthDateTF.setColumns(12);
		infoPanel.add(birthDateTF);

		// 3. Prepare birth location name label
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

		// 4. Prepare country name label
		JLabel countryNameLabel = new JLabel(Msg.getString("TabPanelGeneral.country"), JLabel.RIGHT); //$NON-NLS-1$
		countryNameLabel.setSize(5, 2);
		infoPanel.add(countryNameLabel);

		// Prepare birth location label
		String country = person.getCountry();
		JTextField countryTF = new JTextField(Conversion.capitalize(country));
		countryTF.setEditable(false);
		countryTF.setColumns(15);
		infoPanel.add(countryTF);

		// 5. Prepare weight name label
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

		// 6. Prepare height name label
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

		// 7. Prepare BMI name label
		JLabel BMINameLabel = new JLabel(Msg.getString("TabPanelGeneral.bmi"), JLabel.RIGHT); //$NON-NLS-1$
		BMINameLabel.setSize(5, 2);
		infoPanel.add(BMINameLabel);

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

		JTextField BMITF = new JTextField(Msg.getString("TabPanelGeneral.bmiValue", //$NON-NLS-1$
				Math.round(BMI*100.0)/100.0,	weightClass));
		BMITF.setEditable(false);
		BMITF.setColumns(12);
		infoPanel.add(BMITF);

		// 8. Prepare MBTI label
		JLabel mbtiLabel = new JLabel(Msg.getString("TabPanelGeneral.mbti"), JLabel.RIGHT); //$NON-NLS-1$
		mbtiLabel.setSize(5, 2);
		infoPanel.add(mbtiLabel);
		mbtiLabel.setToolTipText(Msg.getString("TabPanelGeneral.mbti.label"));//$NON-NLS-1$
		
		// Prepare height label
		MBTIPersonality p = person.getMind().getMBTI();
		String mbtiType = p.getTypeString();
		JTextField mbtiTF = new JTextField(mbtiType);
		mbtiTF.setEditable(false);
		mbtiTF.setColumns(12);
		infoPanel.add(mbtiTF);
		
		// Create the text area for displaying the MBTI scores
		createMBTI(p);
		// Create the text area for displaying the Big Five scores
		createBigFive();
		
		//Lay out the spring panel.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                8, 2, //rows, cols
		                                50, 10,        //initX, initY
		                                10, 5);       //xPad, yPad
		
	}
	
	
	public void createMBTI(MBTIPersonality p) {
		
		int ie = p.getIntrovertExtrovertScore();
		int ns = p.getScores().get(1);
		int ft = p.getScores().get(2);
		int jp = p.getScores().get(3);
		
//		StringBuffer sb = new StringBuffer();
//		String ieStr = "";
//		String nsStr = "";
//		String ftStr = "";
//		String jpStr = "";
//		String tab = "&nbsp;&nbsp;&nbsp;&nbsp;";
//		String thinsp = "&thinsp;";
		
		String[] types = new String[4];
		int[] scores = new int[4];
		
//		if (ie < 51) {
//			ieStr = thinsp + "Introvert : " + ie + "<br>" ;
//			
//			types[0] = ieStr;
//		}
//		else
//			ieStr = thinsp + "Extrovert : " + (ie-50) + "<br>" ;
//		
//		if (ns < 51)
//			nsStr = thinsp + thinsp + "Intuitive : " + ns + "<br>" ;
//		else
//			nsStr = thinsp + thinsp + thinsp + "Sensing : " + (ns-50) + "<br>" ;
//		
//		if (ft < 51)
//			ftStr =thinsp + thinsp + thinsp + thinsp + "Feeling : " + ft + "<br>" ;
//		else
//			ftStr = thinsp + thinsp + "Thinking : " + (ft-50) + "<br>" ;
//
//		
//		if (jp < 51)
//			jpStr = thinsp + thinsp + thinsp + "Judging : " + jp + "<br>" ;
//		else
//			jpStr = "Perceiving : " + (jp-50) + "<br>" ;
//		
//		String notestr = "<br> Note : intensity range<br>" + thinsp + thinsp + thinsp + thinsp + "from 1 to 50<br>";
//		
//		sb.append("<html>").append(ieStr).append(nsStr).append(ftStr).append(jpStr).append(notestr).append("</html>");
//		
////		&nbsp; - non-breakable space
////		&ensp; - en space
////		&emsp; - em space
////		&thinsp; - thin space
//		
//		mbtiTF.setToolTipText(sb.toString());
		
		// Prepare MBTI text area
		WebTextArea ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ta.setColumns(13);
//		specialtyTA.setSize(100, 60);
		ta.setBorder(new MarsPanelBorder());
		
		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setSize(110, 130);
		listPanel.add(ta);

		centerContentPanel.add(listPanel, BorderLayout.CENTER);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Personality scores based on MBTI",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new Font("Serif", Font.PLAIN, 12), java.awt.Color.darkGray);
		listPanel.setBorder(titledBorder);
		
		if (ie < 51) {
			types[0] = "Introvert (I)";
			scores[0] = ie;
		}
		else {
			types[0] = "Extrovert (E)";
			scores[0] = ie - 50;
		}
		
		if (ns < 51) {
			types[1] = "Intuitive (N)";
			scores[1] = ns;
		}
		else {
			types[1] = "Sensing (S)";
			scores[1] = ns - 50;
		}
		
		if (ft < 51) {
			types[2] = "Feeling (F)";
			scores[2] = ft;
		}
		else {
			types[2] = "Thinking (T)";
			scores[2] = ft - 50;
		}
		
		if (jp < 51) {
			types[3] = "Judging (J)";
			scores[3] = jp;
		}
		else {
			types[3] = "Perceiving (P)";
			scores[3] = jp - 50;
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

	public void createBigFive() {
		PersonalityTraitManager p = person.getMind().getTraitManager();
		
//		int o = p.getPersonalityTrait(PersonalityTraitType.OPENNESS);
//		int c = p.getPersonalityTrait(PersonalityTraitType.CONSCIENTIOUSNESS);
//		int e = p.getPersonalityTrait(PersonalityTraitType.EXTRAVERSION); //getIntrovertExtrovertScore();
//		int a = p.getPersonalityTrait(PersonalityTraitType.AGREEABLENESS);
//		int n = p.getPersonalityTrait(PersonalityTraitType.NEUROTICISM);
		
		String[] types = new String[5];
		int[] scores = new int[5];
		
    	for (PersonalityTraitType t : PersonalityTraitType.values()) {
    		types[t.ordinal()] = t.getName();
    		scores[t.ordinal()] = p.getPersonalityTrait(t);
    	}
    	
		// Prepare MBTI text area
		WebTextArea ta = new WebTextArea();
		ta.setEditable(false);
		ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
		ta.setColumns(14);
//		specialtyTA.setSize(100, 60);
		ta.setBorder(new MarsPanelBorder());
		
		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setSize(110, 150);
		listPanel.add(ta);

		centerContentPanel.add(listPanel, BorderLayout.SOUTH);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, "Personality scores based on Big Five",
				javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION,
				new Font("Serif", Font.PLAIN, 12), java.awt.Color.darkGray);
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
		if (!uiDone)
			initializeUI();
		// Person person = (Person) unit;
		// Fill in as we have more to update on this panel.
	}
}