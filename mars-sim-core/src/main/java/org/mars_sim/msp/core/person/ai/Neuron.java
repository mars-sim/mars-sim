package org.mars_sim.msp.core.person.ai;

import java.util.ArrayList;
import java.util.List;

public class Neuron {
    private List<Neuron> inputs;
    private float weight;
    private float threshhold;
    private boolean fired;

    public Neuron (float t) {
        threshhold = t;
        fired = false;
        inputs = new ArrayList<Neuron>();
    }

    public void connect (Neuron ... ns) {
        for (Neuron n : ns) inputs.add(n);
    }

    public void setWeight (float newWeight) {
        weight = newWeight;
    }

    public void setWeight (boolean newWeight) {
        weight = newWeight ? 1.0f : 0.0f;
    }

    public float getWeight () {
        return weight;
    }

    public float fire () {
        if (inputs.size() > 0) {
            float totalWeight = 0.0f;
            for (Neuron n : inputs) {
                n.fire();
                totalWeight += (n.isFired()) ? n.getWeight() : 0.0f;
            }
            fired = totalWeight > threshhold;
            return totalWeight;
        }
        else if (weight != 0.0f) {
            fired = weight > threshhold;
            return weight;
        }
        else {
            return 0.0f;
        }
    }

    public boolean isFired () {
        return fired;
    }
}