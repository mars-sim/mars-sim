/*
 * Mars Simulation Project
 * CrewEditor.java
 * @date 2022-06-15
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mars.sim.tools.util.RandomUtil;
import org.mars_sim.msp.core.configuration.UserConfigurableConfig;
import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Member;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.util.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;



/**
 * CrewEditor allows users to design the crew manifest for an initial settlement
 */
public class CrewEditor implements ActionListener {
	/**
	 * Represents a single Crew member in a dedicated panel.
	 */
	private class MemberPanel implements ItemListener, ActionListener {
		private JComboBox<String> countryCB;
		private JComboBox<String> sponsorCB;
		private JComboBox<String> jobCB;
		private List<JRadioButton> radioButtons;
		private Box displayPanel;
		private JTextField nametf;
		private JComboBox<String> genderCB;
		private JTextField agetf;

		/**
		 * Create a crew panel to house the attributes of a crewman
		 * 
		 * @return
		 */
		MemberPanel(int i, CrewEditor parent) {
			displayPanel = Box.createVerticalBox();

			// Name 
			nametf = new JTextField(15);
			nametf.setMargin(new Insets(3, 0, 3, 0));

			JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			namePanel.add(new JLabel("   Name : "));
			namePanel.add(nametf);
			displayPanel.add(namePanel);
			
			// Gender
			genderCB = new JComboBox<>(setUpGenderCBModel());
			genderCB.setToolTipText("Choose male or female");
			JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			genderPanel.add(new JLabel(" Gender : "));
			genderPanel.add(genderCB);
			displayPanel.add(genderPanel);	
			
			// Age
			agetf = new JTextField(5);
			JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			agePanel.add(new JLabel("      Age : "));
			agePanel.add(agetf);
			displayPanel.add(agePanel);
					
			// Personallity
			JPanel fullPane = new JPanel(new FlowLayout());
			fullPane.setLayout(new BoxLayout(fullPane, BoxLayout.Y_AXIS));
			fullPane.setPreferredSize(new Dimension(PANEL_WIDTH, 200));
			fullPane.setSize(PANEL_WIDTH, 200);
			fullPane.setMaximumSize(new Dimension(PANEL_WIDTH, 200));	
			radioButtons = new ArrayList<>();
			for (int row = 0; row < 4; row++) {
				JRadioButton ra = new JRadioButton(QUADRANT_A[row]);
				JRadioButton rb = new JRadioButton(QUADRANT_B[row]);
				radioButtons.add(ra);
				radioButtons.add(rb);
				
				// Use ButtonGroup to limit to choosing either ra or rb
				ButtonGroup bg1 = new ButtonGroup();
				bg1.add(ra);
				bg1.add(rb);
				
				JPanel quadPane = new JPanel(new GridLayout(1, 2));
				quadPane.setPreferredSize(new Dimension(PANEL_WIDTH/2, 60));
				quadPane.setBorder(BorderFactory.createTitledBorder(CATEGORY[row]));
				quadPane.add(ra);
				quadPane.add(rb);
				quadPane.setAlignmentX(Component.CENTER_ALIGNMENT);
				fullPane.add(quadPane);
			}
			displayPanel.add(fullPane);

			// Job
			jobCB = new JComboBox<>(setUpJobCBModel());
			jobCB.setToolTipText("Choose the job of this person");
			jobCB.setMaximumRowCount(8);
			displayPanel.add(jobCB);
			
			// Sponsor
			sponsorCB = new JComboBox<>(setUpSponsorCBModel());
			sponsorCB.setToolTipText("Choose the sponsor of this person");
			sponsorCB.setMaximumRowCount(8);
			displayPanel.add(sponsorCB);

			// Set up and add an item listener to the country combobox
			sponsorCB.addItemListener(this);		    
		    
			// Country
			countryCB = new JComboBox<>(setUpCountryCBModel());
			countryCB.setToolTipText("Choose the country of origin of this person");
			countryCB.setMaximumRowCount(8);
			displayPanel.add(countryCB);
			
			// Create remove button.
			JButton removeButton = new JButton(REMOVE_MEMBER);
			removeButton.addActionListener(this);
			displayPanel.add(removeButton);
			
			// Add the Crewman title border
			int num = i + 1;
			displayPanel.setBorder(BorderFactory.createTitledBorder("Crewman " + num + " "));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			removeMember(this);
		}
		
		/**
		 * Checks if the name textfields are valid
		 * 
		 * @param i
		 * @param goodToGo
		 * @return
		 */
		private boolean checkNameFields(Member m) {

			String nameStr = nametf.getText().trim();
			// Use isBlank() to check against invalid names
			if (!Conversion.isBlank(nameStr)
					&& nameStr.contains(" ")) {
				m.setName(nameStr);
				return true;
				
			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JOptionPane.showMessageDialog(f, 
								"Each Settler's name must include first and last name, separated by a whitespace",
								"Invalid Name Format",
								JOptionPane.ERROR_MESSAGE);
				
				// Disable Start;
				// event.consume();
				nametf.requestFocus();
				return false;
			}
		}
		
		/**
		 * Create a member from the values in the panel.
		 * @return Null if there is a problem
		 */
		public Member toMember() {
			Member m = new Member();
			if (!checkNameFields(m)) {
				return null;
			}
				
			String ageStr = agetf.getText();
			if (!isNumeric(ageStr)) {
				return null;
			}
			m.setAge(ageStr);
			
			GenderType gender;			
			if (genderCB.getSelectedItem().equals(MALE))
				gender = GenderType.MALE;
			else 
				gender = GenderType.FEMALE;

			m.setGender(gender);
			
			m.setMBTI(getSelectedPersonality());
	
			String sponsor = (String) sponsorCB.getSelectedItem();
			if ((sponsor != null) && !sponsor.equals(SETTLEMENT_SPONSOR)) {
				m.setSponsorCode(sponsor);
			}
			else {
				m.setSponsorCode(null);
			}
			
			m.setCountry((String) countryCB.getSelectedItem());
			m.setJob((String) jobCB.getSelectedItem());
		
			return m;
		}
			

		/**
		 * Get a person's personality type (MTBI) as shown in the radio buttons selected
		 * @return the MTBI string
		 */
		private String getSelectedPersonality() {
			StringBuilder type = new StringBuilder();		
			
			for (int num = 0; num < 8; num++) {
				JRadioButton b = radioButtons.get(num);
				String oneType = null;
				if (b.isSelected()) {
					oneType = convert2Type(num);
					type.append(oneType);
				}
			}			
			return type.toString();
		}
		
		/**
		 * Populate the panel with value of a Member
		 * @param m Member to display
		 */
		private void loadMember(Member m) {			
			nametf.setText(m.getName());
			genderCB.setSelectedItem((m.getGender() == GenderType.MALE ? MALE : FEMALE));
	        
			// Age
			int age = 0;
			String ageStr = m.getAge();
			if (ageStr == null)
				age = RandomUtil.getRandomInt(21, 65);
			else
				age = Integer.parseInt(ageStr);	
			agetf.setText(age + "");
			
			jobCB.getModel().setSelectedItem(m.getJob());
		
			String s = m.getSponsorCode(); 
			if (s != null) {
				sponsorCB.getModel().setSelectedItem(s);
			}
			else {
				sponsorCB.getModel().setSelectedItem(SETTLEMENT_SPONSOR);				
			}
			String country = m.getCountry();
			DefaultComboBoxModel<String> countryModel = (DefaultComboBoxModel<String>) countryCB.getModel();
			populateCountryCombo(m.getSponsorCode(), countryModel);
			countryModel.setSelectedItem(country);
					
			// Personality
			for (int row = 0; row < 4; row++) {

				JRadioButton ra = radioButtons.get(2 * row);
				JRadioButton rb = radioButtons.get(2 * row + 1);
						
				if (retrieveCrewMBTI(m, row))
					ra.setSelected(true);
				else
					rb.setSelected(true);
			}
		}
		
		// This method is called only if a new item has been selected.
		public void itemStateChanged(ItemEvent evt) {
		
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				// Item was just selected
		        JComboBox<String> m = sponsorCB;
		        String sponsor = (String) m.getSelectedItem();
		        
				// Get combo box model
		        JComboBox<String> combo = countryCB;
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        populateCountryCombo(sponsor, model);
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
				// Item is no longer selected
		        JComboBox<String> combo = countryCB;
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        // removing old data
		        model.removeAllElements();
				model.addElement(SELECT_SPONSOR);
			}
		}
	}
	
	/** default logger. */
	private static final Logger logger = Logger.getLogger(CrewEditor.class.getName());

	private static final String TITLE = "Crew Editor";
	private static final String SELECT_SPONSOR = "Select Sponsor";

	private static final String ADD_MEMBER = "Add Member";
	private static final String REMOVE_MEMBER = "Remove member";

	private static final String MALE = "Male";
	private static final String FEMALE = "Female";
		
	private static final int PANEL_WIDTH = 180;
	private static final int WIDTH = (int)(PANEL_WIDTH * 4);
	private static final int HEIGHT = 512;

	private static final String[] QUADRANT_A = {"<html><b>E</b>xtravert",
			"<html>I<b>N</b>tuition", "<html><b>F</b>eeling", "<html><b>J</b>udging"};
	private static final String[] QUADRANT_B = {"<html><b>I</b>ntrovert",
			"<html><b>S</b>ensing", "<html><b>T</b>hinking", "<html><b>P</b>erceiving"};
	private static final String[] CATEGORY = {"World",
			"Information", "Decision", "Structure"};

	private static final String SETTLEMENT_SPONSOR = "Settlement Sponsor";
	
	// Data members		
	private JDialog f;
	
	private JPanel mainPane;
	private JPanel scrollPane;
	
	private List<MemberPanel> crewPanels = new ArrayList<>();

	private SimulationConfigEditor simulationConfigEditor;

	private ReportingAuthorityFactory raFactory;

	private PersonConfig pc;

	
	/**
	 * Constructor.
	 * 
	 * @param config
	 *            SimulationConfig
	 * @param simulationConfigEditor
	 *            SimulationConfigEditor
	 */
	public CrewEditor(SimulationConfigEditor simulationConfigEditor,
					  UserConfigurableConfig<Crew> config,
					  ReportingAuthorityFactory raFactory,
					  PersonConfig pc) {
		
		this.simulationConfigEditor = simulationConfigEditor;
		this.raFactory = raFactory;
		this.pc = pc;
		
		createGUI(config);
	}

	/**
	 * Load a member. populate an existing MemberPanel or create a new one if
	 * none are spare.
	 * @param m
	 * @param i
	 */
	private void loadMember(Member m, int i) {
		MemberPanel mp = null;
		
		if (i < crewPanels.size()) {
			mp = crewPanels.get(i);
		}
		else {
			mp = new MemberPanel(i, this);
			crewPanels.add(mp);
			scrollPane.add(mp.displayPanel);
		}
		
		mp.loadMember(m);
	}
	
	private void removeMember(MemberPanel p) {
		crewPanels.remove(p);
		scrollPane.remove(p.displayPanel);
	}

	/**
	 * Creates the GUI
	 */
	private void createGUI(UserConfigurableConfig<Crew> crewConfig) {
	
		f = new JDialog(simulationConfigEditor.getFrame(), TITLE + " - Alpha Crew On-board", true); //new JFrame(TITLE + " - Alpha Crew On-board");
		f.setIconImage(MainWindow.getIconImage());
		f.setResizable(true);
		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				simulationConfigEditor.setCrewEditorOpen(false);
				f.dispose();
				destroy();
			}
		});

		// Create main panel.
		mainPane = new JPanel(new BorderLayout());
		mainPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		f.getContentPane().add(mainPane);
		
		// Create main panel.
		scrollPane = new JPanel();
		scrollPane.setLayout(new BoxLayout(scrollPane, BoxLayout.X_AXIS));
		scrollPane.setBorder(MainDesktopPane.newEmptyBorder());

		// Prepare scroll panel.
		JScrollPane scrollPanel = new JScrollPane(scrollPane);
		mainPane.add(scrollPanel, BorderLayout.CENTER);
				
		// Create button panel.
		UserConfigurableControl<Crew> control = new UserConfigurableControl<Crew>(f, "Crew",
																		crewConfig) {
			@Override
			protected void displayItem(Crew newDisplay) {
				designateCrew(newDisplay);
			}

			@Override
			protected Crew createItem(String newName, String newDescription) {
				return commitChanges(newName, newDescription);
			}
		};
		
		// Create save new crew button.
		JButton addButton = new JButton(ADD_MEMBER);
		addButton.addActionListener(this);
		control.getPane().add(addButton);


		mainPane.add(control.getPane(), BorderLayout.SOUTH);

		
		// Set up the frame to be visible
		f.pack();
		f.setAlwaysOnTop(true);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}
	
	public JDialog getJFrame() {
		return f;
	}

	/**
	 * Takes the actions from the button being clicked on
	 */
	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		switch (cmd) {
		
		case ADD_MEMBER:
			MemberPanel newPanel = new MemberPanel(crewPanels.size(), this);
			crewPanels.add(newPanel);
			scrollPane.add(newPanel.displayPanel);
			break;
				
		default:
			logger.severe("Unknown action " + cmd);
			break;
		}
	}

	/**
	 * Commits the changes to the crew profiles
	 */
	private Crew commitChanges(String name, String description) {
		if (crewPanels.isEmpty()) {
			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
							"Crew must have at least one member.",
							"No Members Defined",
							JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		Crew newCrew = new Crew(name, description, false);
		for (MemberPanel mp : crewPanels) {
			// Find member
			Member m = mp.toMember();
			newCrew.addMember(m);
		}		
		return newCrew;
	}
	
	/**
	 * Checks if a string is purely numeric
	 * 
	 * @param str
	 * @return
	 */
	private static boolean isNumeric(String str) { 
		try {  
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}

	/**
	 * Retrieves the crew's specific MBTI
	 * 
	 * @param row
	 * @param col
	 * @param loadFromXML
	 * @return
	 */
	private static boolean retrieveCrewMBTI(Member m , int row) {
				
		if (row == 0)
			return m.isExtrovert();
		else if (row == 1)
			return m.isIntuitive();
		else if (row == 2)
			return m.isFeeler();
		else if (row == 3)
			return m.isJudger();
		else
			return false;		
	}

	/**
	 * Converts the type of personality from int to String
	 * 
	 * @param num
	 * @return
	 */
	private static String convert2Type(int num) {
		
		switch (num) {
		case 0:
			return "E";
		case 1:
			return "I";
		case 2:
			return "N";
		case 3:
			return "S";
		case 4:
			return "F";
		case 5:	
			return "T";
		case 6:
			return "J";
		case 7:	
			return "P";
		}
		return null;
	}

	/**
	 * Set up the job comboxbox model
	 * 
	 * @return DefaultComboBoxModel<String>
	 */
	private static DefaultComboBoxModel<String> setUpGenderCBModel() {

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
		m.addElement(MALE);
		m.addElement(FEMALE);
		return m;
	}

	
	/**
	 * Set up the job comboxbox model
	 * 
	 * @return DefaultComboBoxModel<String>
	 */
	private static DefaultComboBoxModel<String> setUpJobCBModel() {

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		for (JobType jt : JobType.values()) {
			if (jt != JobType.POLITICIAN) {
				m.addElement(jt.getName());
			}
		}
		return m;
	}

	/**
	 * Set up the country comboxbox model
	 * 
	 * @return DefaultComboBoxModel<String>
	 */
	private DefaultComboBoxModel<String> setUpCountryCBModel() {
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();

		populateCountryCombo(SETTLEMENT_SPONSOR, m);
		return m;
	}
	
	/**
	 * Set up the sponsor comboxbox model
	 * 
	 * @param country
	 * @return DefaultComboBoxModel<String>
	 */
	private DefaultComboBoxModel<String> setUpSponsorCBModel() {
					
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
		m.addElement(SETTLEMENT_SPONSOR);
		m.addAll(raFactory.getItemNames());
		return m;
	}
 

	/**
	 * Load the country model from a ReportingAuthority
	 * @param sponsor
	 * @param model
	 */
	private void populateCountryCombo(String sponsorCode, DefaultComboBoxModel<String> model) {
		// removing old data
		model.removeAllElements();

		if ((sponsorCode == null) || SETTLEMENT_SPONSOR.equals(sponsorCode)) {
			// Load all known countries
			model.addAll(pc.getKnownCountries());			
		}
		else {
			// Load the countries from RA
			ReportingAuthority ra = raFactory.getItem(sponsorCode);
			model.addAll(ra.getCountries());
		}
	}
	
	
	private void designateCrew(Crew newCrew) {		
		String crewName = newCrew.getName();
		List<Member> members = newCrew.getTeam();
		if (members.isEmpty()) {
			logger.warning("Crew " + crewName + " has no members.");
		}
		for(int i = 0; i < members.size(); i++) {
			loadMember(members.get(i), i);
		}
		
		// Any unused panel?
		for(int j = crewPanels.size(); j > members.size(); j--) {
			removeMember(crewPanels.get(j-1));
		}

		f.setTitle(TITLE + " - " + crewName + " Crew On-board");	
	}
	
	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		simulationConfigEditor = null;
		f = null;
		mainPane = null;
	}
}
