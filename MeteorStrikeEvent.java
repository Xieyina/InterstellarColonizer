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
 * 陨石撞击事件：一颗陨石不知道砸向定居点的哪座建筑。
 * 你可以选择花合金开护盾、疏散居民，或者硬扛。
 */
public class MeteorStrikeEvent implements GameEvent {
    private final Random random = new Random();
    private GameManager model;
    private Building target;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
        List<Building> buildings = model.getBuildings().stream().toList();
        if (!buildings.isEmpty()) {
            target = buildings.get(random.nextInt(buildings.size()));
            model.setPendingEventContext(target);
        }
    }

    @Override
    public String getTitle() {
        return "☄️ 陨石撞击";
    }

    @Override
    public String getDescription() {
        return target != null
            ? "一颗陨石正朝着「" + target.getName() + "」坠落！如果不采取行动，这座建筑将被彻底摧毁。"
            : "一颗陨石正在接近定居点！如果不采取行动，它将摧毁一座建筑。";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🛡️ 防御护盾 (消耗100合金)",
            () -> {
                if (target != null && ResourceManager.getResourceAmount(ResourceType.STEEL) >= 100) {
                    ResourceManager.removeResource(ResourceType.STEEL, 100);
                } else if (target != null) {
                    model.removeBuilding(target);
                }
            },
            () -> {
                if (target == null) return "陨石坠落在荒野中，定居点安然无恙。";
                return ResourceManager.getResourceAmount(ResourceType.STEEL) >= 100
                    ? "护盾成功抵御了陨石！（-100合金）"
                    : "合金不足，护盾失效！陨石摧毁了「" + target.getName() + "」。";
            }));
        options.add(new EventOption("🚀 紧急疏散",
            () -> {
                if (target != null) {
                    target.switchState(States.BROKEN, -1);
                }
            },
            () -> target != null
                ? "居民紧急撤离，「" + target.getName() + "」受损但可以修复。"
                : "居民紧急撤离，躲过一劫。"));
        options.add(new EventOption("💪 承受冲击",
            () -> {
                if (target != null) {
                    model.removeBuilding(target);
                }
            },
            () -> target != null
                ? "陨石直接命中，「" + target.getName() + "」被彻底摧毁。"
                : "陨石坠落在荒野中，定居点安然无恙。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getBuildings().size() >= 3;
    }
}
