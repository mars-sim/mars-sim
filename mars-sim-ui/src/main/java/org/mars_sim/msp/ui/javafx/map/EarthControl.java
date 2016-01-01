
package org.mars_sim.msp.ui.javafx.map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Adopted codes from P.J. Meisch (pj.meisch@sothawo.com).
 */
public class EarthControl extends Application {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class */
    //private static final Logger logger;

    // -------------------------- STATIC METHODS --------------------------
    static {
        //SLF4JBridgeHandler.removeHandlersForRootLogger();
        //SLF4JBridgeHandler.install();
        //logger = LoggerFactory.getLogger(MarsScape.class);
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void start(Stage primaryStage) throws Exception {
        //logger.info("Starting MarsScape");
        String fxmlFile = "EarthControl.fxml";
        //logger.debug("loading fxml file {}", fxmlFile);
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent rootNode = fxmlLoader.load(getClass().getResourceAsStream(fxmlFile));
        //logger.trace("stage loaded");
        Scene scene = new Scene(rootNode);
        //logger.trace("scene created");

        primaryStage.setTitle("MarsScape");
        primaryStage.setScene(scene);
        //logger.trace("showing scene");
        primaryStage.show();

        //logger.debug("application start method finished.");
    }

// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        //logger.trace("begin main");
        launch(args);
        //logger.trace("end main");
    }
}
