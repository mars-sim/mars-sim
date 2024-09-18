package com.mars_sim.core.person.ai;

import com.mars_sim.core.data.Rating;
import com.mars_sim.core.time.MarsTime;
import com.mars_sim.core.tool.RandomUtil;

import java.util.ArrayList;
import java.util.List;

public class CacheCreator<T extends Rating> {

    private List<T> cache = new ArrayList<>();
    private double probability = 0;
    private String context;
    private MarsTime createdTime;
    private T lastSelected;

    public CacheCreator(String context, MarsTime createdTime) {
        this.context = context;
        if (createdTime != null) {
            this.createdTime = createdTime;
        }
    }

    public void put(T t) {
        cache.add(t);
        probability += t.getScore().getScore();
    }

    public void add(List<T> t) {
        for(T tt : t) {
            if(tt.getScore().getScore() > 0) {
                cache.add(tt);
                probability += tt.getScore().getScore();
            }
        }
    }

    public MarsTime getCreatedTime() {
        return createdTime;
    }

    public double getTotalProbability() {
        return probability;
    }

    public String getContext() {
        return context;
    }

    public List<T> getCache() {
        return cache;
    }

    public T getLastSelected() {
        return lastSelected;
    }

    public T getRandomSelection() {
        T lastEntry = null;
        double randomDouble = getRandomDoubleBasedOnProbability();

        for (T entry : cache) {
            double probWeight = entry.getScore().getScore();
            if (randomDouble <= probWeight) {
                if (createdTime != null) {
                    lastSelected = entry;
                    cache.remove(entry);
                    probability -= probWeight;
                }
                return entry;
            }
            randomDouble -= probWeight;
            lastEntry = entry;
        }
        return lastEntry;
    }

    private double getRandomDoubleBasedOnProbability() {
        return RandomUtil.getRandomDouble(probability);
    }
}
