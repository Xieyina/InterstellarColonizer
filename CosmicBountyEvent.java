package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 宇宙赏金事件：宇宙赏金猎人工会给定居点发来了一个很丰厚的邀请。
 * 你可以直接领赏金、花钢铁投资生产，或者不要。
 */
public class CosmicBountyEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "💰 宇宙赏金";
    }

    @Override
    public String getDescription() {
        return "宇宙赏金猎人工会向你的定居点发出了一笔丰厚的邀请！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("💵 领取赏金",
            () -> {
                ResourceManager.addResource(ResourceType.FOOD, 60);
                ResourceManager.addResource(ResourceType.TOOLS, 40);
            },
            "赏金到账：+60食物，+40工具！"));
        options.add(new EventOption("📈 投资生产 (消耗50钢铁)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.STEEL) >= 50) {
                    ResourceManager.removeResource(ResourceType.STEEL, 50);
                    ResourceManager.addResource(ResourceType.CEMENT, 80);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.STEEL) >= 50
                ? "投资回报：+80水泥！"
                : "钢铁不足，无法投资。"));
        options.add(new EventOption("🙅 拒绝",
            () -> { },
            "赏金猎人们离开了。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 25;
    }
}
