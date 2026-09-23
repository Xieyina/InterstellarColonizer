package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 富饶小行星事件：一颗有很多矿物的小行星从定居点旁边飞过。
 * 你可以花工具去挖矿、花钢铁派探测器去看看，或者不管它。
 */
public class RichAsteroidEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "☄️ 富饶小行星";
    }

    @Override
    public String getDescription() {
        return "一颗富含矿物的小行星正在经过定居点附近，机会难得！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("⛏️ 开采 (消耗10工具)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 10) {
                    ResourceManager.removeResource(ResourceType.TOOLS, 10);
                    ResourceManager.addResource(ResourceType.IRON, 50);
                    ResourceManager.addResource(ResourceType.COAL, 30);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 10
                ? "开采成功：+50铁矿，+30煤矿！"
                : "工具不足，无法开采。"));
        options.add(new EventOption("🛰️ 派遣探测器 (消耗20钢铁)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.STEEL) >= 20) {
                    ResourceManager.removeResource(ResourceType.STEEL, 20);
                    ResourceManager.addResource(ResourceType.STEEL, 40);
                    ResourceManager.addResource(ResourceType.TOOLS, 10);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.STEEL) >= 20
                ? "探测器带回大量稀有矿物：+40钢铁，+10工具！"
                : "钢铁不足，无法派遣探测器。"));
        options.add(new EventOption("⏭️ 忽略",
            () -> { },
            "小行星与定居点擦肩而过。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        return model.getDay() >= 5;
    }
}
