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
	
	private static final Font SERIF_BOLD_14 = new Font("Serif", Font.BOLD, 14);
	private static final Font SERIF_PLAIN_14 = new Font("Serif", Font.PLAIN, 14);
	private static final Font SERIF_PLAIN_12 = new Font("Serif", Font.PLAIN, 12);
	private static final Font MONOSPACED_PLAIN_12 = new Font("Monospaced", Font.PLAIN, 12);
	
	/** Is UI constructed. */
	private boolean uiDone = false;
	
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
		generalLabel.setFont(SERIF_BOLD_14);
		generalLabelPanel.add(generalLabel);

		// Prepare spring layout info panel.
		JPanel infoPanel = new JPanel(new SpringLayout());//GridLayout(7, 2, 0, 0));
//		infoPanel.setBorder(new MarsPanelBorder());
		centerContentPanel.add(infoPanel, BorderLayout.NORTH);

		// 1. Prepare gender name label
		JLabel genderNameLabel = new JLabel(Msg.getString("TabPanelGeneral.gender"), JLabel.RIGHT); //$NON-NLS-1$
//		genderNameLabel.setSize(5, 2);
		infoPanel.add(genderNameLabel);

		// Prepare gender textfield
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

		birthDate = person.getBirthDate();
		
		// Prepare birthdate and age textfield
		String birthdate = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate,
			Integer.toString(person.getAge())); //$NON-NLS-1$

		birthDateTF = new JTextField(birthdate);
		birthDateTF.setEditable(false);
		birthDateTF.setColumns(12);
		infoPanel.add(birthDateTF);

		// 3. Prepare birth location name label
		JLabel birthLocationNameLabel = new JLabel(Msg.getString("TabPanelGeneral.birthLocation"), JLabel.RIGHT); //$NON-NLS-1$
		birthLocationNameLabel.setSize(5, 2);
		infoPanel.add(birthLocationNameLabel);

		// Prepare birth location textfield
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

		// Prepare birth location textfield
		String country = person.getCountry();
		JTextField countryTF = new JTextField(Conversion.capitalize(country));
		countryTF.setEditable(false);
		countryTF.setColumns(15);
		infoPanel.add(countryTF);

		// 5. Prepare weight name label
		JLabel weightNameLabel = new JLabel(Msg.getString("TabPanelGeneral.weight"), JLabel.RIGHT); //$NON-NLS-1$
		weightNameLabel.setSize(5, 2);
		infoPanel.add(weightNameLabel);

		// Prepare weight textfield
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

		// Prepare height textfield
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

		// Prepare loading cap label
		JLabel loadCapLabel = new JLabel(Msg.getString("TabPanelGeneral.loadCap"), JLabel.RIGHT); //$NON-NLS-1$
		loadCapLabel.setSize(5, 2);
		infoPanel.add(loadCapLabel);

		// Prepare loading textfield
		double load = person.getCarryingCapacity();
//		System.out.println(person.getName() + " can carry " + load + " kg.");
		JTextField loadCapTF = new JTextField(Math.round(load*10.0)/10.0 + " kg");
		loadCapTF.setEditable(false);
		loadCapTF.setColumns(10);
		infoPanel.add(loadCapTF);
		
		// Use spring panel layout.
		SpringUtilities.makeCompactGrid(infoPanel,
		                                8, 2, //rows, cols
		                                50, 10,        //initX, initY
		                                10, 3);       //xPad, yPad
		
		MBTIPersonality p = person.getMind().getMBTI();

		// Create the text area for displaying the MBTI scores
		createMBTI(p);
		// Create the text area for displaying the Big Five scores
		createBigFive();

		
	}
	
	
	/**
	 * Creates the MBTI text area
	 * 
	 * @param p an instance of MBTIPersonality
	 */
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
		
		centerContentPanel.add(listPanel, BorderLayout.CENTER);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, " MBTI Scores",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				SERIF_PLAIN_12, Color.darkGray);
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
		ta.setFont(MONOSPACED_PLAIN_12);
		ta.setSelectedTextColor(Color.ORANGE.darker());
		ta.setColumns(14);
//		specialtyTA.setSize(100, 60);
		ta.setBorder(new MarsPanelBorder());
		
		WebPanel listPanel = new WebPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.setToolTipText(Msg.getString("TabPanelGeneral.bigFive.label"));//$NON-NLS-1$
		listPanel.setSize(110, 150);
		listPanel.add(ta);

		centerContentPanel.add(listPanel, BorderLayout.SOUTH);
		
		TitledBorder titledBorder = BorderFactory.createTitledBorder(null, " Big Five Scores",
				TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
				SERIF_PLAIN_12, Color.darkGray);
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
		
		// Update the age
		String birthdate = Msg.getString(
			TAB_BIRTH_DATE_AGE,
			birthDate,
			Integer.toString(person.getAge())); //$NON-NLS-1$
				
		birthDateTF.setText(birthdate); 
	}
}
