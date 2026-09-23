package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 外交任务事件：外星联盟请定居点派人去参加外交活动。
 * 你可以花资源派人去（会有回报），或者礼貌拒绝。
 */
public class DiplomaticMissionEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "🕊️ 外交任务";
    }

    @Override
    public String getDescription() {
        return "外星联盟邀请定居点派遣使团参加星际外交任务。";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🛸 派遣使团 (消耗20食物+10工具)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20
                    && ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 10) {
                    ResourceManager.removeResource(ResourceType.FOOD, 20);
                    ResourceManager.removeResource(ResourceType.TOOLS, 10);
                    ResourceManager.addResource(ResourceType.CEMENT, 30);
                    ResourceManager.addResource(ResourceType.STEEL, 30);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 20
                && ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 10
                ? "外交成功：+30水泥，+30钢铁！"
                : "资源不足，无法派遣使团。"));
        options.add(new EventOption("🤝 婉拒任务",
            () -> { },
            "外交官们留在定居点。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getPopulation() >= 15;
    }
}
