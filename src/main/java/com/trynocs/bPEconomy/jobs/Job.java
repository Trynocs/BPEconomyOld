package com.trynocs.bPEconomy.jobs;

import org.bukkit.Material;
import java.util.Map;

public class Job {
    private final String name;
    private final String description;
    private final Map<Material, Double> actions; // Material -> Reward

    public Job(String name, String description, Map<Material, Double> actions) {
        this.name = name;
        this.description = description;
        this.actions = actions;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Map<Material, Double> getActions() {
        return actions;
    }

    public double getReward(Material material) {
        return actions.getOrDefault(material, 0.0);
    }
}
