package org.mars_sim.msp.demo;

import javafx.application.Application;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
 
public class H2Tasks extends Application {
  private static final Logger logger = Logger.getLogger(H2Tasks.class.getName());
  private static final String[] SAMPLE_NAME_DATA = { "John", "Jill", "Jack", "Jerry" };
  
  public static void main(String[] args) { launch(args); }
 
  // executes database operations concurrent to JavaFX operations.
  private ExecutorService databaseExecutor;
 
  // the future's data will be available once the database setup has been complete.
  private Future          databaseSetupFuture;
 
  // initialize the program.
  // setting the database executor thread pool size to 1 ensures 
  // only one database command is executed at any one time.
  @Override public void init() throws Exception {
    databaseExecutor = Executors.newFixedThreadPool(
      1, 
      new DatabaseThreadFactory()
    );  
 
    // run the database setup in parallel to the JavaFX application setup.
    DBSetupTask setup = new DBSetupTask();
    databaseSetupFuture = databaseExecutor.submit(setup);
  }
 
  // shutdown the program.
  @Override public void stop() throws Exception {
    databaseExecutor.shutdown();
    if (!databaseExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
      logger.info("Database execution thread timed out after 3 seconds rather than shutting down cleanly.");
    }
  }
  
  // start showing the UI.
  @Override public void start(Stage stage) throws InterruptedException, ExecutionException {
    // wait for the database setup to complete cleanly before showing any UI.
    // a real app might use a preloader or show a splash screen if this 
    // was to take a long time rather than just pausing the JavaFX application thread.
    databaseSetupFuture.get();
      
    final ListView<String> nameView = new ListView<>();
    final ProgressIndicator databaseActivityIndicator = new ProgressIndicator();
    databaseActivityIndicator.setVisible(false);
 
    final Button fetchNames = new Button("Fetch names from the database");
    fetchNames.setOnAction(event ->
            fetchNamesFromDatabaseToListView(
                    fetchNames,
                    databaseActivityIndicator,
                    nameView
            )
    );
    
    final Button clearNameList = new Button("Clear the name list");
    clearNameList.setOnAction(event -> nameView.getItems().clear());
 
    VBox layout = new VBox(10);
    layout.setStyle("-fx-background-color: cornsilk; -fx-padding: 15;");
    layout.getChildren().setAll(
      new HBox(10,
        fetchNames,
        clearNameList,
        databaseActivityIndicator
      ),
      nameView
    );
    layout.setPrefHeight(200);
    
    stage.setScene(new Scene(layout));
    stage.show();
  }
  
  private void fetchNamesFromDatabaseToListView(
          final Button triggerButton, 
          final ProgressIndicator databaseActivityIndicator, 
          final ListView<String> listView) {
    final FetchNamesTask fetchNamesTask = new FetchNamesTask();

    triggerButton.disableProperty().bind(
            fetchNamesTask.runningProperty()
    );
    databaseActivityIndicator.visibleProperty().bind(
            fetchNamesTask.runningProperty()
    );
    databaseActivityIndicator.progressProperty().bind(
            fetchNamesTask.progressProperty()
    );

    fetchNamesTask.setOnSucceeded(t ->
            listView.setItems(fetchNamesTask.getValue())
    );

    databaseExecutor.submit(fetchNamesTask);
  }
 
  abstract class DBTask<T> extends Task<T> {
    DBTask() {
      setOnFailed(t -> logger.log(Level.SEVERE, null, getException()));
    }
  }
  
  class FetchNamesTask extends DBTask<ObservableList<String>> {
    @Override protected ObservableList<String> call() throws Exception {
      // artificially pause for a while to simulate a long running database connection.
      Thread.sleep(1000); 
 
      try (Connection con = getConnection()) {
        return fetchNames(con);
      }
    }
    
    private ObservableList<String> fetchNames(Connection con) throws SQLException {
      logger.info("Fetching names from database");
      ObservableList<String> names = FXCollections.observableArrayList();
 
      Statement st = con.createStatement();      
      ResultSet rs = st.executeQuery("select name from employee");
      while (rs.next()) {
        names.add(rs.getString("name"));
      }
 
      logger.info("Found " + names.size() + " names");
 
      return names;
    }
  }
 
  class DBSetupTask extends DBTask {
    @Override protected Void call() throws Exception {
      try (Connection con = getConnection()) {
        if (!schemaExists(con)) {
          createSchema(con);
          populateDatabase(con);
        }
      }
        
      return null;
    }
    
    private boolean schemaExists(Connection con) {
      logger.info("Checking for Schema existence");      
      try {
        Statement st = con.createStatement();      
        st.executeQuery("select count(*) from employee");
        logger.info("Schema exists");      
      } catch (SQLException ex) {
        logger.info("Existing DB not found will create a new one");
        return false;
      }
 
      return true;
    }
 
    private void createSchema(Connection con) throws SQLException {
      logger.info("Creating schema");
      Statement st = con.createStatement();
      String table = "create table employee(id integer, name varchar(64))";
      st.executeUpdate(table);
      logger.info("Created schema");
    }
 
    private void populateDatabase(Connection con) throws SQLException {
      logger.info("Populating database");      
      Statement st = con.createStatement();      
      for (String name: SAMPLE_NAME_DATA) {
        st.executeUpdate("insert into employee values(1,'" + name + "')");
      }
      logger.info("Populated database");
    }
  }
 
  private Connection getConnection() throws ClassNotFoundException, SQLException {
    logger.info("Getting a database connection");
    Class.forName("org.h2.Driver");
    return DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
  }  
 
  static class DatabaseThreadFactory implements ThreadFactory {
    static final AtomicInteger poolNumber = new AtomicInteger(1);
    
    @Override public Thread newThread(Runnable runnable) {
      Thread thread = new Thread(runnable, "Database-Connection-" + poolNumber.getAndIncrement() + "-thread");
      thread.setDaemon(true);
 
      return thread;
    }
  }  
}