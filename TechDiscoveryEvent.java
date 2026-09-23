package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Tech.TechNode;

import java.util.ArrayList;
import java.util.List;

/**
 * 科技发现事件：科学家在废墟里发现了外星科技残骸。
 * 你可以免费研究一项能学的科技、把资料换成食物，或者直接扔掉。
 */
public class TechDiscoveryEvent implements GameEvent {
    private GameManager model;
    private TechNode freeTech;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
        List<TechNode> available = model.getTechTree().getAvailableTechs();
        freeTech = available.isEmpty() ? null : available.get(0);
    }

    @Override
    public String getTitle() {
        return "🔭 科技发现";
    }

    @Override
    public String getDescription() {
        return "科学家在废墟中发现了外星科技残骸，这可能带来技术突破！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🔬 深入研究 (免费研究一项科技)",
            () -> {
                if (freeTech != null) {
                    model.getTechTree().forceResearch(freeTech.getId());
                }
            },
            () -> freeTech != null
                ? "免费研究完成：「" + freeTech.getName() + "」！"
                : "当前没有可研究的科技。"));
        options.add(new EventOption("📜 记录归档 (换取20食物)",
            () -> ResourceManager.addResource(ResourceType.FOOD, 20),
            "研究资料换来了20食物补给。"));
        options.add(new EventOption("🗑️ 丢弃资料",
            () -> { },
            "科学家们非常失望。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return !model.getTechTree().getAvailableTechs().isEmpty();
    }
}
