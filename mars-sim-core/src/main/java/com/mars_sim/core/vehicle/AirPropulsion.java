/*
 * Mars Simulation Project
 * AirPropulsion.java
 * @date 2024-08-06
 * @author Manny KUng
 */
package com.mars_sim.core.vehicle;

import java.io.Serializable;
import java.util.logging.Level;

import com.mars_sim.core.equipment.Battery;
import com.mars_sim.core.logging.SimLogger;

public class AirPropulsion extends Propulsion implements Serializable {

	/** Default serial id. */
	private static final long serialVersionUID = 1L;

	/** Default logger. */
	private static SimLogger logger = SimLogger.getLogger(AirPropulsion.class.getName());

	/** The standard hovering height for a drone. */
	public static final int STANDARD_HOVERING_HEIGHT = (int) (Flyer.ELEVATION_ABOVE_GROUND * 1000);
	/** The standard stepping up height for a drone. */
	public static final int STEP_UP_HEIGHT = STANDARD_HOVERING_HEIGHT / 50;
	/** The standard stepping down height for a drone. */
	public static final int STEP_DOWN_HEIGHT = -STANDARD_HOVERING_HEIGHT / 15;

	private static final String TWO_WHITESPACES = "  ";

	// 1000 RPM ~ 10.47 rad/s
	private int radPerSec = 10;
	// For now, set thrustCoefficient to 0.3
	private double thrustCoefficient = 0.3;
	// Assume a constant voltage
	private double voltage = Battery.DRONE_VOLTAGE;
	// For now, assume the propeller induced velocity is linearly proportional to
	// the voltage of the battery
	private double vPropeller = voltage * 14;

	private double efficiencyMotor = 0.9;
	// Assume the total radius of the four propellers span the width of the drone
	private double width;

	private double radiusPropeller;

	private double radiusPropellerSquare;

	private Vehicle vehicle;

	private Drone drone;

	/**
	 * Constructor.
	 * 
	 * @param vehicle The vehicle implementing this propulsion
	 * 
	 */
	public AirPropulsion(Vehicle vehicle) {
		super(vehicle);

		this.vehicle = vehicle;
		this.drone = (Drone) vehicle;

		width = vehicle.getWidth();

		radiusPropeller = width / 8;
		radiusPropellerSquare = radiusPropeller * radiusPropeller;
	}

	/**
	 * Drives the rover and calculates overall power and forces acting on it.
	 * 
	 * @param weight
	 * @param vMS
	 * @param averageSpeed
	 * @param fGravity
	 * @param airDensity
	 * @return
	 */
	public double[] driveOnGround(double weight, double vMS, double averageSpeed, double fGravity, double airDensity) {
		return new double[] {};
	}

	/**
	 * Flies in the air and calculate overall power and forces acting on the flyer.
	 * 
	 * @param caseText
	 * @param angle
	 * @param ascentHeight
	 * @param weight
	 * @param airDensity
	 * @param vMS
	 * @param secs
	 * @return
	 */
	public double flyInAir(String caseText, double angle, double ascentHeight, double weight, double airDensity, double vMS,
			double secs) {

		double uKPH = vehicle.getSpeed();

		double vKPH = vMS * KPH_CONV;

		double currentHoveringHeight = drone.getHoveringHeight();

		double step = 0;

		double vAirFlow = 0;

		double REDUCTION_FACTOR = 1;

		if (ascentHeight == 0) {
			// If using Volume-based approach,
			// Power to maintain the height of the drone :
			// P = (m * g * h) / (η * ρ * v)
			// η efficiency of the motor and propeller (typically 0.7-0.9)
			// ρ = air density (kg/m³)
			// v = air speed (m/s)

			vAirFlow = vPropeller + vMS;
		}

		else if (ascentHeight > 0) {
			// Drone will hover over at around ELEVATION_ABOVE_GROUND km and no more
			if (currentHoveringHeight >= STANDARD_HOVERING_HEIGHT + STEP_UP_HEIGHT) {
				step = STANDARD_HOVERING_HEIGHT + STEP_UP_HEIGHT - currentHoveringHeight;
			} else {
				step = STEP_UP_HEIGHT;
			}

			// alpha1 is the angle of propeller disk. 0 -> pi/2;
			double alpha1 = Math.PI / 6;
			// alpha2 is the angle of downstream. 0 -> pi/2
			double alpha2 = Math.PI / 7;

			// if going slightly upward, alpha1 > alpha2
			// if going slightly downward, alpha1 < alpha2 ?

			// Question: how does insufficient power impact powerThrustDrone and thus
			// vAirFlow and vMS ?

			vAirFlow = (vMS * Math.sin(alpha2) + vPropeller * Math.cos(alpha1 - alpha2)) * radiusPropeller * radPerSec; // vPropeller
																														// +
																														// vMS;

		} else if (ascentHeight < 0) {
			// Landing airflow velocity V1 = v1 - abs(V0)
			// double landingVel = vMS - Math.abs(uMS);
			// Landing thrust coefficient: CT = −2(V0_bar + v1_bar)．v1_bar
			// Landing induced velocity: v1_bar = −v0_bar / 2 - sqrt((v0_bar/2)^2 - CT/2))
			// Landing thrust T = - 2 * density * pi * r^2 * ( V0 + v1) * v1

			// See detail strategies on
			// https://aviation.stackexchange.com/questions/64055/how-much-energy-is-wasted-in-an-aeroplanes-descent

			if (currentHoveringHeight >= STEP_DOWN_HEIGHT) {
				step = STEP_DOWN_HEIGHT;
			} else {
				step = currentHoveringHeight;
			}

			// Future: need to vary REDUCTION_FACTOR better with equations
			REDUCTION_FACTOR = currentHoveringHeight / STANDARD_HOVERING_HEIGHT;

			vAirFlow = vPropeller + vMS;

		}

		// Assume the height gained is the same as distanceTravelled
		currentHoveringHeight = currentHoveringHeight + step;
		// Gain in potential energy
		double gainPotentialEnergy = weight * step;

		double potentialEnergyDrone = weight * currentHoveringHeight;

		double thrustForceTotal = REDUCTION_FACTOR * thrustCoefficient * 2 * airDensity * Math.PI
				* radiusPropellerSquare * vAirFlow * vPropeller;
		// Double check with the ratio. Need to be at least 2:1
		double thrustToWeightRatio1 = thrustForceTotal / weight;

		// Question: vary the voltage in order to vary the power provided to the drone

		// The gain of potential energy of the drone require extra the power drain on
		// the drone's fuel and battery system
		double powerThrustDrone = thrustForceTotal * voltage / efficiencyMotor + gainPotentialEnergy / secs;

		drone.setHoveringHeight(currentHoveringHeight);

		logger.log(vehicle, Level.INFO, 10_000, caseText
//				 + "d: " + Math.round(distanceTravelled * 1000.0)/1000.0 + KM__
				+ "h: " + DECIMAL3_M.format(currentHoveringHeight) + TWO_WHITESPACES 
				+ "u -> v: " + DECIMAL3_KPH.format(uKPH) + " -> " + DECIMAL3_KPH.format(vKPH) + TWO_WHITESPACES 
				+ "vAirFlow: " + DECIMAL3_M_S.format(vAirFlow) + TWO_WHITESPACES 
				+ "ascentHeight: " + DECIMAL3_M.format(ascentHeight) + TWO_WHITESPACES 
				+ "powerThrustDrone: " + DECIMAL3_J.format(powerThrustDrone) + TWO_WHITESPACES 
				+ "thrust: " + DECIMAL3_J.format(thrustForceTotal) + TWO_WHITESPACES 
				+ "PE: " + DECIMAL3_J.format(potentialEnergyDrone) + TWO_WHITESPACES 
				+ "gainPE: " + DECIMAL3_J.format(gainPotentialEnergy) + TWO_WHITESPACES 
				+ "ratio: " + DECIMAL3_J.format(thrustToWeightRatio1) + TWO_WHITESPACES);

		return powerThrustDrone;
	}

}
