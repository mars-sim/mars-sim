/**
 * Mars Simulation Project
 * CrewEditor.java
 * @version 3.1.1 2020-07-22
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.configeditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.UnitManager;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

import com.alee.laf.combobox.WebComboBox;
import com.alee.managers.icon.LazyIcon;

/**
 * CrewEditor allows users to design the crew manifest for an initial settlement
 */
public class CrewEditor implements ActionListener {

	public static final String TITLE = "Alpha Crew Editor";


	public static final int ALPHA_CREW = 0;

	private static final String POLITICIAN = "Politician";
	
	// Data members

	private List<JTextField> nameTFs = new ArrayList<JTextField>();

	private int crewNum = 0;

//	private boolean[][] personalityArray = new boolean[4][crewNum];
	
//	private List<List<ButtonGroup>> allButtonGroups = new ArrayList<>();

	private List<List<JRadioButton>> allRadioButtons = new ArrayList<>();

//	private DefaultComboBoxModel<String> jobsComboBoxModel;
	private List<WebComboBox> jobsComboBoxList = new ArrayList<WebComboBox>(JobType.JOB_TYPES.length);

//	private DefaultComboBoxModel<String> countriesComboBoxModel;
	private List<WebComboBox> countriesComboBoxList = new ArrayList<WebComboBox>(SimulationConfig.instance().getPersonConfig().createAllCountryList().size());

//	private DefaultComboBoxModel<String> sponsorsComboBoxModel;
	private List<WebComboBox> sponsorsComboBoxList = new ArrayList<WebComboBox>(2);

//	private DefaultComboBoxModel<String> genderComboBoxModel;
	private List<WebComboBox> genderComboBoxList = new ArrayList<WebComboBox>(2);
	
	// Provides a list of destinations options
	private List<WebComboBox> destinationComboBoxList = new ArrayList<WebComboBox>();
	
	private List<MyItemListener> actionListeners = new ArrayList<>(4);
	
	private CrewConfig crewConfig;
	
	private SimulationConfigEditor simulationConfigEditor;

	private JFrame f;
	
	private JPanel mainPane;
	private JPanel listPane;
	private JPanel radioPane;
	private JPanel ppane;
	private JPanel qpane;

	/**
	 * Constructor.
	 * 
	 * @param config
	 *            SimulationConfig
	 * @param simulationConfigEditor
	 *            SimulationConfigEditor
	 */
	public CrewEditor(SimulationConfigEditor simulationConfigEditor) {

		crewConfig = SimulationConfig.instance().getCrewConfig();
		
		this.simulationConfigEditor = simulationConfigEditor;

		crewNum = crewConfig.getNumberOfConfiguredPeople();
		
//		System.out.println("Number Of ConfiguredPeople is " + crewNum);

		createGUI();
	}


	/**
	 * Creates the GUI
	 */
	public void createGUI() {

		simulationConfigEditor.setCrewEditorOpen(true);

		f = new JFrame(TITLE);
		
		ImageIcon icon = new LazyIcon("lander").getIcon(); //new ImageIcon(CrewEditor.class.getResource(MainWindow.LANDER_PNG));
		f.setIconImage(MainWindow.iconToImage(icon));
		
		// f.setSize(600, 300);
		f.setPreferredSize(new Dimension(800, 512));

		f.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				simulationConfigEditor.setCrewEditorOpen(false);
				f.dispose();
			}
		});
				
		// Create main panel.
		mainPane = new JPanel(new BorderLayout());
		mainPane.setPreferredSize(new Dimension(800, 512));
		mainPane.setBorder(MainDesktopPane.newEmptyBorder());
		f.setContentPane(mainPane);

		// Create list panel.
		listPane = new JPanel(new GridLayout(7, crewNum + 1));
		mainPane.add(listPane, BorderLayout.NORTH);

		// Create radio panel.
		radioPane = new JPanel(new GridLayout(1, crewNum + 1));
		mainPane.add(radioPane, BorderLayout.CENTER);

		// Set up the Crewman slot labels
		JLabel slot = new JLabel("Slot     ", JLabel.RIGHT);
		listPane.add(slot);
		for (int i = 0; i < crewNum; i++) {
			String num = i + 1 + "";
			JLabel crewmanLabel = new JLabel("Crewman " + num, JLabel.CENTER);
			listPane.add(crewmanLabel);
		}
	
		listPane.add(new JLabel("Name :   ", JLabel.RIGHT));
		// Add the name textfield
		setUpCrewName();

		listPane.add(new JLabel("Gender :   ", JLabel.RIGHT));
		// Add the gender combobox options
		setUpCrewGender();

		radioPane.add(new JLabel("Traits :   ", JLabel.RIGHT));
		// Add the personality traits checkboxes 
		setUpCrewPersonality();
		
		listPane.add(new JLabel("Job :   ", JLabel.RIGHT));
		// Add the job combobox options
		setUpCrewJob();

		listPane.add(new JLabel("Country :   ", JLabel.RIGHT));
		// Add the country combobox options
		setUpCrewCountry();
	
		listPane.add(new JLabel("Sponsor :   ", JLabel.RIGHT));
		// Add the sponsor combobox options
		setUpCrewSponsor();
		
		listPane.add(new JLabel("Destination :   ", JLabel.RIGHT));
		// Add the destination combobox options
		setUpDestination();
	
		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create load button.
		JButton loadButton = new JButton("Load Default");
		loadButton.addActionListener(this);
		buttonPane.add(loadButton);
		
		// Create commit button.
		JButton commitButton = new JButton("Commit Changes");
		commitButton.addActionListener(this);
		buttonPane.add(commitButton);

		// Create commit button.
		JButton saveButton = new JButton("Save to xml");
		saveButton.addActionListener(this);
		buttonPane.add(saveButton);
		saveButton.setEnabled(false);
		
		// Manually trigger the country selection again so as to correctly 
		// set up the sponsor combobox at the start of the crew editor
		for (int i = 0; i < crewNum; i++) {
			final WebComboBox g = countriesComboBoxList.get(i);
			g.setWidePopup(true);
//			g.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e1) {
					String s = (String) g.getSelectedItem();
					
					int max = g.getItemCount();
					int index = g.getSelectedIndex();
					
					if (max > 1) {
						int num = getRandom(max, index);
//						System.out.println("num : " + num);
						String c = (String)g.getItemAt(num);
						// Fictitiously select a num (other than the index)
						if (c != null && !c.isBlank())
							g.setSelectedIndex(num);
						// Then choose the one already chosen
						// Note: This should force the sponsor to be chosen correction
						g.setSelectedItem(s);
					}
					
					else
						g.setSelectedItem(s);
//				}
//			});
		}

		// Set up the frame to be visible
		f.pack();
		f.setLocationRelativeTo(null);
		f.setVisible(true);
	}

	private int getRandom(int max, int index) {
		int num = RandomUtil.getRandomInt(max);
		if (num != index)
			return num;
		else
			return getRandom(max, index);
	}
	
	public JFrame getJFrame() {
		return f;
	}


	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		
		if (cmd.equals("Load Default")) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
							"Are you sure you want to reload default profiles ? " + System.lineSeparator()
							+ "All changes made will be lost.",
							"Confirm Loading",
							JOptionPane.YES_NO_OPTION);
			
			loadCrewNames();
			loadCrewGender();
			loadCrewJob();
			loadCrewCountry();
			loadCrewSponsor();
			loadDestination();
			loadCrewPersonality();
		}

		else if (cmd.equals("Commit Changes")) {

			commitChanges();
		}
		
		else if (cmd.equals("Save to xml")) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
					"Are you sure you want to save the changes to crew.xml ? " + System.lineSeparator()
					+ "It will save changes made in this session PERMANENTLY " + System.lineSeparator()
					+ "to crew.xml. " + System.lineSeparator()
					+ "Note : If you only want the changes to apply to " + System.lineSeparator()
					+ "the simulation you are setting up, choose " + System.lineSeparator()
					+ "'Commit Change' instead." + System.lineSeparator(),
					"Confirm Saving XML",
					JOptionPane.OK_CANCEL_OPTION);
			
			// Save to xml ...

		}
//		else {
//			for (int i = 0; i < 4; i++) {
//				for (int j = 0; j < crewNum; j++) {
//					if (cmd.equals("a" + i + j)) {
//						personalityArray[i][j] = true;
//	
//					} else if (cmd.equals("b" + i + j)) {
//						personalityArray[i][j] = false;
//	
//					}
//				}
//			}
//		}
	}

	public void commitChanges() {
		boolean goodToGo = true;
		
		for (int i = 0; i < crewNum; i++) {

			if (checkNameFields(i, goodToGo)) {
				String genderStr = (String) genderComboBoxList.get(i).getSelectedItem();
				if (genderStr.equals("M"))
					genderStr = "MALE";
				else if (genderStr.equals("F"))
					genderStr = "FEMALE";
				crewConfig.setPersonGender(i, genderStr, ALPHA_CREW);
				System.out.print(genderStr + ", ");
				
				String personalityStr = getSelectedPersonality(i);
				crewConfig.setPersonPersonality(i, personalityStr, ALPHA_CREW);
				System.out.print(personalityStr + ", ");
				
				String jobStr = (String) jobsComboBoxList.get(i).getSelectedItem();
				crewConfig.setPersonJob(i, jobStr, ALPHA_CREW);
				System.out.print(jobStr + ", ");

				String countryStr = (String) countriesComboBoxList.get(i).getSelectedItem();
				crewConfig.setPersonCountry(i, countryStr, ALPHA_CREW);
				System.out.print(countryStr + ", ");
				
				String sponsorStr = (String) sponsorsComboBoxList.get(i).getSelectedItem();
				crewConfig.setPersonSponsor(i, sponsorStr, ALPHA_CREW);
				System.out.print(sponsorStr + ", ");	
				
				String maindish = crewConfig.getFavoriteMainDish(i, ALPHA_CREW);
				crewConfig.setMainDish(i, maindish, ALPHA_CREW);
				System.out.print(maindish + ", ");
				
				String sidedish = crewConfig.getFavoriteMainDish(i, ALPHA_CREW);
				crewConfig.setSideDish(i, sidedish, ALPHA_CREW);
				System.out.print(sidedish + ", ");
				
				String dessert = crewConfig.getFavoriteDessert(i, ALPHA_CREW);
				crewConfig.setDessert(i, dessert, ALPHA_CREW);
				System.out.print(dessert + ", ");
				
				String activity = crewConfig.getFavoriteActivity(i, ALPHA_CREW);
				crewConfig.setActivity(i, activity, ALPHA_CREW);
				System.out.print(activity + ", ");
				
				String destinationStr = (String) destinationComboBoxList.get(i).getSelectedItem();//crewConfig.getConfiguredPersonDestination(i, ALPHA_CREW, false);
				crewConfig.setPersonDestination(i, destinationStr, ALPHA_CREW);
				System.out.println(destinationStr + ".");
			}
			
			else {
				goodToGo = false;
				break;
			}

		}

		if (goodToGo) {
			simulationConfigEditor.setCrewEditorOpen(false);
			f.setVisible(false);
			f.dispose();
		}
	}
	
	/**
	 * Checks if the name text fields are valid
	 * 
	 * @param i
	 * @param goodToGo
	 * @return
	 */
	public boolean checkNameFields(int i, boolean goodToGo) {
		
//		String destinationStr = (String) destinationCB.getValue();
//		destinationName = destinationStr;

		String nameStr = nameTFs.get(i).getText().trim();
		// Use isBlank() to check against invalid names
		if (!Conversion.isBlank(nameStr)
				&& nameStr.contains(" ")) {
			System.out.print(nameStr + ", ");
			crewConfig.setPersonName(i, nameStr, ALPHA_CREW);
			return true;
			
		} else {
			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
							"Each Settler's name must include first and last name, separated by a whitespace",
							"Invalid Name Field",
							JOptionPane.ERROR_MESSAGE);
			
			// Disable Start;
			// event.consume();
			nameTFs.get(i).requestFocus();
			return false;
		}
	}
	
	public void setUpCrewName() {
		for (int i = 0; i < crewNum; i++) {
//			int crew_id = cc.getCrew(i);
//			System.out.println("setUpCrewName:: i is " + i);
//			System.out.println("setUpCrewName:: crewNum is " + crewNum);
			String n = crewConfig.getConfiguredPersonName(i, ALPHA_CREW, false);

			JTextField tf = new JTextField();
			nameTFs.add(tf);
			listPane.add(tf);
			tf.setText(n);
		}
	}

	public void loadCrewNames() {
		for (int i = 0; i < crewNum; i++) {
//			int crew_id = cc.getCrew(i);
//			System.out.println("setUpCrewName:: i is " + i);
//			System.out.println("setUpCrewName:: crewNum is " + crewNum);
			String n = crewConfig.getConfiguredPersonName(i, ALPHA_CREW, true);

			JTextField tf = nameTFs.get(i);
			tf.setText(n);
		}
	}
	
	public DefaultComboBoxModel<String> setUpGenderCBModel() {

		List<String> genderList = new ArrayList<String>(2);
		genderList.add("M");
		genderList.add("F");
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();

		Iterator<String> i = genderList.iterator();
		while (i.hasNext()) {
			String s = i.next();
			m.addElement(s);
		}

		return m;
	}

	public WebComboBox setUpCB(int choice, String s) {
		DefaultComboBoxModel<String> m = null;
		if (choice == 0)
			m = setUpGenderCBModel();

		else if (choice == 2)
			m = setUpJobCBModel();
		
		else if (choice == 3) 
			m = setUpCountryCBModel();
		
		else if (choice == 4) 
			m = setUpSponsorCBModel(s);

		else if (choice == 5) 
			m = setUpDestinationCBModel(s);
		
		final WebComboBox g = new WebComboBox(m);
		g.setWidePopup(true);
		
		g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				String s = (String) g.getSelectedItem();

				g.setSelectedItem(s);
			}
		});

		return g;
	}

	/**
	 * Sets up the crew gender
	 */
	public void setUpCrewGender() {

		String s[] = new String[crewNum];
		for (int j = 0; j < crewNum; j++) {
			GenderType n = crewConfig.getConfiguredPersonGender(j, ALPHA_CREW, false);

			s[j] = n.toString();
			if (s[j].equals("MALE"))
				s[j] = "M";
			else
				s[j] = "F";

			WebComboBox g = setUpCB(0, ""); // 0 = Gender
			g.setWidePopup(true);
			g.setMaximumRowCount(2);
			listPane.add(g);
			genderComboBoxList.add(g);
			g.setSelectedItem(s[j]);

		}
	}

	/**
	 * Loads the crew gender
	 */
	public void loadCrewGender() {

		String s[] = new String[crewNum];
		for (int j = 0; j < crewNum; j++) {
			GenderType n = crewConfig.getConfiguredPersonGender(j, ALPHA_CREW, true);

			s[j] = n.toString();
			if (s[j].equals("MALE"))
				s[j] = "M";
			else
				s[j] = "F";

			WebComboBox g = genderComboBoxList.get(j);
			g.setWidePopup(true);
			g.setSelectedItem(s[j]);

		}
	}
	
	/**
	 * Set up personality radio buttons
	 * 
	 * @param col
	 */
	public void setUpCrewPersonality() {
		String quadrant1A = "Extravert", quadrant1B = "Introvert";
		String quadrant2A = "Intuition", quadrant2B = "Sensing";
		String quadrant3A = "Feeling", quadrant3B = "Thinking";
		String quadrant4A = "Judging", quadrant4B = "Perceiving";
		String cat1 = "World", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
		String a = null, b = null, c = null;
		
		for (int col = 0; col < crewNum; col++) {
			ppane = new JPanel(new GridLayout(4, 1));

			List<JRadioButton> radioButtons = new ArrayList<>();	
					
			for (int row = 0; row < 4; row++) {
				qpane = new JPanel(new FlowLayout());
				if (row == 0) {
					a = quadrant1A;
					b = quadrant1B;
					c = cat1;
				} else if (row == 1) {
					a = quadrant2A;
					b = quadrant2B;
					c = cat2;
				} else if (row == 2) {
					a = quadrant3A;
					b = quadrant3B;
					c = cat3;
				} else if (row == 3) {
					a = quadrant4A;
					b = quadrant4B;
					c = cat4;
				}

				JRadioButton ra = new JRadioButton(a);
				ra.addActionListener(this);
//				ra.setActionCommand("a" + row + col);
				
				JRadioButton rb = new JRadioButton(b);
				rb.addActionListener(this);
//				rb.setActionCommand("b" + row + col);
				
				if (retrieveCrewMBTI(row, col, false))
					ra.setSelected(true);
				else
					rb.setSelected(true);
				
				radioButtons.add(ra);
				radioButtons.add(rb);
				
				// Use ButtonGroup to limit to choosing either ra or rb
				ButtonGroup bg1 = new ButtonGroup();
				bg1.add(ra);
				bg1.add(rb);
				
				qpane.setBorder(BorderFactory.createTitledBorder(c));
				qpane.add(ra);
				qpane.add(rb);
				ppane.add(qpane);
			}
			
			radioPane.add(ppane);
			
			allRadioButtons.add(radioButtons);
		}
	}

	/**
	 * Loads crew personality
	 * 
	 * @param col
	 */
	public void loadCrewPersonality() {
		for (int col = 0; col < crewNum; col++) {

			List<JRadioButton> radioButtons = allRadioButtons.get(col);
			
			for (int row = 0; row < 4; row++) {

				JRadioButton ra = radioButtons.get(2 * row);
				JRadioButton rb = radioButtons.get(2 * row + 1);
						
				if (retrieveCrewMBTI(row, col, true))
					ra.setSelected(true);
				else
					rb.setSelected(true);
			}
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
	public boolean retrieveCrewMBTI(int row, int col, boolean loadFromXML) {
				
		if (row == 0)
			return crewConfig.isExtrovert(col, loadFromXML);
		else if (row == 1)
			return crewConfig.isIntuitive(col, loadFromXML);
		else if (row == 2)
			return crewConfig.isFeeler(col, loadFromXML);
		else if (row == 3)
			return crewConfig.isJudger(col, loadFromXML);
		else
			return false;
//		return personalityArray[row][col];
		
	}

//	public boolean getRandomBoolean() {
//	    Random random = new Random();
//	    return random.nextBoolean();
//	}
//	
	/**
	 * Get a person's personality type (MTBI) as shown in the radio buttons selected
	 * 
	 * @param col
	 * @return the MTBI string
	 */
	public String getSelectedPersonality(int col) {
		String type = null;		
		List<JRadioButton> radioButtons = allRadioButtons.get(col);//new ArrayList<>();
		
		for (int num = 0; num < 8; num++) {
			JRadioButton b = radioButtons.get(num);
			String oneType = null;
			if (b.isSelected()) {
				oneType = convert2Type(num);
				if (num == 0 || num == 1)
					type = oneType;
				else
					type += oneType;
			}
		}
		
//		System.out.println(type);
		return type;
	}
	
	/**
	 * Converts the type of personality from int to String
	 * 
	 * @param num
	 * @return
	 */
	public String convert2Type(int num) {
		
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
	public DefaultComboBoxModel<String> setUpJobCBModel() {

		List<String> jobs = JobType.getList();
				
		jobs.remove(POLITICIAN);

		Collections.sort(jobs);

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = jobs.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		return m;
	}

	/**
	 * Set up the country comboxbox model
	 * 
	 * @return DefaultComboBoxModel<String>
	 */
	public DefaultComboBoxModel<String> setUpCountryCBModel() {

		List<String> countries = UnitManager.getAllCountryList();

		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = countries.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		return m;
	}
	
	/**
	 * Set up the sponsor comboxbox model
	 * 
	 * @param country
	 * @return DefaultComboBoxModel<String>
	 */
	public DefaultComboBoxModel<String> setUpSponsorCBModel(String country) {

		List<String> sponsors = new ArrayList<>();

		sponsors.add(ReportingAuthorityType.MARS_SOCIETY_L.getName());
//		// Retrieve the sponsor from the selected country 		
		if (!country.isBlank())
			sponsors.add(UnitManager.mapCountry2Sponsor(country));		
				
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = sponsors.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		return m;
	}
	
	/**
	 * Set up the destination comboxbox model
	 * 
	 * @param destination
	 * @return DefaultComboBoxModel<String>
	 */
	public DefaultComboBoxModel<String> setUpDestinationCBModel(String destination) {

		List<String> destinations = simulationConfigEditor.loadDestinations();
		
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
		Iterator<String> j = destinations.iterator();

		while (j.hasNext()) {
			String s = j.next();
			m.addElement(s);
		}
		
		if (destinations.contains(destination))
			m.setSelectedItem(destination);
		
		return m;
	}
	
	/**
	 * Set up the job comboxbox
	 * 
	 */
	public void setUpCrewJob() {
		int SIZE = JobType.numJobTypes;
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE];
			n[i] = crewConfig.getConfiguredPersonJob(i, ALPHA_CREW, false);
			WebComboBox g = setUpCB(2, n[i]);// 2 = Job
			g.setWidePopup(true);
			g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			jobsComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew job
	 * 
	 */
	public void loadCrewJob() {
		int SIZE = JobType.numJobTypes;
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE];
			n[i] = crewConfig.getConfiguredPersonJob(i, ALPHA_CREW, true);
			WebComboBox g = jobsComboBoxList.get(i); //setUpCB(2, n[i]);// 2 = Job
			g.setWidePopup(true);
			g.getModel().setSelectedItem(n[i]);
		}
	}
	
	/**
	 * Set up the country comboxbox
	 * 
	 */
	public void setUpCrewCountry() {
		int SIZE = UnitManager.getAllCountryList().size();
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE];
			n[i] = crewConfig.getConfiguredPersonCountry(i, ALPHA_CREW, false);
			WebComboBox g = setUpCB(3, n[i]); // 3 = Country
			g.setWidePopup(true);
			
			g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			countriesComboBoxList.add(g);
			
			// Set up and add a item listener to the country combobox
			MyItemListener a = new MyItemListener();
			actionListeners.add(a);
		    g.addItemListener(a);
		}
	}

	/**
	 * Loads the crew country
	 * 
	 */
	public void loadCrewCountry() {
		int SIZE = UnitManager.getAllCountryList().size();
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE];
			n[i] = crewConfig.getConfiguredPersonCountry(i, ALPHA_CREW, true);
			WebComboBox g = countriesComboBoxList.get(i); //setUpCB(3, n[i]); // 3 = Country
			g.setWidePopup(true);
			g.getModel().setSelectedItem(n[i]);
		}
	}
			
	/**
	 * Set up the sponsor comboxbox
	 * 
	 */
	public void setUpCrewSponsor() {
		int SIZE = UnitManager.getAllShortSponsors().size();
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE]; // 10
			n[i] = crewConfig.getConfiguredPersonSponsor(i, ALPHA_CREW, false);
			WebComboBox g = setUpCB(4, n[i]); // 4 = Sponsor
			g.setWidePopup(true);
			g.setMaximumRowCount(8);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			sponsorsComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew sponsor
	 * 
	 */
	public void loadCrewSponsor() {
		int SIZE = UnitManager.getAllShortSponsors().size();
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE]; // 10
			n[i] = crewConfig.getConfiguredPersonSponsor(i, ALPHA_CREW, true);
			WebComboBox g = sponsorsComboBoxList.get(i); // setUpCB(4, n[i]); // 4 = Sponsor
			g.setWidePopup(true);
			g.getModel().setSelectedItem(n[i]);
		}
	}
	
	/**
	 * Set up the destination comboxbox
	 * 
	 */
	public void setUpDestination() {
		int SIZE = 5;
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE]; // 10
			n[i] = crewConfig.getConfiguredPersonDestination(i, ALPHA_CREW, false);
			WebComboBox g = setUpCB(5, n[i]); // 5 = Destination
			g.setWidePopup(true);
			g.setMaximumRowCount(5);
			listPane.add(g);
			g.getModel().setSelectedItem(n[i]);
			destinationComboBoxList.add(g);
		}
	}
	
	/**
	 * Loads the crew destination
	 * 
	 */
	public void loadDestination() {
		int SIZE = 5;
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE]; // 10
			n[i] = crewConfig.getConfiguredPersonDestination(i, ALPHA_CREW, true);
			WebComboBox g = destinationComboBoxList.get(i); //setUpCB(5, n[i]); // 5 = Destination
			g.setWidePopup(true);
			g.getModel().setSelectedItem(n[i]);
		}
	}
	
	class MyItemListener implements ItemListener {
		// This method is called only if a new item has been selected.
		@SuppressWarnings("unchecked")
		public void itemStateChanged(ItemEvent evt) {
//			WebComboBox cb = (WebComboBox) evt.getSource();

			int index = actionListeners.indexOf(this);
			
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED && sponsorsComboBoxList.size() > 0) {
				// Item was just selected

		        WebComboBox m = sponsorsComboBoxList.get(index);
		        
				// Get combo box model
		        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) m.getModel();
		        
		        // removing old data
		        model.removeAllElements();

		        // Add MS and SPACEX as the available options
	            model.addElement(ReportingAuthorityType.MARS_SOCIETY_L.getName());
	            model.addElement(ReportingAuthorityType.SPACEX_L.getName());
	            
				String countryStr = (String) item;
				
	            if (!countryStr.isBlank()) {
					String sponsorStr = UnitManager.mapCountry2Sponsor(countryStr);            
					model.addElement(sponsorStr);
	            }
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED && sponsorsComboBoxList.size() > 0) {
				// Item is no longer selected
				
		        WebComboBox m = sponsorsComboBoxList.get(index);
		        
				// Get combo box model
		        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) m.getModel();
		        
		        // removing old data
		        model.removeAllElements();
		        
				model.addElement("To be determined");

			}
		}
	}
		  
	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		crewConfig = null;
		simulationConfigEditor.setCrewEditorOpen(false);
		simulationConfigEditor = null;
		f = null;
		mainPane = null;
		listPane = null;
		radioPane = null;
		ppane = null;
		qpane = null;
		nameTFs = null;
		sponsorsComboBoxList = null;
		countriesComboBoxList = null;
		jobsComboBoxList = null;
		genderComboBoxList = null;
	}

}
