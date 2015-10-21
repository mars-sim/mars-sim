/**
 * Mars Simulation Project
 * BuildingPanel.java
 * @version 3.08 2015-03-28
 * @author Scott Davis
 */
package org.mars_sim.msp.ui.swing.unit_window.structure.building;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Farming;
import org.mars_sim.msp.core.structure.building.function.FoodProduction;
import org.mars_sim.msp.core.structure.building.function.LifeSupport;
import org.mars_sim.msp.core.structure.building.function.Manufacture;
import org.mars_sim.msp.core.structure.building.function.MedicalCare;
import org.mars_sim.msp.core.structure.building.function.PowerStorage;
import org.mars_sim.msp.core.structure.building.function.Research;
import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
import org.mars_sim.msp.core.structure.building.function.Storage;
import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.tool.DropShadowBorder;
import org.mars_sim.msp.ui.swing.tool.settlement.SettlementMapPanel;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelFoodProduction;
import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelPreparingDessert;

/**
 * The BuildingPanel class is a panel representing a settlement building.
 */
public class BuildingPanel
extends JPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(BuildingPanel.class.getName());

	/** The name of the panel. */
	private String panelName;

	private String newName;

	private boolean isTranslucent = false;
	/** The function panels. */
	private List<BuildingFunctionPanel> functionPanels;


	private JLabel buildingNameLabel;
	private JPanel namePanel;

	/** The building this panel is for. */
	private Building building;

	private MainDesktopPane desktop;

	/**
	 * Constructor 1
	 *
	 * @param panelName the name of the panel.
	 * @param building the building this panel is for.
	 * @param desktop the main desktop.
	 * @throws MalformedURLException
	 */
	public BuildingPanel(String panelName, Building building, MainDesktopPane desktop) {
		super();

        // Initialize data members
        this.panelName = panelName;
        this.building = building;
        this.desktop = desktop;

        init();
	}

	/**
	 * Constructor 2
	 *
	 * @param isTranslucent
	 * @param panelName the name of the panel.
	 * @param building the building this panel is for.
	 * @param desktop the main desktop.
	 */
	// 2014-11-27 Added Constructor 2
	public BuildingPanel(boolean isTranslucent, String panelName, Building building, MainDesktopPane desktop) {

        // Initialize data members
        this.panelName = panelName;
        this.building = building;
        this.desktop = desktop;
		this.isTranslucent = isTranslucent;
        if (isTranslucent) {
        	//JComponent c = getRootPane();
        	//setOpaque(false);
        	//setBackground(new Color(0,0,0,128));
        }
		init();
	}


	//2015-05-01 Added setupDetailButton
	public void setupDetailButton() {

			JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
			JButton detailB = new JButton(Msg.getString(
					"BuildingPanel.detailButton")); //$NON-NLS-1$
			detailB.setPreferredSize(new Dimension(60, 20));
			detailB.setFont(new Font("Serif", Font.PLAIN, 9));
			detailB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					if (building != null) {
						desktop.openUnitWindow(building, false);
					}
				}
			});
			btnPanel.add(detailB);
			namePanel.add(btnPanel);
	        //if (isTranslucent) {
	        	//detailB.setOpaque(false);
	        	//detailB.setBackground(new Color(0,0,0,128));
	        	//detailB.setForeground(Color.ORANGE);
	        //}
			//setPanelStyle(btnPanel);

	}
	/**
	 * Initializes the BuildingPanel
	 */
	// 2015-01-01 init()
	public void init() {

        this.functionPanels = new ArrayList<BuildingFunctionPanel>();
        // Set style and layout
        setPanelStyle(this);
        setLayout(new BorderLayout(0, 0));

        // 2014-11-27 Added namePanel and buildingNameLabel
        namePanel = new JPanel(new GridLayout(2,1,0,0));
		//setupDetailButton();

        //scrollPanel.setPreferredSize(new Dimension(200, 220));
        buildingNameLabel = new JLabel(building.getNickName(), JLabel.CENTER);
        //final ShadowLabel buildingNameLabel = new ShadowLabel(building.getNickName());

        buildingNameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        if (isTranslucent) {
        	//buildingNameLabel.setForeground(Color.YELLOW);
        } else
        	buildingNameLabel.setForeground(new Color(102, 51, 0)); // dark brown

        namePanel.add(buildingNameLabel);
        add(namePanel, BorderLayout.NORTH);
		setPanelStyle(namePanel);


		//2014-11-27  Added renameBtn for renaming a building
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		JButton renameBtn = new JButton(Msg.getString(
				"BuildingPanel.renameBuilding.renameButton")); //$NON-NLS-1$
		renameBtn.setPreferredSize(new Dimension(60, 20));
		renameBtn.setFont(new Font("Serif", Font.PLAIN, 9));
	    renameBtn.setBackground(Color.GRAY);
	    renameBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// if rename is done successfully, then update the building name
				//boolean isRenamed =
				renameBuilding();
				//if (isRenamed)
				//if (newName != null)
				buildingNameLabel.setText(newName);
			}
		});
		btnPanel.add(renameBtn);
		namePanel.add(btnPanel);
        if (isTranslucent) {
    	    renameBtn.setBackground(Color.GRAY);
        }
		setPanelStyle(btnPanel);

	    // Prepare function list panel.
        JPanel functionListPanel = new JPanel();
        functionListPanel.setLayout(new BoxLayout(functionListPanel, BoxLayout.Y_AXIS));

        // Prepare function scroll panel.
        JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(functionListPanel);
        //CustomScroll scrollPanel = new CustomScroll(functionListPanel);
        scrollPanel.setPreferredSize(new Dimension(290, 280));
        scrollPanel.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPanel, BorderLayout.CENTER);

        setPanelStyle(functionListPanel);

		if (isTranslucent) {
			scrollPanel.setOpaque(false);
			//scrollPanel.getViewport().setOpaque(false);
			scrollPanel.setBorder(BorderFactory.createLineBorder(Color.orange));
			//scrollPanel.setViewportBorder(BorderFactory.createLineBorder(Color.orange));
			scrollPanel.setBackground(new Color(0,0,0,128));
		}

		// 2014-11-04 Added SVG Image loading for the building
  	    Dimension expectedDimension = new Dimension(100, 100);
	        //GraphicsNode node = SVGMapUtil.getSVGGraphicsNode("building", buildingType);
	    Settlement settlement = building.getBuildingManager().getSettlement();
	        // Conclusion: this panel is called only once per opening the unit window session.
	    SettlementMapPanel svgPanel = new SettlementMapPanel(settlement, building);

	    svgPanel.setPreferredSize(expectedDimension);
	    svgPanel.setMaximumSize(expectedDimension);
	    svgPanel.setMinimumSize(expectedDimension);
		setPanelStyle(svgPanel);

		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(new MarsPanelBorder());// BorderFactory.createLineBorder(Color.black, 2, true));//
		//borderPanel.setBackground(new Color(255,255,255,255));
		borderPanel.add(svgPanel);

	    Box box = new Box(BoxLayout.Y_AXIS);
	    box.add(Box.createVerticalGlue());
	    box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
	        // 2014-11-05 Added setBorder()
	    //box.setBorder(BorderFactory.createLineBorder(Color.black, 2, true));
	    box.add(borderPanel);
	    box.add(Box.createVerticalGlue());
		//box.setOpaque(false);
		//box.setBackground(new Color(0,0,0,128)); //
		//box.setBackground(new Color(255,255,255,255));
	    functionListPanel.add(box);

        // Prepare inhabitable panel if building has lifeSupport.
        if (building.hasFunction(BuildingFunction.LIFE_SUPPORT)) {
//        	try {
        		LifeSupport lifeSupport = (LifeSupport) building.getFunction(BuildingFunction.LIFE_SUPPORT);
            	BuildingFunctionPanel inhabitablePanel = new BuildingPanelInhabitable(lifeSupport, desktop);
            	functionPanels.add(inhabitablePanel);
            	functionListPanel.add(inhabitablePanel);
//        	}
//        	catch (BuildingException e) {}
        }

        // Prepare manufacture panel if building has manufacturing.
        if (building.hasFunction(BuildingFunction.MANUFACTURE)) {
//        	try {
        		Manufacture workshop = (Manufacture) building.getFunction(BuildingFunction.MANUFACTURE);
        		BuildingFunctionPanel manufacturePanel = new BuildingPanelManufacture(workshop, desktop);
        		//manufacturePanel.setOpaque(false);
        		//manufacturePanel.setBackground(new Color(0,0,0,128));
        		functionPanels.add(manufacturePanel);
        		functionListPanel.add(manufacturePanel);
//        	}
//        	catch (BuildingException e) {}
        }


        // 2014-11-24 Added FoodProduction
        if (building.hasFunction(BuildingFunction.FOOD_PRODUCTION)) {
//        	try {
        		FoodProduction foodFactory = (FoodProduction) building.getFunction(BuildingFunction.FOOD_PRODUCTION);
        		BuildingFunctionPanel foodProductionPanel = new BuildingPanelFoodProduction(foodFactory, desktop);
        		functionPanels.add(foodProductionPanel);
        		functionListPanel.add(foodProductionPanel);
//        	}
//        	catch (BuildingException e) {}
        }

        // Prepare farming panel if building has farming.
        if (building.hasFunction(BuildingFunction.FARMING)) {
//        	try {
        		Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
            	BuildingFunctionPanel farmingPanel = new BuildingPanelFarming(farm, desktop);
            	functionPanels.add(farmingPanel);
            	functionListPanel.add(farmingPanel);
//        	}
//        	catch (BuildingException e) {}
        }

		// Prepare cooking panel if building has cooking.
		if (building.hasFunction(BuildingFunction.COOKING)) {
//			try {
				Cooking kitchen = (Cooking) building.getFunction(BuildingFunction.COOKING);
				BuildingFunctionPanel cookingPanel = new BuildingPanelCooking(kitchen, desktop);
				functionPanels.add(cookingPanel);
				functionListPanel.add(cookingPanel);
				//if (isTranslucent)setPanelStyle(powerPanel);
//			}
//			catch (BuildingException e) {}
		}

		//2014-11-11 Added preparing dessert function
		// Prepare dessert panel if building has preparing dessert function.
		if (building.hasFunction(BuildingFunction.PREPARING_DESSERT)) {
//			try {
			PreparingDessert kitchen = (PreparingDessert) building.getFunction(BuildingFunction.PREPARING_DESSERT);
				BuildingFunctionPanel preparingDessertPanel = new BuildingPanelPreparingDessert(kitchen, desktop);
				functionPanels.add(preparingDessertPanel);
				functionListPanel.add(preparingDessertPanel);
				//if (isTranslucent) setPanelStyle(powerPanel);
//			}
//			catch (BuildingException e) {}
		}

        // Prepare medical care panel if building has medical care.
        if (building.hasFunction(BuildingFunction.MEDICAL_CARE)) {
//        	try {
        		MedicalCare med = (MedicalCare) building.getFunction(BuildingFunction.MEDICAL_CARE);
            	BuildingFunctionPanel medicalCarePanel = new BuildingPanelMedicalCare(med, desktop);
            	functionPanels.add(medicalCarePanel);
            	functionListPanel.add(medicalCarePanel);
            	//if (isTranslucent) setPanelStyle(powerPanel);
//        	}
//        	catch (BuildingException e) {}
        }

		// Prepare vehicle maintenance panel if building has vehicle maintenance.
		if (building.hasFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE)) {
//			try {
				VehicleMaintenance garage = (VehicleMaintenance) building.getFunction(BuildingFunction.GROUND_VEHICLE_MAINTENANCE);
				BuildingFunctionPanel vehicleMaintenancePanel = new BuildingPanelVehicleMaintenance(garage, desktop);
				functionPanels.add(vehicleMaintenancePanel);
				functionListPanel.add(vehicleMaintenancePanel);
				//if (isTranslucent) setPanelStyle(powerPanel);
//			}
//			catch (BuildingException e) {}
		}

        // Prepare research panel if building has research.
        if (building.hasFunction(BuildingFunction.RESEARCH)) {
//        	try {
        		Research lab = (Research) building.getFunction(BuildingFunction.RESEARCH);
            	BuildingFunctionPanel researchPanel = new BuildingPanelResearch(lab, desktop);
            	functionPanels.add(researchPanel);
            	functionListPanel.add(researchPanel);
            	//if (isTranslucent) setPanelStyle(powerPanel);
//        	}
//        	catch (BuildingException e) {}
        }


        // Prepare Observation panel if building has Observatory.
        if (building.hasFunction(BuildingFunction.ASTRONOMICAL_OBSERVATIONS)) {
//        	try {
        		AstronomicalObservation observation = (AstronomicalObservation) building.getFunction(BuildingFunction.ASTRONOMICAL_OBSERVATIONS);
            	BuildingFunctionPanel observationPanel = new BuildingPanelAstronomicalObservation(observation, desktop);
            	functionPanels.add(observationPanel);
            	functionListPanel.add(observationPanel);
            	//if (isTranslucent) setPanelStyle(observationPanel);
//        	}
//        	catch (BuildingException e) {}
        }

        // Prepare power panel.
        BuildingFunctionPanel powerPanel = new BuildingPanelPower(building, desktop);
        functionPanels.add(powerPanel);
        functionListPanel.add(powerPanel);
    	//setPanelStyle(powerPanel);

        // Prepare power storage panel if building has power storage.
        if (building.hasFunction(BuildingFunction.POWER_STORAGE)) {
//            try {
                PowerStorage storage = (PowerStorage) building.getFunction(BuildingFunction.POWER_STORAGE);
                BuildingFunctionPanel powerStoragePanel = new BuildingPanelPowerStorage(storage, desktop);
                functionPanels.add(powerStoragePanel);
                functionListPanel.add(powerStoragePanel);
                //if (isTranslucent) setPanelStyle(powerStoragePanel);
//            }
//            catch (BuildingException e) {}
        }

        //2014-10-27 mkung: Modified Heating Panel
        if (building.hasFunction(BuildingFunction.THERMAL_GENERATION)) {
//          try {
		        BuildingFunctionPanel heatPanel = new BuildingPanelThermal(building, desktop);
		        functionPanels.add(heatPanel);
		        functionListPanel.add(heatPanel);
		        //if (isTranslucent) setPanelStyle(heatPanel);
//        }
//      catch (BuildingException e) {}
  }
        /*
        //2014-10-17 mkung: Added Heating Storage
        // Prepare heating storage panel if building has heating storage.
        if (building.hasFunction(BuildingFunction.THERMAL_STORAGE)) {
//            try {
                ThermalStorage storage = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
                BuildingFunctionPanel heatStoragePanel = new BuildingPanelThermalStorage(storage, desktop);
                functionPanels.add(heatStoragePanel);
                functionListPanel.add(heatStoragePanel);
                //if (isTranslucent) setPanelStyle(heatStoragePanel);
//            }
//            catch (BuildingException e) {}
        }
        */

        // Prepare resource processing panel if building has resource processes.
        if (building.hasFunction(BuildingFunction.RESOURCE_PROCESSING)) {
//        	try {
        		ResourceProcessing processor = (ResourceProcessing) building.getFunction(BuildingFunction.RESOURCE_PROCESSING);
            	BuildingFunctionPanel resourceProcessingPanel = new BuildingPanelResourceProcessing(processor, desktop);
            	functionPanels.add(resourceProcessingPanel);
            	functionListPanel.add(resourceProcessingPanel);
            	//if (isTranslucent) setPanelStyle(resourceProcessingPanel);
//        	}
//        	catch (BuildingException e) {}
        }

        // Prepare storage process panel if building has storage function.
        if (building.hasFunction(BuildingFunction.STORAGE)) {
//            try {
                Storage storage = (Storage) building.getFunction(BuildingFunction.STORAGE);
                BuildingFunctionPanel storagePanel = new BuildingPanelStorage(storage, desktop);
                functionPanels.add(storagePanel);
                functionListPanel.add(storagePanel);
                //if (isTranslucent) setPanelStyle(storagePanel);
//            }
//            catch (BuildingException e) {}
        }

        // Prepare malfunctionable panel.
        BuildingFunctionPanel malfunctionPanel =
            new BuildingPanelMalfunctionable(building, desktop);
        functionPanels.add(malfunctionPanel);
        functionListPanel.add(malfunctionPanel);
        //setPanelStyle(malfunctionPanel);

        // Prepare maintenance panel.
        BuildingFunctionPanel maintenancePanel =
            new BuildingPanelMaintenance(building, desktop);
        functionPanels.add(maintenancePanel);
        functionListPanel.add(maintenancePanel);
        //setPanelStyle(maintenancePanel);

        setPanelTranslucent();
    }


	public void setPanelTranslucent() {
/*
		if (isTranslucent) {
	        Iterator<BuildingFunctionPanel> i = functionPanels.iterator();
	   	 	while (i.hasNext()) {
	   	 		BuildingFunctionPanel p = i.next();
	   	 		setPanelStyle(p);
	   	 	}
		}
*/
	    setBorder(new DropShadowBorder(Color.BLACK, 0, 11, .2f, 16,
                  false, true, true, true));

	}

	public void setPanelStyle(JPanel p) {
		//System.out.println("BuildingPanel.java : isTranslucent is "+ isTranslucent);
		//if (isTranslucent) {
			//p.setOpaque(false);
			//p.setBackground(new Color(0,0,0,128));

		//}
	}

	/**
	 * Ask for a new building name using JOptionPane
	 * @return new name
	 */
	// 2014-11-27 Moved askNameDialog() from TabPanelBuilding.java to here
	public String askNameDialog() {
		return JOptionPane
			.showInputDialog(desktop,
					Msg.getString("BuildingPanel.renameBuilding.dialogInput"),
					Msg.getString("BuildingPanel.renameBuilding.dialogTitle"),
			        JOptionPane.QUESTION_MESSAGE);
	}

	/**
	 * Ask for a new building name using TextInputDialog in JavaFX/8
	 * @return new name
	 */
	public String askNameFX(String oldName) {
		String newName = null;
		TextInputDialog dialog = new TextInputDialog(oldName);
		dialog.setTitle(Msg.getString("BuildingPanel.renameBuilding.dialogTitle"));
		dialog.setHeaderText(Msg.getString("BuildingPanel.renameBuilding.dialog.header"));
		dialog.setContentText(Msg.getString("BuildingPanel.renameBuilding.dialog.content"));

		Optional<String> result = dialog.showAndWait();
		//result.ifPresent(name -> {});

		if (result.isPresent()){
		    logger.info("The old building name has been changed to: " + result.get());
			newName = result.get();
		}

		return newName;
	}

	/**
	 * Change and validate the new name of a Building
	 * @return call Dialog popup
	 */
	// 2014-11-27 Moved renameBuilding() from TabPanelBuilding.java to here
	@SuppressWarnings("restriction")
	private void renameBuilding() {

		//boolean isRenamed;

		String oldName = building.getNickName();
		newName = oldName;
		logger.info("Old name was " + oldName);

		//boolean isFX = Platform.isFxApplicationThread();

		if (desktop.getMainScene() != null) {

			Platform.runLater(() -> {

				String newName = askNameFX(oldName);
				if (!isBlank(newName)) { // newName != null && !newName.isEmpty() && newName with only whitespace(s)
					building.setNickName(newName);
					logger.info("New name is now " + newName);
					buildingNameLabel.setText(building.getNickName());
	            }
				else {
					Alert alert = new Alert(AlertType.ERROR, "Please use a valid name.");
					alert.initOwner(desktop.getMainScene().getStage());
					alert.showAndWait();
				}
				
/*				
 * 
				String n = askNameFX(oldName);
				//newName = name1;
				// Note: do not use if (newName.trim().equals(null), will throw java.lang.NullPointerException
				if (isBlank(n)) { //n == null || n.trim() == "" || (n.trim().length() == 0)) {
					//System.out.println("newName is " + newName);
					n = askNameFX(oldName);
					if (isBlank(n)) //n == null || n.trim() == "" || (n.trim().length() == 0))
						return;
					else {
						building.setNickName(n);
						logger.info("New name is now " + n);
					}
				}

				else {
					building.setNickName(n);
					logger.info("New name is now " + n);
				}

*/				
			});

		}

		else {

			JDialog.setDefaultLookAndFeelDecorated(true);
			newName = askNameDialog();
			// Note: do not use if (newName.trim().equals(null), will throw java.lang.NullPointerException
			if (newName == null || newName.trim() == "" || (newName.trim().length() == 0)) {
				newName = askNameDialog();
			}
			else {
				building.setNickName(newName);
				buildingNameLabel.setText(building.getNickName());
				logger.info("New name is now " + newName);
				//isRenamed = true;
			}
		}

		//return isRenamed;
	}

 /**
	 * <p>Checks if a String is whitespace, empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param str  the String to check, may be null
	 * @return <code>true</code> if the String is null, empty or whitespace
	 * @since 2.0
	 * @author commons.apache.org
	 */
	// 2015-10-19 Added isBlank()
	public static boolean isBlank(String str) {
	    int strLen;
	    if (str == null || (strLen = str.length()) == 0) {
	        return true;
	    }
	    for (int i = 0; i < strLen; i++) {
	        if ((Character.isWhitespace(str.charAt(i)) == false)) {
	            return false;
	        }
	    }
	    return true;
	}

    /**
     * Gets the panel's name.
     * @return panel name
     */
    public String getPanelName() {
        return panelName;
    }

    /**
     * Gets the panel's building.
     * @return building
     */
    public Building getBuilding() {
        return building;
    }

    /**
     * Update this panel.
     */
    public void update() {
        // Update each building function panel.
	    Iterator<BuildingFunctionPanel> i = functionPanels.iterator();
	    while (i.hasNext())
	    	i.next().update();

    }
    /**
     * Sets the panel's color theme.
     * @return panel name
     */
    public void setTheme(boolean value) {
        isTranslucent = value;
		//System.out.println("BuildingPanel.java : setTheme() : isTranslucent is " + isTranslucent);
    }


}