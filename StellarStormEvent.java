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
 * 星际风暴事件：一场很猛的星际风暴要来了。
 * 你可以花食物储备物资、花水泥加固建筑，或者冒险继续生产（能拿到资源但某座建筑会坏）。
 */
public class StellarStormEvent implements GameEvent {
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
        return "🌪️ 星际风暴";
    }

    @Override
    public String getDescription() {
        return "一场猛烈的星际风暴正在逼近定居点！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🥫 储备物资 (消耗10食物)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 10) {
                    ResourceManager.removeResource(ResourceType.FOOD, 10);
                } else if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 10
                ? "风暴掠过，定居点安然无恙。"
                : "食物不足，「" + (target != null ? target.getName() : "一座建筑") + "」被风暴损坏。"));
        options.add(new EventOption("🧱 加固建筑 (消耗30水泥)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.CEMENT) >= 30) {
                    ResourceManager.removeResource(ResourceType.CEMENT, 30);
                } else if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.CEMENT) >= 30
                ? "建筑加固完成，风暴无法造成伤害。"
                : "水泥不足，「" + (target != null ? target.getName() : "一座建筑") + "」被风暴损坏。"));
        options.add(new EventOption("🎲 冒险生产",
            () -> {
                ResourceManager.addResource(ResourceType.WOOD, 40);
                ResourceManager.addResource(ResourceType.STONE, 30);
                ResourceManager.addResource(ResourceType.IRON, 20);
                if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
            },
            () -> "风暴期间冒险生产：+40木材，+30石头，+20铁矿，但「"
                + (target != null ? target.getName() : "一座建筑") + "」受损。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 20 && !model.getBuildings().isEmpty();
    }
}
