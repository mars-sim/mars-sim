package org.mars_sim.msp.ui.javafx.callouts;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class Callout extends Group {
    public static int LEFT = -1;
    public static int RIGHT = 1;

    private Point2D headPoint;
    private Point2D leaderLineToPoint;
    private double endLeaderLineLength;
    private int endLeaderLineDirection = RIGHT;
    private String mainTitleText;
    private String subTitleText;
    private long pauseInMillis = 2000;
    private SequentialTransition calloutAnimation;

    public Callout() {}

    public Point2D getHeadPoint() {
        return headPoint;
    }

    public void setHeadPoint(double x, double y) {
        this.headPoint = new Point2D(x, y);
    }

    public Point2D getLeaderLineToPoint() {
        return leaderLineToPoint;
    }

    public void setLeaderLineToPoint(double x, double y) {
        this.leaderLineToPoint = new Point2D(x, y);
    }

    public double getEndLeaderLineLength() {
        return endLeaderLineLength;
    }

    public void setEndLeaderLineLength(double endLeaderLineLength) {
        this.endLeaderLineLength = endLeaderLineLength;
    }

    public int getEndLeaderLineDirection() {
        return endLeaderLineDirection;
    }

    public void setEndLeaderLineDirection(int endLeaderLineDirection) {
        this.endLeaderLineDirection = endLeaderLineDirection;
    }

    public String getMainTitleText() {
        return mainTitleText;
    }

    public void setMainTitleText(String mainTitleText) {
        this.mainTitleText = mainTitleText;
    }

    public String getSubTitleText() {
        return subTitleText;
    }

    public void setSubTitleText(String subTitleText) {
        this.subTitleText = subTitleText;
    }

    public long getPauseTime() {
        return pauseInMillis;
    }

    public void setPauseTime(long pauseInMillis) {
        this.pauseInMillis = pauseInMillis;
    }

    protected Rectangle2D getBoundsUpfront(Region node) {
        // Calculate main title width and height
        Group titleRoot = new Group();
        new Scene(titleRoot);
        titleRoot.getChildren().add(node);
        titleRoot.applyCss();
        titleRoot.layout();
        return new Rectangle2D(0, 0, node.getWidth(), node.getHeight());
    }

    /**
     *
     * @return
     */
    protected Point2D calcEndPointOfLeaderLine() {
        // Position of the end of the leader line
        double x = getLeaderLineToPoint().getX() + getEndLeaderLineLength() * getEndLeaderLineDirection();
        double y = getLeaderLineToPoint().getY();
        return new Point2D(x, y);
    }

    public void build() {
        // Create head
        Circle head = new Circle(getHeadPoint().getX(), getHeadPoint().getY(), 5);
        head.setFill(Color.WHITE);

        // First leader line
        Line firstLeaderLine = new Line(headPoint.getX(), headPoint.getY(),
                headPoint.getX(), headPoint.getY());
        firstLeaderLine.setStroke(Color.WHITE);
        firstLeaderLine.setStrokeWidth(3);

        // Second part of the leader line
        Line secondLeaderLine = new Line(getLeaderLineToPoint().getX(),
                getLeaderLineToPoint().getY(),
                getLeaderLineToPoint().getX(),
                getLeaderLineToPoint().getY());

        secondLeaderLine.setStroke(Color.WHITE);
        secondLeaderLine.setStrokeWidth(3);

        // Main title Rectangle
        HBox mainTitle = new HBox();
        mainTitle.setBackground(
                new Background(
                        new BackgroundFill(Color.WHITE,
                                new CornerRadii(2),
                                new Insets(0)))
        );

        // Main title text
        Text mainTitleText = new Text(getMainTitleText());
        HBox.setMargin(mainTitleText, new Insets(8, 8, 8, 8));
        mainTitleText.setFont(Font.font(20));
        mainTitle.getChildren().add(mainTitleText);

        // Position sub tile rectangle under main title
        Rectangle subTitleRect = new Rectangle(2, 20);
        subTitleRect.setFill(Color.WHITE);

        // Create the sub title
        HBox subTitle = new HBox();
        subTitle.setBackground(
                new Background(
                        new BackgroundFill(Color.color(0, 0, 0, .20),
                                new CornerRadii(0),
                                new Insets(0)))
        );
        Text subTitleText = new Text(getSubTitleText());
        subTitleText.setVisible(true);
        subTitleText.setFill(Color.WHITE);
        subTitleText.setFont(Font.font(14));
        subTitle.getChildren().add(subTitleText);

        // Build the animation code.
        buildAnimation(head,
                firstLeaderLine,
                secondLeaderLine,
                mainTitle,
                subTitleRect,
                subTitle);

        // Must add nodes after buildAnimation.
        // Positioning calculations are done
        // outside of this Group.
        getChildren().addAll(head,
                firstLeaderLine,
                secondLeaderLine,
                mainTitle,
                subTitleRect,
                subTitle);
        getChildren().forEach(node -> node.setVisible(false));

    }

    protected void buildAnimation(Node head,
                                Line beginLeaderLine,
                                Line endLeaderLine,
                                HBox mainTitle,
                                Rectangle subTitleRect,
                                HBox subTitle) {

        // generate a sequence animation
        calloutAnimation = new SequentialTransition();

        // Allow animation to go in reverse
        calloutAnimation.setCycleCount(2);
        calloutAnimation.setAutoReverse(true);

        // Animation of head
        calloutAnimation.getChildren().add(buildHeadAnim(head));

        // Animation of the beginning leader line.
        calloutAnimation.getChildren().add(buildBeginLeaderLineAnim(beginLeaderLine));

        // Animation of the ending leader line.
        calloutAnimation.getChildren().add(buildEndLeaderLineAnim(endLeaderLine));

        // Animation of the main title
        calloutAnimation.getChildren().add(buildMainTitleAnim(mainTitle));

        // Animation of the subtitle rectangle
        calloutAnimation.getChildren().add(buildSubtitleRectAnim(mainTitle, subTitleRect));

        // Animation of the subtitle
        calloutAnimation.getChildren().add(buildSubTitleAnim(mainTitle, subTitle));

        Timeline pause = new Timeline(new KeyFrame(Duration.millis(getPauseTime()/2)));
        calloutAnimation.getChildren().add(pause);

    }

    /**
     * Create the head animation. The head can be any node or shape,
     * by default it's a circle.
     * @param head The head is the starting point of the callout.
     * @return Animation of the head.
     */
    protected Animation buildHeadAnim(Node head) {
        Circle headCircle = (Circle) head;
        return new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(headCircle.visibleProperty(), true),
                        new KeyValue(headCircle.radiusProperty(), 0)), // show
                new KeyFrame(Duration.millis(300),
                        new KeyValue(headCircle.radiusProperty(), 5.0d)) // max value
        );
    }

    protected Animation buildBeginLeaderLineAnim(Line firstLeaderLine) {
        return new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(firstLeaderLine.visibleProperty(), true)), // show
                new KeyFrame(Duration.millis(300),
                        new KeyValue(firstLeaderLine.endXProperty(), getLeaderLineToPoint().getX()),
                        new KeyValue(firstLeaderLine.endYProperty(), getLeaderLineToPoint().getY())
                )
        );
    }

    protected Animation buildEndLeaderLineAnim(Line endLeaderLine) {
        return new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(endLeaderLine.visibleProperty(), true)), // show
                new KeyFrame(Duration.millis(200),
                        new KeyValue(endLeaderLine.endXProperty(),
                                calcEndPointOfLeaderLine().getX()),
                        new KeyValue(endLeaderLine.endYProperty(), getLeaderLineToPoint().getY())
                )
        );
    }

    protected Animation buildMainTitleAnim(HBox mainTitleBackground) {

        // main title box
        // Calculate main title width and height upfront
        Rectangle2D mainTitleBounds = getBoundsUpfront(mainTitleBackground);

        double mainTitleWidth = mainTitleBounds.getWidth();
        double mainTitleHeight = mainTitleBounds.getHeight();

        // Position mainTitleText background beside the end part of the leader line.
        Point2D endPointLLine = calcEndPointOfLeaderLine();
        double x = endPointLLine.getX();
        double y = endPointLLine.getY();

        // Viewport to make main title appear to scroll
        Rectangle mainTitleViewPort = new Rectangle();
        mainTitleViewPort.setWidth(0);
        mainTitleViewPort.setHeight(mainTitleHeight);

        mainTitleBackground.setClip(mainTitleViewPort);
        mainTitleBackground.setLayoutX(x);
        mainTitleBackground.setLayoutY(y - (mainTitleHeight/2));

        // Animate main title from end point to the left.
        if (LEFT == getEndLeaderLineDirection()) {
            // animate layout x and width
            return new Timeline(
                    new KeyFrame(Duration.millis(1),
                            new KeyValue(mainTitleBackground.visibleProperty(), true),
                            new KeyValue(mainTitleBackground.layoutXProperty(), x)
                    ), // show
                    new KeyFrame(Duration.millis(200),
                            new KeyValue(mainTitleBackground.layoutXProperty(), x - mainTitleWidth),
                            new KeyValue(mainTitleViewPort.widthProperty(), mainTitleWidth)
                    )
            );
        }

        // Animate main title from end point to the right
        return new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(mainTitleBackground.visibleProperty(), true)), // show
                new KeyFrame(Duration.millis(200),
                        new KeyValue(mainTitleViewPort.widthProperty(), mainTitleWidth)
                )
        );
    }

    protected Animation buildSubtitleRectAnim(HBox mainTitleBackground, Rectangle subTitleRect) {

        // Small rectangle (prompt)
        // Calculate main title width and height upfront
        Rectangle2D mainTitleBounds = getBoundsUpfront(mainTitleBackground);

        double mainTitleWidth = mainTitleBounds.getWidth();
        double mainTitleHeight = mainTitleBounds.getHeight();

        // Position of the end
        Point2D endPointLL = calcEndPointOfLeaderLine();
        double x = endPointLL.getX();
        double y = endPointLL.getY();

        int direction = getEndLeaderLineDirection();
        if (direction == LEFT) {
            subTitleRect.setLayoutX( x + (subTitleRect.getWidth() * direction));
        } else {
            subTitleRect.setLayoutX( x );
        }


        subTitleRect.setLayoutY( y + (mainTitleHeight/2) + 2);

        return new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(subTitleRect.visibleProperty(), true),
                        new KeyValue(subTitleRect.heightProperty(), 0)), // show
                new KeyFrame(Duration.millis(300),
                        new KeyValue(subTitleRect.heightProperty(), 20)
                )
        );
    }

    protected Animation buildSubTitleAnim(HBox mainTitle, HBox subTitle) {

        // Small rectangle (prompt)
        // Calculate main title width and height upfront
        Rectangle2D mainTitleBounds = getBoundsUpfront(mainTitle);

        double mainTitleWidth = mainTitleBounds.getWidth();
        double mainTitleHeight = mainTitleBounds.getHeight();

        Pos textPos = (LEFT == getEndLeaderLineDirection()) ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT;
        subTitle.setAlignment(textPos);

        Rectangle2D subTitleBounds = getBoundsUpfront(subTitle);

        double subTitleTextWidth = subTitleBounds.getWidth();
        double subTitleTextHeight = subTitleBounds.getHeight();

        Point2D endPointLL = calcEndPointOfLeaderLine();
        int direction = getEndLeaderLineDirection();
        double x = endPointLL.getX() + (5 * direction);
        double y = endPointLL.getY();
        subTitle.setLayoutX( x );
        subTitle.setLayoutY( y + (mainTitleHeight/2) + 4);

        Rectangle subTitleViewPort = new Rectangle();
        subTitleViewPort.setWidth(0);
        subTitleViewPort.setHeight(subTitleTextHeight);
        subTitle.setClip(subTitleViewPort);

        // Animate subtitle from end point to the left.
        if (LEFT == getEndLeaderLineDirection()) {
            return new Timeline(
                    new KeyFrame(Duration.millis(1),
                            new KeyValue(subTitle.visibleProperty(), true),
                            new KeyValue(subTitle.layoutXProperty(), x)), // show
                    new KeyFrame(Duration.millis(200),

                            new KeyValue(subTitle.layoutXProperty(), x - subTitleTextWidth),
                            new KeyValue(subTitleViewPort.widthProperty(), subTitleTextWidth))
            );
        }

        // Animate subtitle from end point to the right.
        return new Timeline(
                new KeyFrame(Duration.millis(1),
                        new KeyValue(subTitle.visibleProperty(), true)), // show
                new KeyFrame(Duration.millis(200),
                        new KeyValue(subTitleViewPort.widthProperty(), subTitleTextWidth))
        );
    }

    public void play() {
        calloutAnimation.stop();
        calloutAnimation.setRate(1.0);
        getChildren().forEach(node -> node.setVisible(false));
        calloutAnimation.play();
    }

    public void play(double rate) {
        calloutAnimation.stop();
        getChildren().forEach(node -> node.setVisible(false));
        calloutAnimation.setRate(rate);
        calloutAnimation.play();
    }

}
