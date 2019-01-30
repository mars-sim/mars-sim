package org.mars_sim.javafx;

import javax.swing.SwingUtilities;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
//import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Displays the Gettysburg Address */
public class Story {
  private static final String TITLE  = "Mars Journal";

  private static final String START1 = "We've made it! We landed at fairly close to the prescribed coordinates. Our mission has just begun. We are the first wave of human to settle Mars. Humanity is finally going multiplanetary.";

  private static final String START2 = "Mars needs no explanation of its richest for scientific discovery and human exploration. From all the plausible evidence, it has conditions suitable for life in its past. ";

  private static final String MID1 =  "Mars is a fascinating place. Its untouched landscape is a repository of geological knowledge will even "
  		+ "uncover for us to understand our own planet's history and future. ";

  private static final String MID2 = "'What would it be like to spend nearly two Earth years at the Martian north pole,' "
	  		+ "we're asked, 'a place where darkness falls for nine months of the year, carbon dioxide snow flutters down in winter "
	  		+ "and temperatures drop to a chilly minus 150 C?' I would think it's still a far more delightful place to live than the sterilized environment of Moon. ";

  private static final String MID3 = "Mars is now over 140 million miles from Earth. Sometimes it is closer. Sometimes farther, depending on its position in its orbit around the sun "
  		+ "In case of emergency evacuation, it would still take us an average of 200+ days, or just over 6 months, to get back home, at least. "
  		+ "In order to cover that distance, we will need a return ascent vehicle with sufficient fuel. I hate to bust our bubble of hope. Fat chance for such a trip within a year with the shoestring budget of our Mars Program";

  private static final String MID4 = "Besides paying for the trip, yet the biggest challenge remains, namely, how do we keep ourselves healthy for the next two years ? "
	  		+ "We would have to grow crops to sustain us. We would have to manufacture parts if things break. We would have to ensure we don't get depressed and do stupid things. ";

  private static final String DATE    = "September 30, 2043.\nSchiaparelli Point, Mars.";
  private static final String ICON    = "/images/paper/scroll.png";
  private static final String PAPER   = "/images/paper/crumpled_paper_273118.JPG";



  /** render the application on a stage */
  public StackPane createGUI() {

	SwingUtilities.invokeLater(() -> {
		//Simulation.instance().getJConsole().write(ADDRESS,Color.ORANGE,Color.BLACK);
	});

    // place the address content in a bordered title pane.
    Pane titledContent = new BorderedTitledPane(TITLE, getContent());
    titledContent.getStyleClass().add("titled-address");
    titledContent.setPrefSize(800, 745);

    // make some crumpled paper as a background.
    final Image paper = new Image(PAPER);
    final ImageView paperView = new ImageView(paper);
    ColorAdjust colorAdjust = new ColorAdjust(0, -.2, .2, 0);
    paperView.setEffect(colorAdjust);

    // place the address content over the top of the paper.
    StackPane stackedContent = new StackPane();
    stackedContent.getChildren().addAll(paperView, titledContent);

    // manage the viewport of the paper background, to size it to the content.
    paperView.setViewport(new Rectangle2D(0, 0, titledContent.getPrefWidth(), titledContent.getPrefHeight()));
    stackedContent.layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
      @Override public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldValue, Bounds newValue) {
        paperView.setViewport(new Rectangle2D(
          newValue.getMinX(), newValue.getMinY(),
          Math.min(newValue.getWidth(), paper.getWidth()), Math.min(newValue.getHeight(), paper.getHeight())
        ));
      }
    });

    // blend the content into the paper and make it look old.
    titledContent.setMaxWidth(paper.getWidth());
    titledContent.setEffect(new SepiaTone());
    titledContent.setBlendMode(BlendMode.MULTIPLY);

    // configure and display the scene and stage.
    //Scene scene = new Scene(stackedContent);
    //scene.getStylesheets().add(getClass().getResource("/css/gettysburg.css").toExternalForm());

/*
    stage.setTitle(TITLE);
    stage.getIcons().add(new Image(ICON));
    stage.setScene(scene);
    stage.setMinWidth(600); stage.setMinHeight(500);
    stage.show();
*/
    // make the scrollbar in the address scroll pane hide when it is not needed.
    makeScrollFadeable(titledContent.lookup(".address > .scroll-pane"));

    return stackedContent;
  }

  /** @return the content of the address, with a signature and portrait attached. */
  private Pane getContent() {
    final VBox content = new VBox();
    content.getStyleClass().add("address");

    //int rand = RandomUtil.getRandomInt(1);

    final Label address = new Label(START1+START2+MID1+MID2+MID3+MID4);

    address.setWrapText(true);
    ScrollPane addressScroll = makeScrollable(address);

    final ImageView signature = new ImageView(); signature.setId("signature");
    final ImageView lincolnImage = new ImageView(); lincolnImage.setId("portrait");
    VBox.setVgrow(addressScroll, Priority.ALWAYS);

    final Region spring = new Region();
    HBox.setHgrow(spring, Priority.ALWAYS);
    
    //final Node alignedSignature = HBoxBuilder.create().children(spring, signature).build();
    final HBox alignedSignature = new HBox(spring, signature);
    
    Label date = new Label(DATE);
    date.setAlignment(Pos.BOTTOM_RIGHT);

    content.getChildren().addAll(
        lincolnImage,
        addressScroll,
        alignedSignature,
        date
    );

    return content;
  }

  /** @return content wrapped in a vertical, pannable scroll pane. */
  private ScrollPane makeScrollable(final Control content) {
    final ScrollPane scroll = new ScrollPane();
    scroll.setContent(content);
    scroll.setPannable(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.viewportBoundsProperty().addListener(new ChangeListener<Bounds>() {
      @Override public void changed(ObservableValue<? extends Bounds> observableValue, Bounds oldBounds, Bounds newBounds) {
        content.setPrefWidth(newBounds.getWidth() - 10);
      }
    });

    return scroll;
  }

  /** adds a hiding effect on the scroll bar of a scrollable node so that it is not seen unless
   *  the scrollable node is being hovered with a mouse */
  private void makeScrollFadeable(final Node scroll) {
    final Node scrollbar = scroll.lookup(".scroll-bar:vertical");
    //System.out.println(scroll);
    final FadeTransition fader = new FadeTransition(Duration.seconds(1), scrollbar);
    fader.setFromValue(1); fader.setToValue(0);
    fader.setOnFinished(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        if (!scroll.getStyleClass().contains("hide-thumb")) {
          scroll.getStyleClass().add("hide-thumb");
        }
      }
    });
    if (!scroll.isHover()) {
      scroll.getStyleClass().add("hide-thumb");
    }
    scroll.hoverProperty().addListener(new ChangeListener<Boolean>() {
      @Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasHover, Boolean isHover) {
        if (!isHover) {
          fader.playFromStart();
        } else {
          fader.stop();
          //scrollbar.setOpacity(1);
          scroll.getStyleClass().remove("hide-thumb");
        }
      }
    });
  }

  /** Places content in a bordered pane with a title. */
  class BorderedTitledPane extends StackPane {
    BorderedTitledPane(String titleString, Node content) {
      Label title = new Label("  " + titleString + "  ");
      title.getStyleClass().add("bordered-titled-title");
      StackPane.setAlignment(title, Pos.TOP_CENTER);

      StackPane contentPane = new StackPane();
      content.getStyleClass().add("bordered-titled-content");
      contentPane.getChildren().add(content);

      getStyleClass().add("bordered-titled-border");
      getChildren().addAll(title, contentPane);
    }
  }

}