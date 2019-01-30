
// https://github.com/james-d/Animated-Table-Row/blob/master/src/animatedtablerow/AnimatedTableRow.java

package org.mars_sim.javafx;

import java.util.Arrays;

import javafx.animation.Animation;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class AnimatedTableRow extends Application {

    private static final Duration ANIMATION_DURATION = Duration.seconds(0.4);

	public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        stage.setTitle("Table View Sample");
        stage.setWidth(900);
        stage.setHeight(500);

        final ObservableList<Person> data =
            FXCollections.observableArrayList(
                new Person("Jacob", "Smith", "jacob.smith@example.com"),
                new Person("Isabella", "Johnson", "isabella.johnson@example.com"),
                new Person("Ethan", "Williams", "ethan.williams@example.com"),
                new Person("Emma", "Jones", "emma.jones@example.com"),
                new Person("Michael", "Brown", "michael.brown@example.com")
            );

        final TableView<Person> contactTable = createTable();
        contactTable.setPlaceholder(new Label("No more contacts to select"));
        contactTable.setItems(data);

        final Node contactContainer = createTableContainer("Address Book", contactTable);

        final TableView<Person> toTable = createTable();
        toTable.setPlaceholder(new Label("No contacts selected"));

        final Node toContainer = createTableContainer("Selected Contacts: ", toTable);

        final BorderPane root = new BorderPane();
        final SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(contactContainer, toContainer);
        root.setCenter(splitPane);
        final Scene scene = new Scene(root);

        contactTable.setRowFactory(new Callback<TableView<Person>, TableRow<Person>>() {
            @Override
            public TableRow<Person> call(TableView<Person> tableView) {
                final TableRow<Person> row = new TableRow<>();
                row.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        if (event.getClickCount() == 2 && row.getItem() != null) {
                            moveDataWithAnimation(contactTable, toTable, root, row);
                        }
                    }
                });
                return row ;
            }
        });

        stage.setScene(scene);
        stage.show();
    }

    private Node createTableContainer(final String labelText, final TableView<Person> table) {
        final VBox tableAndLabelContainer = new VBox();
        tableAndLabelContainer.setSpacing(5);
        tableAndLabelContainer.setPadding(new Insets(10, 0, 0, 10));
        final Label label = new Label(labelText);
        label.setFont(new Font("Arial", 20));
        tableAndLabelContainer.getChildren().addAll(label, table);
        final HBox container = new HBox();
        container.getChildren().add(tableAndLabelContainer);
        return container;
    }

    private TableView<Person> createTable() {
        final TableView<Person> table = new TableView<Person>();
        table.setEditable(true);

        TableColumn<Person, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setMinWidth(100);
        firstNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("firstName"));

        TableColumn<Person, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setMinWidth(100);
        lastNameCol.setCellValueFactory(new PropertyValueFactory<Person, String>("lastName"));

        TableColumn<Person, String> emailCol = new TableColumn<>("Email");
        emailCol.setMinWidth(200);
        emailCol.setCellValueFactory(new PropertyValueFactory<Person, String>("email"));

        table.getColumns().addAll(Arrays.asList(firstNameCol, lastNameCol, emailCol));
        return table;
    }

    private void moveDataWithAnimation(final TableView<Person> sourceTable,
			final TableView<Person> destinationTable,
			final Pane commonTableAncestor, final TableRow<Person> row) {
		// Create imageview to display snapshot of row:
		final ImageView imageView = createImageView(row);
		// Start animation at current row:
		final Point2D animationStartPoint = row.localToScene(new Point2D(0, 0)); // relative to Scene
		final Point2D animationEndPoint = computeAnimationEndPoint(destinationTable); // relative to Scene
		// Set start location
		final Point2D startInRoot = commonTableAncestor.sceneToLocal(animationStartPoint); // relative to commonTableAncestor
		imageView.relocate(startInRoot.getX(), startInRoot.getY());
		// Create animation
		final Animation transition = createAndConfigureAnimation(
				sourceTable, destinationTable, commonTableAncestor, row,
				imageView, animationStartPoint, animationEndPoint);
		// add animated image to display
		commonTableAncestor.getChildren().add(imageView);
		// start animation
		transition.play();
	}

	private TranslateTransition createAndConfigureAnimation(
			final TableView<Person> sourceTable,
			final TableView<Person> destinationTable,
			final Pane commonTableAncestor, final TableRow<Person> row,
			final ImageView imageView, final Point2D animationStartPoint,
			Point2D animationEndPoint) {
		final TranslateTransition transition = new TranslateTransition(ANIMATION_DURATION, imageView);
		// At end of animation, actually move data, and remove animated image
		transition.setOnFinished(createAnimationFinishedHandler(sourceTable, destinationTable, commonTableAncestor, row.getItem(), imageView));
		// configure transition
		transition.setByX(animationEndPoint.getX() - animationStartPoint.getX()); // absolute translation, computed from coords relative to Scene
		transition.setByY(animationEndPoint.getY() - animationStartPoint.getY()); // absolute translation, computed from coords relative to Scene
		return transition;
	}

	private EventHandler<ActionEvent> createAnimationFinishedHandler(
			final TableView<Person> sourceTable,
			final TableView<Person> destinationTable,
			final Pane commonTableAncestor, final Person person,
			final ImageView imageView) {
		return new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		        // Remove from first table
				sourceTable.getItems().remove(person);
		        // Add to first row of second table, looks better given the animation:
		        destinationTable.getItems().add(0, person);
		        destinationTable.getSelectionModel().select(person);
		        destinationTable.scrollTo(0);
		        destinationTable.requestFocus();
		        // Remove animated image
		        commonTableAncestor.getChildren().remove(imageView);
		    }
		};
	}

	// Animation end point (coordinates relative to scene)
	private Point2D computeAnimationEndPoint(
			final TableView<Person> destinationTable) {
		// End animation at first row (bottom of table header)
		final Node toTableHeader = destinationTable.lookup(".column-header-background");
		if (toTableHeader != null) {
			final Bounds tableHeaderBounds = toTableHeader.localToScene(toTableHeader.getBoundsInLocal()); // relative to Scene
			Point2D animationEndPoint = new Point2D(tableHeaderBounds.getMinX(), tableHeaderBounds.getMaxY());
			return animationEndPoint;
		} else { // fallback in case lookup fails for some reason
		    // just approximate at 24 pixels below top of table:
		    Point2D tableLocation = destinationTable.localToScene(new Point2D(0,0));
		    return new Point2D(tableLocation.getX(), tableLocation.getY() + 24);
		}
	}

	private ImageView createImageView(final TableRow<Person> row) {
		final Image image = row.snapshot(null, null);
		final ImageView imageView = new ImageView(image);
        // Manage image location ourselves (don't let layout manage it)
        imageView.setManaged(false);
		return imageView;
	}

	public final static class Person {

        private final StringProperty firstName;
        private final StringProperty lastName;
        private final StringProperty email;

        private Person(String fName, String lName, String email) {
            this.firstName = new SimpleStringProperty(fName);
            this.lastName = new SimpleStringProperty(lName);
            this.email = new SimpleStringProperty(email);
        }

        public String getFirstName() {
            return firstName.get();
        }

        public void setFirstName(String fName) {
            firstName.set(fName);
        }

        public StringProperty firstNameProperty() {
            return firstName ;
        }

        public String getLastName() {
            return lastName.get();
        }

        public void setLastName(String fName) {
            lastName.set(fName);
        }

        public StringProperty lastNameProperty() {
            return lastName ;
        }

        public String getEmail() {
            return email.get();
        }

        public void setEmail(String fName) {
            email.set(fName);
        }

        public StringProperty emailProperty() {
            return email ;
        }
    }
}