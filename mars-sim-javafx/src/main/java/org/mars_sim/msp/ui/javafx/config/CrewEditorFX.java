/**
 * Mars Simulation Project
 * CrewEditorFX.java
 * @version 3.1.0 2017-10-18
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.ai.job.JobType;
import org.mars_sim.msp.core.reportingAuthority.ReportingAuthorityType;
import org.mars_sim.msp.core.tool.Conversion;
import org.mars_sim.msp.core.person.GenderType;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;

/**
 * CrewEditorFX allows users to design the crew manifest for an initial
 * settlement
 */
public class CrewEditorFX {

	/** Tool name. */
	public static final String TITLE = "Crew Editor for Alpha Crew";

	public static final int COMBOBOX_WIDTH = 270;
	public static final int HEIGHT = 720;
	public static final int WIDTH = 1024;
	public static final int SIZE_OF_CREW = PersonConfig.SIZE_OF_CREW;

	public static final int ALPHA_CREW = PersonConfig.ALPHA_CREW;

	public static final int NAME_ROW = 1;
	public static final int GENDER_ROW = 2;
	public static final int JOB_ROW = 3;
	public static final int SPONSOR_ROW = 4;
	public static final int COUNTRY_ROW = 5;
	public static final int PERSONALITY_ROW = 6;
	public static final int DESTINATION_ROW = 7;

//	private static final double BLUR_AMOUNT = 10;
//	private static final Effect frostEffect = new BoxBlur(BLUR_AMOUNT, BLUR_AMOUNT, 3);

	private static final ImageView background = new ImageView();

	private static final StackPane layout = new StackPane();

	// Data members
	private boolean[][] personalityArray;

	private boolean goodToGo = true;

	private String destinationName;
	private String sponsorName;
	
	private GridPane gridPane;

	private JFXButton commitButton;
	private JFXComboBox<String> destinationCB;
	private JFXComboBox<String> destinationsOListComboBox = new JFXComboBox<String>();

	private List<JFXComboBox<String>> genderList;
	private List<JFXComboBox<String>> jobList;
	private List<JFXComboBox<String>> countryList;
	private List<JFXComboBox<String>> sponsorList;
	
	private List<JFXTextField> nameTF;
	private List<SettlementBase> settlements;
	private List<String> settlementNames = new ArrayList<String>();
	private List<String> sponsorNames = new ArrayList<String>();
	
	private ObservableList<String> destinationsOList;
	private ObservableList<String> sponsorsOList;

	private Stage stage;

	// private SimulationConfig config; // needed in the constructor
	private ScenarioConfigEditorFX scenarioConfigEditorFX;

	private PersonConfig personConfig;

	/**
	 * Constructor.
	 * 
	 * @param simulationConfig       SimulationConfig
	 * @param scenarioConfigEditorFX ScenarioConfigEditorFX
	 */
	public CrewEditorFX(SimulationConfig simulationConfig, ScenarioConfigEditorFX scenarioConfigEditorFX) {

		this.personConfig = simulationConfig.getPersonConfiguration();
		this.scenarioConfigEditorFX = scenarioConfigEditorFX;

		personalityArray = new boolean[4][SIZE_OF_CREW];

		nameTF = new ArrayList<JFXTextField>();

		genderList = new ArrayList<JFXComboBox<String>>();
		jobList = new ArrayList<JFXComboBox<String>>();
		countryList = new ArrayList<JFXComboBox<String>>();
		sponsorList = new ArrayList<JFXComboBox<String>>();

		createGUI();

	}

	public void setID(Label l) {
		l.setId("#textLabel");
	}

	/*
	 * Creates the stage for displaying and modifying the alpha crew member
	 * attributes
	 */
	public void createGUI() {

		scenarioConfigEditorFX.setCrewEditorOpen(true);

		BorderPane borderPane = new BorderPane();
		borderPane.setPadding(new Insets(5, 5, 5, 5));

		// Create list panel.
		gridPane = new GridPane();
		gridPane.setPadding(new Insets(15, 15, 15, 15));
		gridPane.setHgap(10.0);
		gridPane.setVgap(10.0);
		borderPane.setCenter(gridPane);

		Label empty = new Label("");
		Label slotOne = new Label("Slot 1");
		slotOne.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label slotTwo = new Label("Slot 2");
		slotTwo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label slotThree = new Label("Slot 3");
		slotThree.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label slotFour = new Label("Slot 4");
		slotFour.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");

		setID(slotOne);
		setID(slotTwo);
		setID(slotThree);
		setID(slotFour);

		GridPane.setConstraints(empty, 0, 0); // column=2 row=0
		GridPane.setConstraints(slotOne, 1, 0);
		GridPane.setConstraints(slotTwo, 2, 0);
		GridPane.setConstraints(slotThree, 3, 0);
		GridPane.setConstraints(slotFour, 4, 0);
		// Note: don't forget to add children to gridpane
		gridPane.getChildren().addAll(empty, slotOne, slotTwo, slotThree, slotFour);

		Label name = new Label("Name :");
		name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		name.setPrefWidth(70);
		name.setMaxWidth(70);
		name.setMinWidth(70);
		Label gender = new Label("Gender :");
		gender.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label job = new Label("Job :");
		job.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label sponsor = new Label("Sponsor :");
		sponsor.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label country = new Label("Country :");
		country.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		Label personality = new Label("MBTI :");
		personality.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;-fx-text-fill: green;");
		// Label destination = new Label("Destination :");

		setID(name);
		setID(gender);
		setID(job);
		setID(sponsor);
		setID(country);
		setID(personality);
		// setID(destination);

		GridPane.setConstraints(name, 0, 1);
		GridPane.setConstraints(gender, 0, 2);
		GridPane.setConstraints(job, 0, 3);
		GridPane.setConstraints(sponsor, 0, 4);
		GridPane.setConstraints(country, 0, 5);
		GridPane.setConstraints(personality, 0, 6);
		gridPane.getChildren().addAll(name, gender, job, sponsor, country, personality);

		setUpCrewName();
		setUpCrewGender();
		setUpCrewJob();
		setUpCrewCountry();
		setUpCrewSponsor();

		for (int col = 1; col < SIZE_OF_CREW + 1; col++) {
			setUpCrewPersonality(col);
		}

		// Create commit button.
		commitButton = new JFXButton();
		setMouseCursor(commitButton);
		commitButton.setGraphic(
				new ImageView(new Image(this.getClass().getResourceAsStream("/fxui/icons/round_play_32.png"))));
		commitButton.getStyleClass().add("button-mid");
		// commitButton.setTooltip(new
		// Tooltip(Msg.getString("CrewEditorFX.tooltip.commit")));
		scenarioConfigEditorFX.setQuickToolTip(commitButton, Msg.getString("CrewEditorFX.tooltip.commit")); //$NON-NLS-1$
		commitButton.setId("commitButton");
		commitButton.setAlignment(Pos.CENTER);
		commitButton.requestFocus();
		commitButton.setOnAction((event) -> {

			goodToGo = true;
			String destinationStr = (String) destinationCB.getValue();
			destinationName = destinationStr;
			
			for (int i = 0; i < SIZE_OF_CREW; i++) {
				
				// Name
				String nameStr = nameTF.get(i).getText().trim();
				// Added isBlank() and checking against invalid names
				if (!Conversion.isBlank(nameStr)) {
					// update PersonConfig with the new name
					personConfig.setPersonName(i, nameStr, ALPHA_CREW);
					goodToGo = true && goodToGo;
				} else {
					Alert alert = new Alert(AlertType.ERROR, "A settler's name is invalid. Please double check again!");
					alert.initOwner(stage);
					alert.setTitle("Invalid Input");
					alert.showAndWait();
					// it cannot proceed
					goodToGo = false;
					scenarioConfigEditorFX.disableStartButton();
					// event.consume();
					nameTF.get(i).requestFocus();
					return;
				}

				// Gender
				String genderStr = genderList.get(i).getValue();
				if (genderStr.equals("M"))
					genderStr = "MALE";
				else if (genderStr.equals("F"))
					genderStr = "FEMALE";
				// update PersonConfig with the new gender
				personConfig.setPersonGender(i, genderStr, ALPHA_CREW);

				// Personality
				String personalityStr = getPersonality(i);
				// update PersonConfig with the new personality
				personConfig.setPersonPersonality(i, personalityStr, ALPHA_CREW);

				// Job
				String jobStr = (String) jobList.get(i).getValue();

				if (!Conversion.isBlank(jobStr)) {
					personConfig.setPersonJob(i, jobStr, ALPHA_CREW);
					goodToGo = true && goodToGo;
				} else {
					goodToGo = false;
					jobList.get(i).requestFocus();
				}
				
				// Sponsor
				String sponsorStr = (String) sponsorList.get(i).getValue();
				System.out.println("commitButton. " + i + " : " + sponsorStr);
				
				if (!Conversion.isBlank(sponsorStr)) {
					personConfig.setPersonSponsor(i, sponsorStr, ALPHA_CREW);
					goodToGo = true && goodToGo;
				} else {
					goodToGo = false;
					sponsorList.get(i).requestFocus();
				}
								
				// Country
				String countryStr = (String) countryList.get(i).getValue();
				System.out.println("commitButton. " + i + " : " + countryStr);

				if (!Conversion.isBlank(countryStr)) {
					personConfig.setPersonCountry(i, countryStr, ALPHA_CREW);
					goodToGo = true && goodToGo;
				} else {
					goodToGo = false;
					countryList.get(i).requestFocus();
				}

				// Destination
				if (!Conversion.isBlank(destinationStr)) {
					// update PersonConfig with the new destination
					personConfig.setPersonDestination(i, destinationStr, ALPHA_CREW);
					goodToGo = true && goodToGo;
				}
				else {
					goodToGo = false;
					destinationCB.requestFocus();
				}
			}

			
			boolean allHaveSameSponsor = true;
			String s = "";
			for (int i = 0; i < SIZE_OF_CREW; i++) {
				if (i == 0) {
					s = (String) sponsorList.get(i).getValue();
					if (s == null || s.equals("")) {
						goodToGo = false;
						sponsorList.get(i).requestFocus();
					}
				}
				else {
					String ss = (String) sponsorList.get(i).getValue();
					if (ss == null || ss.equals("")) { 
						goodToGo = false;
						sponsorList.get(i).requestFocus();
					}
					else if (s != null && !s.equals(ss)) {
						allHaveSameSponsor = false;
						break;
					}
				}
			}
			
			if (allHaveSameSponsor) {
				// Bring the changes back to the TableViewCombo
				scenarioConfigEditorFX.getTableViewCombo().setSameSponsor(destinationName, s);
			}
			else {
				// Bring the changes back to the TableViewCombo
				scenarioConfigEditorFX.getTableViewCombo().setSameSponsor(destinationName, "Varied");				
			}
			
			if (goodToGo) {
				scenarioConfigEditorFX.setCrewEditorOpen(false);
				stage.hide();
			}
			else
				event.consume();

		});

		// Create button pane.
		HBox commitBox = new HBox();
		commitBox.setPadding(new Insets(2, 10, 2, 10));
		commitBox.setAlignment(Pos.CENTER);
		commitBox.getChildren().add(commitButton);

		Label destLabel = new Label("Settlement Destination :  ");
		destLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;-fx-text-fill: green;");
		String dest = personConfig.getConfiguredPersonDestination(0, ALPHA_CREW);
		destinationCB = setUpCB(DESTINATION_ROW, 0); // 6 = Destination
		destinationCB.setValue(dest);

		// Create button pane.
		HBox hDestBox = new HBox();
		hDestBox.setPadding(new Insets(10, 10, 10, 10));
		hDestBox.setAlignment(Pos.CENTER);
		hDestBox.getChildren().addAll(destLabel, destinationCB, commitBox);

//		VBox vDestBox = new VBox();
//		vDestBox.setPadding(new Insets(10, 10, 25, 10));
//		vDestBox.setAlignment(Pos.CENTER);
//		vDestBox.getChildren().addAll(hDestBox, commitBox);

		// borderPane.setBottom(vDestBox);
		borderPane.setTop(hDestBox);

		layout.setStyle("-fx-background-radius:20; -fx-background-color: null;");
		// -fx-background-color: rgba(209,89,56,)");
		// cyan blue: rgba(56, 176, 209, ");
		// "-fx-background-color: null");
		layout.setEffect(new DropShadow(10, Color.GREY));
		layout.getChildren().addAll(borderPane);

		ScrollPane scrollPane = new ScrollPane(layout);
		// scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
		scrollPane.setPrefHeight(HEIGHT);
		scrollPane.setPrefWidth(WIDTH);
		scrollPane.setMinHeight(HEIGHT);
		scrollPane.setMinWidth(WIDTH);

		Scene scene = new Scene(scrollPane);// , HEIGHT, WIDTH); // Color.TRANSPARENT
		scene.getStylesheets().add("/fxui/css/config/configEditorFXOrange.css");
		scene.getStylesheets().add("/fxui/css/config/crewEditorFXOrange.css");
		scene.setFill(Color.TRANSPARENT); // needed to eliminate the white border

		stage = new Stage();
		stage.setMaxHeight(800);
		stage.setMaxWidth(1366);
		// stage.setMinHeight(720);
		// stage.setMinWidth(800);
		stage.setScene(scene);
		// stage.initStyle(StageStyle.TRANSPARENT);
		stage.setOpacity(.95);
		stage.sizeToScene();
		stage.toFront();
		stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));// toString()));
		stage.centerOnScreen();
		// stage.setResizable(true);
		stage.setFullScreen(false);
		stage.setTitle(TITLE);

		// Call setMonitor()
		scenarioConfigEditorFX.getMainMenu().setMonitor(stage);

		stage.show();
		stage.setOnCloseRequest(e -> {
			scenarioConfigEditorFX.setCrewEditorOpen(false);
			// stage.close(); already implied
		});
		// makeSmoke(stage);
		// background.setImage(copyBackground(stage));
		// background.setEffect(frostEffect);
		// makeDraggable(stage, layout);

	}

	/**
	 * Swaps the mouse cursor type between DEFAULT and HAND
	 *
	 * @param node
	 */
	public void setMouseCursor(Node node) {
		node.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
			node.setCursor(Cursor.DEFAULT);
		});

		node.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
			node.setCursor(Cursor.HAND);
		});
	}

//	/*
//	 * Validates and saves the current alpha crew configuration
//	 */
	// public void validateRecordChange(ActionEvent event) {}
	
	
	/**
	 * Sets up the comboboxes 
	 * 
	 * @param choice
	 * @param index
	 * @return {@link JFXComboBox}
	 */
	public JFXComboBox<String> setUpCB(int choice, int index) {
		JFXComboBox<String> m = null;
		if (choice == GENDER_ROW)
			m = setUpGenderCB(index);
		else if (choice == JOB_ROW)
			m = setUpJobCB(index);
		else if (choice == SPONSOR_ROW)
			m = setUpSponsorCB(index);
		else if (choice == COUNTRY_ROW)
			m = setUpCountryCB(index);
		// else if (choice == 4)
		// m = setUpPersonalityCB();
		else if (choice == DESTINATION_ROW)
			m = setUpDestinationCB();

		final JFXComboBox<String> g = m;
		// g.setPadding(new Insets(10,10,10,10));
		// g.setId("combobox");

//		if (g != null) {
//			g.setOnAction((event) -> {
//				String s = (String) g.getValue();
//				g.setValue(s);
//
//				vs = new ValidationSupport();
//		        vs.registerValidator(g, Validator.createEmptyValidator( "ComboBox Selection required"));
///*
//		        vs.validationResultProperty().addListener( (o, wasInvalid, isNowInvalid) -> {
//			    	//Collection<?> c
//			    	boolean b = o.getValue().getMessages().contains("ComboBox Selection required");
//			    	if (b)
//			    		System.out.println("Missing ComboBox Selection(s) in Crew Editor. Please double check!");
//				    	//if (o.getValue() == null || o.getValue().equals(""))
//				    	//	System.out.println("invalid choice of country of origin !");
//				    }
//			    );
//*/
//			    vs.invalidProperty().addListener((obs, wasInvalid, isNowInvalid) -> {
//			        if (isNowInvalid) {
//			            System.out.println("Missing a comboBox selection in Crew Editor!");
//			        } else {
//			            System.out.println("That comboBox selection is now valid");
//			        }
//			    });
//
//				commitButton.disableProperty().bind(vs.invalidProperty());
//			});
//		}

		return g;
	}

	/**
	 * Set up crew name
	 */
	public void setUpCrewName() {
		for (int i = 0; i < SIZE_OF_CREW; i++) {
			// Generate the crew
			personConfig.getCrew(i);
			// TODO: will assign this person to the crew
			String n = personConfig.getConfiguredPersonName(i, ALPHA_CREW);
//			SimpleStringProperty np = new SimpleStringProperty(n);
			JFXTextField tf = new JFXTextField();
			// tf.setUnFocusColor(Color.rgb(255, 255, 255, 0.5));
			tf.setFocusColor(Color.rgb(225, 206, 30, 1));
			tf.setId("textfield");
			nameTF.add(tf);
			gridPane.add(tf, i + 1, NAME_ROW); // name's row = 1
			tf.setText(n);
			// np.bindBidirectional(tf.textProperty());
			// tf.textProperty().addListener((observable, oldValue, newValue) ->
			// {
			// tf.setText(newValue);
			// });
		}
	}

	/**
	 * Sets up the gender combobox 
	 * 
	 * @param index
	 * @return {@link JFXComboBox}
	 */
	public JFXComboBox<String> setUpGenderCB(int index) {
		// List<String> genderList = new ArrayList<String>(2);
		// genderList.add("M");
		// genderList.add("F");
		List<String> genderList = Arrays.asList("M", "F");
		ObservableList<String> genderOList = FXCollections.observableArrayList(genderList);
		JFXComboBox<String> cb = new JFXComboBox<String>(genderOList);

		// genderCBs.add(index, cb);

		return cb;
	}
	
	/**
	 * Set up the crew gender choice
	 */
	public void setUpCrewGender() {

		String s[] = new String[SIZE_OF_CREW];
		for (int j = 0; j < SIZE_OF_CREW; j++) {
			GenderType n = personConfig.getConfiguredPersonGender(j, ALPHA_CREW);
			// convert MALE to M, FEMAL to F
			s[j] = n.toString();
			if (s[j].equals("MALE"))
				s[j] = "M";
			else
				s[j] = "F";

			JFXComboBox<String> g = setUpCB(GENDER_ROW, j); // 2 = Gender
			// g.setMaximumRowCount(2);
			gridPane.add(g, j + 1, GENDER_ROW); // gender's row = 2
			// genderOListComboBox.add(g);
			g.setValue(s[j]);
			genderList.add(j, g);
		}
	}

	/**
	 * Set up the crew personality choice
	 * 
	 * @param col
	 */
	public void setUpCrewPersonality(int col) {
		// String n[] = new String[SIZE_OF_CREW];

		String quadrant1A = "Extravert", quadrant1B = "Introvert";
		String quadrant2A = "Intuition", quadrant2B = "Sensing";
		String quadrant3A = "Feeling", quadrant3B = "Thinking";
		String quadrant4A = "Judging", quadrant4B = "Perceiving";
		String cat1 = "Focus", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
		String a = null, b = null, c = null;

		VBox vbox = new VBox();

		for (int row = 0; row < 4; row++) {
			VBox options = new VBox();
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

			JFXRadioButton ra = new JFXRadioButton(a);
			JFXRadioButton rb = new JFXRadioButton(b);
			ra.setUserData(a);
			rb.setUserData(b);

			if (personalityArray[row][col - 1])
				ra.setSelected(true);
			else
				rb.setSelected(true);

			final ToggleGroup group = new ToggleGroup();
			group.setUserData(c);
			ra.setToggleGroup(group);
			rb.setToggleGroup(group);

			final int r = row;
			group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
				public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
					if (group.getSelectedToggle() != null) {
						String s = group.getSelectedToggle().getUserData().toString();
						if (s.equals(quadrant1A) | s.equals(quadrant2A) | s.equals(quadrant3A) | s.equals(quadrant4A))
							personalityArray[r][col - 1] = true;
						else
							personalityArray[r][col - 1] = false;
					}
				}
			});

			options.getChildren().addAll(ra, rb);
			TitledPane titledPane = new TitledPane(c, options);
			// titledPane.setId("titledpane");
			titledPane.setPrefSize(100, 50);
			vbox.getChildren().add(titledPane);
		}

		gridPane.add(vbox, col, PERSONALITY_ROW); // personality's row = 5
	}


	/**
	 * Set up the crew personality choice
	 * 
	 * @param col
	 * @return type string
	 */
	public String getPersonality(int col) {
		String type = null;
		boolean value = true;

		for (int row = 0; row < 4; row++) {
			value = personalityArray[row][col];

			switch (row) {
			case 0:
				if (value)
					type = "E";
				else
					type = "I";
				break;
			case 1:
				if (value)
					type += "N";
				else
					type += "S";
				break;
			case 2:
				if (value)
					type += "F";
				else
					type += "T";
				break;
			case 3:
				if (value)
					type += "J";
				else
					type += "P";
				break;
			}
		}
		return type;
	}

	/**
	 * Sets up the job combobox 
	 * 
	 * @param index
	 * @return {@link JFXComboBox}
	 */
	public JFXComboBox<String> setUpJobCB(int index) {
				
		List<String> jobs = JobType.getEditedList();

		Collections.sort(jobs);

		ObservableList<String> jobsOList = FXCollections.observableArrayList(jobs);
		JFXComboBox<String> cb = new JFXComboBox<String>(jobsOList);

		return cb;
	}

	
	/**
	 * Set up the crew job choice
	 */
	public void setUpCrewJob() {

		String n[] = new String[SIZE_OF_CREW];

		for (int i = 0; i < SIZE_OF_CREW; i++) {
			n[i] = personConfig.getConfiguredPersonJob(i, ALPHA_CREW);
			JFXComboBox<String> g = setUpCB(JOB_ROW, i); // 3 = Job
			// g.setMaximumRowCount(8);
			gridPane.add(g, i + 1, JOB_ROW); // job's row = 3
			g.setValue(n[i]);
			jobList.add(i, g);
		}
	}

	/**
	 * Sets up the sponsor combobox 
	 * 
	 * @param index
	 * @return {@link JFXComboBox}
	 */
	public JFXComboBox<String> setUpSponsorCB(int index) {

		List<String> sponsors = personConfig.createLongSponsorList();
		Collections.sort(sponsors);

		ObservableList<String> sponsorOList = FXCollections.observableArrayList(sponsors);

		JFXComboBox<String> cb = new JFXComboBox<String>(sponsorOList);
		// Use COMBOBOX_WIDTH
		cb.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> param) {
				final ListCell<String> cell = new ListCell<String>() {
					{
						super.setPrefWidth(COMBOBOX_WIDTH);
					}

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							setText(item);

						} else {
							setText(null);
							// setTextFill(Color.RED);
						}

					}
				};
				return cell;
			}
		});

		// sponsorCBs.add(index, cb);

		cb.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
			@Override
			public void changed(ObservableValue<?> ov, Object oldValue, Object newValue) {

				if (oldValue != newValue && newValue != null) {

					String sponsor = (String) newValue;

					int code = -1;
					List<String> list = new ArrayList<>();

					if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CNSA_L)
						list.add("China");
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.CSA_L)
						list.add("Canada");
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ISRO_L)
						list.add("India"); // 2
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.JAXA_L)
						list.add("Japan"); // 3
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MARS_SOCIETY
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.MARS_SOCIETY_L
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACEX
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.SPACEX_L)
						code = 0;
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.NASA_L)
						list.add("USA"); // 4
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.RKA_L)
						list.add("Russia"); // 5
					else if (ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA
							|| ReportingAuthorityType.getType(sponsor) == ReportingAuthorityType.ESA_L)
						code = 1;

					if (code == 0) {
						list = personConfig.createCountryList();
					} else if (code == 1) {
						list = personConfig.createESACountryList();
					}

					Collections.sort(list);

					ObservableList<String> countryOList = FXCollections.observableArrayList(list);

					countryList.get(index).setItems(countryOList);
				}
			}
		});

		return cb;
	}

	/**
	 * Set up the crew sponsor choice
	 */
	public void setUpCrewSponsor() {
//		String n[] = new String[SIZE_OF_CREW];

		for (int i = 0; i < SIZE_OF_CREW; i++) {
			String sponsor = personConfig.getConfiguredPersonSponsor(i, ALPHA_CREW);
			System.out.println("setUpCrewSponsor sponsor : " + sponsor);
//			if (!sponsor.equals(sponsorName)) {
//				sponsor = sponsorName;
//			}
				
			JFXComboBox<String> g = setUpCB(SPONSOR_ROW, i); // 4 = sponsor
			// g.setMaximumRowCount(8);
			gridPane.add(g, i + 1, SPONSOR_ROW); // sponsor's row = 4
			g.setValue(sponsor);//n[i]);
			sponsorList.add(i, g);
			
		}
	}

	/**
	 * Sets up the country combobox 
	 * 
	 * @param index
	 * @return {@link JFXComboBox}
	 */
	public JFXComboBox<String> setUpCountryCB(int index) {

		List<String> countries = personConfig.createCountryList();
		Collections.sort(countries);

		ObservableList<String> countryOList = FXCollections.observableArrayList(countries);
		JFXComboBox<String> cb = new JFXComboBox<String>(countryOList);
		
//		 ValidationSupport vs = new ValidationSupport(); 

		return cb;

	}

	
	/**
	 * Set up the crew country choice
	 */
	public void setUpCrewCountry() {

		String n[] = new String[SIZE_OF_CREW];

		for (int i = 0; i < SIZE_OF_CREW; i++) {
			n[i] = personConfig.getConfiguredPersonCountry(i, ALPHA_CREW);
			JFXComboBox<String> g = setUpCB(COUNTRY_ROW, i); // 5 = Country
			// g.setMaximumRowCount(8);
			gridPane.add(g, i + 1, COUNTRY_ROW); // country's row = 5
			g.setValue(n[i]);
			System.out.println("setUpCrewCountry country : " + n[i]);
			countryList.add(i, g);
		}
	}

	/**
	 * Sets up the sponsor combobox 
	 * 
	 * @return {@link JFXComboBox}
	 */
	public JFXComboBox<String> setUpDestinationCB() {

		retrieveFromTable();
		// destinationsOListComboBox = new JFXComboBox<String>(destinationsOList);
		destinationsOListComboBox.setItems(destinationsOList);

		return destinationsOListComboBox;
	}

	/**
	 * Gets destination name Set up the crew gender choice
	 */
	public void retrieveFromTable() {
		settlements = scenarioConfigEditorFX.getTableViewCombo().getSettlementBase();
		
		// Clear the old names every time the crew editor is loaded.
		settlementNames.clear();
		sponsorNames.clear();
		
		for (int i = 0; i < settlements.size(); i++) {
			SettlementBase s = settlements.get(i);
			String name = s.getName();
			String sponsor = s.getSponsor();
			settlementNames.add(name);
			sponsorNames.add(sponsor);
			
			// Gets the sponsor name from the lowest possible phase
			if (s.getTemplate().equals("Mars Direct Base (Phase 1)")) {
				if (name.equals("Schiaparelli Point")) {
					sponsorName = s.getSponsor();			 
				}
				else
					sponsorName = s.getSponsor();
			}
			else if (s.getTemplate().equals("Mars Direct Base (Phase 2)")) {
				sponsorName = s.getSponsor();
			}
			else if (s.getTemplate().equals("Mars Direct Base (Phase 3)")) {
				sponsorName = s.getSponsor();
			}
			else if (s.getTemplate().equals("Alpha Base (Phase 4)")) {
				sponsorName = s.getSponsor();
			}
			
		}

		destinationsOList = FXCollections.observableArrayList(settlementNames);
		sponsorsOList = FXCollections.observableArrayList(sponsorNames);

	}

	public void updateSettlementNames() {

		retrieveFromTable();

		destinationsOListComboBox.getItems().clear();
		destinationsOListComboBox.setItems(destinationsOList);
	}

	boolean isGoodToGo() {
		return goodToGo;
	}

	// copy a background node to be frozen over.
	private Image copyBackground(Stage stage) {
		final int X = (int) stage.getX();
		final int Y = (int) stage.getY();
		final int W = (int) stage.getWidth();
		final int H = (int) stage.getHeight();

		try {
			java.awt.Robot robot = new java.awt.Robot();
			java.awt.image.BufferedImage image = robot.createScreenCapture(new java.awt.Rectangle(X, Y, W, H));

			return SwingFXUtils.toFXImage(image, null);
		} catch (java.awt.AWTException e) {
			System.out.println("The robot of doom strikes!");
			e.printStackTrace();

			return null;
		}
	}


	/**
	 * Makes a stage draggable using a given node.
	 * 
	 * @param stage
	 * @param byNode
	 */
	public void makeDraggable(final Stage stage, final Node byNode) {
		final Delta dragDelta = new Delta();
		byNode.setOnMousePressed(mouseEvent -> {
			// record a delta distance for the drag and drop operation.
			dragDelta.x = stage.getX() - mouseEvent.getScreenX();
			dragDelta.y = stage.getY() - mouseEvent.getScreenY();
			byNode.setCursor(Cursor.MOVE);
		});
		final BooleanProperty inDrag = new SimpleBooleanProperty(false);

		byNode.setOnMouseReleased(mouseEvent -> {
			byNode.setCursor(Cursor.HAND);

			if (inDrag.get()) {
				stage.hide();

				Timeline pause = new Timeline(new KeyFrame(Duration.millis(50), event -> {
					background.setImage(copyBackground(stage));
					layout.getChildren().set(0, background);

					scenarioConfigEditorFX.getMainMenu().setMonitor(stage);
					stage.show();

				}));
				pause.play();
			}

			inDrag.set(false);
		});
		byNode.setOnMouseDragged(mouseEvent -> {
			stage.setX(mouseEvent.getScreenX() + dragDelta.x);
			stage.setY(mouseEvent.getScreenY() + dragDelta.y);

			layout.getChildren().set(0, makeSmoke(stage));

			inDrag.set(true);
		});
		byNode.setOnMouseEntered(mouseEvent -> {
			if (!mouseEvent.isPrimaryButtonDown()) {
				byNode.setCursor(Cursor.HAND);
			}
		});
		byNode.setOnMouseExited(mouseEvent -> {
			if (!mouseEvent.isPrimaryButtonDown()) {
				byNode.setCursor(Cursor.DEFAULT);
			}
		});
	}

	private javafx.scene.shape.Rectangle makeSmoke(Stage stage) {

		return new javafx.scene.shape.Rectangle(stage.getWidth(), stage.getHeight(),
				Color.WHITESMOKE.deriveColor(0, 1, 1, 0.2 // 0.08
				));
	}

	/** records relative x and y co-ordinates. */
	private static class Delta {
		double x, y;
	}

	public Stage getStage() {
		return stage;
	}

	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		personConfig = null;
		stage = null;
		scenarioConfigEditorFX = null;
		personConfig = null;
		gridPane = null;

	}

}