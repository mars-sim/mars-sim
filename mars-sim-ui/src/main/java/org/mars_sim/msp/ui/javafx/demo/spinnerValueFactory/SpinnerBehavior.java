package org.mars_sim.msp.ui.javafx.demo.spinnerValueFactory;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.Collections;

public class SpinnerBehavior<T> extends BehaviorBase<Spinner<T>> {

    // this specifies how long the mouse has to be pressed on a button
    // before the value steps. As the mouse is held down longer, we begin
    // to cut down the duration of subsequent steps (and also increase the
    // step size)
    private static final double INITIAL_DURATION_MS = 750;

    private long startTime;

    private int stepCount = 0;

    private double stepDurationInMS = INITIAL_DURATION_MS;

    // initially we step by 1, but as the button is held down we begin to
    // ramp this up
    private int stepAmount = 1;

    private boolean isIncrementing = false;

    private Timeline timeline;

    final EventHandler<ActionEvent> spinningKeyFrameEventHandler = event -> {
        final SpinnerValueFactory<T> valueFactory = getControl().getValueFactory();
        if (valueFactory == null) {
            return;
        }

        // we ask the value factory if it wants to change the stepAmount
        // or the stepDurationInMS
        final long durationSinceStart = System.currentTimeMillis() - startTime;
        int newStepAmount = valueFactory.calculateStepAmount(stepCount, durationSinceStart);
        long newStepDurationInMS = valueFactory.calculateStepDuration(stepCount, durationSinceStart);

        boolean changed = false;
        if (newStepAmount != stepAmount || newStepDurationInMS != stepDurationInMS) {
            stepAmount = newStepAmount;
            stepDurationInMS = newStepDurationInMS;
            changed = true;
        }

        if (isIncrementing) {
            increment(stepAmount);
        } else {
            decrement(stepAmount);
        }

        stepCount++;

        if (changed) {
            updateKeyFrames();
        }
    };


    public SpinnerBehavior(Spinner<T> spinner) {
        super(spinner, Collections.emptyList());
    }

    public void increment(int steps) {
        getControl().increment(steps);
    }

    public void decrement(int steps) {
        getControl().decrement(steps);
    }

    public void startSpinning(boolean increment) {
        isIncrementing = increment;

        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        updateKeyFrames();
        timeline.play();
        startTime = System.currentTimeMillis();
        spinningKeyFrameEventHandler.handle(null);
    }

    public void stopSpinning() {
        if (timeline != null) {
            timeline.stop();

            stepDurationInMS = INITIAL_DURATION_MS;
            stepCount = 0;
            stepAmount = 1;
            startTime = 0;
        }
    }

    private void updateKeyFrames() {
        final KeyFrame kf = new KeyFrame(Duration.millis(stepDurationInMS), spinningKeyFrameEventHandler);
        timeline.getKeyFrames().setAll(kf);
        timeline.playFromStart();
    }
}