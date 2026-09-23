package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 人口增长事件：定居点发展得不错，是增加人口的好机会。
 * 你可以选择花食物鼓励多生孩子（人会变多），或者顺其自然。
 */
public class PopulationGrowthEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "👶 人口增长";
    }

    @Override
    public String getDescription() {
        return "定居点发展兴旺，生活条件改善，正是鼓励人口增长的好时机！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🍼 鼓励生育 (消耗20食物)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20) {
                    ResourceManager.removeResource(ResourceType.FOOD, 20);
                    model.addPopulation(5);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20
                ? "5名新生儿降生！（-20食物）"
                : "食物不足，生育计划搁置。"));
        options.add(new EventOption("📊 保持现状",
            () -> { },
            "人口保持稳定增长。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getPopulation() >= 5;
    }
}
