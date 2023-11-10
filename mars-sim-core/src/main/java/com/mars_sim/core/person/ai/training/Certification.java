/**
 * Mars Simulation Project
 * Certification.java
 * @date 2023-11-08
 * @author Manny Kung
 */

package com.mars_sim.core.person.ai.training;

import com.mars_sim.core.time.MarsTime;

public class Certification {

	private int level = 0;
	private double score = 0;
	
	private String holder;
	private String trainer;
		
	private CertificationType type; 
	
	private MarsTime date;
	
	public Certification(CertificationType type) {
		this.type = type;
	}
	
	public CertificationType getCertType() {
		return type;
	}
}
