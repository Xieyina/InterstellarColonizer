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
 * 外星袭击事件：有不明外星舰队靠近定居点。
 * 你可以选择迎战（某座建筑会被打坏，还有人会牺牲）、花资源去讲和，或者逃跑（有人会走丢）。
 */
public class AlienAttackEvent implements GameEvent {
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
        return "👽 外星袭击";
    }

    @Override
    public String getDescription() {
        return "不明外星舰队正在接近定居点，防空警报响彻全城！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("⚔️ 迎战",
            () -> {
                if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
                model.killPeople(1);
            },
            () -> "战斗惨烈：「" + (target != null ? target.getName() : "一座建筑") + "」受损，1名居民阵亡。"));
        options.add(new EventOption("🕊️ 和谈 (消耗50食物+30钢铁)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 50
                    && ResourceManager.getResourceAmount(ResourceType.STEEL) >= 30) {
                    ResourceManager.removeResource(ResourceType.FOOD, 50);
                    ResourceManager.removeResource(ResourceType.STEEL, 30);
                } else {
                    if (target != null) {
                        target.switchState(States.BROKEN, -1);
                    }
                    model.killPeople(2);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 50
                && ResourceManager.getResourceAmount(ResourceType.STEEL) >= 30
                ? "和谈成功，外星舰队撤离，定居点安然无恙。"
                : "资源不足，谈判破裂！「" + (target != null ? target.getName() : "一座建筑") + "」受损，2名居民伤亡。"));
        options.add(new EventOption("🏃 撤离",
            () -> model.killPeople(2),
            "居民紧急撤离，2名居民在混乱中失踪。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getBuildings().size() >= 5;
    }
}
