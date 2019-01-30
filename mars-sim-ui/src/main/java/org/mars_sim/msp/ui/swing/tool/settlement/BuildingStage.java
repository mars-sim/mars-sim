///**
// * Mars Simulation Project
// * BuildingStage.java
// * @version 3.1.0 2016-10-22
// * @author Manny Kung
// */
//
//package org.mars_sim.msp.ui.swing.tool.settlement;
//
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.net.MalformedURLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.logging.Logger;
//
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JComponent;
//import javax.swing.JDialog;
//import javax.swing.JOptionPane;
//import javax.swing.SwingUtilities;
//
//import org.mars_sim.msp.core.Msg;
//import org.mars_sim.msp.core.structure.Settlement;
//import org.mars_sim.msp.core.structure.building.Building;
//import org.mars_sim.msp.core.structure.building.function.AstronomicalObservation;
//import org.mars_sim.msp.core.structure.building.function.FunctionType;
//import org.mars_sim.msp.core.structure.building.function.FoodProduction;
//import org.mars_sim.msp.core.structure.building.function.LifeSupport;
//import org.mars_sim.msp.core.structure.building.function.Manufacture;
//import org.mars_sim.msp.core.structure.building.function.MedicalCare;
//import org.mars_sim.msp.core.structure.building.function.PowerStorage;
//import org.mars_sim.msp.core.structure.building.function.Research;
//import org.mars_sim.msp.core.structure.building.function.ResourceProcessing;
//import org.mars_sim.msp.core.structure.building.function.Storage;
//import org.mars_sim.msp.core.structure.building.function.ThermalGeneration;
//import org.mars_sim.msp.core.structure.building.function.VehicleMaintenance;
//import org.mars_sim.msp.core.structure.building.function.cooking.Cooking;
//import org.mars_sim.msp.core.structure.building.function.cooking.PreparingDessert;
//import org.mars_sim.msp.core.structure.building.function.farming.Farming;
//import org.mars_sim.msp.ui.javafx.MainScene;
//import org.mars_sim.msp.ui.swing.MainDesktopPane;
//import org.mars_sim.msp.ui.swing.MarsPanelBorder;
//import org.mars_sim.msp.ui.swing.unit_window.UnitWindow;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingFunctionPanel;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelAstronomicalObservation;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelFarming;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelInhabitable;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelMaintenance;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelMalfunctionable;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelManufacture;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelMedicalCare;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelPower;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelPowerStorage;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelResearch;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelResourceProcessing;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelStorage;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelThermal;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.BuildingPanelVehicleMaintenance;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelCooking;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelFoodProduction;
//import org.mars_sim.msp.ui.swing.unit_window.structure.building.food.BuildingPanelPreparingDessert;
//
//import com.alee.laf.panel.WebPanel;
//import com.alee.laf.scroll.WebScrollPane;
//
//import javafx.application.Platform;
//import javafx.embed.swing.SwingNode;
//import javafx.scene.control.Alert.AlertType;
//import javafx.scene.layout.BorderPane;
//import javafx.scene.layout.VBox;
//import javafx.scene.control.*;
//import javafx.scene.text.TextAlignment;
//import javafx.geometry.Insets;
//import javafx.geometry.Pos;
//
//
//public class BuildingStage {
//
//	/** default serial id. */
//	//private static final long serialVersionUID = 1L;
//
//    private static final Logger logger = Logger.getLogger(BuildingStage.class.getName());
//
//	/** The name of the panel. */
//	private String panelName;
//	private String newName;
//
//	private Label buildingNameLabel;
//	private VBox box1;
//	private BorderPane borderPane;
//	private Button renameBtn;
//	
//	/** The building this panel is for. */
//	private Building building;
//	private MainDesktopPane desktop;
//	
//	/** The function panels. */
//	private List<BuildingFunctionPanel> functionPanels;
//
//
//	/**
//	 * Constructor 1
//	 *
//	 * @param panelName the name of the panel.
//	 * @param building the building this panel is for.
//	 * @param desktop the main desktop.
//	 * @throws MalformedURLException
//	 */
//	public BuildingStage(String panelName, Building building, MainDesktopPane desktop) {
//		super();
//
//        // Initialize data members
//        this.panelName = panelName;
//        this.building = building;
//        this.desktop = desktop;
//
//        init();
//
//	}
//	
//    public void applyTheme() {
//        String cssFile = null;
//        int theme = MainScene.getTheme(); 
//        if (theme == 0 || theme == 6)
//        	cssFile = MainDesktopPane.BLUE_CSS;
//        else if (theme == 7)
//        	cssFile = MainDesktopPane.ORANGE_CSS;
//        
//        buildingNameLabel.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
//        buildingNameLabel.getStyleClass().add("label-large");
//            
//        borderPane.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
//        borderPane.getStyleClass().add("borderpane");
//               
//        box1.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
//        box1.getStyleClass().add("borderpane");
//
//	    renameBtn.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
//	    renameBtn.getStyleClass().add("button-broadcast");	
//    }
//    
//
//	/**
//	 * Initializes the BuildingStage
//	 */
//	public BorderPane init() {
//
//        this.functionPanels = new ArrayList<BuildingFunctionPanel>();
//   
//        borderPane = new BorderPane();
//        borderPane.setPadding(new Insets(3,3,3,3));    
//        borderPane.setMaxWidth(PopUpUnitMenu.WIDTH);
//        
//        box1 = new VBox();
//        box1.setAlignment(Pos.CENTER);
//        box1.setSpacing(2);
//        
//        buildingNameLabel = new Label(building.getNickName());
//        
//        buildingNameLabel.setTextAlignment(TextAlignment.CENTER);
//        buildingNameLabel.setContentDisplay(ContentDisplay.TOP);
//        buildingNameLabel.setLineSpacing(2);
//        
//		renameBtn = new Button(Msg.getString(
//				"BuildingPanel.renameBuilding.renameButton")); //$NON-NLS-1$
//	    renameBtn.setOnAction(e -> {
//		// if rename is done successfully, then update the building name
//				renameBuilding();
//				buildingNameLabel.setText(newName);
//		});
//	    renameBtn.setLineSpacing(2);
//
//	    box1.getChildren().addAll(buildingNameLabel, renameBtn);
//		
//        applyTheme();
//        
//		borderPane.setTop(box1);
//		
//    	//Popup stage = new Popup();
//    	SwingNode swingNode  = new SwingNode();
//    	borderPane.setCenter(swingNode);
//    	//borderPane.setMaxSize(PopUpUnitMenu.WIDTH, PopUpUnitMenu.HEIGHT-70);
//    	
//		WebPanel mainPanel = new WebPanel(new BorderLayout(0,0));
//
//		SwingUtilities.invokeLater(() -> {
//			swingNode.setContent(mainPanel);
//	    	swingNode.setStyle("-fx-background-radius:5; -fx-background-color: transparent;");
//	    });		
//		
//	    // Prepare function list panel.
//        WebPanel functionListPanel = new WebPanel();
//        functionListPanel.setMaximumWidth(PopUpUnitMenu.WIDTH-80); // This width is very important
//        functionListPanel.setLayout(new BoxLayout(functionListPanel, BoxLayout.Y_AXIS));
//        //mainPanel.add(functionListPanel, BorderLayout.CENTER);
//        
//        // Prepare function scroll panel.
//        WebScrollPane scrollPanel = new WebScrollPane();
//        scrollPanel.setViewportView(functionListPanel);
//        //CustomScroll scrollPanel = new CustomScroll(functionListPanel);
//        scrollPanel.setPreferredSize(new Dimension(PopUpUnitMenu.WIDTH-80, PopUpUnitMenu.HEIGHT-70));
//        scrollPanel.getVerticalScrollBar().setUnitIncrement(20);
//        mainPanel.add(scrollPanel, BorderLayout.CENTER);
//
//		// Add SVG Image loading for the building
//  	    Dimension expectedDimension = new Dimension(100, 100);
//	        //GraphicsNode node = SVGMapUtil.getSVGGraphicsNode("building", buildingType);
//	    Settlement settlement = building.getSettlement();
//	        // Conclusion: this panel is called only once per opening the unit window session.
//	    SettlementMapPanel svgPanel = new SettlementMapPanel(settlement, building);
//
//	    svgPanel.setPreferredSize(expectedDimension);
//	    svgPanel.setMaximumSize(expectedDimension);
//	    svgPanel.setMinimumSize(expectedDimension);
//		//setPanelStyle(svgPanel);
//
//		WebPanel svgHolder = new WebPanel();
//		svgHolder.setBorder(new MarsPanelBorder());// BorderFactory.createLineBorder(Color.black, 2, true));//
//		//svgHolder.setBackground(new Color(255,255,255,255));
//		svgHolder.add(svgPanel);
//
//	    Box box = new Box(BoxLayout.Y_AXIS);
//	    box.add(Box.createVerticalGlue());
//	    box.setAlignmentX(JComponent.CENTER_ALIGNMENT);
//	    //box.setBorder(BorderFactory.createLineBorder(Color.black, 2, true));
//	    box.add(svgHolder);
//	    box.add(Box.createVerticalGlue());
//		//box.setOpaque(false);
//		//box.setBackground(new Color(0,0,0,128)); //
//		//box.setBackground(new Color(255,255,255,255));
//	    functionListPanel.add(box);
//
//        // Prepare inhabitable panel if building has lifeSupport.
//        if (building.hasFunction(FunctionType.LIFE_SUPPORT)) {
////        	try {
//        		LifeSupport lifeSupport = building.getLifeSupport();//(LifeSupport) building.getFunction(FunctionType.LIFE_SUPPORT);
//            	BuildingFunctionPanel inhabitablePanel = new BuildingPanelInhabitable(lifeSupport, desktop);
//            	functionPanels.add(inhabitablePanel);
//            	functionListPanel.add(inhabitablePanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//        // Prepare manufacture panel if building has manufacturing.
//        if (building.hasFunction(FunctionType.MANUFACTURE)) {
////        	try {
//        		Manufacture workshop = building.getManufacture();//.getFunction(FunctionType.MANUFACTURE);
//        		BuildingFunctionPanel manufacturePanel = new BuildingPanelManufacture(workshop, desktop);
//        		//manufacturePanel.setOpaque(false);
//        		//manufacturePanel.setBackground(new Color(0,0,0,128));
//        		functionPanels.add(manufacturePanel);
//        		functionListPanel.add(manufacturePanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//        if (building.hasFunction(FunctionType.FOOD_PRODUCTION)) {
////        	try {
//        		FoodProduction foodFactory = building.getFoodProduction();//.getFunction(FunctionType.FOOD_PRODUCTION);
//        		BuildingFunctionPanel foodProductionPanel = new BuildingPanelFoodProduction(foodFactory, desktop);
//        		functionPanels.add(foodProductionPanel);
//        		functionListPanel.add(foodProductionPanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//        // Prepare farming panel if building has farming.
//        if (building.hasFunction(FunctionType.FARMING)) {
////        	try {
//        		Farming farm = building.getFarming();//.getFunction(FunctionType.FARMING);
//            	BuildingFunctionPanel farmingPanel = new BuildingPanelFarming(farm, desktop);
//            	functionPanels.add(farmingPanel);
//            	functionListPanel.add(farmingPanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//		// Prepare cooking panel if building has cooking.
//		if (building.hasFunction(FunctionType.COOKING)) {
////			try {
//				Cooking kitchen = building.getCooking();//.getFunction(FunctionType.COOKING);
//				BuildingFunctionPanel cookingPanel = new BuildingPanelCooking(kitchen, desktop);
//				functionPanels.add(cookingPanel);
//				functionListPanel.add(cookingPanel);
//				//if (isTranslucent)setPanelStyle(powerPanel);
////			}
////			catch (BuildingException e) {}
//		}
//
//		// Prepare dessert panel if building has preparing dessert function.
//		if (building.hasFunction(FunctionType.PREPARING_DESSERT)) {
////			try {
//			PreparingDessert kitchen = building.getPreparingDessert();//.getFunction(FunctionType.PREPARING_DESSERT);
//				BuildingFunctionPanel preparingDessertPanel = new BuildingPanelPreparingDessert(kitchen, desktop);
//				functionPanels.add(preparingDessertPanel);
//				functionListPanel.add(preparingDessertPanel);
//				//if (isTranslucent) setPanelStyle(powerPanel);
////			}
////			catch (BuildingException e) {}
//		}
//
//        // Prepare medical care panel if building has medical care.
//        if (building.hasFunction(FunctionType.MEDICAL_CARE)) {
////        	try {
//        		MedicalCare med = building.getMedical();//.getFunction(FunctionType.MEDICAL_CARE);
//            	BuildingFunctionPanel medicalCarePanel = new BuildingPanelMedicalCare(med, desktop);
//            	functionPanels.add(medicalCarePanel);
//            	functionListPanel.add(medicalCarePanel);
//            	//if (isTranslucent) setPanelStyle(powerPanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//		// Prepare vehicle maintenance panel if building has vehicle maintenance.
//		if (building.hasFunction(FunctionType.GROUND_VEHICLE_MAINTENANCE)) {
////			try {
//				VehicleMaintenance garage = building.getVehicleMaintenance();//.getFunction(FunctionType.GROUND_VEHICLE_MAINTENANCE);
//				BuildingFunctionPanel vehicleMaintenancePanel = new BuildingPanelVehicleMaintenance(garage, desktop);
//				functionPanels.add(vehicleMaintenancePanel);
//				functionListPanel.add(vehicleMaintenancePanel);
//				//if (isTranslucent) setPanelStyle(powerPanel);
////			}
////			catch (BuildingException e) {}
//		}
//
//        // Prepare research panel if building has research.
//        if (building.hasFunction(FunctionType.RESEARCH)) {
////        	try {
//        		Research lab = building.getResearch();//.getFunction(FunctionType.RESEARCH);
//            	BuildingFunctionPanel researchPanel = new BuildingPanelResearch(lab, desktop);
//            	functionPanels.add(researchPanel);
//            	functionListPanel.add(researchPanel);
//            	//if (isTranslucent) setPanelStyle(powerPanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//
//        // Prepare Observation panel if building has Observatory.
//        if (building.hasFunction(FunctionType.ASTRONOMICAL_OBSERVATIONS)) {
////        	try {
//        		AstronomicalObservation observation = building.getAstronomicalObservation();//.getFunction(FunctionType.ASTRONOMICAL_OBSERVATIONS);
//            	BuildingFunctionPanel observationPanel = new BuildingPanelAstronomicalObservation(observation, desktop);
//            	functionPanels.add(observationPanel);
//            	functionListPanel.add(observationPanel);
//            	//if (isTranslucent) setPanelStyle(observationPanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//        // Prepare power panel.
//        BuildingFunctionPanel powerPanel = new BuildingPanelPower(building, desktop);
//        functionPanels.add(powerPanel);
//        functionListPanel.add(powerPanel);
//    	//setPanelStyle(powerPanel);
//
//        // Prepare power storage panel if building has power storage.
//        if (building.hasFunction(FunctionType.POWER_STORAGE)) {
////            try {
//                PowerStorage storage = building.getPowerStorage();//.getFunction(FunctionType.POWER_STORAGE);
//                BuildingFunctionPanel powerStoragePanel = new BuildingPanelPowerStorage(storage, desktop);
//                functionPanels.add(powerStoragePanel);
//                functionListPanel.add(powerStoragePanel);
//                //if (isTranslucent) setPanelStyle(powerStoragePanel);
////            }
////            catch (BuildingException e) {}
//        }
//
//        if (building.hasFunction(FunctionType.THERMAL_GENERATION)) {
////          try {
//        		ThermalGeneration heat = building.getThermalGeneration();
//		        BuildingFunctionPanel heatPanel = new BuildingPanelThermal(heat, desktop);
//		        functionPanels.add(heatPanel);
//		        functionListPanel.add(heatPanel);
//		        //if (isTranslucent) setPanelStyle(heatPanel);
////        }
////      catch (BuildingException e) {}
//  }
//        
//
//        // Prepare heating storage panel if building has heating storage.
////        if (building.hasFunction(BuildingFunction.THERMAL_STORAGE)) {
//////            try {
////                ThermalStorage storage = (ThermalStorage) building.getFunction(BuildingFunction.THERMAL_STORAGE);
////                BuildingFunctionPanel heatStoragePanel = new BuildingPanelThermalStorage(storage, desktop);
////                functionPanels.add(heatStoragePanel);
////                functionListPanel.add(heatStoragePanel);
////                //if (isTranslucent) setPanelStyle(heatStoragePanel);
//////            }
//////            catch (BuildingException e) {}
////        }
//        
//
//        // Prepare resource processing panel if building has resource processes.
//        if (building.hasFunction(FunctionType.RESOURCE_PROCESSING)) {
////        	try {
//        		ResourceProcessing processor = (ResourceProcessing) building.getFunction(FunctionType.RESOURCE_PROCESSING);
//            	BuildingFunctionPanel resourceProcessingPanel = new BuildingPanelResourceProcessing(processor, desktop);
//            	functionPanels.add(resourceProcessingPanel);
//            	functionListPanel.add(resourceProcessingPanel);
//            	//if (isTranslucent) setPanelStyle(resourceProcessingPanel);
////        	}
////        	catch (BuildingException e) {}
//        }
//
//        // Prepare storage process panel if building has storage function.
//        if (building.hasFunction(FunctionType.STORAGE)) {
////            try {
//                Storage storage = (Storage) building.getFunction(FunctionType.STORAGE);
//                BuildingFunctionPanel storagePanel = new BuildingPanelStorage(storage, desktop);
//                functionPanels.add(storagePanel);
//                functionListPanel.add(storagePanel);
//                //if (isTranslucent) setPanelStyle(storagePanel);
////            }
////            catch (BuildingException e) {}
//        }
//
//        // Prepare malfunctionable panel.
//        BuildingFunctionPanel malfunctionPanel =
//            new BuildingPanelMalfunctionable(building, desktop);
//        functionPanels.add(malfunctionPanel);
//        functionListPanel.add(malfunctionPanel);
//        //setPanelStyle(malfunctionPanel);
//
//        // Prepare maintenance panel.
//        BuildingFunctionPanel maintenancePanel =
//            new BuildingPanelMaintenance(building, desktop);
//        functionPanels.add(maintenancePanel);
//        functionListPanel.add(maintenancePanel);
//        //setPanelStyle(maintenancePanel);
//
//        return borderPane;
//    }
//
//	/**
//	 * Ask for a new building name using JOptionPane
//	 * @return new name
//	 */
//	public String askNameDialog() {
//		return JOptionPane
//			.showInputDialog(desktop,
//					Msg.getString("BuildingPanel.renameBuilding.dialogInput"),
//					Msg.getString("BuildingPanel.renameBuilding.dialogTitle"),
//			        JOptionPane.QUESTION_MESSAGE);
//	}
//
//	/**
//	 * Ask for a new building name using TextInputDialog in JavaFX/8
//	 * @return new name
//	 */
//	public String askNameFX(String oldName) {
//		String newName = null;
//		TextInputDialog dialog = new TextInputDialog(oldName);
//		dialog.setTitle(Msg.getString("BuildingPanel.renameBuilding.dialogTitle"));
//		dialog.setHeaderText(Msg.getString("BuildingPanel.renameBuilding.dialog.header"));
//		dialog.setContentText(Msg.getString("BuildingPanel.renameBuilding.dialog.content"));
//
//		Optional<String> result = dialog.showAndWait();
//		//result.ifPresent(name -> {});
//
//		if (result.isPresent()){
//		    logger.info("The old building name has been changed to: " + result.get());
//			newName = result.get();
//		}
//
//		return newName;
//	}
//
//	/**
//	 * Change and validate the new name of a Building
//	 * @return call Dialog popup
//	 */
//	private void renameBuilding() {
//
//		//boolean isRenamed;
//
//		String oldName = building.getNickName();
//		newName = oldName;
//		logger.info("Old name was " + oldName);
//
//		//boolean isFX = Platform.isFxApplicationThread();
//
//		if (desktop.getMainScene() != null) {
//
//			Platform.runLater(() -> {
//
//				String newName = askNameFX(oldName);
//				if (!isBlank(newName)) { // newName != null && !newName.isEmpty() && newName with only whitespace(s)
//					building.setNickName(newName);
//					logger.info("New name is now " + newName);
//					buildingNameLabel.setText(building.getNickName());
//	            }
//				else {
//					Alert alert = new Alert(AlertType.ERROR, "Please use a valid name.");
//					alert.initOwner(desktop.getMainScene().getStage());
//					alert.showAndWait();
//				}
//				
///*				
// * 
//				String n = askNameFX(oldName);
//				//newName = name1;
//				// Note: do not use if (newName.trim().equals(null), will throw java.lang.NullPointerException
//				if (isBlank(n)) { //n == null || n.trim() == "" || (n.trim().length() == 0)) {
//					//System.out.println("newName is " + newName);
//					n = askNameFX(oldName);
//					if (isBlank(n)) //n == null || n.trim() == "" || (n.trim().length() == 0))
//						return;
//					else {
//						building.setNickName(n);
//						logger.info("New name is now " + n);
//					}
//				}
//
//				else {
//					building.setNickName(n);
//					logger.info("New name is now " + n);
//				}
//
//*/				
//			});
//
//		}
//
//		else {
//
//			JDialog.setDefaultLookAndFeelDecorated(true);
//			newName = askNameDialog();
//			// Note: do not use if (newName.trim().equals(null), will throw java.lang.NullPointerException
//			if (newName == null || newName.trim() == "" || (newName.trim().length() == 0)) {
//				newName = askNameDialog();
//			}
//			else {
//				building.setNickName(newName);
//				buildingNameLabel.setText(building.getNickName());
//				logger.info("New name is now " + newName);
//				//isRenamed = true;
//			}
//		}
//
//		//return isRenamed;
//	}
//
// /**
//	 * <p>Checks if a String is whitespace, empty ("") or null.</p>
//	 *
//	 * <pre>
//	 * StringUtils.isBlank(null)      = true
//	 * StringUtils.isBlank("")        = true
//	 * StringUtils.isBlank(" ")       = true
//	 * StringUtils.isBlank("bob")     = false
//	 * StringUtils.isBlank("  bob  ") = false
//	 * </pre>
//	 *
//	 * @param str  the String to check, may be null
//	 * @return <code>true</code> if the String is null, empty or whitespace
//	 * @since 2.0
//	 * @author commons.apache.org
//	 */
//	public static boolean isBlank(String str) {
//	    int strLen;
//	    if (str == null || (strLen = str.length()) == 0) {
//	        return true;
//	    }
//	    for (int i = 0; i < strLen; i++) {
//	        if ((Character.isWhitespace(str.charAt(i)) == false)) {
//	            return false;
//	        }
//	    }
//	    return true;
//	}
//
//    /**
//     * Gets the panel's name.
//     * @return panel name
//     */
//    public String getPanelName() {
//        return panelName;
//    }
//
//    /**
//     * Gets the panel's building.
//     * @return building
//     */
//    public Building getBuilding() {
//        return building;
//    }
//
//    /**
//     * Update this panel.
//     */
//    public void update() {
//        // Update each building function panel.
////	    Iterator<BuildingFunctionPanel> i = functionPanels.iterator();
////	    while (i.hasNext())
////	    	i.next().update();
//		for (BuildingFunctionPanel p : functionPanels)
//			if (p.isVisible() && p.isShowing())
//				p.update();
//
//    }
//	
//}
