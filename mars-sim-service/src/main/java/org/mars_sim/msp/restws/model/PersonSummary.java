package org.mars_sim.msp.restws.model;

/**
 * Summary properties of a Person entity.
 */
public class PersonSummary extends EntityReference {

	private String gender;
	private int age;
	private String task;
	
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}
	
}
