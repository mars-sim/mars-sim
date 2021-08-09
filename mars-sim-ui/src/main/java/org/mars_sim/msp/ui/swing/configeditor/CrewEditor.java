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
	private class MemberPanel implements ItemListener {
		WebComboBox countryCB;
		WebComboBox sponsorCB;
		WebComboBox jobCB;
		List<JRadioButton> radioButtons;
		private Box crewPanel;
		private WebTextField nametf;
		private WebSwitch webSwitch;
		private JTextField agetf;

		/**
		 * Create a crew panel to house the attributes of a crewman
		 * 
		 * @return
		 */
		MemberPanel(int i) {
	    	SvgIcon icon = IconManager.getIcon ("info_red");//new LazyIcon("info").getIcon();
			final WebOverlay overlay = new WebOverlay(StyleId.overlay);
	        final WebLabel overlayLabel = new WebLabel(icon);

			crewPanel = Box.createVerticalBox();

			// Name 
			nametf = new WebTextField(15);
			nametf.setMargin(3, 0, 3, 0);
			overlay.setContent(nametf);
			onChange(nametf, overlayLabel, overlay);
			WebPanel namePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			namePanel.add(new WebLabel("   Name : "));
			namePanel.add(nametf);
			crewPanel.add(namePanel);
			
			// Gender
			webSwitch = new WebSwitch(true);		
			webSwitch.setSwitchComponents(
					"M", 
					"F");
			TooltipManager.setTooltip(webSwitch, "Choose male or female", TooltipWay.down);
			WebPanel genderPanel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			genderPanel.add(new WebLabel(" Gender : "));
			genderPanel.add(webSwitch);
			crewPanel.add(genderPanel);	
			
			// Age
			agetf = new JTextField(5);
			JPanel agePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			agePanel.add(new WebLabel("      Age : "));
			agePanel.add(agetf);
			crewPanel.add(agePanel);
					
			// Personallity
			JPanel fullPane = new JPanel(new FlowLayout());
			fullPane.setLayout(new BoxLayout(fullPane, BoxLayout.Y_AXIS));
			fullPane.setPreferredSize(new Dimension(PANEL_WIDTH, 200));
			fullPane.setSize(PANEL_WIDTH, 200);
			fullPane.setMaximumSize(new Dimension(PANEL_WIDTH, 200));	
			radioButtons = new ArrayList<>();
			for (int row = 0; row < 4; row++) {
				WebRadioButton ra = new WebRadioButton(StyleId.radiobuttonStyled, 
								QUADRANT_A[row]);
				//ra.addActionListener(this);
				
				WebRadioButton rb = new WebRadioButton(StyleId.radiobuttonStyled, 
								QUADRANT_B[row]);
				//rb.addActionListener(this);	
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
			
			// Job
			jobCB = setUpCB(2);// 2 = Job
			TooltipManager.setTooltip(jobCB, "Choose the job of this person", TooltipWay.down);
			jobCB.setMaximumRowCount(8);
			crewPanel.add(jobCB);
			
			// Sponsor
			DefaultComboBoxModel<String> m = setUpSponsorCBModel();
			sponsorCB = new WebComboBox(StyleId.comboboxHover, m);
			sponsorCB.setWidePopup(true);
			sponsorCB.setPreferredWidth(PANEL_WIDTH);
			sponsorCB.setMaximumWidth(PANEL_WIDTH);
		    			
			TooltipManager.setTooltip(sponsorCB, "Choose the sponsor of this person", TooltipWay.down);
			sponsorCB.setMaximumRowCount(8);
			crewPanel.add(sponsorCB);

			// Set up and add an item listener to the country combobox
			sponsorCB.addItemListener(this);		    
		    
			// Country
			countryCB = setUpCB(3); // 3 = Country
			TooltipManager.setTooltip(countryCB, "Choose the country of origin of this person", TooltipWay.down);
			countryCB.setMaximumRowCount(8);
			crewPanel.add(countryCB);
			
			// Add the Crewman title border
			String num = i + 1 + "";
			crewPanel.setBorder(BorderFactory.createTitledBorder("Crewman " + num));
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
			if (webSwitch.isSelected())
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
			webSwitch.setSelected(m.getGender() == GenderType.MALE);
	        
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
		@SuppressWarnings("unchecked")
		public void itemStateChanged(ItemEvent evt) {
		
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				// Item was just selected
		        WebComboBox m = sponsorCB;
		        String sponsor = (String) m.getSelectedItem();
		        
				// Get combo box model
		        WebComboBox combo = countryCB;
		        DefaultComboBoxModel<String> model =
		        		(DefaultComboBoxModel<String>) combo.getModel();
		        
		        populateCountryCombo(sponsor, model);
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED) {
				// Item is no longer selected
				
		        WebComboBox combo = countryCB;
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

	public static final String TITLE = "Crew Editor";
	private static final String SELECT_SPONSOR = "Select Sponsor";

	private static final String SAVE_CREW = "Save Crew";
	private static final String SAVE_NEW_CREW = "Save As New Crew";
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

	private static final String SETTLEMENT_SPONSOR = "Settlement Sponsor";
	
	// Data members		
	private WebDialog<?> f;
	
	private JPanel mainPane;
	private JPanel scrollPane;
	
	private List<MemberPanel> crewPanels = new ArrayList<>();
	
	private CrewConfig crewConfig;

	private SimulationConfigEditor simulationConfigEditor;

	private Crew crew;

	private JButton saveButton;

	private DefaultComboBoxModel<String> crewCB;

	private WebTextField descriptionTF;


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
		
		// Start with first crew
		String defaultCrew = crewConfig.getKnownCrewNames().get(0);
		this.crew = config.getCrew(defaultCrew);
		
		createGUI();
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
			mp = new MemberPanel(i);
			crewPanels.add(mp);
			scrollPane.add(mp.crewPanel);
		}
		
		mp.loadMember(m);
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
				
		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Crew name selection
		buttonPane.add(new WebLabel("Crew Loaded :"));
		crewCB = new DefaultComboBoxModel<>();
		crewCB.addAll(0, crewConfig.getKnownCrewNames());
		crewCB.setSelectedItem(crew.getName());
		JComboBox<String> crewSelector = new JComboBox<>(crewCB) ;
		crewSelector.addActionListener(this);
		crewSelector.setActionCommand(LOAD_CREW);
		buttonPane.add(crewSelector);
		
		// Description field
		buttonPane.add(new WebLabel("   Description : "));
		descriptionTF = new WebTextField(15);
		buttonPane.add(descriptionTF);
		
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

		// Load the first crew
		designateCrew(crew.getName());

		
		// Set up the frame to be visible
		f.pack();
		f.setAlwaysOnTop(true);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
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
			if (!crew.getName().equalsIgnoreCase(loadCrew)) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				int result = JOptionPane.showConfirmDialog(f, 
								"Are you sure you want to reload the Crew " + loadCrew + " ? " + System.lineSeparator()
								+ "All the changes made will be lost.",
								"Confirm Reloading Crew",
								JOptionPane.YES_NO_CANCEL_OPTION);
				
				if (result == JOptionPane.YES_OPTION) {
					designateCrew(loadCrew);
				}
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
				commitChanges(crew.getName());
				crewConfig.save(crew);
			}
			break;

		case SAVE_NEW_CREW:
			JDialog.setDefaultLookAndFeelDecorated(true);
			String newCrewName = (String)JOptionPane.showInputDialog(
                    f, "Enter the name of the new Crew.");

			if ((newCrewName != null) && (newCrewName.length() > 0)) {
				// Create new Crew
				commitChanges(newCrewName);
				crewConfig.save(crew); 
				f.setTitle(TITLE + " - " + newCrewName + " Crew On-board");	
				
				crewCB.addElement(newCrewName);
				crewCB.setSelectedItem(newCrewName);
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
	private boolean commitChanges(String name) {
		boolean goodToGo = true;
		Crew newCrew = new Crew(name, false);
		for (MemberPanel mp : crewPanels) {
			// Find member
			Member m = mp.toMember();
			newCrew.addMember(m);
		}
		
		newCrew.setDescription(descriptionTF.getText());
		
		crew = newCrew;
		return goodToGo;
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
		m.addElement(SETTLEMENT_SPONSOR);
		m.addAll(ReportingAuthorityFactory.getSupportedCodes());
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
		Crew newCrew = crewConfig.getCrew(crewName);
		if (newCrew == null) {
			return false;
		}
		crew = newCrew;
		List<Member> members = crew.getTeam();
		for(int i = 0; i < members.size(); i++) {
			loadMember(members.get(i), i);
		}
		
		// Show alpha crew in title 
		f.setTitle(TITLE + " - " + crew.getName() + " Crew On-board");
		saveButton.setEnabled(!crew.isBundled());
		
		descriptionTF.setText(crew.getDescription());
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
	}


}
