/**
 * Mars Simulation Project
 * TabPanelDashboard.java
 * @version 3.07 2014-12-01
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.swing.unit_window.structure;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.structure.ObjectiveType;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.SubmitButton;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.MarsPanelBorder;
import org.mars_sim.msp.ui.swing.unit_window.TabPanel;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;

/**
 * Tab panel displaying general info regarding the settlement <br>
 */
@SuppressWarnings("restriction")
public class TabPanelDashboard extends TabPanel {

	/** default serial id. */
	private static final long serialVersionUID = 1L;

	private double progress;
	private long lastTimerCall;
	private boolean toggle, running = false;

	private String[] objectives;

	// Data members
	private JFXPanel jfxpanel;
	private Scene scene;
	private StackPane stack;
	private Label objLabel;

	// private ToggleGroup group;
	private ToggleButton toggleBtn;
	private SubmitButton commitButton;
	private VBox buttonBox = new VBox();
	private AnimationTimer timer;

	private List<ToggleButton> buttons = new ArrayList<>();

	private Settlement settlement;

	/**
	 * Constructor.
	 * 
	 * @param settlement
	 *            {@link Settlement} the settlement this tab panel is for.
	 * @param desktop
	 *            {@link MainDesktopPane} the main desktop panel.
	 */
	@SuppressWarnings("restriction")
	public TabPanelDashboard(Settlement settlement, MainDesktopPane desktop) {
		// Use the TabPanel constructor
		super(Msg.getString("TabPanelDashboard.title"), //$NON-NLS-1$
				null, Msg.getString("TabPanelDashboard.title.tooltip"), //$NON-NLS-1$
				settlement, desktop);

		// Initialize data members.
		this.settlement = settlement;
		objectives = settlement.getObjectiveArray();
		// this.cb = new
		// ComboBox<ObjectiveType>();//FXCollections.observableArrayList(objArray));
		// this.cb.getItems().setAll(ObjectiveType.values());
		// setupChoiceBox();

		// setupToggleGroup();
		createButtonPane();

		jfxpanel = new JFXPanel();

		int width = 400;
		int height = 500;

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stack = new StackPane();
				stack.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;"
						+ "-fx-border-color: #c1bf9d;" + "-fx-background-radius: 2px;");

				scene = new Scene(stack, width, height);
				scene.setFill(Color.TRANSPARENT);// .BLACK);
				jfxpanel.setScene(scene);

				Label title = new Label(Msg.getString("TabPanelDashboard.title"));
				Reflection reflection = new Reflection();
				title.setEffect(reflection);
				reflection.setTopOffset(0.0);
				title.setPadding(new Insets(5, 5, 0, 5));
				// title.setFont(new Font("Arial", 20));
				title.setFont(Font.font("Cambria", FontWeight.BOLD, 16));

				VBox toggleVBox = new VBox();
				toggleVBox.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;"
						+ "-fx-border-color: #c1bf9d;" + "-fx-background-radius: 2px;");
				toggleVBox.getChildren().addAll(buttonBox);
				toggleVBox.setAlignment(Pos.TOP_CENTER);
				toggleVBox.setPadding(new Insets(5, 5, 5, 5));

				VBox topVBox = new VBox();
				topVBox.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;"
						+ "-fx-border-color: #c1bf9d;" + "-fx-background-radius: 2px;");
				topVBox.setAlignment(Pos.TOP_CENTER);
				// vbox.getChildren().addAll(title, cb, hbox);
				topVBox.getChildren().addAll(title, new Label(), toggleVBox);

				stack.getChildren().add(topVBox);
			}
		});

		centerContentPanel.add(jfxpanel);
		this.setSize(new Dimension(width, height));
		this.setVisible(true);

		/*
		 * JPanel centerPanel = new JPanel(new BorderLayout());
		 * centerPanel.setBorder(new MarsPanelBorder());
		 * centerContentPanel.add(centerPanel, BorderLayout.CENTER);
		 */

	}

	public String addSpace(String s) {
		s = s.replace(" ", System.lineSeparator());
		return s;
	}

	public void createButtonPane() {

		String header = "Settlement Objective";

		int size = objectives.length;

		for (int i = 0; i < size; i++) {

			int index = i;
			String s = objectives[i];
			String ss = null;
			String sss = addSpace(s);
			toggleBtn = new ToggleButton();

			toggleBtn.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {

					toggleBtn.setSelected(true);

					for (int j = 0; j < size; j++) {
						if (index != j) {
							buttons.get(j).setSelected(false);
						}
					}
				}
			});

			toggleBtn.setPadding(new Insets(5, 5, 5, 5));
			toggleBtn.setTooltip(new Tooltip(sss));

			buttons.add(toggleBtn);
			// btn.setGraphic(new Rectangle(10,10, Color.BURLYWOOD));
			if (i == 0)
				ss = "/icons/settlement_goals/cropfarm.png";
			else if (i == 1)
				ss = "/icons/settlement_goals/manufacture.png";
			else if (i == 2)
				ss = "/icons/settlement_goals/research.png";
			else if (i == 3)
				ss = "/icons/settlement_goals/transport.png";
			else if (i == 4)
				ss = "/icons/settlement_goals/trade.png";
			else if (i == 5)
				ss = "/icons/settlement_goals/trip_128.png";//free_market_128.png";

			toggleBtn.setGraphic(new ImageView(new Image(this.getClass().getResourceAsStream(ss))));
			// btn.setStyle("-fx-alignment: LEFT;");
			toggleBtn.setAlignment(Pos.BASELINE_CENTER);
			toggleBtn.setMaxHeight(90);
			toggleBtn.setMaxWidth(90);

			if (settlement.getObjective().toString().equals(s))
				toggleBtn.setSelected(true);
			else
				toggleBtn.setSelected(false);

		}

		VBox options = new VBox();
		options.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;" + "-fx-border-color: #c1bf9d;"
				+ "-fx-background-radius: 2px;");
		HBox hbox0 = new HBox();
		hbox0.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;" + "-fx-border-color: #c1bf9d;"
				+ "-fx-background-radius: 2px;");
		HBox hbox1 = new HBox();
		hbox1.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;" + "-fx-border-color: #c1bf9d;"
				+ "-fx-background-radius: 2px;");
		hbox0.getChildren().addAll(buttons.get(0), buttons.get(1), buttons.get(2));
		hbox1.getChildren().addAll(buttons.get(3), buttons.get(4), buttons.get(5));

		createCommitButton();

		StackPane commitPane = new StackPane(commitButton);
		commitPane.setPrefSize(100, 50);
		commitPane.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;" + "-fx-border-color: #c1bf9d;"
				+ "-fx-background-radius: 2px;");

		options.getChildren().addAll(hbox0, hbox1, commitPane);

		TitledPane titledPane = new TitledPane(header, options);
		titledPane.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;" + "-fx-border-color: #c1bf9d;"
				+ "-fx-background-radius: 2px;");
		titledPane.setTooltip(new Tooltip("The direction where the settlement would devote its surplus resources"));
		// titledPane.setId("titledpane");
		// titledPane.setPrefSize(100, 100);
		buttonBox.getChildren().add(titledPane);
		buttonBox.setStyle("-fx-border-style: 2px; " + "-fx-background-color: #c1bf9d;" + "-fx-border-color: #c1bf9d;"
				+ "-fx-background-radius: 2px;");

	}


	public void createCommitButton() {
		commitButton = new SubmitButton();
		commitButton.setOnMousePressed(e -> {
        	if (!running) {

        		timer.start();
                running = true;
                commitButton.setDisable(true);

    			int index = -1;
    			for (int j = 0; j < 5; j++) {
    				if (buttons.get(j).isSelected()) {
    					index = j;
    				}
    			}

    			ObjectiveType type = null;

    			if (index == 0)
    				type = ObjectiveType.CROP_FARM;
    			else if (index == 1)
    				type = ObjectiveType.MANUFACTURING;
    			else if (index == 2)
    				type = ObjectiveType.RESEARCH_CENTER;
    			else if (index == 3)
    				type = ObjectiveType.TRADE_TOWN;
    			else if (index == 4)
    				type = ObjectiveType.TRANSPORTATION_HUB;
    			else if (index == 5)
    				type = ObjectiveType.TOURISM;

    			settlement.setObjective(type);
    			
        	}
 
        	e.consume();
		});
		
		commitButton.statusProperty().addListener(o -> System.out.println(commitButton.getStatus()));


		progress = 0;
		lastTimerCall = System.nanoTime();
		timer = new AnimationTimer() {
			@Override
			public void handle(long now) {
                if (now > lastTimerCall + 2_000l) {
					progress += 0.005;
					commitButton.setProgress(progress);
					lastTimerCall = now;
                    if (toggle) {
                       	
                        if (progress > 0.75) {
                            progress = 0;
                            commitButton.setFailed();
                            timer.stop();
                            running = false;
                            toggle ^= true;
                            commitButton.setDisable(false);
                        }
                        
                    } else {
                        if (progress > 1) {
                            progress = 0;
                            commitButton.setSuccess();
                            timer.stop();
                            running = false;
                            toggle ^= true;
                            commitButton.setDisable(false);
                        }
                    }
				}
			}
		};
	}

	/*
	 * Display the initial system greeting and update the css style
	 */
	// 2016-10-31 Added update()
	@Override
	public void update() {

		 int theme = MainScene.getTheme(); 
		 if (theme == 6) { 
			 //String cssFile ="/fxui/css/snowBlue.css"; 
		 	commitButton.setColor(Color.web("#34495e")); // navy blue
			 //commitButton.getStylesheets().clear();
			 //commitButton.getStyleClass().add("button-broadcast");
			 //commitButton.getStylesheets().add(getClass().getResource(cssFile).toExternalForm()); 
		 } else if (theme == 7) { 
			 commitButton.setColor(Color.web("#b85c01")); // orange
			 //String cssFile ="/fxui/css/nimrodskin.css"; commitButton.getStylesheets().clear();
			 //commitButton.getStyleClass().add("button-broadcast");
			 //commitButton.getStylesheets().add(getClass().getResource(cssFile).toExternalForm()); 
		 } else { 
			 commitButton.setColor(Color.web("#b85c01")); // orange
			 //String cssFile ="/fxui/css/nimrodskin.css"; commitButton.getStylesheets().clear();
		 	//commitButton.getStyleClass().add("button-broadcast");
		 	//commitButton.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
		 }

	}

	/**
     * Prepare object for garbage collection.
     */
    public void destroy() {
    	jfxpanel = null;
    	scene = null;
		stack	= null;
		objLabel= null;
		toggleBtn = null;
		commitButton = null;
		buttonBox = null;
		timer.stop();
		timer = null;
		commitButton = null;
    }
}