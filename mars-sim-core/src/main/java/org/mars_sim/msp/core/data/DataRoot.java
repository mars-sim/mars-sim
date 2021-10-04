package org.mars_sim.msp.core.data;

public class DataRoot {
	private String content;
//	private Collection<Settlement> s;
	private Person person;
		
	private class Person {
		String name;
		
		public Person(String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	public DataRoot() {
		super();
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(final String content) {
		this.content = content;
	}

//	public void setContent(final Collection<Settlement> s) {
//		this.s = s;
//	}
	
	public void prepContent() 	{
		Person p = new Person("Joe");
		this.person = p;
	}
	
//	public void prepContent(final UnitManager unitManager) 	{
//		this.unitManager = unitManager;
//	}
	
	@Override
	public String toString() {
		return "Root: " + this.person + " was born in " + this.content;
	}
}


