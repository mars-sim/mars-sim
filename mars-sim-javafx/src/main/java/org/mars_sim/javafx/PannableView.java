

// Adapted from jewelsea's PannableView.java at https://gist.github.com/jewelsea/5032398

package org.mars_sim.javafx;

import static javafx.application.Application.launch;

import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/** Constructs a scene with a pannable Map background. */
public class PannableView {
  private Image backgroundImage;
  private ScrollPane scroll;

  public PannableView() {
	  backgroundImage = new Image(this.getClass().getResource("/maps/Mars_Viking_MDIM21_ClrMosaic_global_2500m(compressed).jpg").toExternalForm());
  }


  public static void main(String[] args) { launch(args); }
 /*
  public void setWindow(FXInternalWindow window) {
	    // bind the preferred size of the scroll area to the size of the scene.
	    scroll.prefWidthProperty().bind(window.widthProperty());
	    scroll.prefHeightProperty().bind(window.widthProperty());

	    // center the scroll contents.
	    scroll.setHvalue(scroll.getHmin() + (scroll.getHmax() - scroll.getHmin()) / 2);
	    scroll.setVvalue(scroll.getVmin() + (scroll.getVmax() - scroll.getVmin()) / 2);

  }
*/
  public ScrollPane createMap() {

	    // construct the scene contents over a stacked background.
	    StackPane layout = new StackPane();
	    layout.getChildren().setAll(
	      new ImageView(backgroundImage)
	      //, createKillButton()
	    );

	    // wrap the scene contents in a pannable scroll pane.
	    scroll = createScrollPane(layout);
	    //StackPane scrollPane = new StackPane();
	    //scrollPane.getChildren().add(scrollPane);
	    //return layout;//
	    return scroll;//scroll;
	  }


  /** @return a control to place on the scene.
  private Button createKillButton() {
    final Button killButton = new Button("Kill the evil witch");
    killButton.setStyle("-fx-base: firebrick;");
    killButton.setTranslateX(65);
    killButton.setTranslateY(-130);
    killButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent t) {
        killButton.setStyle("-fx-base: forestgreen;");
        killButton.setText("Ding-Dong! The Witch is Dead");
      }
    });
    return killButton;
  }
*/
  /** @return a ScrollPane which scrolls the layout. */
  private ScrollPane createScrollPane(Pane layout) {
    ScrollPane scroll = new ScrollPane();
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setPannable(true);
    scroll.setPrefSize(800, 600);
    scroll.setContent(layout);
    return scroll;
  }

}
