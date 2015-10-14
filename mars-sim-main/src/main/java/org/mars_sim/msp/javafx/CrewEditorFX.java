/**
 * Mars Simulation Project
 * CrewEditorFX.java
 * @version 3.08 2015-03-30
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonGender;
import org.mars_sim.msp.javafx.configEditor.ScenarioConfigEditorFX;


/**
 * CrewEditorFX allows users to design the crew manifest for an initial settlement
 */
public class CrewEditorFX {

	/** Tool name. */
	public static final String TITLE = "Alpha Crew Editor";

	public static final int SIZE_OF_CREW = 4;

	// Data members
	private PersonConfig pc;// = SimulationConfig.instance().getPersonConfiguration();

	private GridPane gridPane;

	//private SimulationConfig config; // needed in the constructor

	private List<TextField> nameTF  = new ArrayList<TextField>();

	private ComboBox<String> personalityOListComboBox;
	private ComboBox<String> jobsOListComboBox;
	private ComboBox<String> genderOListComboBox;

	private List<ComboBox<String>> genderList = new ArrayList<ComboBox<String>>();
	private List<ComboBox<String>> jobsList = new ArrayList<ComboBox<String>>();
	private List<ComboBox<String>> personalityList = new ArrayList<ComboBox<String>>();

	private boolean[][] personalityArray;// = new boolean [4][SIZE_OF_CREW];

	private Stage stage;
	private ScenarioConfigEditorFX scenarioConfigEditorFX;

	/**
	 * Constructor.
	 * @param config SimulationConfig
	 * @param scenarioConfigEditorFX ScenarioConfigEditorFX
	 */
	public CrewEditorFX(SimulationConfig config, ScenarioConfigEditorFX scenarioConfigEditorFX ) {

		//this.config = config;
		pc = config.getPersonConfiguration();
		this.scenarioConfigEditorFX = scenarioConfigEditorFX;

		personalityArray = new boolean [4][SIZE_OF_CREW];

		createGUI();
	}

	// 2015-10-07 Added and revised createGUI()
	public void createGUI() {

		scenarioConfigEditorFX.setCrewEditorOpen(true);

		stage = new Stage();

		Group root = new Group();

		BorderPane borderAll = new BorderPane();
		borderAll.setPadding(new Insets(5, 5, 5, 5));

		root.getChildren().add(borderAll);

		Label titleLabel = new Label("Alpha Crew Manifest");
		titleLabel.setAlignment(Pos.CENTER);
		HBox hTop = new HBox();
		hTop.setAlignment(Pos.CENTER);
		hTop.getChildren().add(titleLabel);
		borderAll.setTop(titleLabel);

		// Create list panel.
		gridPane = new GridPane();
		gridPane.setPadding(new Insets(5, 5, 5, 5));
		gridPane.setHgap(3.0);
		gridPane.setVgap(3.0);
		borderAll.setCenter(gridPane);

		Label empty = new Label("");
		Label slotOne = new Label("Slot 1");
		Label slotTwo = new Label("Slot 2");
		Label slotThree = new Label("Slot 3");
		Label slotFour = new Label("Slot 4");

	    GridPane.setConstraints(empty, 0, 0); // column=2 row=0
	    GridPane.setConstraints(slotOne, 1, 0);
	    GridPane.setConstraints(slotTwo, 2, 0);
	    GridPane.setConstraints(slotThree, 3, 0);
	    GridPane.setConstraints(slotFour, 4, 0);

	    // Note: don't forget to add children to gridpane
	    gridPane.getChildren().addAll(empty, slotOne, slotTwo, slotThree, slotFour);

		Label name = new Label("Name :");
		Label gender = new Label("Gender :");
		Label job = new Label("Job :");
		Label personality = new Label("MBTI :");

	    GridPane.setConstraints(name, 0, 1);
	    GridPane.setConstraints(gender, 0, 2);
	    GridPane.setConstraints(job, 0, 3);
	    GridPane.setConstraints(personality, 0, 4);

	    gridPane.getChildren().addAll(name, gender, job, personality);

		setUpCrewName();
		setUpCrewGender();
		setUpCrewJob();

		for (int col = 1 ; col < SIZE_OF_CREW + 1; col++) {
			setUpCrewPersonality(col);
		}

		// Create button pane.
		HBox hBottom = new HBox();
		hBottom.setAlignment(Pos.CENTER);

		// Create commit button.
		Button commitButton = new Button("Commit Changes");
		commitButton.setAlignment(Pos.CENTER);
		commitButton.setOnAction((event) -> {

				for (int i = 0; i< SIZE_OF_CREW; i++) {
					String nameStr = nameTF.get(i).getText();
					//System.out.println(" name is " + nameStr);
					// update PersonConfig with the new name
					pc.setPersonName(i, nameStr);
					//System.out.println(" i is " + i);
					String genderStr = genderList.get(i).getValue();
					if ( genderStr.equals("M")  )
						genderStr = "MALE";
					else if ( genderStr.equals("F") )
						genderStr = "FEMALE";
					//System.out.println(" gender is " + genderStr);
					// update PersonConfig with the new gender
					pc.setPersonGender(i, genderStr);

					String personalityStr = getPersonality(i); //(String) personalityList.get(i).getValue();
					//System.out.println(" personality is " + personalityStr);
					// update PersonConfig with the new personality
					pc.setPersonPersonality(i, personalityStr);

					//String jobStr = jobTF.get(i).getText();
					String jobStr = (String) jobsList.get(i).getValue();
					//System.out.println(" job is " + jobStr);
					// update PersonConfig with the new job
					pc.setPersonJob(i, jobStr);
				}

				scenarioConfigEditorFX.setCrewEditorOpen(false);
				stage.close();


		});

		hBottom.getChildren().add(commitButton);
		borderAll.setBottom(hBottom);

		Scene scene = new Scene(root);

		//scene.setFill(Color.TRANSPARENT); // needed to eliminate the white border
		//stage.initStyle(StageStyle.TRANSPARENT);

		stage.setScene(scene);
		stage.sizeToScene();
		stage.toFront();
	    stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab64.png").toExternalForm()));//toString()));
        stage.centerOnScreen();
        //stage.setResizable(true);
 	   	stage.setFullScreen(false);
        stage.setTitle(TITLE);
        stage.show();

    	stage.setOnCloseRequest(e -> {
			scenarioConfigEditorFX.setCrewEditorOpen(false);
			//stage.close(); already implied
		} );
	}

	public Stage getStage() {
		return stage;
	}

	public void setUpCrewName() {
		for (int i = 0 ; i < SIZE_OF_CREW; i++) {
			String n = pc.getConfiguredPersonName(i);
			//System.out.println(" name is "+ n);
				TextField tf = new TextField();
				nameTF.add(tf);
				gridPane.add(tf, i+1, 1);
				tf.setText(n);
		}
	}

	public ComboBox<String> setUpGenderCB() {

		//List<String> genderList = new ArrayList<String>(2);
		//genderList.add("M");
		//genderList.add("F");
		List<String> genderList = Arrays.asList("M", "F");

		ObservableList<String> genderOList = FXCollections.observableArrayList(genderList);
		genderOListComboBox = new ComboBox<String>(genderOList);

		return genderOListComboBox;
	}


	public ComboBox<String> setUpCB(int choice) {
		ComboBox<String> m = null;
		if (choice == 0)
			 m = setUpGenderCB() ;
		//else if (choice == 1)
			 //m = setUpPersonalityCB();
		else if (choice == 2)
			 m = setUpJobCB();

		final ComboBox<String> g = m;
		g.setOnAction((event) -> {
			String s = (String) g.getValue();
           	g.setValue(s);
		});

		return g;
	}


	public void setUpCrewGender() {

		String s[] = new String[SIZE_OF_CREW];
		for (int j = 0 ; j < SIZE_OF_CREW; j++) {
			PersonGender n = pc.getConfiguredPersonGender(j);
			// convert MALE to M, FEMAL to F
			s[j] = n.toString();
			if (s[j].equals("MALE")) s[j] = "M";
			else s[j] = "F";

			ComboBox<String> g = setUpCB(0); // 0 = Gender
			//g.setMaximumRowCount(2);
			gridPane.add(g, j+1, 2);			// row = 2
			//genderOListComboBox.add(g);
			g.setValue(s[j]);
			genderList.add(g);
		}
	}

/*
	public ComboBox<String> setUpPersonalityCB() {

		List<String> personalityTypes = new ArrayList<String>(16);
		personalityTypes.add("ISTP");
		personalityTypes.add("ISTJ");
		personalityTypes.add("ISFP");
		personalityTypes.add("ISFJ");
		personalityTypes.add("INTP");
		personalityTypes.add("INTJ");
		personalityTypes.add("INFP");
		personalityTypes.add("INFJ");
		personalityTypes.add("ESTP");
		personalityTypes.add("ESTJ");
		personalityTypes.add("ESFP");
		personalityTypes.add("ESFJ");
		personalityTypes.add("ENTP");
		personalityTypes.add("ENTJ");
		personalityTypes.add("ENFP");
		personalityTypes.add("ENFJ");
		Collections.sort(personalityTypes);

		ObservableList<String> personalityOList = FXCollections.observableArrayList(personalityTypes);
		personalityOListComboBox = new ComboBox<String>(personalityOList);

		return personalityOListComboBox;

	}

	public void setUpCrewPersonality() {
		for (int j = 0 ; j < SIZE_OF_CREW; j++) {
			String n[] = new String[16];
			n[j] = pc.getConfiguredPersonPersonalityType(j);

			ComboBox<String> g = setUpCB(1);		 // 1 = Personality
			//g.setMaximumRowCount(8);
		    gridPane.add(g, j+1, 3);
			//g.getModel().setSelectedItem(n[j]);
			g.setValue(n[j]);
			//g.setSelectedItem(n[j]);
			personalityList.add(g);
		}

	}
*/

	// 2015-10-07 Revised setUpCrewPersonality()
	public void setUpCrewPersonality(int col) {
		//String n[] = new String[SIZE_OF_CREW];

		String quadrant1A = "Extravert", quadrant1B = "Introvert";
		String quadrant2A = "Intuition", quadrant2B = "Sensing";
		String quadrant3A = "Feeling", quadrant3B = "Thinking";
		String quadrant4A = "Judging", quadrant4B = "Perceiving";
		String cat1 = "World", cat2 = "Information", cat3 = "Decision", cat4 = "Structure";
		String a = null, b = null, c = null;

		VBox vbox = new VBox();

		for (int row = 0 ; row < 4; row++) {
			VBox options = new VBox();
			if (row == 0) {
				a = quadrant1A;
				b = quadrant1B;
				c = cat1;
			}
			else if (row == 1) {
				a = quadrant2A;
				b = quadrant2B;
				c = cat2;
			}
			else if (row == 2) {
				a = quadrant3A;
				b = quadrant3B;
				c = cat3;
			}
			else if (row == 3) {
				a = quadrant4A;
				b = quadrant4B;
				c = cat4;
			}

			RadioButton ra = new RadioButton(a);
			RadioButton rb = new RadioButton(b);
			ra.setUserData(a);
			rb.setUserData(b);

			if (personalityArray[row][col-1])
				ra.setSelected(true);
			else
				rb.setSelected(true);

			final ToggleGroup group = new ToggleGroup();
			group.setUserData(c);
			ra.setToggleGroup(group);
			rb.setToggleGroup(group);

        	final int r = row;
			group.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
			    public void changed(ObservableValue<? extends Toggle> ov,
			        Toggle old_toggle, Toggle new_toggle) {
			            if (group.getSelectedToggle() != null) {
			            	String s = group.getSelectedToggle().getUserData().toString();
			            	//System.out.println(" selected : " + s);
			                if (s.equals(quadrant1A) | s.equals(quadrant2A) | s.equals(quadrant3A) | s.equals(quadrant4A))
			                	personalityArray [r][col-1] = true;
			                else
			                	personalityArray [r][col-1] = false;
			            }
			    }
			});

			options.getChildren().addAll(ra, rb);
			TitledPane border = new TitledPane(c, options);
		    border.setPrefSize(100, 50);
			vbox.getChildren().add(border);
		}

		gridPane.add(vbox, col, 4);
	}


	// 2015-10-07 Added getPersonality()
	public String getPersonality(int col) {
		String type = null;
		boolean value = true;

		for (int row = 0 ; row < 4; row++) {
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
		//System.out.println("For " + col + " type is " + type);
		return type;
	}

	public ComboBox<String> setUpJobCB() {
	/*
		ObservableList<String> options =
			    FXCollections.observableArrayList(
			        "Option 1",
			        "Option 2",
			        "Option 3"
			    );
		final ComboBox comboBox = new ComboBox(options);
	*/

		List<String> jobs = new ArrayList<String>(15);
		jobs.add("Architect");
		jobs.add("Areologist");
		jobs.add("Astronomer");
		jobs.add("Biologist");
		jobs.add("Botanist");
		jobs.add("Chef");
		jobs.add("Chemist");
		jobs.add("Doctor");
		jobs.add("Driver");
		jobs.add("Engineer");
		//jobs.add("Manager");
		jobs.add("Mathematician");
		jobs.add("Meteorologist");
		jobs.add("Physicist");
		jobs.add("Technician");
		jobs.add("Trader");
		Collections.sort(jobs);

		ObservableList<String> jobsOList = FXCollections.observableArrayList(jobs);
		jobsOListComboBox = new ComboBox<String>(jobsOList);

		return jobsOListComboBox;
	}


	public void setUpCrewJob() {
		for (int i = 0 ; i < SIZE_OF_CREW; i++) {
			String n[] = new String[15];
			n[i] = pc.getConfiguredPersonJob(i);
			ComboBox<String> g = setUpCB(2);		// 2 = Job
		    //g.setMaximumRowCount(8);
		    gridPane.add(g, i+1, 3);			// row = 3
			g.setValue(n[i]);
			jobsList.add(g);
		}
	}


	/**
	 * Prepare this window for deletion.
	 */
	public void destroy() {
		pc = null;
		//config = null;
	}


}