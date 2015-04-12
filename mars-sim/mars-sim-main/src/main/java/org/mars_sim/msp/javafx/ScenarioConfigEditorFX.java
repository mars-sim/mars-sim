/**
 * Mars Simulation Project
 * ScenarioConfigEditorFX.java
 * @version 3.08 2015-04-10
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

import org.mars_sim.msp.javafx.insidefx.undecorator.Undecorator;
import org.mars_sim.msp.javafx.insidefx.undecorator.UndecoratorController;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.mars_sim.msp.core.Coordinates;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.RandomUtil;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.structure.SettlementConfig;
import org.mars_sim.msp.core.structure.SettlementTemplate;
import org.mars_sim.msp.ui.swing.JComboBoxMW;
import org.mars_sim.networking.MultiplayerClient;
import org.mars_sim.networking.MultiplayerServer;
/**
 * ScenarioConfigEditorFX allows users to configure the types of settlements available at the start of the simulation.
 */
public class ScenarioConfigEditorFX {

	/** default logger. */
	private static Logger logger = Logger.getLogger(ScenarioConfigEditorFX.class.getName());

	// Data members.
	//private String TITLE = Msg.getString("SimulationConfigEditor.title");

	private int clientID;
    double orgSceneX, orgSceneY;
    double orgTranslateX, orgTranslateY;
	private boolean hasError;

	private String gameMode;

	private SettlementTableModel settlementTableModel;
	private JTable settlementTable;
	private JScrollPane settlementScrollPane;
	private Label errorLabel;
	private Button createButton;
	private Button addButton;
	private Button removeButton;
	private Button defaultButton;
	private Button alphaButton;
	private Label titleLabel;
	private Label gameModeLabel;
	private Label clientIDLabel;
	private TilePane titlePane;

	private TableCellEditor editor;

	private SwingNode swingNode;
	private Stage stage;
	private SimulationConfig config;
	private MainMenu mainMenu;
	private CrewEditorFX crewEditorFX;

	private MultiplayerClient multiplayerClient;


	/**
	 * Constructor
	 * @param mainMenu
	 * @param config the simulation configuration.
	 */
	public ScenarioConfigEditorFX(MainMenu mainMenu, SimulationConfig config) {
		// Initialize data members.
		this.config = config;
		this.mainMenu = mainMenu;
		this.hasError = false;

		stage = new Stage();

		if (mainMenu.getMultiplayerMode() != null) {
			multiplayerClient = mainMenu.getMultiplayerMode().getMultiplayerClient();
			multiplayerClient.sendGetNewID();
			clientID = multiplayerClient.getClientID();
			gameMode = "Simulation Mode : Multi-Player";//  + "   ";
			stage.setTitle("Mars Simulation Project \t\t Scenario Configuration Editor \t\t" + gameMode + "\t\t Player ID : " + clientID);
		}
		else {
			gameMode = "Simulation Mode : Single-Player";//  + "   ";
			stage.setTitle("Mars Simulation Project \t\t Scenario Configuration Editor \t\t" + gameMode + "\t\t");
		}


    	Parent parent = null;
		FXMLLoader fxmlLoader = null;

		try {
			fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(getClass().getResource("/fxui/fxml/Editor2.fxml"));//ClientArea.fxml"));
            fxmlLoader.setController(this);
			parent = (Parent) fxmlLoader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Undecorator undecorator = new Undecorator(stage, (Region) parent);
		undecorator.getStylesheets().add("skin/undecorator.css");
		if ( parent.lookup("#anchorRoot") == null)
			System.out.println("not found");
		//((BorderPane) parent.lookup("border")).setCenter(createEditor());

	     AnchorPane anchorpane = ((AnchorPane) parent.lookup("#anchorRoot"));
	     // List should stretch as anchorpane is resized
	     BorderPane bp = createEditor();
	     AnchorPane.setTopAnchor(bp, 5.0);
	     AnchorPane.setLeftAnchor(bp, 5.0);
	     AnchorPane.setRightAnchor(bp, 5.0);
	     // Button will float on right edge
	     //Button button = new Button("Add");
	     //AnchorPane.setTopAnchor(button, 10.0);
	     //AnchorPane.setRightAnchor(button, 10.0);
	     anchorpane.getChildren().add(bp);
		//Group group = new Group();
		//group.getChildren().add(undecorator);
		//Parent borderParent = createEditor();
		//group.getChildren().add(borderParent);
		//((BorderPane) parent).setCenter(createEditor());
		Scene scene = new Scene(undecorator);
		//Scene scene = new Scene(group);

		// Fade transition on window closing request
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			 @Override
			 public void handle(WindowEvent we) {
				 swingNode.setOpacity(0);
				 createButton.setOpacity(0);
				 addButton.setOpacity(0);
				 defaultButton.setOpacity(0);
				 alphaButton.setOpacity(0);
				 removeButton.setOpacity(0);
				 titlePane.setOpacity(0);
				 we.consume(); // Do not hide
				 undecorator.setFadeOutTransition();
				 if (crewEditorFX != null)
					 crewEditorFX.getStage().close();
			}
		});

		//undecorator.setOnMousePressed(buttonOnMousePressedEventHandler);

		// Transparent scene and stage
		scene.setFill(Color.TRANSPARENT); // needed to eliminate the white border
		stage.initStyle(StageStyle.TRANSPARENT);
		//stage.setMinWidth(undecorator.getMinWidth());
		//stage.setMinHeight(undecorator.getMinHeight());

		stage.setScene(scene);
		stage.sizeToScene();
		stage.toFront();

        stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));

        stage.centerOnScreen();
        stage.setResizable(true);
 	   	stage.setFullScreen(false);
        //stage.setTitle(TITLE);
        stage.show();

	}

/*
	  EventHandler<MouseEvent> buttonOnMousePressedEventHandler =
		        new EventHandler<MouseEvent>() {

		        @Override
		        public void handle(MouseEvent t) {
		            orgSceneX = t.getSceneX();
		            orgSceneY = t.getSceneY();
		            orgTranslateX = ((Button)(t.getSource())).getTranslateX();
		            orgTranslateY = ((Button)(t.getSource())).getTranslateY();

		            ((Button)(t.getSource())).toFront();
		        }
		    };
*/

	//private Parent createEditor() {
	private BorderPane createEditor() {

		//AnchorPane pane = new AnchorPane();

		BorderPane borderAll = new BorderPane();
		//AnchorPane.setTopAnchor(borderAll, 50.0);
	    //AnchorPane.setLeftAnchor(borderAll, 50.0);
	    //AnchorPane.setRightAnchor(borderAll, 50.0);

		borderAll.setPadding(new Insets(10, 10, 10, 10));

		// Create the title label.
		//if (multiplayerClient != null)
		//	clientIDLabel = new Label("Player ID : " + clientID);

		//gameModeLabel = new Label(gameMode);
		titleLabel = new Label(Msg.getString("SimulationConfigEditor.chooseSettlements")); //$NON-NLS-1$
		//titleLabel.setPadding(new Insets(5, 10, 5, 10));
		//titlePane = new TilePane(Orientation.VERTICAL);
		titlePane = new TilePane(Orientation.HORIZONTAL);
		titlePane.setMaxWidth(600);
		titlePane.setPadding(new Insets(3, 3, 3, 3));
		titlePane.setHgap(2.0);
		titlePane.setVgap(2.0);
		//if (multiplayerClient != null) {
		//	titlePane.getChildren().addAll(clientIDLabel, titleLabel);
		//	clientIDLabel.setAlignment(Pos.TOP_LEFT);
		//}
		//else
		titlePane.getChildren().addAll(titleLabel);
		titlePane.setAlignment(Pos.TOP_LEFT);
		//titleLabel.setAlignment(Pos.CENTER);
		//gameModeLabel.setAlignment(Pos.TOP_LEFT);

		borderAll.setTop(titlePane);

		// Create settlement scroll panel.
		//ScrollPane settlementScrollPane = new ScrollPane();
		//settlementScrollPane.setPreferredSize(new Dimension(585, 200));

		// Create settlement scroll panel.
		settlementScrollPane = new JScrollPane();
		settlementScrollPane.setPreferredSize(new Dimension(700, 200));
		settlementScrollPane.setSize(new Dimension(700, 200));
		//.add(settlementScrollPane, BorderLayout.CENTER);

		//TableView table = new TableView();
		//table.setEditable(true);
        //TableColumn col1 = new TableColumn("");
        //TableColumn col2 = new TableColumn("");
        //TableColumn col3 = new TableColumn("");
        //table.getColumns().addAll(col1, col2, col3);

		StackPane swingPane = new StackPane();
		swingPane.setMaxSize(700, 200);
		swingNode = new SwingNode();
		swingNode.setOpacity(.7);
		swingNode.setBlendMode(BlendMode.SRC_OVER);
		createSwingNode(swingNode);
		swingPane.getChildren().add(swingNode);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		swingPane.setPrefWidth(primaryScreenBounds.getWidth());
		//swingPane.setMaxSize(Region.USE_COMPUTED_SIZE, 200);
		borderAll.setCenter(swingPane);

		// Create configuration button outer panel.
		BorderPane borderButtons = new BorderPane();
		borderAll.setLeft(borderButtons);

		// Create configuration button inner top panel.
		VBox vbTop = new VBox();
		borderButtons.setTop(vbTop);
		vbTop.setSpacing(10);
		vbTop.setPadding(new Insets(0, 10, 10, 10));

		// Create add settlement button.
		addButton = new Button(Msg.getString("SimulationConfigEditor.button.add")); //$NON-NLS-1$
		//addButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.add")); //$NON-NLS-1$
		addButton.setOnAction((event) -> {
			addNewSettlement();
		});
		vbTop.getChildren().add(addButton);

		// Create remove settlement button.
		removeButton = new Button(Msg.getString("SimulationConfigEditor.button.remove")); //$NON-NLS-1$
		//removeButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.remove")); //$NON-NLS-1$
		removeButton.setOnAction((event) -> {
			removeSelectedSettlements();
		});
		vbTop.getChildren().add(removeButton);

		// Create configuration button inner bottom panel.
		VBox vbCenter = new VBox();
		vbCenter.setSpacing(10);
		vbCenter.setPadding(new Insets(0, 10, 10, 10));
		borderButtons.setBottom(vbCenter);

		// Create default button.
		defaultButton = new Button(Msg.getString("SimulationConfigEditor.button.default")); //$NON-NLS-1$
		//defaultButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.default")); //$NON-NLS-1$
		defaultButton.setOnAction((event) -> {
			setDefaultSettlements();
		});
		vbCenter.getChildren().add(defaultButton);

		addButton.setMaxWidth(Double.MAX_VALUE);
		removeButton.setMaxWidth(Double.MAX_VALUE);
		defaultButton.setMaxWidth(Double.MAX_VALUE);

		// Create bottom panel.
		BorderPane bottomPanel = new BorderPane();
		borderAll.setBottom(bottomPanel);

		// Create error label.
		errorLabel = new Label(" "); //$NON-NLS-1$
		//errorLabel.set//setColor(Color.RED);
		errorLabel.setStyle("-fx-font: 15 arial; -fx-color: red; -fx-base: #ff5400;");
		bottomPanel.setTop(errorLabel);

		// Create the bottom button panel.
		//HBox bottomButtonPanel = new HBox();
		//bottomPanel.setBottom(bottomButtonPanel);

		// Create the create button.
		createButton = new Button(Msg.getString("SimulationConfigEditor.button.newSim")); //$NON-NLS-1$
		//createButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.newSim")); //$NON-NLS-1$
		createButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.newSim")));
		//createButton.setStyle("-fx-font: 16 arial; -fx-base: #cce6ff;");
		createButton.setOnAction((event) -> {
			// Make sure any editing cell is completed, then check if error.
			if (editor != null) {
				editor.stopCellEditing();
			}

			if (!hasError) {
				stage.hide();
				setConfiguration();
				Simulation.createNewSimulation();
				mainMenu.runMainScene();
				Simulation.instance().start();
				closeWindow();
			}

		});
		//bottomButtonPanel.getChildren().add(createButton);

		// 2014-12-15 Added Edit Alpha Crew button.
		alphaButton = new Button(Msg.getString("SimulationConfigEditor.button.crewEditor")); //$NON-NLS-1$
		//alphaButton.setToolTipText(Msg.getString("SimulationConfigEditor.tooltip.crewEditor")); //$NON-NLS-1$
		alphaButton.setTooltip(new Tooltip(Msg.getString("SimulationConfigEditor.tooltip.crewEditor")));
		//alphaButton.setStyle("-fx-font: 16 arial; -fx-base: #cce6ff;");
		alphaButton.setOnAction((event) -> {
			editCrewProile("alpha");
		});
		//bottomButtonPanel.getChildren().add(alphaButton);

		TilePane tileButtons = new TilePane(Orientation.HORIZONTAL);
		tileButtons.setPadding(new Insets(5, 5, 5, 5));
		tileButtons.setHgap(20.0);
		tileButtons.setVgap(8.0);
		tileButtons.getChildren().addAll(createButton, alphaButton);
		tileButtons.setAlignment(Pos.CENTER);
		bottomPanel.setBottom(tileButtons);

		//pane.getChildren().add(borderAll);
		return borderAll;
		//return borderAll;
	}

	private void createSwingNode(final SwingNode swingNode) {

        SwingUtilities.invokeLater(() -> {

			// Create settlement table.
			settlementTableModel = new SettlementTableModel();
			settlementTable = new JTable(settlementTableModel);
			settlementTable.setRowSelectionAllowed(true);
			settlementTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			settlementTable.getColumnModel().getColumn(0).setPreferredWidth(50);
			settlementTable.getColumnModel().getColumn(1).setPreferredWidth(100);
			settlementTable.getColumnModel().getColumn(2).setPreferredWidth(15);
			settlementTable.getColumnModel().getColumn(3).setPreferredWidth(15);
			settlementTable.getColumnModel().getColumn(4).setPreferredWidth(15);
			settlementTable.getColumnModel().getColumn(5).setPreferredWidth(15);
			settlementScrollPane.setViewportView(settlementTable);

			// Create combo box for editing template column in settlement table.
			TableColumn templateColumn = settlementTable.getColumnModel().getColumn(1);
			JComboBoxMW<String> templateCB = new JComboBoxMW<String>();
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				templateCB.addItem(i.next().getTemplateName());
			}

			templateColumn.setCellEditor(new DefaultCellEditor(templateCB));

			editor = settlementTable.getCellEditor();

            swingNode.setContent(settlementScrollPane);
        });
    }

	/**
	 * Adds a new settlement with default values.
	 */
	private void addNewSettlement() {
		SettlementInfo settlement = determineNewSettlementConfiguration();
		settlementTableModel.addSettlement(settlement);
	}

	/**
	 * Removes the settlements selected on the table.
	 */
	private void removeSelectedSettlements() {
		settlementTableModel.removeSettlements(settlementTable.getSelectedRows());
	}


	/**
	 * Edits team profile.
	 */
	private void editCrewProile(String crew) {
		crewEditorFX = new CrewEditorFX(config);
	}



	/**
	 * Sets the default settlements from the loaded configuration.
	 */
	private void setDefaultSettlements() {
		settlementTableModel.loadDefaultSettlements();
	}

	/**
	 * Set the simulation configuration based on dialog choices.
	 */
	private void setConfiguration() {
		SettlementConfig settlementConfig = config.getSettlementConfiguration();

		// Clear configuration settlements.
		settlementConfig.clearInitialSettlements();

		// Add configuration settlements from table data.
		for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
			String name = (String) settlementTableModel.getValueAt(x, 0);
			String template = (String) settlementTableModel.getValueAt(x, 1);
			String population = (String) settlementTableModel.getValueAt(x, 2);
			int populationNum = Integer.parseInt(population);
			//System.out.println("populationNum is " + populationNum);
			String numOfRobotsStr = (String) settlementTableModel.getValueAt(x, 3);
			int numOfRobots = Integer.parseInt(numOfRobotsStr);
			//System.out.println("SimulationConfigEditor : numOfRobots is " + numOfRobots);
			String latitude = (String) settlementTableModel.getValueAt(x, 4);
			String longitude = (String) settlementTableModel.getValueAt(x, 5);
			settlementConfig.addInitialSettlement(name, template, populationNum, numOfRobots, latitude, longitude);

			//TODO: send new name latitude, longitude to host server
			multiplayerClient = mainMenu.getMultiplayerMode().getMultiplayerClient();

		}
	}

	/**
	 * Close and dispose dialog window.
	 */
	private void closeWindow() {
		stage.setIconified(true);
		stage.hide();
	}

	/**
	 * Sets an edit-check error.
	 * @param errorString the error description.
	 */
	private void setError(String errorString) {
		// Platform.runLater is needed to switch from Swing EDT to JavaFX Thread
		Platform.runLater(() -> {
			if (!hasError) {
    			hasError = true;
    			errorLabel.setText(errorString);
    			//errorLabel.setStyle("-fx-font-color:red;");
    			errorLabel.setTextFill(Color.RED);
    			createButton.setDisable(true);
    		}
		});
	}

	/**
	 * Clears all edit-check errors.
	 */
	private void clearError() {
		// Platform.runLater is needed to switch from Swing EDT to JavaFX Thread
		Platform.runLater(() -> {
        		hasError = false;
        		errorLabel.setText(""); //$NON-NLS-1$
       			errorLabel.setTextFill(Color.BLACK);
        		createButton.setDisable(false);
        });
	}

	/**
	 * Determines the configuration of a new settlement.
	 * @return settlement configuration.
	 */
	private SettlementInfo determineNewSettlementConfiguration() {
		SettlementInfo settlement = new SettlementInfo();

		settlement.name = determineNewSettlementName();
		settlement.template = determineNewSettlementTemplate();
		settlement.population = determineNewSettlementPopulation(settlement.template);
		settlement.numOfRobots = determineNewSettlementNumOfRobots(settlement.template);
		settlement.latitude = determineNewSettlementLatitude();
		settlement.longitude = determineNewSettlementLongitude();

		return settlement;
	}

	/**
	 * Determines a new settlement's name.
	 * @return name.
	 */
	private String determineNewSettlementName() {
		String result = null;

		// Try to find unique name in configured settlement name list.
		// Randomly shuffle settlement name list first.
		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<String> settlementNames = settlementConfig.getSettlementNameList();
		Collections.shuffle(settlementNames);
		Iterator<String> i = settlementNames.iterator();
		while (i.hasNext()) {
			String name = i.next();

			// Make sure settlement name isn't already being used in table.
			boolean nameUsed = false;
			for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
				if (name.equals(settlementTableModel.getValueAt(x, 0))) {
					nameUsed = true;
				}
			}

			// TODO: check if the name is being used in the host server's settlement registry or not

			// If not being used already, use this settlement name.
			if (!nameUsed) {
				result = name;
				break;
			}
		}

		// If no name found, create numbered settlement name: "Settlement 1", "Settlement 2", etc.
		int count = 1;
		while (result == null) {
			String name = Msg.getString(
				"SimulationConfigEditor.settlement", //$NON-NLS-1$
				Integer.toString(count)
			);

			// Make sure settlement name isn't already being used in table.
			boolean nameUsed = false;
			for (int x = 0; x < settlementTableModel.getRowCount(); x++) {
				if (name.equals(settlementTableModel.getValueAt(x, 0))) {
					nameUsed = true;
				}
			}

			// TODO: check if the name is being used in the host server's settlement registry or not

			// If not being used already, use this settlement name.
			if (!nameUsed) {
				result = name;
			}


			count++;
		}

		return result;
	}

	/**
	 * Determines a new settlement's template.
	 * @return template name.
	 */
	private String determineNewSettlementTemplate() {
		String result = null;

		SettlementConfig settlementConfig = config.getSettlementConfiguration();
		List<SettlementTemplate> templates = settlementConfig.getSettlementTemplates();
		if (templates.size() > 0) {
			int index = RandomUtil.getRandomInt(templates.size() - 1);
			result = templates.get(index).getTemplateName();
		}
		else logger.log(Level.WARNING, Msg.getString("SimulationConfigEditor.log.settlementTemplateNotFound")); //$NON-NLS-1$

		return result;
	}

	/**
	 * Determines the new settlement population.
	 * @param templateName the settlement template name.
	 * @return the new population number.
	 */
	private String determineNewSettlementPopulation(String templateName) {

		String result = "0"; //$NON-NLS-1$

		if (templateName != null) {
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				SettlementTemplate template = i.next();
				if (template.getTemplateName().equals(templateName)) {
					result = Integer.toString(template.getDefaultPopulation());
				}
			}
		}

		return result;
	}


	/**
	 * Determines the new settlement number of robots.
	 * @param templateName the settlement template name.
	 * @return number of robots.
	 */
	private String determineNewSettlementNumOfRobots(String templateName) {

		String result = "0"; //$NON-NLS-1$

		if (templateName != null) {
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
			Iterator<SettlementTemplate> i = settlementConfig.getSettlementTemplates().iterator();
			while (i.hasNext()) {
				SettlementTemplate template = i.next();
				if (template.getTemplateName().equals(templateName)) {
					result = Integer.toString(template.getDefaultNumOfRobots());
					//System.out.println("SimulationConfigEditor : determineNewSettlementNumOfRobots() : result is " + result);
				}
			}
		}

		return result;
	}

	/**
	 * Determines a new settlement's latitude.
	 * @return latitude string.
	 */
	private String determineNewSettlementLatitude() {

		// TODO: check if there is an existing settlement with the same latitude (within 1 decimal places) at this location from the host server's settlement registry
		// note: d = 6779km. each one degree is 59.1579km. each .1 degree is 5.91579 km apart.
		// e.g. if an existing town is at (0.1, 0.1), one cannot "reuse" these coordinates again. He can only create a new town at (0.1, 0.1)

		double phi = Coordinates.getRandomLatitude();
		String formattedLatitude = Coordinates.getFormattedLatitudeString(phi);
		int degreeIndex = formattedLatitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return
			formattedLatitude.substring(0, degreeIndex) + " " +
			formattedLatitude.substring(degreeIndex + 1, formattedLatitude.length())
		;
	}

	/**
	 * Determines a new settlement's longitude.
	 * @return longitude string.
	 */
	private String determineNewSettlementLongitude() {

		// TODO: check if there is an existing settlement with the same latitude (within 1 decimal places) at this location from the host server's settlement registry
		// note: d = 6779km. each one degree is 59.1579km. each .1 degree is 5.91579 km apart.
		// e.g. if an existing town is at (0.1, 0.1), one cannot "reuse" these coordinates again. He can only create a new town at (0.1, 0.1)

		double theta = Coordinates.getRandomLongitude();
		String formattedLongitude = Coordinates.getFormattedLongitudeString(theta);
		int degreeIndex = formattedLongitude.indexOf(Msg.getString("direction.degreeSign")); //$NON-NLS-1$
		return
			formattedLongitude.substring(0, degreeIndex) + " " +
			formattedLongitude.substring(degreeIndex + 1, formattedLongitude.length())
		;
	}

	/**
	 * Inner class representing a settlement configuration.
	 */
	private class SettlementInfo {
		String name;
		String template;
		String population;
		String numOfRobots;
		String latitude;
		String longitude;
	}

	/**
	 * Inner class for the settlement table model.
	 */
	private class SettlementTableModel extends AbstractTableModel {

		/** default serial id. */
		private static final long serialVersionUID = 1L;
		// Data members
		private String[] columns;
		private List<SettlementInfo> settlements;

		/**
		 * Hidden Constructor.
		 */
		private SettlementTableModel() {
			super();

			// Add table columns.
			columns = new String[] {
				Msg.getString("SimulationConfigEditor.column.name"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.template"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.population"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.numOfRobots"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.latitude"), //$NON-NLS-1$
				Msg.getString("SimulationConfigEditor.column.longitude") //$NON-NLS-1$
			};

			// Load default settlements.
			settlements = new ArrayList<SettlementInfo>();
			loadDefaultSettlements();
		}

		/**
		 * Load the default settlements in the table.
		 */
		private void loadDefaultSettlements() {
			SettlementConfig settlementConfig = config.getSettlementConfiguration();
			settlements.clear();
			for (int x = 0; x < settlementConfig.getNumberOfInitialSettlements(); x++) {
				SettlementInfo info = new SettlementInfo();
				info.name = settlementConfig.getInitialSettlementName(x);
				info.template = settlementConfig.getInitialSettlementTemplate(x);
				info.population = Integer.toString(settlementConfig.getInitialSettlementPopulationNumber(x));
				info.numOfRobots = Integer.toString(settlementConfig.getInitialSettlementNumOfRobots(x));
				info.latitude = settlementConfig.getInitialSettlementLatitude(x);
				info.longitude = settlementConfig.getInitialSettlementLongitude(x);
				settlements.add(info);
			}
			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return settlements.size();
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if ((columnIndex > -1) && (columnIndex < columns.length)) {
				return columns[columnIndex];
			}
			else {
				return Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return true;
		}

		@Override
		public Object getValueAt(int row, int column) {
			Object result = Msg.getString("unknown"); //$NON-NLS-1$
			if ((row > -1) && (row < getRowCount())) {
				SettlementInfo info = settlements.get(row);
				if ((column > -1) && (column < getColumnCount())) {
					switch (column) {
					case 0:
						result = info.name;
						break;
					case 1:
						result = info.template;
						break;
					case 2:
						result = info.population;
						break;
					case 3:
						result = info.numOfRobots;
						break;
					case 4:
						result = info.latitude;
						break;
					case 5:
						result = info.longitude;
						break;
					}
				} else {
					result = Msg.getString("SimulationConfigEditor.log.invalidColumn"); //$NON-NLS-1$
				}
			} else {
				result = Msg.getString("SimulationConfigEditor.log.invalidRow"); //$NON-NLS-1$
			}

			return result;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if ((rowIndex > -1) && (rowIndex < getRowCount())) {
				SettlementInfo info = settlements.get(rowIndex);
				if ((columnIndex > -1) && (columnIndex < getColumnCount())) {

					switch (columnIndex) {
					case 0:
						info.name = (String) aValue;
						break;
					case 1:
						info.template = (String) aValue;
						info.population = determineNewSettlementPopulation(info.template);
						info.numOfRobots = determineNewSettlementNumOfRobots(info.template);
						break;
					case 2:
						info.population = (String) aValue;
						break;
					case 3:
						info.numOfRobots = (String) aValue;
						break;

					case 4:
						String latStr = ((String) aValue).trim();
						double doubleLat = 0;
						String dir1 = latStr.substring(latStr.length() - 1, latStr.length());
						if (dir1.toUpperCase().equals("N") | dir1.toUpperCase().equals("S")) {
							if (latStr.length() > 2) {
								doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
								doubleLat = Math.round(doubleLat*10.0)/10.0;
								info.latitude =  doubleLat + " " + dir1;
							}
							else
								info.latitude = (String) aValue;
						}
						else
							info.latitude = (String) aValue;
						break;

					case 5:
						String longStr = ((String) aValue).trim();
						double doubleLong = 0;
						String dir = longStr.substring(longStr.length() - 1, longStr.length());
						if (dir.toUpperCase().equals("E") | dir.toUpperCase().equals("W")) {
							if (longStr.length() > 2) {
								doubleLong = Double.parseDouble(longStr.substring(0, longStr.length() - 1));
								doubleLong = Math.round(doubleLong*10.0)/10.0;
								info.longitude =  doubleLong + " " + dir;
							}
							else
								info.longitude = (String) aValue;
						}
						else
							info.longitude = (String) aValue;
						break;
					}
				}

				checkForErrors();
				fireTableDataChanged();
			}
		}

		/**
		 * Remove a set of settlements from the table.
		 * @param rowIndexes an array of row indexes of the settlements to remove.
		 */
		private void removeSettlements(int[] rowIndexes) {
			List<SettlementInfo> removedSettlements = new ArrayList<SettlementInfo>(rowIndexes.length);

			for (int x = 0; x < rowIndexes.length; x++) {
				if ((rowIndexes[x] > -1) && (rowIndexes[x] < getRowCount())) {
					removedSettlements.add(settlements.get(rowIndexes[x]));
				}
			}

			Iterator<SettlementInfo> i = removedSettlements.iterator();
			while (i.hasNext()) {
				settlements.remove(i.next());
			}

			fireTableDataChanged();
		}

		/**
		 * Adds a new settlement to the table.
		 * @param settlement the settlement configuration.
		 */
		private void addSettlement(SettlementInfo settlement) {
			settlements.add(settlement);
			fireTableDataChanged();
		}

		/**
		 * Check for errors in table settlement values.
		 */
		private void checkForErrors() {
			clearError();

			// TODO: check to ensure the latitude/longitude is not being used in the host server's settlement registry

			try {
				// Ensure the latitude/longitude is not being taken already in the table by another settlement
				boolean repeated = false;
				int size = settlementTableModel.getRowCount();
				for (int x = 0; x < size; x++) {

					String latStr = ((String) (settlementTableModel.getValueAt(x, 4))).trim().toUpperCase();
					String longStr = ((String) (settlementTableModel.getValueAt(x, 5))).trim().toUpperCase();

					// check if the second from the last character is a digit or a letter, if a letter, setError
					if (Character.isLetter(latStr.charAt(latStr.length() - 2))){
						setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
						return;
					}

					// check if the last character is a digit or a letter, if digit, setError
					if (Character.isDigit(latStr.charAt(latStr.length() - 1))){
						setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
						return;
					}

					if (latStr == null || latStr.length() < 2) {
						setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
						return;
					}

					if (longStr == null || longStr.length() < 2 ) {
						setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
						return;
					}

					//System.out.println("settlement.latitude is "+ settlement.latitude);
					if (x + 1 < size ) {
						String latNextStr = ((String) (settlementTableModel.getValueAt(x + 1, 4))).trim().toUpperCase();
						String longNextStr = ((String) (settlementTableModel.getValueAt(x + 1, 5))).trim().toUpperCase();

						//System.out.println("latStr is "+ latStr);
						//System.out.println("latNextStr is "+ latNextStr);
						if ( latNextStr == null || latNextStr.length() < 2) {
							setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
							return;
						}
						else if (latStr.equals(latNextStr)) {
							repeated = true;
							break;
						}

						else {
							double doubleLat = Double.parseDouble(latStr.substring(0, latStr.length() - 1));
							double doubleLatNext = Double.parseDouble(latNextStr.substring(0, latNextStr.length() - 1));
							//System.out.println("doubleLat is "+ doubleLat);
							//System.out.println("doubleLatNext is "+ doubleLatNext);
							if (doubleLatNext == 0 && doubleLat == 0) {
								repeated = true;
								break;
							}
						}

						//System.out.println("now checking for longitude");

						if ( longNextStr == null ||  longNextStr.length() < 2) {
							setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
							return;
						}
						else if (longStr.equals(longNextStr)) {
							repeated = true;
							break;
						}

						else {
							double doubleLong = Double.parseDouble(longStr.substring(0, longStr.length() - 1));
							double doubleLongNext = Double.parseDouble(longNextStr.substring(0, longNextStr.length() - 1));
							//System.out.println("doubleLong is "+ doubleLong);
							//System.out.println("doubleLongNext is "+ doubleLongNext);
							if (doubleLongNext == 0 && doubleLong == 0) {
								repeated = true;
								break;
							}
						}
					}
				}

				if (repeated) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeRepeating")); //$NON-NLS-1$
					return;
				}

			} catch(NumberFormatException e) {
				setError(Msg.getString("SimulationConfigEditor.error.latitudeLongitudeBadEntry")); //$NON-NLS-1$
			}

			Iterator<SettlementInfo> i = settlements.iterator();
			while (i.hasNext()) {
				SettlementInfo settlement = i.next();

				// Check that settlement name is valid.
				if ((settlement.name == null) || (settlement.name.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.nameMissing")); //$NON-NLS-1$
				}

				// Check if population is valid.
				if ((settlement.population == null) || (settlement.population.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.populationMissing")); //$NON-NLS-1$
				} else {
					try {
						int popInt = Integer.parseInt(settlement.population);
						if (popInt < 0) {
							setError(Msg.getString("SimulationConfigEditor.error.populationTooFew")); //$NON-NLS-1$
						}
					} catch (NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.populationInvalid")); //$NON-NLS-1$
					}
				}

				// Check if number of robots is valid.
				if ((settlement.numOfRobots == null) || (settlement.numOfRobots.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsMissing")); //$NON-NLS-1$
				} else {
					try {
						int num = Integer.parseInt(settlement.numOfRobots);
						if (num < 0) {
							setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsTooFew")); //$NON-NLS-1$
						}
					} catch (NumberFormatException e) {
						setError(Msg.getString("SimulationConfigEditor.error.numOfRobotsInvalid")); //$NON-NLS-1$
					}
				}

				// Check that settlement latitude is valid.
				if ((settlement.latitude == null) || (settlement.latitude.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.latitudeMissing")); //$NON-NLS-1$
				} else {
					String cleanLatitude = settlement.latitude.trim().toUpperCase();
					if (!cleanLatitude.endsWith(Msg.getString("direction.northShort")) &&
					        !cleanLatitude.endsWith(Msg.getString("direction.southShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
						setError(
							Msg.getString(
								"SimulationConfigEditor.error.latitudeEndWith", //$NON-NLS-1$
								Msg.getString("direction.northShort"), //$NON-NLS-1$
								Msg.getString("direction.southShort") //$NON-NLS-1$
							)
						);
					}
					else {
						String numLatitude = cleanLatitude.substring(0, cleanLatitude.length() - 1);
						try {
							double doubleLatitude = Double.parseDouble(numLatitude);
							if ((doubleLatitude < 0) || (doubleLatitude > 90)) {
								setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
							}
						}
						catch(NumberFormatException e) {
							setError(Msg.getString("SimulationConfigEditor.error.latitudeBeginWith")); //$NON-NLS-1$
						}
					}
				}

				// Check that settlement longitude is valid.
				if ((settlement.longitude == null) || (settlement.longitude.isEmpty())) {
					setError(Msg.getString("SimulationConfigEditor.error.longitudeMissing")); //$NON-NLS-1$
				} else {
					String cleanLongitude = settlement.longitude.trim().toUpperCase();
					if (!cleanLongitude.endsWith(Msg.getString("direction.westShort")) &&
					        !cleanLongitude.endsWith(Msg.getString("direction.eastShort"))) { //$NON-NLS-1$ //$NON-NLS-2$
						setError(
							Msg.getString(
								"SimulationConfigEditor.error.longitudeEndWith", //$NON-NLS-1$
								Msg.getString("direction.eastShort"), //$NON-NLS-1$
								Msg.getString("direction.westShort") //$NON-NLS-1$
							)
						);
					} else {
						String numLongitude = cleanLongitude.substring(0, cleanLongitude.length() - 1);
						try {
							double doubleLongitude = Double.parseDouble(numLongitude);
							if ((doubleLongitude < 0) || (doubleLongitude > 180)) {
								setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
							}
						} catch(NumberFormatException e) {
							setError(Msg.getString("SimulationConfigEditor.error.longitudeBeginWith")); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

}