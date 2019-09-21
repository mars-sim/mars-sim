/* Mars Simulation Project
 * Browser.java
 * @version 3.1.0 2019-09-20
 * @author Manny Kung
 */

package org.mars_sim.javafx;

//import com.sibvisions.rad.ui.javafx.ext.mdi.FXInternalWindow;
//import com.sibvisions.rad.ui.javafx.ext.scene.StackedScenePane;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Browser {

	/** Tool name. */
	public static final String NAME = "Web Tool";

	private MainScene mainScene;

	public Browser(MainScene mainScene) {
		this.mainScene = mainScene;
	}

	public Stage startWebTool() {

	    Stage webStage = new Stage();
	    webStage.setTitle("Mars Simulation Project - Online Tool");
	    webStage.initModality(Modality.APPLICATION_MODAL);

	    final WebView webView = new WebView();
	    final WebEngine webEngine = webView.getEngine();

	    ScrollPane scrollPane = new ScrollPane();
	    scrollPane.setFitToWidth(true);
	    scrollPane.setContent(webView);

	    webEngine.getLoadWorker().stateProperty()
	        .addListener(new ChangeListener<State>() {
	          //@Override
	          public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, State oldState, State newState) {
	            if (newState == Worker.State.SUCCEEDED) {
	            	webStage.setTitle(webEngine.getLocation());
	            }
	          }
	        });

	    webEngine.load("http://mars-sim.sourceforge.net/#development");
	    //webEngine.load("http://jquerymy.com/");

	    Scene webScene = new Scene(scrollPane);
	    webStage.setScene(webScene);

	    return webStage;
	}

/*
	public FXInternalWindow startMSPWebSite() {


		FXInternalWindow fxInternalWindow = new FXInternalWindow();
		//Stage webStage = new Stage();
	    //Group root = new Group();
	    //webStage.setTitle("Mars Simulation Project Website");
	    //webStage.initModality(Modality.APPLICATION_MODAL);

		BorderPane borderPane = new BorderPane();

		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(5,5,5,5));
		vbox.setAlignment(Pos.CENTER_LEFT);

		HBox hbox = new HBox(10);
		hbox.setPadding(new Insets(5,5,5,5));
		hbox.setAlignment(Pos.CENTER_LEFT);

		TextField textField = new TextField();
		textField.setStyle("-fx-text-inner-color: orange;");
		textField.setPadding(new Insets(5,5,5,5));
		textField.setPrefColumnCount(800);
		//textField.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
		textField.setMaxWidth(800);
		textField.setTooltip(new Tooltip("Enter your URL here"));
		textField.setPromptText("mars-sim.sourceforge.net/#development");
		//textField.setOnAction(eh);

		//EventHandler<ActionEvent> eh = new EventHandler<ActionEvent>() {
        //    @Override
        //    public void handle(ActionEvent event) {
        //    	textField.requestFocus();
        //    	textField.appendText(((Button) event.getSource()).getText());
        //    }
        //};


		Button homeButton = new Button("Home");
		Button reloadButton = new Button("Load");

	    final WebView webView = new WebView();
	    final WebEngine webEngine = webView.getEngine();

	    webEngine.getLoadWorker().stateProperty()
	        .addListener(new ChangeListener<State>() {
	          //@Override
	          public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, State oldState, State newState) {
	            if (newState == Worker.State.SUCCEEDED) {
	            	//webStage.setTitle(webEngine.getLocation());
	            }
	          }
	        });
	    //webEngine.load("http://jquerymy.com/");
	    webEngine.load("http://mars-sim.sourceforge.net/#development");

		hbox.getChildren().addAll(homeButton, textField, reloadButton);
		vbox.getChildren().add(hbox);

		borderPane.setTop(vbox);
		borderPane.setCenter(webView);
	    //vbox.getChildren().add(webView);

	    reloadButton.defaultButtonProperty().bind(reloadButton.focusedProperty());
		//button.setDefaultButton(true);
		//button.setOnAction(buttonAction(textField, progressBar, webEngine, webView));
		reloadButton.setOnAction(buttonAction(textField, webEngine, webView));
		homeButton.setOnAction(buttonAction(new TextField("http://mars-sim.sourceforge.net/#development"), webEngine, webView));


        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    //loadPage(textField, progressBar, webEngine, webView);
            		if (textField.getText() !=null)
            			loadPage(textField, webEngine, webView);
                }
            }
        });

	    //ObservableList<Node> children = root.getChildren();
        //children.add(webView);

		StackPane pane = new StackPane();
        pane.getChildren().add(borderPane);
		//Parent parent = new Region();
        //parent.getChildrenUnmodifiable().add(vbox);
        //StackedScenePane stackedScenePane = new StackedScenePane(webStage, root);
 	    fxInternalWindow = mainScene.getMarsNode().createFXInternalWindow("Web Tool", pane, 800, 600, false);

	    return fxInternalWindow;
	}
*/

	public void loadPage(TextField textField, //ProgressBar progressBar,
			WebEngine webEngine, WebView webView) {

		String route = textField.getText();
		if (route !=null)
			if (!route.substring(0, 7).equals("http://")) {
				route = "http://" + route;
				textField.setText(route);
			}

		System.out.println("Loading route: " + route);
		//progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());

		webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
			@Override
			public void changed(ObservableValue<? extends State> value,
					State oldState, State newState) {
				if(newState == State.SUCCEEDED){
					System.out.println("Location loaded + " + webEngine.getLocation());
				}
			}
		});
		webEngine.load(route);


	}

	private EventHandler<ActionEvent> buttonAction(final TextField textField, //final ProgressBar progressBar,
			final WebEngine webEngine,
			final WebView webView) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (textField.getText() != null) {
					loadPage(textField, //progressBar,
							webEngine,
							webView);
				}
			}
		};
	}

}
