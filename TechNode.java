package tfgirls.project.javarts.Model.Tech;

import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.*;

public class TechNode {
    private final String id;
    private final String name;
    private final String description;
    private final List<String> prerequisites;
    private final Map<ResourceType, Integer> cost;
    private final TechEffect effect;
    private boolean researched = false;

    public TechNode(String id, String name, String description,
                    List<String> prerequisites, Map<ResourceType, Integer> cost,
                    TechEffect effect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.cost = cost != null ? cost : new HashMap<>();
        this.effect = effect;
    }

    // 取值方法
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<String> getPrerequisites() { return prerequisites; }
    public Map<ResourceType, Integer> getCost() { return cost; }
    public TechEffect getEffect() { return effect; }
    public boolean isResearched() { return researched; }

    public void setResearched(boolean researched) { this.researched = researched; }
}