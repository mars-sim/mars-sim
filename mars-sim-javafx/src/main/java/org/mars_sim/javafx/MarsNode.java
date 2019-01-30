/**
 * Mars Simulation Project
 * MarsNode.java
 * @version 3.1.0 2015-04-18
 * @author Manny Kung
 */

package org.mars_sim.javafx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.controlsfx.control.PopOver;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.function.FunctionType;
import org.mars_sim.msp.core.structure.building.function.farming.Farming;

import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
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

//import com.sibvisions.rad.ui.javafx.ext.mdi.FXDesktopPane;
//import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;
//import com.sibvisions.rad.ui.javafx.ext.mdi.windowmanagers.FXDesktopWindowManager;
//import com.sibvisions.rad.ui.javafx.ext.mdi.windowmanagers.FXGlobalModalWindowManager;

/**
 * The MarsNode class is the is the container for housing
 * new javaFX UI tools.
 */

public class MarsNode {

	private static Logger logger = Logger.getLogger(MarsNode.class.getName());

	/** Tool name. */
	public static final String NAME = "Node Tool";

	private MainScene mainScene;
	private Stage stage;
	private Scene scene;
	//private FXDesktopWindowManager windowManager;
	//private FXDesktopPane fxDesktopPane;
	private Pane jmePane;
	//private FXInternalWindow jmeWindow;
	JPanel panel;

	public MarsNode(MainScene mainScene, Stage stage) {
		this.mainScene = mainScene;
		this.stage = stage;
		this.scene = stage.getScene();

		//windowManager = new FXDesktopWindowManager();

	}

	
//	public FXDesktopPane createFXDesktopPane() {
//		fxDesktopPane = new FXDesktopPane();
//		fxDesktopPane.setWindowManager(windowManager);
//
//
//		//fxDesktopPane.getScene();
//		//fxDesktopPane.setBackground(Color.BLACK);
//
//		// The FXDesktopPane supports window manager. The standard window manager shows multiple windows on the desktop and handles global windows.
//
//		// It's also possible to set a window manager for single-window mode. This window manager shows the content as maximized window:
//
//		//fxDesktopPane.setWindowManager(new FXGlobalModalWindowManager(new FXSingleWindowManager()));
//		//If you don't need support for global windows:
//		//fxDesktopPane.setWindowManager(new FXSingleWindowManager());
//
//		return fxDesktopPane;
//	}
//
//	public FXDesktopPane getFXDesktopPane() {
//		return fxDesktopPane;
//	}
//
//	public void removeFXInternalWindow(FXInternalWindow fxInternalWindow ) {
//		windowManager.removeWindow(fxInternalWindow);
//	}

	// Create the chat box
	public void createChatBox() {
		ChatBox cb = new ChatBox(mainScene);

		TextArea ta = cb.getTextArea();
		ta.setTooltip(new Tooltip ("Voice chatters from settlement's Comm Nets"));

 		//AutoFillTextBox<?> autoFillTextBox = cb.getAutoFillTextBox();
  		//autoFillTextBox.setTooltip(new Tooltip ("Use UP/DOWN arrows to scroll input history."));
  		//autoFillTextBox.getTextbox().setPromptText("Type your msg here");// to broadcast to a channel");

		//TextField tf = cb.getTextField();
		//tf.setTooltip(new Tooltip ("Broadcast your message to the global channel on Mars"));
		//tf.setPromptText("Type here");

		ta.appendText("Safe Net : a small dust storm is being built up within 20 km of Alpha Base"
    		  + System.lineSeparator());

		//StackPane pane = new StackPane(cb);

		//FXInternalWindow window = createFXInternalWindow("Comm Radio", pane, 350, 200, false);
		//window.setMinWidth(350);
		//window.setMinHeight(200);
		//window.setMaxSize(scene.getWidth(), scene.getHeight());

	}

	public void createMarsMap() {

//	    String fxmlFile = "/fxml/DemoApp.fxml";
//        FXMLLoader fxmlLoader = new FXMLLoader();
//        Parent rootNode = null;
//		try {
//			rootNode = fxmlLoader.load(getClass().getResourceAsStream(fxmlFile));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		PannableView p = new PannableView();
		ScrollPane s = p.createMap();

        //FXInternalWindow window = createFXInternalWindow("MarsScape Map", s, 1280, 700, false);
        //p.setWindow(window);

        //window.setMaxSize(scene.getWidth(), scene.getHeight());
		//window.setMinWidth(1280);
		//window.setMinHeight(700);
		//window.setMaximizeable(true);

	}

	public void createEarthMap() {

	    String fxmlFile = "/fxml/DemoApp.fxml";
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent rootNode = null;
		try {
			rootNode = fxmlLoader.load(getClass().getResourceAsStream(fxmlFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

       // FXInternalWindow window = createFXInternalWindow("Earth Real-time Tiled Map", rootNode, 1280, 700, false);

        //window.setMaxSize(scene.getWidth(), scene.getHeight());
		//window.setMinWidth(1280);
		//window.setMinHeight(700);

	}


	public void createDragDrop() {
		DragDrop dd = new DragDrop();
		StackPane pane = dd.createDragDropBox();

		//FXInternalWindow window = createFXInternalWindow("Drag and Drop", pane, 100, 200, false);
		//window.setMinWidth(600);
		//window.setMinHeight(500);
		//window.setMaxSize(scene.getWidth(), scene.getHeight());

	}

	public void createStory() {

		Story story = new Story();
		StackPane pane = story.createGUI();
		pane.getStylesheets().add(getClass().getResource("/css/story.css").toExternalForm());

		//FXInternalWindow window = createFXInternalWindow("Day One", pane, 800, 400, false);
		//window.setMinWidth(600);
		//window.setMinHeight(500);

		//Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		//window.setX((screenBounds.getWidth() - fxDesktopPane.getWidth()) / 2);
		//window.setY((screenBounds.getHeight() - fxDesktopPane.getHeight()) / 2);

	}

	// Test the use of fxml
	public void createMaterialDesignWindow() {
		//Pane pane = createPane("black");

		Parent root = null;
		try {
			root = javafx.fxml.FXMLLoader.load(getClass().getResource("/materialdesign/MaterialFxTester.fxml"));
			//materialdesign/Materialfx-toggleswitch.fxml"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO: can use createFXInternalWindow(String title, StackPane pane, //Pane pane, //Parent parent,
		//		double prefWidth, double prefHeight, boolean resizable)

//		FXInternalWindow fxInternalWindow = new FXInternalWindow("Material Design Showcase");
//		fxInternalWindow.setContent(root);
//		fxInternalWindow.getStylesheets().add(getClass().getResource("/materialdesign/material-fx-v0_3.css").toExternalForm());
//
//		fxInternalWindow.setActive(true);
//		fxInternalWindow.setCloseable(false);
//		fxInternalWindow.setMinimizable(true);
//		fxInternalWindow.setPrefSize(1024, 640);
//		fxInternalWindow.setMinHeight(512);
//		fxInternalWindow.setMinWidth(1024);
//		fxInternalWindow.setMaximizeable(true);
//		//.setMaxSize(scene.getWidth(), scene.getHeight());
//
//		windowManager.addWindow(fxInternalWindow);
//		windowManager.updateActiveWindow();

	}

	
//	public void createSettlementWindow() {
//		Pane pane = createPane("black");
//
//		FXInternalWindow fxInternalWindow = new FXInternalWindow("Settlements");
//		fxInternalWindow.setContent(pane);
//		fxInternalWindow.setActive(true);
//		fxInternalWindow.setCloseable(false);
//		fxInternalWindow.setMinimizable(true);
//		fxInternalWindow.setPrefSize(210, 210);
//		fxInternalWindow.setMinHeight(210);
//		fxInternalWindow.setMinWidth(210);
//		fxInternalWindow.setMaximizeable(false);
//		windowManager.addWindow(fxInternalWindow);
//		windowManager.updateActiveWindow();
//	}


//	public FXInternalWindow createFXInternalWindow(String title, Parent parent, //StackPane pane, //Pane pane, //Parent parent,
//			double prefWidth, double prefHeight, boolean resizable) {
//		FXInternalWindow fxInternalWindow = new FXInternalWindow(title);
//
//		fxInternalWindow.setActive(true);
//		fxInternalWindow.setCloseable(false);
//		fxInternalWindow.setMinimizable(true);
//		fxInternalWindow.setPrefSize(prefWidth, prefHeight); // not compatible with webView
//		//fxInternalWindow.setMinHeight(prefHeight);
//		//fxInternalWindow.setMinWidth(prefWidth);
//		fxInternalWindow.setMaxHeight(fxDesktopPane.getHeight());
//		fxInternalWindow.setMaxWidth(fxDesktopPane.getWidth());
//		fxInternalWindow.setResizeable(resizable);
//		fxInternalWindow.setMaximizeable(true);
//		fxInternalWindow.setContent(parent);
//
//		windowManager.addWindow(fxInternalWindow);
//		windowManager.updateActiveWindow();
//		return fxInternalWindow;
//	}


	/**
	 * Creates settlement nodes
	 *@param color
	 *@return Pane
	 */
	public Pane createPane(String color) {
	    Pane pane = new Pane();
	    //pane.setPrefSize(400, 200);
	    pane.setStyle("-fx-background-color: " + color);
	    //pane.setEffect(new DropShadow(2d, 0d, +2d, Color.BLACK));
	    VBox v = new VBox(10);
	    v.setSpacing(10);
	    v.setPadding(new Insets(20, 20, 20, 20));

	    Collection<Settlement> settlements = Simulation.instance().getUnitManager().getSettlements();
		List<Settlement> settlementList = new ArrayList<Settlement>(settlements);
		Iterator<Settlement> i = settlementList.iterator();
		while(i.hasNext()) {
			Settlement settlement = i.next();
			v.getChildren().addAll(createEach(settlement));
		}

		pane.getChildren().add(v);

	    return pane;
	  }

	public Label createEach(Settlement settlement) {
		Label l = new Label(settlement.getName());

		l.setPadding(new Insets(20));
		l.setAlignment(Pos.CENTER);
		l.setMaxWidth(Double.MAX_VALUE);

		l.setId("settlement-node");
		l.getStylesheets().add("/fxui/css/settlementnode.css");

		l.setOnMouseClicked(new EventHandler<MouseEvent>() {
      	PopOver popOver = null;
          @Override
          public void handle(MouseEvent evt) {
        	if (popOver == null ) {
                 popOver = createPopOver(l, settlement);
          	}
        	else if (evt.getClickCount() >= 1) {
                popOver.hide(Duration.seconds(.5));
         	}
        	else if (popOver.isShowing()) {
          		popOver.hide(Duration.seconds(.5));
          	}
        	else if (!popOver.isShowing()) {
          		popOver = createPopOver(l, settlement);
          	}
          }
      });
		return l;
	}

	public PopOver createPopOver(Label l, Unit unit) {
		Settlement settlement = null;
		Person person = null;
		if (unit instanceof Settlement)
			settlement = (Settlement) unit;
		else if (unit instanceof Person)
			person = (Person) unit;

		String title = l.getText();
		PopOver popover = new PopOver();
		//popover.setDetachedTitle(title);
		popover.setDetachable(true);
		popover.show(l);

		HBox root = new HBox();
		root.setSpacing(2);
		root.setPadding(new Insets(10, 10, 10, 10));

		if (settlement != null) {

			Label topLabel = new Label("Settlement Dashboard");
			topLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18));
			topLabel.setLineSpacing(1);
			topLabel.setPadding(new Insets(5, 5, 5, 5));

			topLabel.setTextFill(Color.FIREBRICK);
			topLabel.setAlignment(Pos.BOTTOM_CENTER);

	        VBox vBox = new VBox();
	        Accordion acc = new Accordion();

	        acc.getPanes().addAll(this.createPanes(settlement));
	        vBox.getChildren().addAll(topLabel, acc);
	        root.getChildren().addAll(vBox);

      }

      else if (person != null) {
    	  System.out.println("inside createPopOver() if person...");

			Label topLabel = new Label("Settler Dashboard");
			topLabel.setFont(Font.font("Verdana", FontWeight.BOLD, 18)); //"Cambria"
			topLabel.setLineSpacing(1);
			topLabel.setPadding(new Insets(5, 5, 5, 5));
			topLabel.setTextFill(Color.FIREBRICK);
			topLabel.setAlignment(Pos.BOTTOM_CENTER);

	        VBox vBox = new VBox();

	        vBox.getChildren().addAll(topLabel);
	        root.getChildren().addAll(vBox);
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

	
//	public TitledPane createTemperatureGauge() {
//
//		TitledPane tp = new TitledPane();
//	    tp.setText("Weather");
//
//	    String family = "Helvetica";
//	    double size = 12;
//
//		TemperatureGauge temperatureGauge = new TemperatureGauge();
//		Pane pane = temperatureGauge.toDraw();
//		tp.setContent(pane);
//
//	    return tp;
//	}

	
	private Collection<TitledPane> createPanes(Settlement settlement){
      Collection<TitledPane> result = new ArrayList<TitledPane>();
      TitledPane tp = new TitledPane();

      // TODO: need to hack this eu.hansolo.enzo package to make it small.
      //tp = createTemperatureGauge();
      //result.add(tp);

      tp = new TitledPane();
      tp.setText("Population");

      String family = "Helvetica";
      double size = 12;

      TextFlow textFlow = new TextFlow();
      textFlow.setLayoutX(80);
      textFlow.setLayoutY(80);
      Text text1 = new Text("Settlers: " + settlement.getIndoorPeopleCount() + " of " + settlement.getPopulationCapacity() + "\n");
      text1.setFont(Font.font(family, FontWeight.LIGHT, size));
      //Text text2 = new Text("\nNames: " + settlement.getInhabitants());
      //text2.setFont(Font.font(family, FontPosture.ITALIC, size));
      Text text3 = new Text("Bots: " + settlement.getNumBots() + " of " + settlement.getRobotCapacity() + "\n");
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

	    Collection<Person> persons = settlement.getIndoorPeople();
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
              	if (popOver == null ) {
                       popOver = createPopOver(l, person);
              	}
              	else if (evt.getClickCount() >= 1) {
                      popOver.hide(Duration.seconds(.5));
               	}
              	else if (popOver.isShowing()) {
                		popOver.hide(Duration.seconds(.5));
              	}
              	else if (!popOver.isShowing()) {
                		popOver = createPopOver(l, person);
              	}
          }
      });

		return l;
	}

  public void createGreenhouses(TitledPane tp, Settlement settlement) {
  	VBox v = new VBox();
	    v.setSpacing(10);
	    v.setPadding(new Insets(0, 20, 10, 20));

  	List<Building> buildings = settlement.getBuildingManager().getACopyOfBuildings();

		Iterator<Building> iter1 = buildings.iterator();
		while (iter1.hasNext()) {
			Building building = iter1.next();
	    	if (building.hasFunction(FunctionType.FARMING)) {
	//        	try {
	        		Farming farm = (Farming) building.getFunction(FunctionType.FARMING);
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

//	public void createDialog(Alert alert) {
//
//	    DialogPane dialogPane = alert.getDialogPane();
//	    dialogPane.getStylesheets().add(
//	    //getClass().getResource("dialog.css").toExternalForm());
//	    "/fxui/css/dialog.css");
//
//	    final DialogPane dlgPane = dlg.getDialogPane();
//	    dlgPane.getButtonTypes().add(ButtonType.OK);
//	    dlgPane.getButtonTypes().add(ButtonType.CANCEL);
//
//	    final Button btOk = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
//	    btOk.addEventFilter(ActionEvent.ACTION, (event) -> {
//	      if (!validateAndStore()) {
//	        event.consume();
//	      }
//	    });
//
//	    dlg.showAndWait();
//	}
	

	public class JmeTask implements Runnable {

		JPanel panel;
		public JmeTask(JPanel panel) {
			this.panel = panel;
		}

		@Override
		public void run() {
	 		//JmeCanvas jmeCanvas = new JmeCanvas();
	 		//panel.add(jmeCanvas.setupJME());
		}
	}


//	public void createJMEWindow(Stage stage) {
//		this.scene = stage.getScene();
//
//		jmePane = new Pane();
//
//		//SwingNode swingNode = new SwingNode();
//
//        //SwingUtilities.invokeLater(() -> {
//    	//	swingNode.setContent(panel);
//        //});
//
//		Runnable jmeTask = new JmeTask(panel);
//
//        SwingUtilities.invokeLater(jmeTask);
//
//        SwingNode swingNode = new SwingNode();
//		swingNode.setContent(panel);
//
//
//		jmePane.getChildren().add(swingNode);
//
//		jmeWindow = new FXInternalWindow("Mars Viewer");
//		jmeWindow.setContent(jmePane);
//		jmeWindow.setActive(true);
//		jmeWindow.setCloseable(false);
//		jmeWindow.setMinimizable(true);
//		jmeWindow.setPrefSize(512, 512);
//		jmeWindow.setMinHeight(480);
//		jmeWindow.setMinWidth(480);
//
//		windowManager.addWindow(jmeWindow);
//	}

	
    public void jmeCall() {

 		//JmeCanvas jmeCanvas = new JmeCanvas();
 		//JPanel panel = new JPanel(new BorderLayout(0, 0));
 		//panel.add(jmeCanvas.setupJME());
    }



//	public void createJMEWindow(Stage stage) {
//		this.scene = stage.getScene();
//
//
//		JmeCanvas jmeCanvas = new JmeCanvas();
//		JPanel panel = new JPanel(new BorderLayout(0, 0));
//		panel.add(jmeCanvas.setupJME());
//
//		jmePane = new Pane();
//		//ChangeListener<Object> repoListener = new RepositionListener();
//
//		SwingNode swingNode = new SwingNode();
//
//        SwingUtilities.invokeLater(() -> {
//    		swingNode.setContent(panel);
//        });
//
//		jmePane.getChildren().add(swingNode);
//
//		jmeWindow = new FXInternalWindow("Mars Viewer");
//		jmeWindow.setContent(jmePane);
//		jmeWindow.setActive(true);
//		jmeWindow.setCloseable(false);
//		jmeWindow.setMinimizable(true);
//		jmeWindow.setPrefSize(512, 512);
//		jmeWindow.setMinHeight(480);
//		jmeWindow.setMinWidth(480);
//
//		//fxDesktopPane.widthProperty().addListener(repoListener);
//		//fxDesktopPane.heightProperty().addListener(repoListener);
//
//		//stage.xProperty().addListener(repoListener);
//		//stage.yProperty().addListener(repoListener);
//		//stage.getScene().xProperty().addListener(repoListener);
//		//stage.getScene().yProperty().addListener(repoListener);
//
//		//jmePane.widthProperty().addListener(repoListener);
//		//jmePane.heightProperty().addListener(repoListener);
//
//		windowManager.addWindow(jmeWindow);
//	}
//
//	private class RepositionListener implements ChangeListener<Object> {
//
//		@Override
//		public void changed(ObservableValue<? extends Object> arg0, Object arg1, Object arg2) {
//			int x = (int) (fxDesktopPane.getLayoutX() + jmePane.getLocalToParentTransform().getTx());
//			int y = (int) (fxDesktopPane.getLayoutY() + jmePane.getLocalToParentTransform().getTy());
//			//int y = (int) (stage.getX() + jmePane.getLocalToSceneTransform().getTx()); //+ scene.getX()
//			//int y = (int) (stage.getY() + jmePane.getLocalToSceneTransform().getTy()); // scene.getY()
//			int w = (int) jmePane.getWidth();
//			int h = (int) jmePane.getHeight();
//			EventBus.publish(new JmeRepositionEvent(x, y, w, h));
//		}
//	}


}


