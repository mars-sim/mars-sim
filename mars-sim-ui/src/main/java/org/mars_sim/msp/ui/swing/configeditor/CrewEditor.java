/**
 * Mars Simulation Project
 * CrewEditor.java
 * @version 3.2.0 2021-06-20
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;

import org.mars_sim.msp.core.person.Crew;
import org.mars_sim.msp.core.person.CrewConfig;
import org.mars_sim.msp.core.person.GenderType;
import org.mars_sim.msp.core.person.Member;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthority;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityFactory;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.tool.RandomUtil;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MainWindow;

import com.alee.api.annotations.NotNull;
import com.alee.api.annotations.Nullable;
import com.alee.api.data.BoxOrientation;
import com.alee.extended.button.WebSwitch;
import com.alee.extended.overlay.AlignedOverlay;
import com.alee.extended.overlay.WebOverlay;
import com.alee.extended.svg.SvgIcon;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.label.WebLabel;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.radiobutton.WebRadioButton;
import com.alee.laf.text.WebTextField;
import com.alee.laf.window.WebDialog;
import com.alee.managers.icon.IconManager;
import com.alee.managers.style.StyleId;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.CoreSwingUtils;
import com.alee.utils.swing.extensions.DocumentEventRunnable;

/**
 * CrewEditor allows users to design the crew manifest for an initial settlement
 */
public class CrewEditor implements ActionListener {
		
	/** default logger. */
	private static final Logger logger = Logger.getLogger(CrewEditor.class.getName());

	public static final String TITLE = "Crew Editor";
	private static final String SELECT_SPONSOR = "Select Sponsor";


	private static final String SAVE_CREW = "Save Crew";
	private static final String SAVE_NEW_CREW = "Save As New Crew";
	private static final String COMMIT = "Commit Changes";
	private static final String LOAD_CREW = "Load Crew";
				
	public static final int PANEL_WIDTH = 180;
	public static final int WIDTH = (int)(PANEL_WIDTH * 3.5);
	public static final int HEIGHT = 512;

	private static final String[] QUADRANT_A = {"{E:u}xtravert",
			"I{N:u}tuition", "{F:u}eeling", "{J:u}udging"};
	private static final String[] QUADRANT_B = {"{I:u}ntrovert",
			"{S:u}ensing", "{T:u}hinking", "{P:u}erceiving"};
	private static final String[] CATEGORY = {"World",
			"Information", "Decision", "Structure"};
	
	// Data members
	private int crewNum = 0;

	private List<JTextField> nameTFs = new ArrayList<JTextField>();

	private List<JTextField> ageTFs = new ArrayList<JTextField>();
	
	private List<List<JRadioButton>> allRadioButtons = new ArrayList<>();

	private List<WebComboBox> jobsComboBoxList = new ArrayList<WebComboBox>();

	private List<WebComboBox> countriesComboBoxList = new ArrayList<WebComboBox>();

	private List<WebComboBox> sponsorsComboBoxList = new ArrayList<WebComboBox>();
	
	private List<WebSwitch> webSwitches = new ArrayList<>();
	
	private List<SponsorListener> actionListeners = new ArrayList<>();
	
	private WebDialog<?> f;
	
	private JPanel mainPane;
	private JPanel scrollPane;
	
	private List<Box> crewPanels = new ArrayList<>();
	
	private CrewConfig crewConfig;

	private SimulationConfigEditor simulationConfigEditor;

	private Crew crew;

	private JButton saveButton;

	private DefaultComboBoxModel<String> crewCB;


	/**
	 * Constructor.
	 * 
	 * @param config
	 *            SimulationConfig
	 * @param simulationConfigEditor
	 *            SimulationConfigEditor
	 */
	public CrewEditor(SimulationConfigEditor simulationConfigEditor, CrewConfig config) {
		
		this.simulationConfigEditor = simulationConfigEditor;
		this.crewConfig = config;
		
		// Start with Alpha
		this.crew = config.loadCrew(CrewConfig.ALPHA_NAME);
		crewNum = crew.getNumberOfConfiguredPeople();
		
		createGUI();
	}


	/**
	 * Create a crew panel to house the attributes of a crewman
	 * 
	 * @return
	 */
	private Box createCrewPanel() {
    	SvgIcon icon = IconManager.getIcon ("info_red");//new LazyIcon("info").getIcon();
		final WebOverlay overlay = new WebOverlay(StyleId.overlay);
        final WebLabel overlayLabel = new WebLabel(icon);

		Box crewPanel = Box.createVerticalBox();

		// Name 
		WebTextField nametf = new WebTextField(15);
		nametf.setMargin(3, 0, 3, 0);
		overlay.setContent(nametf);
		onChange(nametf, overlayLabel, overlay);
		WebPanel namePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		namePanel.add(new WebLabel("   Name : "));
		namePanel.add(nametf);
		nameTFs.add(nametf);
		crewPanel.add(namePanel);
		
		// Gender
		WebSwitch webSwitch = new WebSwitch(true);		
		webSwitches.add(webSwitch);
		webSwitch.setSwitchComponents(
				"M", 
				"F");
		TooltipManager.setTooltip(webSwitch, "Choose male or female", TooltipWay.down);
		WebPanel genderPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		genderPanel.add(new WebLabel(" Gender : "));
		genderPanel.add(webSwitch);
		crewPanel.add(genderPanel);	
		
		// Age
		JTextField agetf = new JTextField(5);
		JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		agePanel.add(new WebLabel("      Age : "));
		agePanel.add(agetf);
		ageTFs.add(agetf);
		crewPanel.add(agePanel);
				
		// Personallity
		JPanel fullPane = new JPanel(new FlowLayout());
		fullPane.setLayout(new BoxLayout(fullPane, BoxLayout.Y_AXIS));
		fullPane.setPreferredSize(new Dimension(PANEL_WIDTH, 200));
		fullPane.setSize(PANEL_WIDTH, 200);
		fullPane.setMaximumSize(new Dimension(PANEL_WIDTH, 200));		
		List<JRadioButton> radioButtons = new ArrayList<>();			
		for (int row = 0; row < 4; row++) {
			WebRadioButton ra = new WebRadioButton(StyleId.radiobuttonStyled, 
							QUADRANT_A[row]);
			ra.addActionListener(this);
			
			WebRadioButton rb = new WebRadioButton(StyleId.radiobuttonStyled, 
							QUADRANT_B[row]);
			rb.addActionListener(this);	
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
		crewPanel.add(fullPane);
		allRadioButtons.add(radioButtons);
		
		// Job
		WebComboBox g = setUpCB(2);// 2 = Job
		TooltipManager.setTooltip(g, "Choose the job of this person", TooltipWay.down);
		g.setMaximumRowCount(8);
		crewPanel.add(g);
		jobsComboBoxList.add(g);
		
		// Sponsor
		DefaultComboBoxModel<String> m = setUpSponsorCBModel();
		WebComboBox sponsorCB = new WebComboBox(StyleId.comboboxHover, m);
		sponsorCB.setWidePopup(true);
		sponsorCB.setPreferredWidth(PANEL_WIDTH);
		sponsorCB.setMaximumWidth(PANEL_WIDTH);
	    			
		TooltipManager.setTooltip(g, "Choose the sponsor of this person", TooltipWay.down);
		sponsorCB.setMaximumRowCount(8);
		crewPanel.add(sponsorCB);
		sponsorsComboBoxList.add(sponsorCB);

		// Set up and add an item listener to the country combobox
		SponsorListener l = new SponsorListener();
		actionListeners.add(l);
		sponsorCB.addItemListener(l);		    
	    
		// Country
		WebComboBox countryCB = setUpCB(3); // 3 = Country
		TooltipManager.setTooltip(g, "Choose the country of origin of this person", TooltipWay.down);
		countryCB.setMaximumRowCount(8);
		crewPanel.add(countryCB);
		countriesComboBoxList.add(countryCB);
		
		return crewPanel;
	}
	

	private void loadMember(Member m, int i) {
		
		// Name
		nameTFs.get(i).setText(m.getName());
		
		// Gender
		webSwitches.get(i).setSelected(m.getGender() == GenderType.MALE);
        
		// Age
		int age = 0;
		String ageStr = m.getAge();
		if (ageStr == null)
			age = RandomUtil.getRandomInt(21, 65);
		else
			age = Integer.parseInt(ageStr);	
		ageTFs.get(i).setText(age + "");
		
		// Job
		jobsComboBoxList.get(i).getModel().setSelectedItem(m.getJob());
		
		// Sponsor
		String s = m.getSponsorCode();
		WebComboBox sponsorCB = sponsorsComboBoxList.get(i); 
		sponsorCB.getModel().setSelectedItem(s);
		
		// Country
		String country = m.getCountry();
		WebComboBox g = countriesComboBoxList.get(i); 
		populateCountryCombo(m.getSponsorCode(),
							(DefaultComboBoxModel<String>) g.getModel());
		g.getModel().setSelectedItem(country);
				
		// Personality
		List<JRadioButton> radioButtons = allRadioButtons.get(i);
		for (int row = 0; row < 4; row++) {

			JRadioButton ra = radioButtons.get(2 * row);
			JRadioButton rb = radioButtons.get(2 * row + 1);
					
			if (retrieveCrewMBTI(m, row))
				ra.setSelected(true);
			else
				rb.setSelected(true);
		}

	}
	
	
	/**
	 * Creates the GUI
	 */
	private void createGUI() {
	
		f = new WebDialog(simulationConfigEditor.getFrame(), TITLE + " - Alpha Crew On-board", true); //new JFrame(TITLE + " - Alpha Crew On-board");
		f.setIconImage(MainWindow.getIconImage());
//		f.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		f.setResizable(false);
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
//		f.setContentPane(mainPane);
		f.getContentPane().add(mainPane);
		
		// Create main panel.
		scrollPane = new JPanel();
		scrollPane.setLayout(new BoxLayout(scrollPane, BoxLayout.X_AXIS));
//		scrollPane.setPreferredSize(new Dimension(WIDTH, HEIGHT));
//		scrollPane.setAlignmentX(LEFT_ALIGNMENT);
		scrollPane.setBorder(MainDesktopPane.newEmptyBorder());

		// Prepare scroll panel.
		JScrollPane scrollPanel = new JScrollPane(scrollPane);
//		scrollPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
//		scrollPanel.getVerticalScrollBar().setUnitIncrement(CREW_WIDTH);
//		scrollPanel.getHorizontalScrollBar().setUnitIncrement(CREW_WIDTH * 2);
		mainPane.add(scrollPanel, BorderLayout.CENTER);
		
		List<Member> members = crew.getTeam();
		for (int i=0; i< crew.getTeam().size(); i++) {
			Box crewPanel = createCrewPanel();
			crewPanels.add(crewPanel);
			
			// Add the Crewman title border
			String num = i + 1 + "";
			crewPanel.setBorder(BorderFactory.createTitledBorder("Crewman " + num));
			scrollPane.add(crewPanel);
			
			loadMember(members.get(i), i);
		}
	
		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Crew name selection
		crewCB = new DefaultComboBoxModel<>();
		crewCB.addAll(0, crewConfig.getKnownCrewNames());
		crewCB.setSelectedItem(crew.getName());
		JComboBox<String> crewSelector = new JComboBox<>(crewCB) ;
		buttonPane.add(crewSelector);

		// Create load crew button.
		JButton loadButton = new JButton(LOAD_CREW);
		loadButton.addActionListener(this);
		buttonPane.add(loadButton);
		
		// Create commit button.
		JButton commitButton = new JButton(COMMIT);
		commitButton.addActionListener(this);
		buttonPane.add(commitButton);
		
		// Create save crew button.
		saveButton = new JButton(SAVE_CREW);
		saveButton.addActionListener(this);
		buttonPane.add(saveButton);		

		// Create save new crew button.
		JButton newButton = new JButton(SAVE_NEW_CREW);
		newButton.addActionListener(this);
		buttonPane.add(newButton);
		
//		// Manually trigger the country selection again so as to correctly 
//		// set up the sponsor combobox at the start of the crew editor
//		for (int i = 0; i < crewNum; i++) {
//			final WebComboBox g = countriesComboBoxList.get(i);
//
////			g.addActionListener(new ActionListener() {
////				public void actionPerformed(ActionEvent e1) {
//					String s = (String) g.getSelectedItem();
//					
//					int max = g.getItemCount();
//					int index = g.getSelectedIndex();
//					
//					if (max > 1) {
//						int num = getRandom(max, index);
////						System.out.println("num : " + num);
//						String c = (String)g.getItemAt(num);
//						// Fictitiously select a num (other than the index)
//						if (c != null && !c.isBlank())
//							g.setSelectedIndex(num);
//						// Then choose the one already chosen
//						// Note: This should force the sponsor to be chosen correction
//						g.setSelectedItem(s);
//					}
//					
//					else
//						g.setSelectedItem(s);
////				}
////			});
//		}

		// Set up the frame to be visible
		f.pack();
		f.setAlwaysOnTop(true);
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
	
	public WebDialog getJFrame() {
		return f;
	}

	/**
	 * Takes the actions from the button being clicked on
	 */
	public void actionPerformed(ActionEvent evt) {

		String cmd = (String) evt.getActionCommand();
		switch (cmd) {
		case LOAD_CREW: 
			String loadCrew = (String) crewCB.getSelectedItem();
			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f, 
							"Are you sure you want to reload the Crew " + loadCrew + " ? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Reloading Crew",
							JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				designateCrew(loadCrew);
			}
			break;

		
		case COMMIT: 
			if (commitChanges()) {
//				simulationConfigEditor.setCrewEditorOpen(false);
				f.setVisible(false);
				f.dispose();
			}
			break;
		
		case SAVE_CREW:
			JDialog.setDefaultLookAndFeelDecorated(true);
			int result2 = JOptionPane.showConfirmDialog(f,
					"Are you sure you want to save the changes to for " + crew.getName() + " ? " 
					+ System.lineSeparator() + System.lineSeparator()
					+ "Note : If you only want the changes to apply to " + System.lineSeparator()
					+ "the simulation you are setting up, choose " + System.lineSeparator()
					+ "'Commit Change' instead." + System.lineSeparator(),
					"Confirm Saving Crew",
					JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result2 == JOptionPane.YES_OPTION) {
				commitChanges();
				crewConfig.save(crew);
			}
			break;

		case SAVE_NEW_CREW:
			JDialog.setDefaultLookAndFeelDecorated(true);
			String newCrewName = (String)JOptionPane.showInputDialog(
                    f, "Enter the name of the new Crew.");

			if ((newCrewName != null) && (newCrewName.length() > 0)) {
				// Create new Crew
				crew = new Crew(newCrewName, false);
				commitChanges();
				crewConfig.save(crew); 
				f.setTitle(TITLE + " - " + newCrewName + " Crew On-board");	
				
				crewCB.addElement(newCrewName);
			}
			break;
			
		default:
			logger.severe("Unknown action " + cmd);
			break;
		}
	}

	/**
	 * Commits the changes to the crew profiles
	 */
	private boolean commitChanges() {
		boolean goodToGo = true;
		
		List<Member> members = crew.getTeam();
		for (int i = 0; i < crewNum; i++) {
			// Find member
			Member m = null;
			if (i == members.size()) {
				m = new Member();
				members.add(m);
			}
			else {
				m = members.get(i);
			}
			
			if (!checkNameFields(i, m, goodToGo)) {
				goodToGo = false;
				break;
			}
				
			if (!checkAgeFields(i, m, goodToGo)) {
				goodToGo = false;
				break;
			}
				
			GenderType gender;
			boolean isSelected = webSwitches.get(i).isSelected();
			
			if (isSelected)
				gender = GenderType.MALE;
			else 
				gender = GenderType.FEMALE;

			m.setGender(gender);
			
			m.setMBTI(getSelectedPersonality(i));
	
			String sponsor = (String) sponsorsComboBoxList.get(i).getSelectedItem();
			if (sponsor != null) {
				m.setSponsorCode(sponsor);
			}
			
			m.setCountry((String) countriesComboBoxList.get(i).getSelectedItem());
			m.setJob((String) jobsComboBoxList.get(i).getSelectedItem());
		}
		return goodToGo;
	}
	
	/**
	 * Checks if the name textfields are valid
	 * 
	 * @param i
	 * @param goodToGo
	 * @return
	 */
	private boolean checkNameFields(int i, Member m, boolean goodToGo) {

		String nameStr = nameTFs.get(i).getText().trim();
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
			nameTFs.get(i).requestFocus();
			return false;
		}
	}
	
	/**
	 * Checks if the age textfields are valid
	 * 
	 * @param i
	 * @param goodToGo
	 * @return
	 */
	private boolean checkAgeFields(int i, Member m, boolean goodToGo) {
		
		String s = ageTFs.get(i).getText().trim();
		// Use isBlank() to check against invalid names
		if (!Conversion.isBlank(s)
				&& isNumeric(s)) {
			
			int age = Integer.parseInt(s);
			
			if (age < 0 || age > 100) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				JOptionPane.showMessageDialog(f, 
								"A settler's age must be between 0 and 100",
								"Invalid Age Range",
								JOptionPane.ERROR_MESSAGE);
				
				ageTFs.get(i).requestFocus();
				return false;
			}
			
			else {
				m.setAge(s);
				return true;
			}
		} 
		
		else {
			JDialog.setDefaultLookAndFeelDecorated(true);
			JOptionPane.showMessageDialog(f, 
							"A settler's age must be numeric and between 5 and 100",
							"Invalid Age Format",
							JOptionPane.ERROR_MESSAGE);
			
			ageTFs.get(i).requestFocus();
			return false;
		}
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
	


	private void onChange(WebTextField tf, WebLabel overlayLabel, WebOverlay overlay) {
		tf.onChange(new DocumentEventRunnable<WebTextField> () {
            @Override
            public void run(@NotNull final WebTextField component, @Nullable final DocumentEvent event ) {
                CoreSwingUtils.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final String text = tf.getText();
                        if (text.length() == 0) {
                        	overlayLabel.setToolTip("This name textfield cannot be blank !", TooltipWay.right);
                            if (overlay.getOverlayCount() == 0){
                                overlay.addOverlay(
                                        new AlignedOverlay(
                                                overlayLabel,
                                                BoxOrientation.right,
                                                BoxOrientation.top,
                                                new Insets(0, 0, 0, 3)
                                        )
                                );
                            }
                        }
                        else {
//                        	System.out.println("The name textfield is not blank.");
                            if (overlay.getOverlayCount() > 0) {
                                overlay.removeOverlay(overlayLabel);
                            }
                        }
                    }
                } );
            }
        } );
	}
	


	/**
	 * Sets up the combo box for a few attributes
	 * 
	 * @param choice
	 * @param s
	 * @return
	 */
	private WebComboBox setUpCB(int choice) {
		DefaultComboBoxModel<String> m = null;
//		if (choice == 0)
//			m = setUpGenderCBModel();
//		else 
		if (choice == 2)
			m = setUpJobCBModel();
		
		else if (choice == 3) 
			m = setUpCountryCBModel();
		
		final WebComboBox g = new WebComboBox(StyleId.comboboxHover, m);
		g.setWidePopup(true);
//		g.setPreferredSize(new Dimension(CREW_WIDTH, 25));
//		g.setSize(CREW_WIDTH, 25);
		g.setPreferredWidth(PANEL_WIDTH);
//		g.setMinimumWidth(CREW_WIDTH);
		g.setMaximumWidth(PANEL_WIDTH);
		
		g.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e1) {
				String s = (String) g.getSelectedItem();

				g.setSelectedItem(s);
			}
		});

		return g;
	}



	/**
	 * Retrieves the crew's specific MBTI
	 * 
	 * @param row
	 * @param col
	 * @param loadFromXML
	 * @return
	 */
	private boolean retrieveCrewMBTI(Member m , int row) {
				
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
	 * Get a person's personality type (MTBI) as shown in the radio buttons selected
	 * 
	 * @param col
	 * @return the MTBI string
	 */
	private String getSelectedPersonality(int col) {
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
	private DefaultComboBoxModel<String> setUpJobCBModel() {

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
		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();

		m.addElement(SELECT_SPONSOR);
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
		m.addAll(ReportingAuthorityFactory.getSupportedCodes());
		return m;
	}

	/**
	 * The SponsorListener class serves to listen to the change made in the country combo box. 
	 * It triggers a corresponding change in the country combo box.
	 * 
	 * @author mkhelios
	 *
	 */
	class SponsorListener implements ItemListener {

		// This method is called only if a new item has been selected.
		@SuppressWarnings("unchecked")
		public void itemStateChanged(ItemEvent evt) {

			int index = actionListeners.indexOf(this);
			
			if (evt.getStateChange() == ItemEvent.SELECTED && sponsorsComboBoxList.size() > 0) {
				// Item was just selected
		        WebComboBox m = sponsorsComboBoxList.get(index);
		        String sponsor = (String) m.getSelectedItem();
		        
				// Get combo box model
		        WebComboBox combo = countriesComboBoxList.get(index);
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        populateCountryCombo(sponsor, model);
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED && sponsorsComboBoxList.size() > 0) {
				// Item is no longer selected
				
		        WebComboBox combo = countriesComboBoxList.get(index);
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        // removing old data
		        model.removeAllElements();
		        
				model.addElement(SELECT_SPONSOR);

			}
		}
	}	 

	/**
	 * Load the country model from a ReportingAuthority
	 * @param sponsor
	 * @param model
	 */
	private void populateCountryCombo(String sponsorCode, DefaultComboBoxModel<String> model) {
		// removing old data
		model.removeAllElements();

		if (sponsorCode == null) {
			// Load all known countries
			// TODO need PersonConfig
			sponsorCode = ReportingAuthorityFactory.MS_CODE;
		}
		// Load the countries
		ReportingAuthority ra = ReportingAuthorityFactory.getAuthority(sponsorCode);
		for (String country : ra.getCountries()) {
			model.addElement(country);
		}
	}
	
	/**
	 * Get the current committed changes
	 * @return
	 */
	public Crew getCrewConfig() {
		return crew;
	}
	
	private boolean designateCrew(String crewName) {		
		Crew newCrew = crewConfig.loadCrew(crewName);
		if (newCrew == null) {
			return false;
		}
		crew = newCrew;
		crewNum = crew.getNumberOfConfiguredPeople();
		List<Member> members = crew.getTeam();
		for(int i = 0; i < members.size(); i++) {
			loadMember(members.get(i), i);
		}
		
		// Show alpha crew in title 
		f.setTitle(TITLE + " - " + crew.getName() + " Crew On-board");
		saveButton.setEnabled(!crew.isBundled());
		
		logger.config("crew.xml loaded.");
		return true;
	}
	
	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		crewConfig = null;
		simulationConfigEditor = null;
		f = null;
		mainPane = null;
		nameTFs.clear();
		nameTFs = null;
		ageTFs.clear();
		ageTFs = null;
		sponsorsComboBoxList = null;
		countriesComboBoxList = null;
		jobsComboBoxList = null;
	}


}
