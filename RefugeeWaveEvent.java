package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 难民潮事件：一群星际难民来到定居点求收留。
 * 你可以花食物收留他们（人会变多）、给点物资（他们会回赠资源），或者拒绝。
 */
public class RefugeeWaveEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "🏕️ 难民潮";
    }

    @Override
    public String getDescription() {
        return "一群星际难民来到定居点，请求获得庇护。";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🏡 接纳难民 (消耗20食物)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20) {
                    ResourceManager.removeResource(ResourceType.FOOD, 20);
                    model.addPopulation(5);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20
                ? "5名难民加入了定居点！（-20食物）"
                : "食物不足，无法接纳难民。"));
        options.add(new EventOption("🎁 提供物资 (消耗15食物)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 15) {
                    ResourceManager.removeResource(ResourceType.FOOD, 15);
                    ResourceManager.addResource(ResourceType.WOOD, 30);
                    ResourceManager.addResource(ResourceType.STONE, 30);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 15
                ? "难民回赠了30木材和30石头作为感谢！"
                : "食物不足，无法提供物资。"));
        options.add(new EventOption("🚫 拒绝",
            () -> { },
            "难民们继续漂泊。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 5;
    }
}
