package org.mars_sim.javafx;

import javafx.scene.Node;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javafx.geometry.*;
//import java.util.*;
//import java.util.function.*;

public class GroupLayoutPane extends Pane {

    enum Axis {
        HORIZONTAL, VERTICAL
    }

    public static double PREFERRED_SIZE = -1;

    private Group horizontalGroup;
    private Group verticalGroup;

    private boolean performingLayout = false;
    private Axis currentAxis;
    private Map<Node, Double> nodeWidths = new HashMap<>();
    private Map<Node, Double> nodeHeights = new HashMap<>();
    private Map<Node, Double> nodeX = new HashMap<>();
    private Map<Node, Double> nodeY = new HashMap<>();

    public GroupLayoutPane() {
        super();
    }

    @Override protected double computeMinWidth(double height) {
        Insets insets = getInsets();
        double insetsWidth = snapSpace(insets.getLeft()) + snapSpace(insets.getRight());
        if (horizontalGroup != null) {
            currentAxis = Axis.HORIZONTAL;
            horizontalGroup.recalculateSizes();
            return horizontalGroup.getMinSize() + insetsWidth;
        } else {
            return insetsWidth;
        }
    }

    @Override protected double computeMinHeight(double width) {
        Insets insets = getInsets();
        double insetsHeight = snapSpace(insets.getTop()) + snapSpace(insets.getBottom());
        if (verticalGroup != null) {
            currentAxis = Axis.VERTICAL;
            verticalGroup.recalculateSizes();
            return verticalGroup.getMinSize() + insetsHeight;
        } else {
            return insetsHeight;
        }
    }

    @Override protected double computePrefWidth(double height) {
        Insets insets = getInsets();
        double insetsWidth = snapSpace(insets.getLeft()) + snapSpace(insets.getRight());
        if (horizontalGroup != null) {
            currentAxis = Axis.HORIZONTAL;
            horizontalGroup.recalculateSizes();
            return horizontalGroup.getPrefSize() + insetsWidth;
        } else {
            return insetsWidth;
        }
    }

    @Override protected double computePrefHeight(double width) {
        Insets insets = getInsets();
        double insetsHeight = snapSpace(insets.getTop()) + snapSpace(insets.getBottom());
        if (verticalGroup != null) {
            currentAxis = Axis.VERTICAL;
            verticalGroup.recalculateSizes();
            return verticalGroup.getPrefSize() + insetsHeight;
        } else {
            return insetsHeight;
        }
    }

    @Override protected double computeMaxWidth(double height) {
        Insets insets = getInsets();
        double insetsWidth = snapSpace(insets.getLeft()) + snapSpace(insets.getRight());
        if (horizontalGroup != null) {
            currentAxis = Axis.HORIZONTAL;
            horizontalGroup.recalculateSizes();
            return horizontalGroup.getMaxSize() + insetsWidth;
        } else {
            return insetsWidth;
        }
    }

    @Override protected double computeMaxHeight(double width) {
        Insets insets = getInsets();
        double insetsHeight = snapSpace(insets.getTop()) + snapSpace(insets.getBottom());
        if (verticalGroup != null) {
            currentAxis = Axis.VERTICAL;
            verticalGroup.recalculateSizes();
            return verticalGroup.getMaxSize() + insetsHeight;
        } else {
            return insetsHeight;
        }

    }

    @Override public void requestLayout() {
        if (performingLayout) {
            return;
        }
        super.requestLayout();
    }

    @Override protected void layoutChildren() {
        performingLayout = true;
        Insets insets = getInsets();
        double width = getWidth();
        double height = getHeight();
        double top = snapSpace(insets.getTop());
        double left = snapSpace(insets.getLeft());
        double bottom = snapSpace(insets.getBottom());
        double right = snapSpace(insets.getRight());

        nodeWidths.clear();
        nodeHeights.clear();
        nodeX.clear();
        nodeY.clear();

        currentAxis = Axis.HORIZONTAL;
        horizontalGroup.recalculateSizes();
        double effectiveWidth = width - left - right;
        horizontalGroup.resizeRelocate(left, effectiveWidth);

        currentAxis = Axis.VERTICAL;
        verticalGroup.recalculateSizes();
        double effectiveHeight = height - top - bottom;
        verticalGroup.resizeRelocate(top, effectiveHeight);

        for (Node child : getManagedChildren()) {
            child.resize(nodeWidths.get(child), nodeHeights.get(child));
            child.relocate(nodeX.get(child), nodeY.get(child));
        }

        performingLayout = false;
    }

    public void setHorizontalGroup(Group group) {
        horizontalGroup = group;
    }

    public void setVerticalGroup(Group group) {
        verticalGroup = group;
    }

    public SequentialGroup createSequentialGroup() {
        return new SequentialGroup();
    }

    public ParallelGroup createParallelGroup() {
        return new ParallelGroup();
    }

    private abstract class Spring {
        protected double minSize;
        protected double prefSize;
        protected double maxSize;

        abstract void recalculateSizes();

        double getMinSize() { return minSize; }
        double getPrefSize() { return prefSize; }
        double getMaxSize() { return maxSize; }

        abstract void resizeRelocate(double location, double size);
    }

    public class Gap extends Spring {
        Gap(double minSize, double prefSize, double maxSize) {
            this.minSize = minSize;
            this.prefSize = prefSize;
            this.maxSize = maxSize;
        }

        void recalculateSizes() {}
        void resizeRelocate(double location, double size) {}
    }

    public class NodeWrapper extends Spring {
        private Node node;
        private Optional<Double> setMinSize = Optional.empty();
        private Optional<Double> setPrefSize = Optional.empty();
        private Optional<Double> setMaxSize = Optional.empty();

        NodeWrapper(Node node) {
            this.node = node;
        }

        NodeWrapper(Node node, double minSize, double prefSize, double maxSize) {
            this.node = node;
            this.setMinSize = Optional.of(minSize);
            this.setPrefSize = Optional.of(prefSize);
            this.setMaxSize = Optional.of(maxSize);
        }

        void recalculateSizes() {
            boolean horiz = currentAxis == Axis.HORIZONTAL;
            this.minSize = snapSize(setMinSize.map(s -> {
                        if (s == PREFERRED_SIZE) {
                            return horiz ? node.prefWidth(-1) : node.prefHeight(-1);
                        } else return s;
                    }).orElse(horiz ? node.minWidth(-1) : node.minHeight(-1)));
            this.prefSize = snapSize(setPrefSize.map(s -> {
                        if (s == PREFERRED_SIZE) {
                            return horiz ? node.prefWidth(-1) : node.prefHeight(-1);
                        } else return s;
                    }).orElse(horiz ? node.prefWidth(-1) : node.prefHeight(-1)));
            this.maxSize = snapSize(setMaxSize.map(s -> {
                        if (s == PREFERRED_SIZE) {
                            return horiz ? node.prefWidth(-1) : node.prefHeight(-1);
                        } else return s;
                    }).orElse(horiz ? node.maxWidth(-1) : node.maxHeight(-1)));
        }

        void resizeRelocate(double location, double size) {
            double boundedSize = Math.max(minSize, Math.min(maxSize, size));
            if (currentAxis == Axis.HORIZONTAL) {
                nodeX.put(node, location);
                nodeWidths.put(node, boundedSize);
            } else {
                nodeY.put(node, location);
                nodeHeights.put(node, boundedSize);
            }
        }
    }

    public abstract class Group extends Spring {
        protected List<Spring> springs = new ArrayList<Spring>();

        public Group addGap(double size) {
            springs.add(new Gap(size, size, size));
            return this;
        }

        public Group addGap(double minGapSize, double prefGapSize, double maxGapSize) {
            springs.add(new Gap(minGapSize, prefGapSize, maxGapSize));
            return this;
        }

        public Group addGlue() {
            springs.add(new Gap(0, 0, Short.MAX_VALUE));
            return this;
        }

        public Group addNode(Node n) {
            springs.add(new NodeWrapper(n));
            return this;
        }

        public Group addNode(Node n, double nodeSize) {
            springs.add(new NodeWrapper(n, nodeSize, nodeSize, nodeSize));
            return this;
        }

        public Group addNode(Node n, double minNodeSize, double prefNodeSize, double maxNodeSize) {
            springs.add(new NodeWrapper(n, minNodeSize, prefNodeSize, maxNodeSize));
            return this;
        }
        public Group addGroup(Group g) {
            springs.add(g);
            return this;
        }
    }

    public class SequentialGroup extends Group {
        SequentialGroup() {}

        void recalculateSizes() {
            for (Spring s : springs) {
                s.recalculateSizes();
            }
            this.minSize =
                springs.stream().mapToDouble(spring -> spring.getMinSize()).sum();
            this.prefSize =
                springs.stream().mapToDouble(spring -> spring.getPrefSize()).sum();
            this.maxSize =
                springs.stream().mapToDouble(spring -> spring.getMaxSize()).sum();
        }

        void resizeRelocate(double location, double size) {
            double prefScale = size / this.prefSize;
            boolean useMin = size < this.prefSize;

            // sort the list in order of descending maximum possible global pref change
            List<Spring> sorted = new ArrayList<>(springs);
            Function<Spring, Double> invPossibleChange;
            if (size < this.prefSize) {
                invPossibleChange = s -> - s.getMinSize() / (s.getPrefSize() * prefScale);
            } else {
                invPossibleChange = s -> s.getMaxSize() / (s.getPrefSize() * prefScale);
            }
            Collections.sort(sorted, (a, b) ->
                             Double.compare(invPossibleChange.apply(a), invPossibleChange.apply(b)));

            Map<Spring, Double> sizes = new HashMap<>();
            double remainingSize = size;
            double remainingPref = this.prefSize;
            int glueCount = 0;
            for (Spring spring : sorted) {
                if (spring.getPrefSize() == 0) {
                    glueCount++;
                } else {
                    double springSize = spring.getPrefSize() * (remainingSize / remainingPref);
                    if (springSize < spring.getMinSize()) {
                        springSize = spring.getMinSize();
                    } else if (springSize > spring.getMaxSize()) {
                        springSize = spring.getMaxSize();
                    }
                    sizes.put(spring, springSize);
                    remainingSize -= springSize;
                    remainingPref -= spring.getPrefSize();
                }
            }
            for (Spring spring : sorted) {
                if (spring.getPrefSize() == 0) {
                    sizes.put(spring, Math.max(0, remainingSize / glueCount));
                }
            }

            for (Spring spring : springs) {
                double springSize = sizes.get(spring);
                spring.resizeRelocate(location, springSize);
                location += springSize;
            }
        }
    }

    public class ParallelGroup extends Group {
        ParallelGroup() {}

        void recalculateSizes() {
            for (Spring s : springs) {
                s.recalculateSizes();
            }
            this.minSize =
                snapSize(springs.stream().mapToDouble(s -> s.getMinSize()).max().orElse(0));
            this.maxSize =
                snapSize(springs.stream().mapToDouble(s -> s.getMaxSize()).min().orElse(0));
            double prefSize =
                snapSize(springs.stream().mapToDouble(s -> s.getPrefSize()).max().orElse(0));
            this.prefSize = Math.max(this.minSize, Math.min(this.maxSize, prefSize));
        }

        void resizeRelocate(double location, double size) {
            for (Spring spring : springs) {
                spring.resizeRelocate(location, size);
            }
        }
    }
}
