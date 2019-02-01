package org.mars_sim.javafx.tools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.Duration;

// from https://gist.github.com/james-d/9686094

public class Clock {
    private final ReadOnlyObjectWrapper<LocalDateTime> time ;
    private final List<ChronoUnit> units = Arrays.asList( 
        ChronoUnit.MILLENNIA,
        ChronoUnit.CENTURIES,
        ChronoUnit.YEARS,
        ChronoUnit.MONTHS,
        ChronoUnit.WEEKS,
        ChronoUnit.DAYS,
        ChronoUnit.HOURS,
        ChronoUnit.MINUTES,
        ChronoUnit.SECONDS
    );
    private final Timeline clockwork ;
    
    public Clock(Duration tickInterval, boolean start) {
        time = new ReadOnlyObjectWrapper<>(LocalDateTime.now());
        clockwork = new Timeline(new KeyFrame(tickInterval, event -> time.set(LocalDateTime.now())));
        clockwork.setCycleCount(Animation.INDEFINITE);
        if (start) {
            clockwork.play();
        }
    }
    
    public Clock(Duration tickInterval) {
        this(tickInterval, false);
    }
    
    public StringBinding getElapsedStringBinding(LocalDateTime start) {
        return Bindings.createStringBinding(() -> 
            units.stream()
                .filter(u -> start.plus(1, u).isBefore(time.get())) // ignore units where less than 1 unit has elapsed
                .sorted(Comparator.reverseOrder()) // sort with biggest first
                .findFirst() // get the first (biggest) unit
                .map(u -> String.format("%d %s ago", u.between(start, time.get()), u)) // format as string
                .orElse("Just now") // default if elapsed time is less than smallest unit
        , time);
    }
    
    public StringBinding getElapsedStringBinding() {
        return getElapsedStringBinding(LocalDateTime.now());
    }
    
    public StringBinding getTimeStringBinding(DateTimeFormatter formatter) {
        return Bindings.createStringBinding(() -> formatter.format(time.get()), time);
    }
    
    public StringBinding getTimeStringBinding() {
        return getTimeStringBinding(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
    }
    
    public ReadOnlyObjectProperty<LocalDateTime> timeProperty() {
        return time ;
    }
    
    public LocalDateTime getTime() {
        return time.get();
    }
    
    public void start() {
        clockwork.play();
    }
    
    public void stop() {
        clockwork.stop();
    }

}