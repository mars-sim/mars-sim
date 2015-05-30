/**
 * Mars Simulation Project
 * MarsNode.java
 * @version 3.08 2015-04-18
 * @author Manny Kung
 */

package org.mars_sim.msp.ui.javafx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import org.controlsfx.control.PopOver;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.BuildingFunction;
import org.mars_sim.msp.core.structure.building.function.Farming;

import com.sibvisions.rad.ui.javafx.ext.mdi.FXDesktopPane;
import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;
import com.sibvisions.rad.ui.javafx.ext.mdi.windowmanagers.FXDesktopWindowManager;

/**
 * The MarsNode class is the is the container for housing
 * new javaFX UI tools.
 */
public class MarsNode {

	private static Logger logger = Logger.getLogger(MarsNode.class.getName());

	private MainScene mainScene;
	private Stage stage;
	private FXDesktopWindowManager windowManager;
	private FXDesktopPane fxDesktopPane;

	public MarsNode(MainScene mainScene, Stage stage) {
		this.mainScene = mainScene;
		this.stage = stage;

		Pane pane = createPane("black");

		FXInternalWindow fxInternalWindow = new FXInternalWindow("Settlements");
		fxInternalWindow.setContent(pane);
		fxInternalWindow.setActive(true);
		fxInternalWindow.setCloseable(false);
		fxInternalWindow.setMinimizable(true);
		fxInternalWindow.setPrefSize(185, 165);
		fxInternalWindow.setMinHeight(165);
		fxInternalWindow.setMinWidth(185);

		windowManager = new FXDesktopWindowManager();
		windowManager.addWindow(fxInternalWindow);

	}


	public FXDesktopPane createFXDesktopPane() {
		fxDesktopPane = new FXDesktopPane();

		fxDesktopPane.setWindowManager(windowManager);
		//fxDesktopPane.setBackgroundPane();
		//fxDesktopPane.getActiveWindow();

		return fxDesktopPane;
	}

	public FXDesktopPane getFXDesktopPane() {
		return fxDesktopPane;
	}


	public FXInternalWindow createFXInternalWindow(String title, Pane pane,
			double prefWidth, double prefHeight, boolean resizable) {
		FXInternalWindow fxInternalWindow = new FXInternalWindow(title);

		fxInternalWindow.setActive(true);
		fxInternalWindow.setCloseable(false);
		fxInternalWindow.setMinimizable(true);
		//fxInternalWindow.setPrefSize(prefWidth, prefHeight); // not compatible with webView
		fxInternalWindow.setMinHeight(prefHeight);
		fxInternalWindow.setMinWidth(prefWidth);
		fxInternalWindow.setResizeable(resizable);

		//ScrollPane scrollPane = new ScrollPane();
	    //scrollPane.setFitToWidth(true);
	    //scrollPane.setContent(pane);
		//fxInternalWindow.setContent(scrollPane);
		fxInternalWindow.setContent(pane);

		windowManager.addWindow(fxInternalWindow);
		windowManager.updateActiveWindow();
		return fxInternalWindow;
	}

	public void removeFXInternalWindow(FXInternalWindow fxInternalWindow ) {
		windowManager.removeWindow(fxInternalWindow);
	}
	/**
	 * Creates settlement nodes
	 *@param color
	 *@return Pane
	 */
	public Pane createPane(String color) {
	    Pane pane = new Pane();
	    //pane.setPrefSize(400, 200);
	    pane.setStyle("-fx-background-color: " + color);
	    VBox v = new VBox(10);
	    v.setSpacing(10);
	    v.setPadding(new Insets(20, 20, 20, 20));

	    Collection<Settlement> settlements = Simulation.instance().getUnitManager().getSettlements();
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
		Iterator<Settlement> i = settlementList.iterator();
		while(i.hasNext()) {
			Settlement settlement = i.next();
			//String sname = settlement.getName();
			//Label label = new Label(" ");
			v.getChildren().addAll(createEach(settlement));

		}

		pane.getChildren().add(v);


	    return pane;
	  }

	public Label createEach(Settlement settlement) {
		//VBox v = new VBox(10);
	    //v.setSpacing(10);
	    //v.setPadding(new Insets(0, 20, 10, 20));
	    //v.getChildren().addAll();
		Label l = new Label(settlement.getName());
		//Button b = new Button("Detail");

		l.setPadding(new Insets(20));
		l.setMaxWidth(Double.MAX_VALUE);

		l.setId("settlement-node");
		l.getStylesheets().add("/fxui/css/settlementnode.css");

		l.setOnMouseClicked(new EventHandler<MouseEvent>() {
      	PopOver popOver = null;
          @Override
          public void handle(MouseEvent evt) {
          	if (popOver == null) {
          		// TODO: the new popover will go to the front, pushing the old popover to the background
                  popOver = createPopOver(l, settlement);
          	}

          	else if (popOver.isShowing() && !popOver.isDetached()) {
                  popOver.hide(Duration.seconds(.5));  // (Duration.ZERO)
                  //popOver = null;
              }

          	else if (popOver.isShowing() && popOver.isDetached()) {
                  popOver.hide(Duration.ZERO);  // (Duration.ZERO)
                  //popOver.show(b);
                  //popOver.setDetached(true);
              }

          	else if (!popOver.isShowing()) {
                  popOver = createPopOver(l, settlement);
                  //popOver.setDetached(false);
          		//popOver.show(b);
              }

          	else if (evt.getClickCount() == 2 ) {
          		if (!popOver.isShowing()) {
          			popOver = createPopOver(l, settlement);
          			popOver.setDetached(false);
          		}
          		//popOver.show(b);
              }

          }
      });
      /*
		// Handle Button event.
		b.setOnAction((event) -> {

			if (popover == null)
				popover = createPopOver(b);
			else
				popover.hide();
			//System.out.println("create popover");
		});


		b.setOnMouseDragged(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent t) {
	            if (t.isPrimaryButtonDown()) {
	                System.out.println("rockets armed");
	            }
	            if (t.isSecondaryButtonDown()) {
	                System.out.println("autoaim engaged");
	            }
	        }
	    });
		*/

		//b.setStyle("-fx-background-color: orange");

		return l;
	}

	public PopOver createPopOver(Label l, Unit unit) {
		Settlement settlement = null;
		Person person = null;
		if (unit instanceof Settlement)
			settlement = (Settlement) unit;
		else if (unit instanceof Person)
			person = (Person) unit;

		//isPopped = true;
		String title = l.getText();
		PopOver popover = new PopOver();
		popover.setDetachedTitle(title);
		popover.setDetachable(true);
		popover.show(l);

		/*
      popover.setOnShown(evt -> {
      // The user clicked somewhere into the transparent background. If
      // this is the case the hide the window (when attached).
	    	popover.getScene().addEventHandler(MOUSE_CLICKED, mouseEvent -> {
	            if (mouseEvent.getTarget().equals(popover.getScene().getRoot())) {
	                if (!popover.isDetached()) {
	                	popover.hide();
	                }
	            }
	        });

	    });
	      */
      HBox root = new HBox();
	  root.setSpacing(2);
	  root.setPadding(new Insets(10, 10, 10, 10));

      if (settlement != null) {
			//String sname = settlement.getName();
			//Label sideLabel = new Label("Settlement Dashboard");
			//sideLabel.setRotate(-90);
			Label topLabel = new Label("Settlement Dashboard");
			//topLabel.setFont(new Font("Cambria", 20));
			topLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
			//topLabel.setSpacing(10);
			topLabel.setPadding(new Insets(5, 5, 5, 5));

			//topLabel.setOnMouseEntered(new EventHandler<MouseEvent>() {
			 //   @Override public void handle(MouseEvent e) {
			//    	topLabel.setScaleX(1.5);
			//    	topLabel.setScaleY(1.5);
			//    }
			//});

			topLabel.setTextFill(Color.FIREBRICK);
			topLabel.setAlignment(Pos.BOTTOM_CENTER);

	        VBox vBox = new VBox();
	        Accordion acc = new Accordion();

	        acc.getPanes().addAll(this.createPanes(settlement));
	        vBox.getChildren().addAll(topLabel, acc);
	        root.getChildren().addAll(vBox);

      }

      else if (person != null) {
			//String sname = person.getName();
			//Label label = new Label("Your Rating : ");
			//Rating rating = new Rating();
			Pane pane = new Pane();



			//pane.getChildren().add(rating);
			//rating.setMaxSize(200, 20);
			root.getChildren().addAll(pane);



      }

      // Fade In
      Node skinNode = popover.getSkin().getNode();
      skinNode.setOpacity(0);

      Duration fadeInDuration = Duration.seconds(1);

      FadeTransition fadeIn = new FadeTransition(fadeInDuration, skinNode);
      fadeIn.setFromValue(0);
      fadeIn.setToValue(1);
      fadeIn.play();

      popover.setContentNode(root);

      return popover;
	}

	public TitledPane createTemperatureGauge() {

		TitledPane tp = new TitledPane();
	    tp.setText("Weather");

	    String family = "Helvetica";
	    double size = 12;

		TemperatureGauge temperatureGauge = new TemperatureGauge();
		Pane pane = temperatureGauge.toDraw();
		tp.setContent(pane);

	    return tp;
	}

	private Collection<TitledPane> createPanes(Settlement settlement){
      Collection<TitledPane> result = new ArrayList<TitledPane>();
      TitledPane tp = new TitledPane();

      tp = createTemperatureGauge();
      result.add(tp);

      tp = new TitledPane();
      tp.setText("Population");

      String family = "Helvetica";
      double size = 12;

      TextFlow textFlow = new TextFlow();
      textFlow.setLayoutX(80);
      textFlow.setLayoutY(80);
      Text text1 = new Text("Settlers: " + settlement.getCurrentPopulationNum() + " of " + settlement.getPopulationCapacity() + "\n");
      text1.setFont(Font.font(family, FontWeight.LIGHT, size));
      //Text text2 = new Text("\nNames: " + settlement.getInhabitants());
      //text2.setFont(Font.font(family, FontPosture.ITALIC, size));
      Text text3 = new Text("Bots: " + settlement.getCurrentNumOfRobots() + " of " + settlement.getRobotCapacity() + "\n");
      text3.setFont(Font.font(family, FontWeight.LIGHT, size));
      //Text text4 = new Text("\nNames: " + settlement.getRobots());
      //text4.setFont(Font.font(family, FontPosture.ITALIC, size));
      textFlow.getChildren().addAll(text1, text3);

      //TextArea ta1 = new TextArea();
      //ta1.setText("Settlers: " + settlement.getCurrentPopulationNum() + " of " + settlement.getPopulationCapacity() );
      //ta1.appendText("Names: " + settlement.getInhabitants());
      //ta1.appendText("Bots: " + settlement.getCurrentNumOfRobots() + " of " + settlement.getRobotCapacity() );
      //ta1.appendText("Names: " + settlement.getRobots());

      ScrollPane s1 = new ScrollPane();
      s1.setPrefSize(200, 200);
      s1.setContent(createPeople(settlement));
   // Add listener to set ScrollPane FitToWidth/FitToHeight when viewport bounds changes
      //s1.viewportBoundsProperty().addListener(new ChangeListener() {
      //  public void changed(ObservableValue<? extends Bounds> arg0, Bounds arg1, Bounds arg2) {
       //     Node content = s1.getContent();
      //      s1.setFitToWidth(content.prefWidth(-1)<arg2.getWidth());
      //      s1.setFitToHeight(content.prefHeight(-1)<arg2.getHeight());
      //    }}});

      VBox vb = new VBox();
      vb.getChildren().addAll(textFlow, s1);
      tp.setContent(vb);

      result.add(tp);

      tp = new TitledPane();
      tp.setText("Food Preparation");
      tp.setContent(new Button("Kitchen 1"));
      result.add(tp);

      tp = new TitledPane();
      tp.setText("Greenhouse");
      createGreenhouses(tp, settlement);
      result.add(tp);

      return result;
  }

	public VBox createPeople(Settlement settlement) {
	    //BorderPane border = new BorderPane();
	    //border.setPadding(new Insets(20, 0, 20, 20));

	    VBox v = new VBox(10);
	    v.setSpacing(10);
	    v.setPadding(new Insets(0, 20, 10, 20));


	    Collection<Person> persons = settlement.getInhabitants();
		List<Person> personList = new ArrayList<Person>(persons);
		Iterator<Person> i = personList.iterator();
		while(i.hasNext()) {
			Person person = i.next();
			//String sname = person.getName();
			v.getChildren().add(createPerson(person));
		}

		return v;
	}

	public Label createPerson(Person person) {
		//Button b = new Button(person.getName());
		Label l = new Label(person.getName());

		l.setPadding(new Insets(20));
		l.setMaxWidth(Double.MAX_VALUE);
		l.setId("settlement-node");
		l.getStylesheets().add("/fxui/css/personnode.css");
		l.setOnMouseClicked(new EventHandler<MouseEvent>() {
      	PopOver popOver = null;
          @Override
          public void handle(MouseEvent evt) {
          	if (popOver == null) {
          		// TODO: the new popover will go to the front, pushing the old popover to the background
                  popOver = createPopOver(l, person);
          	}

          	else if (popOver.isShowing() && !popOver.isDetached()) {
                  popOver.hide(Duration.seconds(.5));  // (Duration.ZERO)
                  //popOver = null;
              }

          	else if (popOver.isShowing() && popOver.isDetached()) {
                  popOver.hide(Duration.ZERO);  // (Duration.ZERO)
                  //popOver.show(b);
                  //popOver.setDetached(true);
              }

          	else if (!popOver.isShowing()) {
                  popOver = createPopOver(l, person);
                  //popOver.setDetached(false);
          		//popOver.show(b);
              }

          	else if (evt.getClickCount() == 2 ) {
          		if (!popOver.isShowing()) {
          			popOver = createPopOver(l, person);
          			popOver.setDetached(false);
          		}
          		//popOver.show(b);
              }

          }
      });

		return l;
	}

  public void createGreenhouses(TitledPane tp, Settlement settlement) {
  	VBox v = new VBox();
	    v.setSpacing(10);
	    v.setPadding(new Insets(0, 20, 10, 20));

  	List<Building> buildings = settlement.getBuildingManager().getBuildings();

		Iterator<Building> iter1 = buildings.iterator();
		while (iter1.hasNext()) {
			Building building = iter1.next();
	    	if (building.hasFunction(BuildingFunction.FARMING)) {
	//        	try {
	        		Farming farm = (Farming) building.getFunction(BuildingFunction.FARMING);
	            	Button b = createGreenhouseDialog(farm);
	            	v.getChildren().add(b);
	//        	}
	//        	catch (BuildingException e) {}
	        }
		}


	    tp.setContent(v);//"1 2 3 4 5..."));
	    tp.setExpanded(true);

  }


	public Button createGreenhouseDialog(Farming farm) {
		String name = farm.getBuilding().getNickName();
		Button b = new Button(name);
		b.setMaxWidth(Double.MAX_VALUE);

      List<String> choices = new ArrayList<>();
      choices.add("Lettuce");
      choices.add("Green Peas");
      choices.add("Carrot");

      ChoiceDialog<String> dialog = new ChoiceDialog<>("List of Crops", choices);
      dialog.setTitle(name);
      dialog.setHeaderText("Plant a Crop");
      dialog.setContentText("Choose Your Crop:");
      dialog.initOwner(stage); // post the same icon from stage
      dialog.initStyle(StageStyle.UTILITY);
      //dialog.initModality(Modality.NONE);

		b.setPadding(new Insets(20));
		b.setId("settlement-node");
		b.getStylesheets().add("/fxui/css/settlementnode.css");
	    b.setOnAction(e->{
	        // The Java 8 way to get the response value (with lambda expression).
	    	Optional<String> selected = dialog.showAndWait();
	        selected.ifPresent(crop -> System.out.println("Crop added to the queue: " + crop));
	    });

	   //ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
	   //ButtonType buttonTypeOk = new ButtonType("OK", ButtonData.OK_DONE);
	   //dialog.getButtonTypes().setAll(buttonTypeCancel, buttonTypeOk);

	    return b;
	}

	/*
	public void createDialog(Alert alert) {

	    DialogPane dialogPane = alert.getDialogPane();
	    dialogPane.getStylesheets().add(
	    //getClass().getResource("dialog.css").toExternalForm());
	    "/fxui/css/dialog.css");

	    final DialogPane dlgPane = dlg.getDialogPane();
	    dlgPane.getButtonTypes().add(ButtonType.OK);
	    dlgPane.getButtonTypes().add(ButtonType.CANCEL);

	    final Button btOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
	    btOk.addEventFilter(ActionEvent.ACTION, (event) -> {
	      if (!validateAndStore()) {
	        event.consume();
	      }
	    });

	    dlg.showAndWait();
	}
	*/

}
