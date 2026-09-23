package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 贸易禁运事件：外星贸易联盟不准跟定居点做生意了。
 * 你可以花食物走私、花木材去谈判说情，或者硬扛。
 */
public class TradeEmbargoEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "🚫 贸易禁运";
    }

    @Override
    public String getDescription() {
        return "外星贸易联盟对定居点实施了贸易禁运，物资供应中断！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🤐 走私贸易 (消耗30食物)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 30) {
                    ResourceManager.removeResource(ResourceType.FOOD, 30);
                    ResourceManager.addResource(ResourceType.STEEL, 25);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 30
                ? "走私成功：+25钢铁！"
                : "食物不足，走私计划失败。"));
        options.add(new EventOption("💬 外交斡旋 (消耗30木材)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.WOOD) >= 30) {
                    ResourceManager.removeResource(ResourceType.WOOD, 30);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.WOOD) >= 30
                ? "禁运解除，贸易恢复正常。"
                : "木材不足，谈判破裂，禁运持续。"));
        options.add(new EventOption("😤 硬扛",
            () -> { },
            "禁运期间日子艰难，但定居点撑住了。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 10;
    }
}
