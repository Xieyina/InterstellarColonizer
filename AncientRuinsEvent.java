package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 远古遗迹事件：探险队在边境发现了一处古代遗迹。
 * 你可以花工具去考古探索、直接抢资源，或者走人。
 */
public class AncientRuinsEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "🏛️ 远古遗迹";
    }

    @Override
    public String getDescription() {
        return "探险队在边境发现了一座远古文明留下的遗迹！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🔍 考古探索 (消耗15工具)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 15) {
                    ResourceManager.removeResource(ResourceType.TOOLS, 15);
                    ResourceManager.addResource(ResourceType.STEEL, 60);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 15
                ? "发现了外星合金：+60钢铁！"
                : "工具不足，无法深入探索。"));
        options.add(new EventOption("⛏️ 掠夺资源",
            () -> {
                ResourceManager.addResource(ResourceType.WOOD, 50);
                ResourceManager.addResource(ResourceType.STONE, 50);
            },
            "掠夺成功：+50木材，+50石头。"));
        options.add(new EventOption("🚶 离开",
            () -> { },
            "探险队空手而归。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getBuildings().size() >= 4;
    }
}
