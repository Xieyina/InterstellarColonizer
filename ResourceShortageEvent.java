package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.List;

/**
 * 资源危机事件：定居点东西不够用了。
 * 你可以花木材紧急去买、花工具走黑市，或者跟居民一起扛过去。
 */
public class ResourceShortageEvent implements GameEvent {
    private GameManager model;

    @Override
    public void trigger(GameManager model) {
        this.model = model;
    }

    @Override
    public String getTitle() {
        return "📦 资源危机";
    }

    @Override
    public String getDescription() {
        return "定居点出现资源危机，仓库告急，居民们忧心忡忡！";
    }

    @Override
    public List<EventOption> getOptions() {
        List<EventOption> options = new ArrayList<>();
        options.add(new EventOption("🛒 紧急采购 (消耗30木材)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.WOOD) >= 30) {
                    ResourceManager.removeResource(ResourceType.WOOD, 30);
                    ResourceManager.addResource(ResourceType.FOOD, 15);
                    ResourceManager.addResource(ResourceType.STONE, 15);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.WOOD) >= 30
                ? "采购成功：+15食物，+15石头！"
                : "木材不足，采购失败。"));
        options.add(new EventOption("⚫ 黑市交易 (消耗5工具)",
            () -> {
                if (ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 5) {
                    ResourceManager.removeResource(ResourceType.TOOLS, 5);
                    ResourceManager.addResource(ResourceType.FOOD, 25);
                }
            },
            () -> ResourceManager.getResourceAmount(ResourceType.TOOLS) >= 5
                ? "黑市交易成功：+25食物！"
                : "工具不足，黑市商人拒绝交易。"));
        options.add(new EventOption("💪 自力更生",
            () -> { },
            "居民们团结一致，共渡难关。"));
        return options;
    }

    @Override
    public boolean canTrigger(GameManager model) {
        for (ResourceType rt : ResourceType.values()) {
            if (ResourceManager.getResourceAmount(rt) == 0) {
                return true;
            }
        }
        return false;
    }
}
