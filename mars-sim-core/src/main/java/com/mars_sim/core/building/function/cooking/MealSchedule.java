/*
 * Mars Simulation Project
 * MealSchedule.java
 * @date 2025-03-23
 * @author Barry Evans
 */
package com.mars_sim.core.building.function.cooking;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mars_sim.core.time.MSolPeriod;

/**
 * This class represents the meal times for a Sol. It is used to determine if a meal is active at a given time.
 * It caters for a different offsets round the planet so the meals follow the sunrise.
 */
public class MealSchedule implements Serializable {

    // Starting meal times (millisol) for 0 degrees longitude.
	private static final int BREAKFAST_START = 250; // at 6am
	private static final int LUNCH_START = 500; // at 12 am
	private static final int DINNER_START = 750; // at 6 pm
	private static final int MIDNIGHT_SNACK_START = 5; 

	// Time (millisols) duration of meals.
	// Note: 80 millisols ~= 2 hours
	private static final int MEALTIME_DURATION = 80; 
	// Note: 40 millisols ~= 1 hour
	private static final int SNACK_TIME_DURATION = 40;

    private static final long serialVersionUID = 1L;

    /**
     * This class represents a meal time period.
     */
    public record MealTime(String name, MSolPeriod period) implements Serializable {}
    
    private List<MealTime> meals = new ArrayList<>();

    public MealSchedule(int offset) {
        meals.add(createMeal("Breakfast", BREAKFAST_START, SNACK_TIME_DURATION, offset));
        meals.add(createMeal("Lunch", LUNCH_START, MEALTIME_DURATION, offset));
        meals.add(createMeal("Dinner", DINNER_START, MEALTIME_DURATION, offset));
        meals.add(createMeal("Midnight", MIDNIGHT_SNACK_START, SNACK_TIME_DURATION, offset));
    }

    private static MealTime createMeal(String name, int start, int duration, int offset) {
        return new MealTime(name, new MSolPeriod((start + offset) % 1000,
                                            (start + offset + duration) % 1000));
    }

    /**
     * Find if there is a meal time active at the given time of day.
     * @param timeOfDay
     * @return
     */
    public boolean isMealTime(int timeOfDay) {
        return meals.stream().anyMatch(meal -> meal.period.isBetween(timeOfDay));
    }

    /**
     * Get the list of meals.
     * @return
     */
    public List<MealTime> getMeals() {
        return meals;
    }

    /**
     * Find if there is a meal time active at the given time of day.
     * @param timeOfDay
     * @return
     */
    public MealTime getActiveMeal(int timeOfDay) {
        return meals.stream().filter(meal -> meal.period.isBetween(timeOfDay)).findAny().orElse(null);
    }
}
