package org.mars_sim.msp.demo;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Animates an object when its position is changed. For instance, when
 * additional items are added to a Region, and the layout has changed, then the
 * layout animator makes the transition by sliding each item into its final
 * place.
 */
public class LayoutAnimator implements ChangeListener<Object>, ListChangeListener<Node> {

  private Map<Node, Transition> nodesInTransition;

  public LayoutAnimator() {
    this.nodesInTransition = new HashMap<>();
  }

  /**
   * Animates all the children of a Region.
   * <code>
   *   VBox myVbox = new VBox();
   *   LayoutAnimator animator = new LayoutAnimator();
   *   animator.observe(myVbox.getChildren());
   * </code>
   *
   * @param nodes
   */
  public void observe(ObservableList<Node> nodes) {
    for (Node node : nodes) {
      this.observe(node);
    }
    nodes.addListener(this);
  }

  public void unobserve(ObservableList<Node> nodes) {
    nodes.removeListener(this);
  }

  public void observe(Node n) {
    n.layoutXProperty().addListener(this);
    n.layoutYProperty().addListener(this);
  }

  public void unobserve(Node n) {
    n.layoutXProperty().removeListener(this);
    n.layoutYProperty().removeListener(this);
  }

  @Override
  public void changed(ObservableValue ov, Object oldValue, Object newValue) {
    final Double oldValueDouble = (Double) oldValue;
    final Double newValueDouble = (Double) newValue;
    final Double changeValueDouble = newValueDouble - oldValueDouble;
    DoubleProperty doubleProperty = (DoubleProperty) ov;

    Node node = (Node) doubleProperty.getBean();
    final TranslateTransition t;
    if ((TranslateTransition) nodesInTransition.get(node) == null) {
      t = new TranslateTransition(Duration.millis(150), node);
    } else {
      t = (TranslateTransition) nodesInTransition.get(node);
    }

    if (doubleProperty.getName().equals("layoutX")) {
      Double orig = node.getTranslateX();
      if (Double.compare(t.getFromX(), Double.NaN) == 0) {
        t.setFromX(orig - changeValueDouble);
        t.setToX(orig);
      }
    }
    if (doubleProperty.getName().equals("layoutY")) {
      Double orig = node.getTranslateY();
      if (Double.compare(t.getFromY(), Double.NaN) == 0) {
        t.setFromY(orig - changeValueDouble);
        t.setToY(orig);
      }
    }
    t.play();

  }

  @Override
  public void onChanged(ListChangeListener.Change change) {
    while (change.next()) {
      if (change.wasAdded()) {
        for (Node node : (List<Node>) change.getAddedSubList()) {
          this.observe(node);
        }
      } else if (change.wasRemoved()) {
        for (Node node : (List<Node>) change.getRemoved()) {
          this.unobserve(node);
        }
      }
    }
  }
}