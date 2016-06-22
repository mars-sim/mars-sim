package org.mars_sim.msp.demo;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


// based on http://stackoverflow.com/questions/27150414/javafx-creating-two-stages-at-the-same-time

public class DataLoadingStageExample extends Application {

	private Stage monitorStage;
	private Task<DataClass> dataLoadingTask;
	
    @Override
    public void start(Stage primaryStage) {

        Scene scene = buildInitialUI();
        primaryStage.setScene(scene);

        buildMonitorStage(scene);

        buildProgressUI(monitorStage, dataLoadingTask);
        
        // manage stage layout:
        primaryStage.yProperty().addListener((obs, oldY, newY) -> monitorStage.setY(newY.doubleValue() - 100));
        primaryStage.setTitle("Applcation");

        // show both stages:
        monitorStage.show();
        primaryStage.show();

        // start data loading in a background thread:
        new Thread(dataLoadingTask).start();

    }

    
    private Scene buildInitialUI() {
        Label loadingLabel = new Label("Loading...");
        StackPane root = new StackPane(loadingLabel);
        Scene scene = new Scene(root, 600, 400);
        return scene;
    }

    public void buildMonitorStage(Scene scene) {
    
	    monitorStage = new Stage();
	    
	    dataLoadingTask = createDataLoadingTask();
	
	    // update UI when dataLoadingTask finishes
	    // this will run on the FX Applcation Thread
	    dataLoadingTask.setOnSucceeded(event -> {
	        DataClass data = dataLoadingTask.getValue();
	        scene.setRoot(createUIFromData(data));
	        monitorStage.hide();
	    });
	    
	}
    
    private Task<DataClass> createDataLoadingTask() {
        return new Task<DataClass>() {
            @Override
            public DataClass call() throws Exception {
                // mimic connecting to database
                for (int i=0; i < 100; i++) {
                    updateProgress(i+1, 100);
                    Thread.sleep(50);
                }
                return new DataClass("Data");
            }
        };
    }

    
    private void buildProgressUI(Stage monitorStage,
            Task<?> task) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(task.progressProperty());

        Scene monitorScene = new Scene(new StackPane(progressBar), 400, 75);
        monitorStage.setScene(monitorScene);
        monitorStage.setTitle("Loading progress");
    }


    private Parent createUIFromData(DataClass data) {
        // obviously much more complex in real life
        Label label = new Label(data.getData());
        return new StackPane(label);
    }

    public static class DataClass {
        // obviously much more complex in real life
        private final String data ;
        public DataClass(String data) {
            this.data = data ;
        }
        public String getData() {
            return data ;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}