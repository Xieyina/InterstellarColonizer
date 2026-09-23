package tfgirls.project.javarts.Model.Resource;

import static java.lang.Math.abs;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfgirls.project.javarts.Exception.NotEnoughResources;
import tfgirls.project.javarts.Model.GameManager;

public class ResourceManager {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceManager.class);
    private static Map<ResourceType, AbstractResource> resources;
    private static ResourceManager instance = null;

    private ResourceManager() {
        resetToInitial();
    }

    /**
     * 把各种资源重置回开局的数量。
     * 游戏失败后从开屏重新开始新一局时用。
     */
    public static void resetToInitial() {
        resources = new HashMap<>();
        resources.put(ResourceType.WOOD, new Wood(100));
        resources.put(ResourceType.IRON, new Iron(50));
        resources.put(ResourceType.STONE, new Stone(100));
        resources.put(ResourceType.COAL, new Coal(50));
        resources.put(ResourceType.STEEL, new Steel(50));
        resources.put(ResourceType.CEMENT, new Cement(50));
        resources.put(ResourceType.FOOD, new Food(100));
        resources.put(ResourceType.TOOLS, new Tools(10));
    }

    public static void addResource(ResourceType type, int quantity) {
        if (resources.containsKey(type)) {
            resources.get(type).addResources(quantity);
            if (resources.get(type).getQuantity() < 0) {
                if (type.equals(ResourceType.FOOD)) {
                    // ===== 改过：饿肚子的惩罚（不再一下把人全饿死 / 直接结束）=====
                    // 食物不够时只把食物清零，由 GameManager.checkFoodAndApplyHunger()
                    // 统一加上「饿肚子效果」（干活慢一半 + 人慢慢变少）并弹窗提醒。
                    int deficit = abs(resources.get(type).getQuantity());
                    resources.get(type).addResources(deficit); // 把食物清零，不让它变负数
                    LOG.warn("食物不足（缺口 {}），已清零；饥饿 Debuff 交由 GameManager 处理", deficit);
                } else {
                    // 别的资源不能变成负数，直接归零
                    resources.get(type).removeResources(resources.get(type).getQuantity());
                    throw new NotEnoughResources("资源不足（" + type + "），已归零");
                }
            }
        }
    }

    public static void removeResource(ResourceType type, int quantity) {
        if (resources.containsKey(type)) {
            if (resources.get(type).getQuantity() >= quantity) {
                resources.get(type).removeResources(quantity);
            } else {
                throw new NotEnoughResources("资源不足（" + type + "），无法完成操作");
            }
        }
    }

    // ===== 新加：查一下某种资源有多少 =====
    public static int getResourceAmount(ResourceType type) {
        if (resources.containsKey(type)) {
            return resources.get(type).getQuantity();
        }
        return 0;
    }

    // ===== 新加：直接设置资源数量（读档时用），最少不能低于 0 =====
    public static void setResourceAmount(ResourceType type, int quantity) {
        if (!resources.containsKey(type)) return;
        quantity = Math.max(0, quantity);
        int current = getResourceAmount(type);
        if (quantity > current) {
            resources.get(type).addResources(quantity - current);
        } else if (quantity < current) {
            resources.get(type).removeResources(current - quantity);
        }
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    // ===== 改过：支持 Map 参数 =====
    public static boolean areAvailable(Map<ResourceType, Integer> neededResources) {
        if (neededResources == null) return true;
        for (ResourceType type : neededResources.keySet()) {
            if (getResourceAmount(type) < neededResources.get(type)) {
                return false;
            }
        }
        return true;
    }

    // ===== 保留原来的 HashMap 版本 =====
    public static boolean areAvailable(HashMap<ResourceType, Integer> neededResources) {
        return areAvailable((Map<ResourceType, Integer>) neededResources);
    }

    public static Map<ResourceType, AbstractResource> getResources() {
        return resources;
    }
}