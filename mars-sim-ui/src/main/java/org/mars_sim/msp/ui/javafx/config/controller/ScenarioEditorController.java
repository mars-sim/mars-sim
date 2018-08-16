/**
 * Mars Simulation Project
 * ScenarioEditorController.java
 * @version 3.1.0 2017-05-08
 * @author Manny Kung
 */
package org.mars_sim.msp.ui.javafx.config.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ResourceBundle;

import org.mars_sim.msp.ui.javafx.config.Scenario;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
//import javafx.scene.control.TooltipBuilder;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author cdea
 */
@SuppressWarnings("restriction")
public class ScenarioEditorController implements Initializable {
    	
        @FXML Hyperlink carlfxHyperlink;
        @FXML TableView<Scenario> recipeTableView1;
        @FXML TableView<Scenario> recipeTableView2;
        //@FXML TableView<Scenario> chap3RecipeTableView;
        //@FXML TableView<Scenario> chap4RecipeTableView;
        Map<String, List<Scenario>> map = new HashMap<>();
        
        
        String facebookURL = "mars-sim.sourceforge.net \n https://www.facebook.com/groups/125541663548/";
        
        @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
            
            loadScenarios();
            Tooltip tt = new Tooltip();
            tt.setText(facebookURL);
            carlfxHyperlink.setTooltip(tt);
                carlfxHyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent evt) {
                        
                        //webEngine.load(rssFeed.link);
                    }
                });
                createColumns(recipeTableView1);
                recipeTableView1.setItems(getChapterRecipes("1"));
                createLaunchAppEvent(recipeTableView1);
                
                createColumns(recipeTableView2);
                recipeTableView2.setItems(getChapterRecipes("2"));
                createLaunchAppEvent(recipeTableView2);
/*                
                createColumns(chap3RecipeTableView);
                chap3RecipeTableView.setItems(getChapterRecipes("3"));
                createLaunchAppEvent(chap3RecipeTableView);
                
                createColumns(chap4RecipeTableView);
                chap4RecipeTableView.setItems(getChapterRecipes("4"));
                createLaunchAppEvent(chap4RecipeTableView);
*/
	}
        private void createLaunchAppEvent(final TableView<Scenario> tableView) {
            
            EventHandler<MouseEvent> x =
                    new EventHandler<MouseEvent>(){
                    public void handle(MouseEvent event){
                        if (event.getClickCount() == 2) {
                            final Scenario recipe = tableView.getSelectionModel().getSelectedItem();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Stage stage = new Stage();
                                    
                                    try {
                                        Class<?> appClass = Class.forName(recipe.getFullClassName());
                                        Object object = appClass.newInstance();
                                        if (object instanceof Application) {
                                            Application app = (Application) object;
                                            app.start(stage);
                                        } else {
                                            Method mainMethod = appClass.getDeclaredMethod("main", new Class[]{String[].class});
                                            String[] argu = null;
                                            mainMethod.invoke(object, new Object[]{argu});
                                        }
                                        
                                    } catch (Exception e) {
                                        e.printStackTrace();

                                    }
                                }
                            });
                        }
                    }
                };
            tableView.setOnMouseClicked(x);
        }
        
        private void loadScenarios() {
        
            
            Properties prop = new Properties();
            try {
                prop.load(getClass().getResourceAsStream("scenarios.properties"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            for (Entry<Object, Object> element:prop.entrySet()){
                String key = String.valueOf(element.getKey());
                String value = String.valueOf(element.getValue());
                String[] scenarioStrings = value.split("[,]+", 5);
                int sortOrder = Integer.parseInt(scenarioStrings[0].trim());
                String chapter = scenarioStrings[1].trim();
                Scenario scenario = new Scenario(key, sortOrder, chapter, scenarioStrings[2].trim(), scenarioStrings[3].trim(), scenarioStrings[4].trim());
                System.out.println(scenario);
                if (map.get(chapter) == null) {
                    map.put(chapter, new ArrayList<Scenario>());
                }
                map.get(chapter).add(scenario);
            }
        }
        private void createColumns(TableView<Scenario> tableView) {
            TableColumn<Scenario, String> recipeNameCol = new TableColumn<>("Settlement");
            recipeNameCol.setCellValueFactory(new PropertyValueFactory("name"));
            recipeNameCol.setPrefWidth( 100 );

            TableColumn<Scenario, String> classNameCol = new TableColumn<>("Template");
            classNameCol.setCellValueFactory(new PropertyValueFactory("className"));
            classNameCol.setPrefWidth( 100 );

            TableColumn<Scenario, String> descriptionCol = new TableColumn<>("Description");
            descriptionCol.setCellValueFactory(new PropertyValueFactory("description"));
            descriptionCol.setPrefWidth(tableView.getPrefWidth() - 200 );
            tableView.getColumns().setAll(recipeNameCol, classNameCol, descriptionCol);
        }
        
        private ObservableList<Scenario> getChapterRecipes(String chapter) {
            ObservableList<Scenario> chap = FXCollections.observableArrayList();
            Collections.sort(map.get(chapter), new Comparator<Scenario>(){
                public int compare(Scenario r1, Scenario r2) {
                    return (r1.getSortOrderProperty()<r2.getSortOrderProperty() ? -1 : (r1==r2 ? 0 : 1));
                }
            });
            
            chap.addAll(map.get(chapter));
            return chap;
        }
}
