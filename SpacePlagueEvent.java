package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 太空瘟疫事件：一种不知道什么来头的瘟疫在定居点传开了。
 * 你可以花钢铁研究疫苗、花食物强制隔离，或者听天由命，不同选择死的人不一样多。
 */
public class SpacePlagueEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "🦠 太空瘟疫";
    }

    @Override
    public String getDescription() {
        return "一场未知的太空瘟疫正在定居点蔓延，居民们人心惶惶！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🔬 研发疫苗 (消耗50钢铁)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.STEEL) >= 50) {
                    ResourceManager.removeResource(ResourceType.STEEL, 50);
                } else {
                    model.killPeople(3);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.STEEL) >= 50
                ? "疫苗研制成功，瘟疫被控制！"
                : "钢铁不足，疫苗研制失败，3名居民死亡。"));
        options.add(new EventOption("😷 强制隔离 (消耗10食物)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.FOOD) >= 10) {
                    ResourceManager.removeResource(ResourceType.FOOD, 10);
                    model.killPeople(2);
                } else {
                    model.killPeople(5);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.FOOD) >= 10
                ? "隔离有效，但仍有2名居民在隔离中病逝。"
                : "食物不足无法维持隔离，5名居民死亡。"));
        options.add(new EventOption("🙏 听天由命",
            () -> model.killPeople(4),
            "瘟疫肆虐，4名居民死亡。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getPopulation() >= 10;
    }
}
