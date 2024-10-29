/*
 * Mars Simulation Project
 * Balance.java
 * @date 2024-10-25
 * @author Manny Kung
 */

package com.mars_sim.core.moon;

import java.io.Serializable;

import com.mars_sim.core.logging.SimLogger;
import com.mars_sim.core.tool.RandomUtil;

public class Balance implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final SimLogger logger = SimLogger.getLogger(Balance.class.getName());

	private double runningBalance;
	
	private double monthlyOperatingExpense;

	private double monthlyIncome;

	private double lastMonthFeeCollected;

	private double lastMonthTaxCollected;

	private double lastMonthStaffExpense;

	private double lastMonthInfrastructureExpense;
	
	Balance(double pop) {
		runningBalance = RandomUtil.getRandomDouble(10, 1000) * pop;
	}
	
	public double getRunningBalance() {
		return runningBalance;
	}
	
	public void adjustRunningBalance(double amount) {
		runningBalance += amount;
	}
		
}
