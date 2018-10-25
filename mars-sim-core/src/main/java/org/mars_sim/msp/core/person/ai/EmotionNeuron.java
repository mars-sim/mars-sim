package org.mars_sim.msp.core.person.ai;

public class EmotionNeuron {
	
    private static String[] inputs = {
    		".8",
    		".2"
    		};
    //{ 0, 0 ; 0, 1; 1, 0; 1, 1};

    
	
	public static void main (String [] args) {
		//String[] input = this.args;
		compute(inputs);
	}
	
	
	public static void compute(String [] args) {
		
        Neuron emot = new Neuron(0.5f);
        Neuron food = new Neuron(0.5f);
        Neuron relationship = new Neuron(0.5f);
        food.setWeight(0.0f);
        relationship.setWeight(0.0f);
        emot.connect(food, relationship);
        		
        for (String val : args) {
            Neuron op = new Neuron(0.0f);
            op.setWeight(Boolean.parseBoolean(val));
            food.connect(op);
            relationship.connect(op);
            System.out.println("op.getWeight() : " + op.getWeight ());
        }

        emot.fire();

        System.out.println("Emotion state : " + emot.isFired());

    }
}