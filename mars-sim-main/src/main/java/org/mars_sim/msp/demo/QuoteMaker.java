package org.mars_sim.msp.demo;

import javafx.beans.property.*;
import javafx.application.Application;
import javafx.beans.value.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.util.Random;

// allows you to add random quotes to a message board.
public class QuoteMaker extends Application {
  // setup some data for quotes to add to a message board.
  final Random random = new Random();
  final Quotes quotes = new Quotes();
  final Colors colors = new Colors();

  final ObjectProperty<Label> selectedQuote = new SimpleObjectProperty<>();

  public static void main(String[] args) throws Exception { launch(args); }
  public void start(final Stage stage) throws Exception {
    stage.setTitle("Quotes of Benjamin Franklin");

    // create a message board on which to place quotes.
    final MessageBoard messageBoard = new MessageBoard();
    messageBoard.setStyle("-fx-background-color: cornsilk;");
    messageBoard.setPrefSize(800, 600);

    // create a control panel for the message board.
    VBox controls = new VBox(10);
    controls.setStyle("-fx-background-color: coral; -fx-padding: 10;");
    controls.setAlignment(Pos.TOP_CENTER);

    // create some sliders to modify properties of the existing quote.
    final LabeledSlider widthSlider = new LabeledSlider("Width", 75, 450, 75);
    final LabeledSlider heightSlider = new LabeledSlider("Height", 30, 300, 30);
    final LabeledSlider layoutXSlider = new LabeledSlider("X Pos", 0, messageBoard.getWidth(), 0);
    layoutXSlider.slider.maxProperty().bind(messageBoard.widthProperty());
    final LabeledSlider layoutYSlider = new LabeledSlider("Y Pos", 0, messageBoard.getHeight(), 0);
    layoutYSlider.slider.maxProperty().bind(messageBoard.heightProperty());

    // create a button to generate a new quote.
    Button quoteButton = new Button("New\nQuote");
    quoteButton.setStyle("-fx-font-size: 18px; -fx-text-alignment: center; -fx-padding: 10; -fx-graphic-text-gap: 10;");
    quoteButton.setGraphic(new ImageView(new Image("http://img.freebase.com/api/trans/image_thumb/en/benjamin_franklin?pad=1&errorid=%2Ffreebase%2Fno_image_png&maxheight=64&mode=fillcropmid&maxwidth=64")));
    quoteButton.setMaxWidth(Double.MAX_VALUE);
    quoteButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override public void handle(ActionEvent actionEvent) {
        // post a new quote to the message board and select it.
        final Label newQuote = messageBoard.post(quotes.next(), colors.next());
        selectedQuote.set(newQuote);

        // make the new quote the selected quote when it is been clicked.
        newQuote.setOnMouseClicked(new EventHandler<MouseEvent>() {
          @Override public void handle(MouseEvent mouseEvent) {
            selectedQuote.set(newQuote);
            newQuote.toFront();
          }
        });
      }
    });
    final Label quotedText = new Label();
    quotedText.setWrapText(true);
    quotedText.setStyle("-fx-font-size: 16px;");
    controls.getChildren().addAll(quoteButton, new Separator(), widthSlider, heightSlider, layoutXSlider, layoutYSlider, new Separator(), quotedText);
    controls.setPrefWidth(180);
    controls.setMinWidth(180);
    controls.setMaxWidth(Control.USE_PREF_SIZE);

    // wire up the slider controls to the selected quote.
    selectedQuote.addListener(new ChangeListener<Label>() {
      @Override public void changed(ObservableValue<? extends Label> observableValue, Label oldQuote, final Label newQuote) {
        if (oldQuote != null) {
          // disassociate the sliders from the old quote.
          widthSlider.slider.valueProperty().unbindBidirectional(oldQuote.prefWidthProperty());
          heightSlider.slider.valueProperty().unbindBidirectional(oldQuote.prefHeightProperty());
          layoutXSlider.slider.valueProperty().unbindBidirectional(oldQuote.layoutXProperty());
          layoutYSlider.slider.valueProperty().unbindBidirectional(oldQuote.layoutYProperty());
        }

        if (newQuote != null) {
          // associate the sliders with the new quote.
          widthSlider.slider.valueProperty().bindBidirectional(newQuote.prefWidthProperty());
          heightSlider.slider.valueProperty().bindBidirectional(newQuote.prefHeightProperty());
          layoutXSlider.slider.valueProperty().bindBidirectional(newQuote.layoutXProperty());
          layoutYSlider.slider.valueProperty().bindBidirectional(newQuote.layoutYProperty());

          // bind the quote summary with the new quotes' text
          quotedText.textProperty().bind(newQuote.textProperty());
        }
      }
    });

    // layout the scene.
    HBox layout = new HBox();
    layout.getChildren().addAll(controls, messageBoard);
    HBox.setHgrow(messageBoard, Priority.ALWAYS);
    final Scene scene = new Scene(layout);

    // allow the selected quote to be deleted.
    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override public void handle(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.DELETE)) {
          if (selectedQuote.get() != null) {
            messageBoard.getChildren().remove(selectedQuote.get());
          }
        }
      }
    });

    // show the stage.
    stage.setScene(scene);
    stage.show();
  }

  class LabeledSlider extends HBox {
    Label label;
    Slider slider;
    LabeledSlider(String name, double min, double max, double value) {
      slider = new Slider(min, max, value);
      label = new Label(name);
      label.setPrefWidth(60);
      label.setLabelFor(slider);
      this.getChildren().addAll(label, slider);
    }
  }

  // a board on which you can place messages.
  class MessageBoard extends Pane {
    MessageBoard() {
      setId("messageBoard");
    }

    Label post(String quote, Color color) {
      // choose a quote and style it.
      final Label label = new Label(quote);
      String bgColor = "#" + color.deriveColor(color.getHue(), color.getSaturation(), color.getBrightness(), random.nextDouble() * 0.5 + 0.5).toString().substring(2, 10);
      label.setStyle("-fx-background-radius: 5; -fx-background-color: linear-gradient(to bottom, " + bgColor + ", derive(" + bgColor + ", 20%)); -fx-text-fill: ladder(" + bgColor +", lavender 49%, midnightblue 50%); -fx-font: 18px 'Segoe Script'; -fx-padding:10;");
      label.setWrapText(true);
      label.setAlignment(Pos.CENTER);
      label.setTextAlignment(TextAlignment.CENTER);
      final DropShadow dropShadow = new DropShadow();
      final Glow glow = new Glow();
      label.setEffect(dropShadow);

      // give the quote a random fixed size and position.
      label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
      label.setPrefSize(random.nextInt(150) + 300, random.nextInt(150) + 75);
      label.setMaxSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
      label.relocate(
        random.nextInt((int) Math.floor(this.getPrefWidth() - label.getPrefWidth())),
        random.nextInt((int) Math.floor(this.getPrefHeight() - label.getPrefHeight()))
      );

      // allow the label to be dragged around.
      final Delta dragDelta = new Delta();
      label.setOnMousePressed(new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent mouseEvent) {
          // record a delta distance for the drag and drop operation.
          dragDelta.x = label.getLayoutX() - mouseEvent.getSceneX();
          dragDelta.y = label.getLayoutY() - mouseEvent.getSceneY();
          label.setCursor(Cursor.MOVE);
        }
      });
      label.setOnMouseReleased(new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent mouseEvent) {
          label.setCursor(Cursor.HAND);
        }
      });
      label.setOnMouseDragged(new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent mouseEvent) {
          label.setLayoutX(mouseEvent.getSceneX() + dragDelta.x);
          label.setLayoutY(mouseEvent.getSceneY() + dragDelta.y);
        }
      });
      label.setOnMouseEntered(new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent mouseEvent) {
          label.setCursor(Cursor.HAND);
          dropShadow.setInput(glow);
        }
      });
      label.setOnMouseExited(new EventHandler<MouseEvent>() {
        @Override public void handle(MouseEvent mouseEvent) {
          dropShadow.setInput(null);
        }
      });

      this.getChildren().add(label);

      return label;
    }
  }

  // records relative x and y co-ordinates.
  class Delta { double x, y; }

  // some of Benjamin Franklin's quotes.
  class Quotes {
    private final String[] quotes = {
      "A house is not a home unless it contains food and fire for the mind as well as the body.",
      "A life of leisure and a life of laziness are two things. There will be sleeping enough in the grave.",
      "A penny saved is a penny earned.",
      "A place for everything, everything in its place.",
      "Admiration is the daughter of ignorance.",
      "All mankind is divided into three classes: those that are immovable, those that are movable, and those that move.",
      "All wars are follies, very expensive and very mischievous ones.",
      "An investment in knowledge pays the best interest.",
      "Anger is never without a reason, but seldom with a good one.",
      "Any fool can criticize, condemn and complain and most fools do.",
      "A great empire, like a great cake, is most easily diminished at the edges.",
      "As we must account for every idle word, so must we account for every idle silence.",
      "At twenty years of age the will reigns; at thirty, the wit; and at forty, the judgment."
    };

    private String next() {
      return quotes[random.nextInt(quotes.length)];
    }
  }

  // a selection of colors for the text boxes.
  class Colors {
    final String[][] smallPalette = {
      {"aliceblue", "#f0f8ff"},{"antiquewhite", "#faebd7"},{"aqua", "#00ffff"},{"aquamarine", "#7fffd4"},
      {"azure", "#f0ffff"},{"beige", "#f5f5dc"},{"bisque", "#ffe4c4"},{"black", "#000000"},
      {"blanchedalmond", "#ffebcd"},{"blue", "#0000ff"},{"blueviolet", "#8a2be2"},{"brown", "#a52a2a"},
      {"burlywood", "#deb887"},{"cadetblue", "#5f9ea0"},{"chartreuse", "#7fff00"},{"chocolate", "#d2691e"},
      {"coral", "#ff7f50"},{"cornflowerblue", "#6495ed"},{"cornsilk", "#fff8dc"},{"crimson", "#dc143c"},
      {"cyan", "#00ffff"},{"darkblue", "#00008b"},{"darkcyan", "#008b8b"},{"darkgoldenrod", "#b8860b"},
    };

    private Color next() {
      return Color.valueOf(smallPalette[random.nextInt(smallPalette.length)][0]);
    }
  }
}
