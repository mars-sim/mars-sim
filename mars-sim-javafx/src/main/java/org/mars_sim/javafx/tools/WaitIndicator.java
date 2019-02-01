/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mars_sim.javafx.tools;

import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Created by hansolo on 08.04.16.
 */
public class WaitIndicator { //extends Application {
    private CircularProgressIndicator indicator;

    private Stage stage;
    
    //@Override  
    public void init() {
        indicator = new CircularProgressIndicator();
    }

	//@Override 
    //public void start(Stage stage) {
    public WaitIndicator(Stage stage) {	
    	this.stage = stage;
    	//stage = new Stage();
    	indicator = new CircularProgressIndicator();
        StackPane pane = new StackPane(indicator);
        
		//stackPane.setScaleX(1.2);
		//stackPane.setScaleY(1.2);

        pane.setBackground(Background.EMPTY);
        pane.setStyle(
     		   //"-fx-border-style: none; "
     		   //"-fx-background-color: #231d12; "
        			"-fx-background-color: transparent; "
        			+ 
        			"-fx-background-radius: 1px;"
     		   );
        
        Scene scene = new Scene(pane, 128, 128, true);

		scene.setFill(Color.TRANSPARENT);
		
		stage.requestFocus();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("Circular Progress Indicator");
        stage.setScene(scene);
        stage.toFront();
        stage.show();
        
        indicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }

    //@Override public void stop() {
    //    //System.exit(0);
    //	stage.close();//.hide();
    //}

    public Stage getStage() {
    	return stage;
    }
    
    public static void main(String[] args) {
        //launch(args); 	
    }

}
