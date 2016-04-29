
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
import javafx.scene.control.TooltipBuilder;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author cdea
 */
public class AppSamplerController implements Initializable {
    	
        @FXML Hyperlink carlfxHyperlink;
        @FXML TableView<Recipe> chap1RecipeTableView;
        @FXML TableView<Recipe> chap2RecipeTableView;
        @FXML TableView<Recipe> chap3RecipeTableView;
        @FXML TableView<Recipe> chap4RecipeTableView;
        Map<String, List<Recipe>> recipeMap = new HashMap<>();
        
        @Override
	public void initialize(URL arg0, ResourceBundle arg1) {
            
            loadRecipes();
            carlfxHyperlink.setTooltip(TooltipBuilder.create()
                                                     .text("Blog: carlfx.wordpress.com \nTwitter: @carldea \ne-mail: carldea@yahoo.com")
                                                     .build()
                    );
                carlfxHyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent evt) {
                        
                        //webEngine.load(rssFeed.link);
                    }
                });
                createColumns(chap1RecipeTableView);
                chap1RecipeTableView.setItems(getChapterRecipes("1"));
                createLaunchAppEvent(chap1RecipeTableView);
                
                createColumns(chap2RecipeTableView);
                chap2RecipeTableView.setItems(getChapterRecipes("2"));
                createLaunchAppEvent(chap2RecipeTableView);
                
                createColumns(chap3RecipeTableView);
                chap3RecipeTableView.setItems(getChapterRecipes("3"));
                createLaunchAppEvent(chap3RecipeTableView);
                
                createColumns(chap4RecipeTableView);
                chap4RecipeTableView.setItems(getChapterRecipes("4"));
                createLaunchAppEvent(chap4RecipeTableView);
	}
        private void createLaunchAppEvent(final TableView<Recipe> tableView) {
            
            EventHandler<MouseEvent> x =
                    new EventHandler<MouseEvent>(){
                    public void handle(MouseEvent event){
                        if (event.getClickCount() == 2) {
                            final Recipe recipe = tableView.getSelectionModel().getSelectedItem();
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    Stage stage = new Stage();
                                    
                                    try {
                                        Class appClass = Class.forName(recipe.getFullClassName());
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
        
        private void loadRecipes() {
        
            
            Properties prop = new Properties();
            try {
                prop.load(getClass().getResourceAsStream("recipes.properties"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            for (Entry<Object, Object> element:prop.entrySet()){
                String key = String.valueOf(element.getKey());
                String value = String.valueOf(element.getValue());
                String[] recipeStrings = value.split("[,]+", 5);
                int sortOrder = Integer.parseInt(recipeStrings[0].trim());
                String chapter = recipeStrings[1].trim();
                Recipe recipe = new Recipe(key, sortOrder, chapter, recipeStrings[2].trim(), recipeStrings[3].trim(), recipeStrings[4].trim());
                System.out.println(recipe);
                if (recipeMap.get(chapter) == null) {
                    recipeMap.put(chapter, new ArrayList<Recipe>());
                }
                recipeMap.get(chapter).add(recipe);
            }
        }
        private void createColumns(TableView<Recipe> tableView) {
            TableColumn<Recipe, String> recipeNameCol = new TableColumn<>("Recipe");
            recipeNameCol.setCellValueFactory(new PropertyValueFactory("name"));
            recipeNameCol.setPrefWidth( 100 );

            TableColumn<Recipe, String> classNameCol = new TableColumn<>("Class Name");
            classNameCol.setCellValueFactory(new PropertyValueFactory("className"));
            classNameCol.setPrefWidth( 100 );

            TableColumn<Recipe, String> descriptionCol = new TableColumn<>("Description");
            descriptionCol.setCellValueFactory(new PropertyValueFactory("description"));
            descriptionCol.setPrefWidth(tableView.getPrefWidth() - 200 );
            tableView.getColumns().setAll(recipeNameCol, classNameCol, descriptionCol);
        }
        
        private ObservableList<Recipe> getChapterRecipes(String chapter) {
            ObservableList<Recipe> chap = FXCollections.observableArrayList();
            Collections.sort(recipeMap.get(chapter), new Comparator<Recipe>(){
                public int compare(Recipe r1, Recipe r2) {
                    return (r1.getSortOrderProperty()<r2.getSortOrderProperty() ? -1 : (r1==r2 ? 0 : 1));
                }
            });
            
            chap.addAll(recipeMap.get(chapter));
            return chap;
        }
}
