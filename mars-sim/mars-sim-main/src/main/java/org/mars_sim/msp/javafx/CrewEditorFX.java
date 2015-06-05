/**
 * Mars Simulation Project
 * CrewEditorFX.java
 * @version 3.08 2015-03-30
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.core.person.PersonConfig;
import org.mars_sim.msp.core.person.PersonGender;


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


	private Stage stage;

	/**
	 * Constructor.
	 * @param config SimulationConfig
	 */
	public CrewEditorFX(SimulationConfig config) {

		//this.config = config;
		pc = config.getPersonConfiguration();

		stage = new Stage();

		Group root = new Group();

		BorderPane borderAll = new BorderPane();
		borderAll.setPadding(new Insets(5, 5, 5, 5));

		borderAll.setCenter(gridPane);
		root.getChildren().add(borderAll);

		Label titleLabel = new Label("Alpha Crew Manifest");
		titleLabel.setAlignment(Pos.CENTER);

		HBox hTop = new HBox();
		hTop.setAlignment(Pos.CENTER);
		hTop.getChildren().add(titleLabel);

		borderAll.setTop(hTop);

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

	    // don't forget to add children to gridpane
	    gridPane.getChildren().addAll(empty, slotOne, slotTwo, slotThree, slotFour);

		Label name = new Label("Name :");
		Label gender = new Label("Gender :");
		Label personality = new Label("Personality :");
		Label job = new Label("Job :");

	    GridPane.setConstraints(name, 0, 1);
	    GridPane.setConstraints(gender, 0, 2);
	    GridPane.setConstraints(personality, 0, 3);
	    GridPane.setConstraints(job, 0, 4);

	    gridPane.getChildren().addAll(name, gender, personality, job);

		setUpCrewName();
		setUpCrewGender();
		setUpCrewPersonality();
		setUpCrewJob();

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

				String personalityStr = (String) personalityList.get(i).getValue();
				//System.out.println(" personality is " + personalityStr);
				// update PersonConfig with the new personality
				pc.setPersonPersonality(i, personalityStr);

				//String jobStr = jobTF.get(i).getText();
				String jobStr = (String) jobsList.get(i).getValue();
				//System.out.println(" job is " + jobStr);
				// update PersonConfig with the new job
				pc.setPersonJob(i, jobStr);
			}

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

        stage.getIcons().add(new Image(this.getClass().getResource("/icons/lander_hab.svg").toString()));

        stage.centerOnScreen();
        //stage.setResizable(true);
 	   	stage.setFullScreen(false);
        stage.setTitle(TITLE);
        stage.show();
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
		else if (choice == 1)
			 m = setUpPersonalityCB();
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
			gridPane.add(g, j+1, 2);
			//genderOListComboBox.add(g);
			g.setValue(s[j]);
			genderList.add(g);
		}
	}


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
		jobs.add("Botanist");
		jobs.add("Areologist");
		jobs.add("Doctor");
		jobs.add("Engineer");
		jobs.add("Driver");
		jobs.add("Chef");
		jobs.add("Trader");
		jobs.add("Technician");
		jobs.add("Architect");
		jobs.add("Biologist");
		jobs.add("Astronomer");
		jobs.add("Chemist");
		jobs.add("Physicist");
		jobs.add("Mathematician");
		jobs.add("Meteorologist");
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
		    gridPane.add(g, i+1, 4);
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