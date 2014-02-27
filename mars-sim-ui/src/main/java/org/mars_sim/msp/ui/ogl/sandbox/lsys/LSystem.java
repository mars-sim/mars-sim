package org.mars_sim.msp.ui.ogl.sandbox.lsys;

import java.util.ArrayList;
import java.util.List;

/**
 * a simple implementation of l-systems.
 * @see http://de.wikipedia.org/de/L-System
 * @author stpa
 */
public class LSystem {

	protected String start;
	protected String content;
	protected List<Rule> rules;
	protected int step;
	protected int steps;
	protected int replacements;
	protected int maxReplacements;
	protected int maxLength;

	/**
	 * constructor.
	 * @param start
	 * @param rules
	 * @param steps
	 * @param maxReplacements
	 * @param maxLength
	 */
	public LSystem(
		String start,
		List<Rule> rules,
		int steps,
		int maxReplacements,
		int maxLength
	) {
		this.start = start;
		if (rules != null) {
			this.rules = rules;
		} else {
			this.rules = new ArrayList<Rule>();
		}
		this.steps = steps;
		this.maxReplacements = maxReplacements;
		this.maxLength = maxLength;
		reset();
	}

	public LSystem(String start,List<Rule> rules) {
		this.start = start;
		if (rules != null) {
			this.rules = rules;
		} else {
			this.rules = new ArrayList<Rule>();
		}
		reset();
	}

	protected void reset() {
		this.step = 0;
		this.content = start;
		this.replacements = 0;
	}

	public void addRule(Rule rule) {
		if (rule != null) {
			this.rules.add(rule);
			reset();
		}
	}

	public void addRule(String source, String target, double probability) {
		Rule rule = new Rule(source,target,probability);
		addRule(rule);
	}

	public String getStart() {
		return start;
	}

	public String getContent() {
		return content;
	}

	public List<Rule> getRules() {
		return rules;
	}

	public int getStep() {
		return step;
	}

	public int getSteps() {
		return steps;
	}

	public int getMaxReplacements() {
		return maxReplacements;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setSteps(int steps) {
		if (steps < this.steps) {
			this.steps = steps;
			reset();
		} else {
			this.steps = steps;
		}
	}

	public void setMaxReplacements(int maxReplacements) {
		this.maxReplacements = maxReplacements;
		reset();
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
		reset();
	}

	public boolean step() {
		boolean success = true;
		if (this.step < this.steps) {
			if (step > 0) {
				String rest = content;
				StringBuffer result = new StringBuffer();			
				while (rest.length() > 0 && success) {
					int i = 0;
					boolean hit = false;
					while (i < rules.size() && !hit && success) {
						Rule rule = rules.get(i);
						if (rest.startsWith(rule.getSource())) {
							if (Math.random() < rule.getProbability()) {
								if (this.replacements < this.maxReplacements) {
									hit = true;
									if (result.length() < this.maxLength) {
										result.append(rule.getTarget());
										rest = rest.substring(rule.getSource().length());
									} else {
										success = false;
									}
									this.replacements++;
								} else {
									success = false;
								}
							}
						}					
						i++;
					}
					if (!hit) {
						if (result.length() < this.maxLength) {
							result.append(rest.charAt(0));
							rest = rest.substring(1);
						} else {
							success = false;
						}
					}
				}
				this.content = result.toString();
			}
			step++;
		} else {
			success = false;
		}
		return success;
	}

	public void doIt() {
		while (step()) {
//			System.out.println(this.enhavo);
		}
	}

	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append(Integer.toString(steps));
		s.append(",");
		s.append(Integer.toString(maxReplacements));
		s.append(",");
		s.append(Integer.toString(maxLength));
		s.append("\n");
		s.append(this.getStart());
		for (Rule regulo : this.getRules()) {
			s.append("\n");
			s.append("(");
			s.append(Double.toString(regulo.getProbability()));
			s.append(")");
			s.append(regulo.getSource());
			s.append(" ==> ");
			s.append(regulo.getTarget());
		}
		return s.toString();
	}
}
