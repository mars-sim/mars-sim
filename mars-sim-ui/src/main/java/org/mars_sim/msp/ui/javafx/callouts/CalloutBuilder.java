package org.mars_sim.msp.ui.javafx.callouts;

public class CalloutBuilder {

    private Callout callout = new Callout();
    private CalloutBuilder() {}

    public static CalloutBuilder create() {
        return new CalloutBuilder();
    }

    public CalloutBuilder headPoint(double x, double y) {
        callout.setHeadPoint(x, y);
        return this;
    }

    public CalloutBuilder leaderLineToPoint(double x, double y) {
        callout.setLeaderLineToPoint(x, y);
        return this;
    }

    public CalloutBuilder endLeaderLineRight(double length) {
        callout.setEndLeaderLineLength(length);
        callout.setEndLeaderLineDirection(Callout.RIGHT);
        return this;
    }
    public CalloutBuilder endLeaderLineRight() {
        callout.setEndLeaderLineLength(75);
        callout.setEndLeaderLineDirection(Callout.RIGHT);
        return this;
    }

    public CalloutBuilder endLeaderLineLeft(double length) {
        callout.setEndLeaderLineLength(length);
        callout.setEndLeaderLineDirection(Callout.LEFT);
        return this;
    }

    public CalloutBuilder endLeaderLineLeft() {
        callout.setEndLeaderLineLength(75);
        callout.setEndLeaderLineDirection(Callout.LEFT);
        return this;
    }

    public CalloutBuilder endLeaderLineLength(double length) {
        callout.setEndLeaderLineLength(length);
        return this;
    }

    public CalloutBuilder endLeaderLineDirection(int direction) {
        callout.setEndLeaderLineDirection(direction);
        return this;
    }

    public CalloutBuilder mainTitle(String title) {
        callout.setMainTitleText(title);
        return this;
    }

    public CalloutBuilder subTitle(String title) {
        callout.setSubTitleText(title);
        return this;
    }
    public CalloutBuilder pause(long inMillis) {
        callout.setPauseTime(inMillis);
        return this;
    }


    public Callout build() {
        callout.build();
        return callout;
    }
}
