package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 外星商人事件：一艘外星商船停到定居点了，商人愿意跟你换东西。
 * 你可以用木材换钢铁、用食物换水泥，或者不换。
 */
public class AlienTraderEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "🛸 外星商人";
    }

    @Override
    public String getDescription() {
        return "一艘外星商船停靠在定居点，商人愿意与你交换资源。";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🤝 用50木材换30钢铁",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.WOOD) >= 50) {
                    ResourceManager.removeResource(ResourceType.WOOD, 50);
                    ResourceManager.addResource(ResourceType.STEEL, 30);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.WOOD) >= 50
                ? "交易完成：-50木材，+30钢铁！"
                : "木材不足，交易失败。"));
        options.add(new EventOption("🍖 用20食物换25水泥",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20) {
                    ResourceManager.removeResource(ResourceType.FOOD, 20);
                    ResourceManager.addResource(ResourceType.CEMENT, 25);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20
                ? "交易完成：-20食物，+25水泥！"
                : "食物不足，交易失败。"));
        options.add(new EventOption("🚪 拒绝交易",
            () -> { },
            "商人失望地离开了。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 10;
    }
}
