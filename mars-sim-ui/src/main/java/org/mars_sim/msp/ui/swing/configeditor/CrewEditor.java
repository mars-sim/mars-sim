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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;

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
	private static Logger logger = Logger.getLogger(CrewEditor.class.getName());

	public static final String TITLE = "Crew Editor";

	private static final String SAVE_BETA = "Save as Beta Crew";
	private static final String COMMIT = "Commit Changes";
	private static final String LOAD_ALPHA = "Load Alpha Crew";
	private static final String LOAD_BETA = "Load Beta Crew";
	
	private static final String ALPHA = "alpha";
	private static final String BETA = "beta";
				
	public static final int ALPHA_CREW_ID = 0;
	public static final int BETA_CREW_ID = 1;
	public static final int PANEL_WIDTH = 180;
	public static final int WIDTH = (int)(PANEL_WIDTH * 3.5);
	public static final int HEIGHT = 512;
	
	// Data members
	private int crewID = 0;
	
	private int crewNum = 0;

	private List<JTextField> nameTFs = new ArrayList<JTextField>();

	private List<JTextField> ageTFs = new ArrayList<JTextField>();
	
	private List<List<JRadioButton>> allRadioButtons = new ArrayList<>();

	private List<WebComboBox> jobsComboBoxList = new ArrayList<WebComboBox>();

	private List<WebComboBox> countriesComboBoxList = new ArrayList<WebComboBox>(SimulationConfig.instance().getPersonConfig().createAllCountryList().size());

	// Note: sponsorsComboBoxList has the size of 3 only.
	// 1. sponsor's own country
	// 2. SpaceX
	// 3. Mars Society
	private List<WebComboBox> sponsorsComboBoxList = new ArrayList<WebComboBox>(3);

	private List<WebComboBox> destinationComboBoxList = new ArrayList<WebComboBox>();
	
	private List<WebSwitch> webSwitches = new ArrayList<>();
	
	private List<MyItemListener> actionListeners = new ArrayList<>(4);
	
	private WebDialog<?> f;
	
	private JPanel mainPane;
	private JPanel scrollPane;
	
	private List<Box> crewPanels = new ArrayList<>();
	
	private CrewConfig crewConfig;
	
	private SimulationConfigEditor simulationConfigEditor;

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

		crewNum = crewConfig.getNumberOfConfiguredPeople(crewID);
		
		createGUI();
	}


	/**
	 * Create a crew panel to house the attributes of a crewman
	 * 
	 * @return
	 */
	public Box createCrewPanel() {
		// Create attribute panel.
//		JPanel crewPanel = new JPanel();
		Box crewPanel = Box.createVerticalBox();
//		crewPanel.setLayout(new BoxLayout(crewPanel, BoxLayout.Y_AXIS));
//		crewPanel.setPreferredSize(new Dimension(CREW_WIDTH, HEIGHT));
		crewPanels.add(crewPanel);
		return crewPanel;
	}
	
	/**
	 * Creates the GUI
	 */
	public void createGUI() {
	
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
		
		for (int i=0; i<crewNum; i++) {
			Box crewPanel = createCrewPanel();
			// Add the Crewman title border
			String num = i + 1 + "";
			crewPanel.setBorder(BorderFactory.createTitledBorder("Crewman " + num));
			scrollPane.add(crewPanel);
		}
		
		// Create attribute header panel.	
//		attributeHeader = new JPanel(new GridLayout(8, 1));
//		mainPane.add(attributeHeader, BorderLayout.WEST);
	
//		attributeHeader.add(new JLabel("Name :   ", JLabel.RIGHT));
		// Add the name textfields
		setUpCrewName();

//		attributeHeader.add(new JLabel("Gender :   ", JLabel.RIGHT));
		// Add the gender combobox options
		setUpCrewGender();

		// Add the age textfields
		setUpCrewAge();
		
//		attributeHeader.add(new JLabel("Traits :   ", JLabel.RIGHT));
		// Add the personality traits checkboxes 
		setUpCrewPersonality();
		
//		attributeHeader.add(new JLabel("Job :   ", JLabel.RIGHT));
		// Add the job combobox options
		setUpCrewJob();

//		attributeHeader.add(new JLabel("Country :   ", JLabel.RIGHT));
		// Add the country combobox options
		setUpCrewCountry();
	
//		attributeHeader.add(new JLabel("Sponsor :   ", JLabel.RIGHT));
		// Add the sponsor combobox options
		setUpCrewSponsor();
		
//		attributeHeader.add(new JLabel("Destination :   ", JLabel.RIGHT));
		// Add the destination combobox options
		setUpDestination();
	
		// Create button panel.
		JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		mainPane.add(buttonPane, BorderLayout.SOUTH);

		// Create load alpha crew button.
		JButton loadAlphaButton = new JButton(LOAD_ALPHA);
		loadAlphaButton.addActionListener(this);
		buttonPane.add(loadAlphaButton);
		
		// Create load beta crew button.
		JButton loadBetaButton = new JButton(LOAD_BETA);
		loadBetaButton.addActionListener(this);
		buttonPane.add(loadBetaButton);		
		
		// Create commit button.
		JButton commitButton = new JButton(COMMIT);
		commitButton.addActionListener(this);
		buttonPane.add(commitButton);

		// Create commit button.
		JButton saveButton = new JButton(SAVE_BETA);
		saveButton.addActionListener(this);
		buttonPane.add(saveButton);
//		saveButton.setEnabled(false);
		
		// Manually trigger the country selection again so as to correctly 
		// set up the sponsor combobox at the start of the crew editor
		for (int i = 0; i < crewNum; i++) {
			final WebComboBox g = countriesComboBoxList.get(i);

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
		
		if (cmd.equals(LOAD_ALPHA)) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f, 
							"Are you sure you want to reload the default Alpha Crew? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Reloading Alpha Crew",
							JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {

				if (crewConfig.loadCrewDoc(ALPHA_CREW_ID)) {	
					
					designateCrew(ALPHA_CREW_ID);
					
					loadCrewNames(crewID);
					loadCrewGender(crewID);
					loadCrewAges(crewID);
					loadCrewJob(crewID);
					loadCrewCountry(crewID);
					loadCrewSponsor(crewID);
					loadDestination(crewID);
					loadCrewPersonality(crewID);
					
					// Show alpha crew in title 
					f.setTitle(TITLE + " - Alpha Crew On-board");
					
					logger.config("crew.xml loaded.");
				}
			}
		}

		else if (cmd.equals(LOAD_BETA)) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f, 
							"Are you sure you want to load the Beta Crew? " + System.lineSeparator()
							+ "All the changes made will be lost.",
							"Confirm Loading Beta Crew",
							JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				
				if (crewConfig.loadCrewDoc(BETA_CREW_ID)) {

					designateCrew(BETA_CREW_ID);
					
					loadCrewNames(crewID);
					loadCrewGender(crewID);
					loadCrewAges(crewID);
					loadCrewJob(crewID);
					loadCrewCountry(crewID);
					loadCrewSponsor(crewID);
					loadDestination(crewID);
					loadCrewPersonality(crewID);
					
					// Show beta crew in title 
					f.setTitle(TITLE + " - Beta Crew On-board");
					
					logger.config("beta_crew.xml loaded.");
				}
				
				else {
					JDialog.setDefaultLookAndFeelDecorated(true);
					JOptionPane.showMessageDialog(f, 
									"beta_crew.xml does not exist !",
									"File Not Found",
									JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		
		else if (cmd.equals(COMMIT)) {

			commitChanges();
			
			logger.config("'Commit Change' button clicked.");

		}
		
		else if (cmd.equals(SAVE_BETA)) {

			JDialog.setDefaultLookAndFeelDecorated(true);
			int result = JOptionPane.showConfirmDialog(f,
					"Are you sure you want to save the changes to beta_crew.xml ? " 
					+ System.lineSeparator() + System.lineSeparator()
					+ "Note : If you only want the changes to apply to " + System.lineSeparator()
					+ "the simulation you are setting up, choose " + System.lineSeparator()
					+ "'Commit Change' instead." + System.lineSeparator(),
					"Confirm Saving as Beta Crew",
					JOptionPane.YES_NO_CANCEL_OPTION);
			
			if (result == JOptionPane.YES_OPTION) {
				// Save to beta_crew.xml ...
				
				List<List<String>> roster = createRoster();
				if (!roster.isEmpty()) {
					crewConfig.writeCrewXML(roster);
					
					designateCrew(BETA_CREW_ID);
					
					// Show beta crew in title 
					f.setTitle(TITLE + " - Beta Crew On-board");	
				}
			}
		}
	}

	/**
	 * Creates the crew roster for the beta crew
	 *  
	 * @return
	 */
	public List<List<String>> createRoster() {
		boolean goodToGo = true;
		
		List<List<String>> roster = new ArrayList<>();
		
		for (int i = 0; i < crewNum; i++) {
			List<String> person = new ArrayList<>();
			
			System.out.print("");
			
			if (!checkNameFields(i, crewID, goodToGo)) {
				goodToGo = false;
				break;
			}
				
			person.add(nameTFs.get(i).getText().trim());			
			
			String genderStr = "";
			boolean isSelected = webSwitches.get(i).isSelected();
			
			if (isSelected)
				genderStr = GenderType.MALE.getName();
			else 
				genderStr = GenderType.FEMALE.getName();
			
			System.out.print(genderStr + ", ");
			person.add(genderStr);

			if (!checkAgeFields(i, crewID, goodToGo)) {
				goodToGo = false;
				break;
			}
			
			String ageStr = ageTFs.get(i).getText().trim();
			person.add(ageStr);
			System.out.print(ageStr + ", ");
			
			String personalityStr = getSelectedPersonality(i);
			person.add(personalityStr);
			System.out.print(personalityStr + ", ");
			
			String destinationStr = (String) destinationComboBoxList.get(i).getSelectedItem();
			person.add(destinationStr);
			System.out.print(destinationStr + ", ");
			
			ReportingAuthorityType sponsor = (ReportingAuthorityType) sponsorsComboBoxList.get(i).getSelectedItem();
			String sponsorStr = sponsor.toString();
			person.add(sponsorStr);
			System.out.print(sponsorStr + ", ");	
			
			String countryStr = (String) countriesComboBoxList.get(i).getSelectedItem();
			person.add(countryStr);
			System.out.print(countryStr + ", ");
			
			String jobStr = (String) jobsComboBoxList.get(i).getSelectedItem();
			person.add(jobStr);
			System.out.println(jobStr + ", ");
			
//			String maindish = crewConfig.getFavoriteMainDish(i, ALPHA_CREW);
//			crewConfig.setMainDish(i, maindish, ALPHA_CREW);
//			System.out.print(maindish + ", ");
//			
//			String sidedish = crewConfig.getFavoriteMainDish(i, ALPHA_CREW);
//			crewConfig.setSideDish(i, sidedish, ALPHA_CREW);
//			System.out.print(sidedish + ", ");
//			
//			String dessert = crewConfig.getFavoriteDessert(i, ALPHA_CREW);
//			crewConfig.setDessert(i, dessert, ALPHA_CREW);
//			System.out.print(dessert + ", ");
//			
//			String activity = crewConfig.getFavoriteActivity(i, ALPHA_CREW);
//			crewConfig.setActivity(i, activity, ALPHA_CREW);
//			System.out.print(activity + ", ");
			
			roster.add(person);

		}

		if (!goodToGo) {
			logger.config("Invalid field(s) detected. Please correct it before saving it.");
		}
		
		return roster;
	}
	
	/**
	 * Commits the changes to the crew profiles
	 */
	public void commitChanges() {
		boolean goodToGo = true;
		
		for (int i = 0; i < crewNum; i++) {
			
			if (crewID == ALPHA_CREW_ID)
				crewConfig.setPersonCrewName(i, ALPHA, crewID);
			else if (crewID == BETA_CREW_ID)
				crewConfig.setPersonCrewName(i, BETA, crewID);
			
			if (!checkNameFields(i, crewID, goodToGo)) {
				goodToGo = false;
				break;
			}
				
			if (!checkAgeFields(i, crewID, goodToGo)) {
				goodToGo = false;
				break;
			}
				
//			String genderStr = (String) genderComboBoxList.get(i).getSelectedItem();			
			String genderStr = "";
			boolean isSelected = webSwitches.get(i).isSelected();
			
			if (isSelected)
				genderStr = GenderType.MALE.getName();
			else 
				genderStr = GenderType.FEMALE.getName();
		
			crewConfig.setPersonGender(i, genderStr, crewID);
			System.out.print(genderStr + ", ");	
			
			String personalityStr = getSelectedPersonality(i);
			crewConfig.setPersonPersonality(i, personalityStr, crewID);
			System.out.print(personalityStr + ", ");
			
			String destinationStr = (String) destinationComboBoxList.get(i).getSelectedItem();
			crewConfig.setPersonDestination(i, destinationStr, crewID);
			System.out.print(destinationStr + ", ");
			
			ReportingAuthorityType sponsor = (ReportingAuthorityType) sponsorsComboBoxList.get(i).getSelectedItem();
			crewConfig.setPersonSponsor(i, sponsor, crewID);
			System.out.print(sponsor + ", ");
			
			String countryStr = (String) countriesComboBoxList.get(i).getSelectedItem();
			crewConfig.setPersonCountry(i, countryStr, crewID);
			System.out.print(countryStr + ", ");
			
			String jobStr = (String) jobsComboBoxList.get(i).getSelectedItem();
			crewConfig.setPersonJob(i, jobStr, crewID);
			System.out.println(jobStr);
			
			String maindish = crewConfig.getFavoriteMainDish(i, crewID);
			crewConfig.setMainDish(i, maindish, crewID);
			
			String sidedish = crewConfig.getFavoriteMainDish(i, crewID);
			crewConfig.setSideDish(i, sidedish, crewID);
			
			String dessert = crewConfig.getFavoriteDessert(i, crewID);
			crewConfig.setDessert(i, dessert, crewID);
			
			String activity = crewConfig.getFavoriteActivity(i, crewID);
			crewConfig.setActivity(i, activity, crewID);
			
		}

		if (goodToGo) {
//			simulationConfigEditor.setCrewEditorOpen(false);
			f.setVisible(false);
			f.dispose();
		}
	}
	
	/**
	 * Checks if the name textfields are valid
	 * 
	 * @param i
	 * @param goodToGo
	 * @return
	 */
	public boolean checkNameFields(int i, int crewID, boolean goodToGo) {
		
//		String destinationStr = (String) destinationCB.getValue();
//		destinationName = destinationStr;

		String nameStr = nameTFs.get(i).getText().trim();
		// Use isBlank() to check against invalid names
		if (!Conversion.isBlank(nameStr)
				&& nameStr.contains(" ")) {
			System.out.print(nameStr + ", ");
			crewConfig.setPersonName(i, nameStr, crewID);
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
	public boolean checkAgeFields(int i, int crewID, boolean goodToGo) {
		
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
				System.out.print(s + ", ");
				crewConfig.setPersonAge(i, s, crewID);
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
	public static boolean isNumeric(String str) { 
		try {  
			Integer.parseInt(str);
//		    Double.parseDouble(str);  
			return true;
		} catch(NumberFormatException e){  
			return false;  
		}  
	}
	
	/**
	 * Sets up the name textfields
	 */
	public void setUpCrewName() {
    	SvgIcon icon = IconManager.getIcon ("info_red");//new LazyIcon("info").getIcon();
//    	icon.apply(new SvgStroke(Color.ORANGE));
    	
		for (int i = 0; i < crewNum; i++) {
//			int crew_id = cc.getCrew(i);
//			System.out.println("setUpCrewName:: i is " + i);
//			System.out.println("setUpCrewName:: crewNum is " + crewNum);
			String n = crewConfig.getConfiguredPersonName(i, ALPHA_CREW_ID, true);
			WebTextField tf = new WebTextField(15);
			tf.setMargin(3, 0, 3, 0);
			final WebOverlay overlay = new WebOverlay(StyleId.overlay);
			overlay.setContent(tf);
	        final WebLabel overlayLabel = new WebLabel(icon);
			onChange(tf, overlayLabel, overlay);
			WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			panel.add(new WebLabel("   Name : "));
			panel.add(tf);
//			panel.setPreferredSize(new Dimension(CREW_WIDTH, 30));
//			panel.setSize(CREW_WIDTH, 30);
			nameTFs.add(tf);
			crewPanels.get(i).add(panel);
			tf.setText(n);
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
//                        	System.out.println("The name textfield is blank.");
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
	 * Loads the names of the crew into the name textfields
	 */
	public void loadCrewNames(int crewID) {
		for (int i = 0; i < crewNum; i++) {
//			int crew_id = cc.getCrew(i);
//			System.out.println("setUpCrewName:: i is " + i);
//			System.out.println("setUpCrewName:: crewNum is " + crewNum);
			String n = crewConfig.getConfiguredPersonName(i, crewID, true);

			JTextField tf = nameTFs.get(i);
			tf.setText(n);
		}
	}

	/**
	 * Sets up the age textfields
	 */
	public void setUpCrewAge() {
		for (int i = 0; i < crewNum; i++) {
			
			int age = 0;
			String ageStr = crewConfig.getConfiguredPersonAge(i, ALPHA_CREW_ID, true);
			if (ageStr == null)
				age = RandomUtil.getRandomInt(21, 65);
			else
				age = Integer.parseInt(ageStr);		
			
			JTextField tf = new JTextField(5);
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
			panel.add(new WebLabel("      Age : "));
			panel.add(tf);

			ageTFs.add(tf);
			crewPanels.get(i).add(panel);
			tf.setText(age + "");
		}
	}

	
	/**
	 * Get the random age for each crew member into the age textfields
	 */
	public void loadCrewAges(int crewID) {
		for (int i = 0; i < crewNum; i++) {
			int age = 0;
			String ageStr = crewConfig.getConfiguredPersonAge(i, crewID, true);
			if (ageStr == null)
				age = RandomUtil.getRandomInt(21, 65);
			else
				age = Integer.parseInt(ageStr);	
			JTextField tf = ageTFs.get(i);
			tf.setText(age + "");
		}
	}
	
//	/**
//	 * Sets up the gender combo box model
//	 * @return
//	 */
//	public DefaultComboBoxModel<String> setUpGenderCBModel() {
//
//		List<String> genderList = new ArrayList<String>(2);
//		genderList.add("M");
//		genderList.add("F");
//		DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
//
//		Iterator<String> i = genderList.iterator();
//		while (i.hasNext()) {
//			String s = i.next();
//			m.addElement(s);
//		}
//
//		return m;
//	}

	/**
	 * Sets up the combo box for a few attributes
	 * 
	 * @param choice
	 * @param s
	 * @return
	 */
	public WebComboBox setUpCB(int choice, String s) {
		DefaultComboBoxModel<String> m = null;
//		if (choice == 0)
//			m = setUpGenderCBModel();
//		else 
		if (choice == 2)
			m = setUpJobCBModel();
		
		else if (choice == 3) 
			m = setUpCountryCBModel();

		else if (choice == 5) 
			m = setUpDestinationCBModel(s);
		
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
	 * Sets up the crew gender
	 */
	public void setUpCrewGender() {

//		String s[] = new String[crewNum];
		String s = "";
		for (int j = 0; j < crewNum; j++) {
			GenderType n = crewConfig.getConfiguredPersonGender(j, ALPHA_CREW_ID, true);

			s = n.toString();
			
			WebSwitch webSwitch = new WebSwitch(true);
			
			if (s.equalsIgnoreCase(GenderType.MALE.getName())) {
//				s[j] = "M";
				webSwitch.setSelected(true);
			}
			else {
//				s[j] = "F";
				webSwitch.setSelected(false);
			}
			
			webSwitches.add(webSwitch);
			webSwitch.setSwitchComponents(
					"M", 
					"F");
			TooltipManager.setTooltip(webSwitch, "Choose male or female", TooltipWay.down);
			
			WebPanel panel = new WebPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
//			panel.setPreferredSize(new Dimension(55, 35));
//			panel.setSize(55, 35);
//			panel.setMaximumSize(55, 35);
			panel.add(new WebLabel(" Gender : "));
			panel.add(webSwitch);
			
			crewPanels.get(j).add(panel);
			
//			webSwitch.addActionListener(new ActionListener() {
//				public void actionPerformed(ActionEvent e) {
////					masterClock.setPaused(!masterClock.isPaused(), false);
//					if (webSwitch.isSelected())
//						;
//					else
//						;
//				};
//			});
				
//			WebComboBox g = setUpCB(0, ""); // 0 = Gender
//			g.setMaximumRowCount(2);
//			crewPanels.get(j).add(g);
//			genderComboBoxList.add(g);
//			g.setSelectedItem(s[j]);
		}
	}

	/**
	 * Loads the crew gender
	 */
	public void loadCrewGender(int crewID) {

//		String s[] = new String[crewNum];
		String s = "";
		for (int j = 0; j < crewNum; j++) {
			GenderType n = crewConfig.getConfiguredPersonGender(j, crewID, true);
			
			s = n.toString();
			
			WebSwitch webSwitch = webSwitches.get(j);
			
			if (s.equalsIgnoreCase(GenderType.MALE.getName())) {
//				s[j] = "M";
				webSwitch.setSelected(true);
			}
			else {
//				s[j] = "F";
				webSwitch.setSelected(false);
			}
			
			
//			WebComboBox g = genderComboBoxList.get(j);
//			g.setSelectedItem(s[j]);
			
		}
	}
	
	

	
	/**
	 * Set up personality radio buttons
	 * 
	 * @param col
	 */
	public void setUpCrewPersonality() {
		String quadrant1A = "{E:u}xtravert";
		String quadrant1B = "{I:u}ntrovert"; 
		String quadrant2A = "I{N:u}tuition";
		String quadrant2B = "{S:u}ensing";
		String quadrant3A = "{F:u}eeling"; 
		String quadrant3B = "{T:u}hinking";
		String quadrant4A = "{J:u}udging";
		String quadrant4B = "{P:u}erceiving";
		String cat1 = "World", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
		String a = null, b = null, c = null;
		
		for (int col = 0; col < crewNum; col++) {
			JPanel fullPane = new JPanel(new FlowLayout());
			fullPane.setLayout(new BoxLayout(fullPane, BoxLayout.Y_AXIS));
			fullPane.setPreferredSize(new Dimension(PANEL_WIDTH, 200));
			fullPane.setSize(PANEL_WIDTH, 200);
			fullPane.setMaximumSize(new Dimension(PANEL_WIDTH, 200));
//			ppane.setAlignmentX(0);
			
			List<JRadioButton> radioButtons = new ArrayList<>();	
					
			for (int row = 0; row < 4; row++) {
				
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

				WebRadioButton ra = new WebRadioButton(StyleId.radiobuttonStyled, a);
				ra.addActionListener(this);
//				ra.setActionCommand("a" + row + col);
				
				WebRadioButton rb = new WebRadioButton(StyleId.radiobuttonStyled, b);
				rb.addActionListener(this);
//				rb.setActionCommand("b" + row + col);
				
				if (retrieveCrewMBTI(row, col, ALPHA_CREW_ID, true))
					ra.setSelected(true);
				else
					rb.setSelected(true);
				
				radioButtons.add(ra);
				radioButtons.add(rb);
				
				// Use ButtonGroup to limit to choosing either ra or rb
				ButtonGroup bg1 = new ButtonGroup();
				bg1.add(ra);
				bg1.add(rb);
				
				JPanel quadPane = new JPanel(new GridLayout(1, 2));
//				quadPane.setLayout(new BoxLayout(quadPane, BoxLayout.Y_AXIS));
//				quadPane.setAlignmentX(Component.LEFT_ALIGNMENT);
				quadPane.setPreferredSize(new Dimension(PANEL_WIDTH/2, 60));
//				quadPane.setMinimumSize(new Dimension(CREW_WIDTH/2, 60));
//				quadPane.setSize(CREW_WIDTH/2, 60);
				quadPane.setBorder(BorderFactory.createTitledBorder(c));
				quadPane.add(ra);
				quadPane.add(rb);
				quadPane.setAlignmentX(Component.CENTER_ALIGNMENT);
				fullPane.add(quadPane);
			}
			
			crewPanels.get(col).add(fullPane);
			
			allRadioButtons.add(radioButtons);
		}
	}

	/**
	 * Loads crew personality
	 * 
	 * @param col
	 */
	public void loadCrewPersonality(int crewID) {
		for (int col = 0; col < crewNum; col++) {

			List<JRadioButton> radioButtons = allRadioButtons.get(col);
			
			for (int row = 0; row < 4; row++) {

				JRadioButton ra = radioButtons.get(2 * row);
				JRadioButton rb = radioButtons.get(2 * row + 1);
						
				if (retrieveCrewMBTI(row, col, crewID, true))
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
	public boolean retrieveCrewMBTI(int row, int col, int crewID, boolean loadFromXML) {
				
		if (row == 0)
			return crewConfig.isExtrovert(col, crewID, loadFromXML);
		else if (row == 1)
			return crewConfig.isIntuitive(col, crewID, loadFromXML);
		else if (row == 2)
			return crewConfig.isFeeler(col, crewID, loadFromXML);
		else if (row == 3)
			return crewConfig.isJudger(col, crewID, loadFromXML);
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
	public DefaultComboBoxModel<ReportingAuthorityType> setUpSponsorCBModel(String country) {

		List<ReportingAuthorityType> sponsors = new ArrayList<>();

		sponsors.add(ReportingAuthorityType.MS);
//		// Retrieve the sponsor from the selected country 		
		if (!country.isBlank())
			sponsors.add(mapCountry2Sponsor(country));		
				
		DefaultComboBoxModel<ReportingAuthorityType> m = new DefaultComboBoxModel<>();
		Iterator<ReportingAuthorityType> j = sponsors.iterator();

		while (j.hasNext()) {
			ReportingAuthorityType s = j.next();
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
		
		if (m.getIndexOf(destination) != -1 && destinations.contains(destination))
			m.setSelectedItem(destination);
		
		return m;
	}
	
	/**
	 * Set up the job comboxbox
	 * 
	 */
	public void setUpCrewJob() {
		for (int i = 0; i < crewNum; i++) {
			String n = crewConfig.getConfiguredPersonJob(i, ALPHA_CREW_ID, false);
			WebComboBox g = setUpCB(2, n);// 2 = Job
			TooltipManager.setTooltip(g, "Choose the job of this person", TooltipWay.down);
			g.setMaximumRowCount(8);
			crewPanels.get(i).add(g);
			g.getModel().setSelectedItem(n);
			jobsComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew job
	 * 
	 */
	public void loadCrewJob(int crewID) {
		for (int i = 0; i < crewNum; i++) {
			String n = crewConfig.getConfiguredPersonJob(i, crewID, true);
			WebComboBox g = jobsComboBoxList.get(i); //setUpCB(2, n[i]);// 2 = Job

			g.getModel().setSelectedItem(n);
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
			n[i] = crewConfig.getConfiguredPersonCountry(i, ALPHA_CREW_ID, false);
			WebComboBox g = setUpCB(3, n[i]); // 3 = Country
			TooltipManager.setTooltip(g, "Choose the country of origin of this person", TooltipWay.down);
			g.setMaximumRowCount(8);
			crewPanels.get(i).add(g);
			g.getModel().setSelectedItem(n[i]);
			countriesComboBoxList.add(g);
			
			// Set up and add an item listener to the country combobox
			MyItemListener l = new MyItemListener();
			actionListeners.add(l);
		    g.addItemListener(l);
		}
	}

	/**
	 * Loads the crew's country
	 * 
	 */
	public void loadCrewCountry(int crewID) {
		int SIZE = UnitManager.getAllCountryList().size();
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE];
			n[i] = crewConfig.getConfiguredPersonCountry(i, crewID, true);
			WebComboBox g = countriesComboBoxList.get(i); //setUpCB(3, n[i]); // 3 = Country

			g.getModel().setSelectedItem(n[i]);
		}
	}
			
	/**
	 * Set up the sponsor combox box
	 * 
	 */
	public void setUpCrewSponsor() {
		for (int i = 0; i < crewNum; i++) {
			ReportingAuthorityType s = crewConfig.getConfiguredPersonSponsor(i, ALPHA_CREW_ID, false);
			String c = crewConfig.getConfiguredPersonCountry(i, ALPHA_CREW_ID, false);
			DefaultComboBoxModel<ReportingAuthorityType> m = setUpSponsorCBModel(c);

			WebComboBox g = new WebComboBox(StyleId.comboboxHover, m);
			g.setWidePopup(true);
			g.setPreferredWidth(PANEL_WIDTH);
			g.setMaximumWidth(PANEL_WIDTH);
			
			g.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e1) {
					Object s = g.getSelectedItem();

					g.setSelectedItem(s);
				}
			});

			
			TooltipManager.setTooltip(g, "Choose the sponsor of this person", TooltipWay.down);
			g.setMaximumRowCount(8);
			crewPanels.get(i).add(g);
			g.getModel().setSelectedItem(s);
			sponsorsComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew's sponsor
	 * 
	 */
	public void loadCrewSponsor(int crewID) {
		for (int i = 0; i < crewNum; i++) {
			ReportingAuthorityType s = crewConfig.getConfiguredPersonSponsor(i, crewID, true);
			WebComboBox g = sponsorsComboBoxList.get(i); // setUpCB(4, n[i]); // 4 = Sponsor
			
			g.getModel().setSelectedItem(s);
		}
	}
	
	/**
	 * Set up the destination combox box
	 * 
	 */
	public void setUpDestination() {
		int SIZE = 5;
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE]; // 10
			n[i] = crewConfig.getConfiguredPersonDestination(i, ALPHA_CREW_ID, false);
			WebComboBox g = setUpCB(5, n[i]); // 5 = Destination
			TooltipManager.setTooltip(g, "Choose the settlement destination of this person", TooltipWay.down);
			g.setMaximumRowCount(5);
			crewPanels.get(i).add(g);
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel)g.getModel();
			if (model.getIndexOf(n[i]) != -1)
				model.setSelectedItem(n[i]);
			destinationComboBoxList.add(g);
		}
	}

	/**
	 * Loads the crew's destination
	 * 
	 */
	public void loadDestination(int crewID) {
		int SIZE = 5;
		for (int i = 0; i < crewNum; i++) {
			String n[] = new String[SIZE]; // 10
			n[i] = crewConfig.getConfiguredPersonDestination(i, crewID, true);
			WebComboBox g = destinationComboBoxList.get(i); //setUpCB(5, n[i]); // 5 = Destination
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel)g.getModel();
			if (model.getIndexOf(n[i]) != -1)
				g.getModel().setSelectedItem(n[i]);
		}
	}
	
	/**
	 * The MyItemListener class serves to listen to the change made in the country combo box. 
	 * It triggers a corresponding change in the sponsor combo box.
	 * 
	 * @author mkhelios
	 *
	 */
	class MyItemListener implements ItemListener {
		// This method is called only if a new item has been selected.
		@SuppressWarnings("unchecked")
		public void itemStateChanged(ItemEvent evt) {

			int index = actionListeners.indexOf(this);
			
			Object item = evt.getItem();

			if (evt.getStateChange() == ItemEvent.SELECTED && sponsorsComboBoxList.size() > 0) {
				// Item was just selected
		        WebComboBox m = sponsorsComboBoxList.get(index);
		        
				// Get combo box model
		        DefaultComboBoxModel<ReportingAuthorityType> model =
		        		(DefaultComboBoxModel<ReportingAuthorityType>) m.getModel();
		        
		        // removing old data
		        model.removeAllElements();

		        // Add MS and SPACEX as the universally available options
	            model.addElement(ReportingAuthorityType.MS);
	            model.addElement(ReportingAuthorityType.SPACEX);
	            
				String countryStr = (String) item;
				
	            if (!countryStr.isBlank()) {
					ReportingAuthorityType sponsor = mapCountry2Sponsor(countryStr);            
					model.addElement(sponsor);
	            }
		        
			} else if (evt.getStateChange() == ItemEvent.DESELECTED && sponsorsComboBoxList.size() > 0) {
				// Item is no longer selected
				
		        WebComboBox m = sponsorsComboBoxList.get(index);
		        
				// Get combo box model
		        DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) m.getModel();
		        
		        // removing old data
		        model.removeAllElements();
		        
				//model.addElement("To be determined");

			}
		}
	}
		 
	
	/**
	 * Maps the country to its sponsor
	 * 
	 * @param country
	 * @return sponsor
	 */
	private static ReportingAuthorityType mapCountry2Sponsor(String country) {
		int id = SimulationConfig.instance().getPersonConfig().getCountryNum(country);
		if (id == 0)
			return ReportingAuthorityType.CNSA;
		else if (id == 1)
			return ReportingAuthorityType.CSA;
		else if (id == 2)
			return ReportingAuthorityType.ISRO;
		else if (id == 3)
			return ReportingAuthorityType.JAXA;
		else if (id == 4)
			return ReportingAuthorityType.NASA; 
		else if (id == 5)			
			return ReportingAuthorityType.RKA;	
		else //if (id >= 6)
			return ReportingAuthorityType.ESA;
	}
	

	public void designateCrew(int crewID) {
		crewConfig.setSelectedCrew(crewID);
		this.crewID = crewID;
		crewNum = crewConfig.getNumberOfConfiguredPeople(crewID);
	}
	
	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		crewConfig = null;
		simulationConfigEditor = null;
		f = null;
		mainPane = null;
//		attributeHeader = null;
		nameTFs.clear();
		nameTFs = null;
		ageTFs.clear();
		ageTFs = null;
		sponsorsComboBoxList = null;
		countriesComboBoxList = null;
		jobsComboBoxList = null;
//		genderComboBoxList = null;
	}

}
