package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 太阳耀斑事件：太阳发出强烈的辐射冲击定居点。
 * 你可以选择断电（所有正在生产的建筑都会暂停）、花水泥加固防护，或者硬扛（某座建筑会被打坏）。
 */
public class SolarFlareEvent implements GameEvent {
    private final Random random = new Random();
    private GameManager model;
    private Building target;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
        List<Building> buildings = model.getBuildings().stream().toList();
        if (!buildings.isEmpty()) {
            target = buildings.get(random.nextInt(buildings.size()));
        }
    }

    @Override
    public String getTitle() {
        return "🌞 太阳耀斑";
    }

    @Override
    public String getDescription() {
        return "太阳耀斑爆发，强烈的辐射正在冲击定居点的电力系统！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("⚡ 切断电网",
            () -> {
                for (Building b : model.getBuildings()) {
                    if (b.getState() == States.RUNNING || b.getState() == States.BOOSTED) {
                        b.switchState(States.BLOCKED, -1);
                    }
                }
            },
            "电网已关闭，所有运行中的生产建筑被暂时阻断。"));
        options.add(new EventOption("🛡️ 加固防护 (消耗30水泥)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.CEMENT) >= 30) {
                    ResourceManager.removeResource(ResourceType.CEMENT, 30);
                } else if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.CEMENT) >= 30
                ? "防护生效，定居点安然无恙。"
                : "水泥不足，耀斑损坏了「" + (target != null ? target.getName() : "一座建筑") + "」。"));
        options.add(new EventOption("💥 承受耀斑",
            () -> {
                if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
            },
            () -> "耀斑击中定居点，「" + (target != null ? target.getName() : "一座建筑") + "」损坏。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 15 && !model.getBuildings().isEmpty();
    }
}
