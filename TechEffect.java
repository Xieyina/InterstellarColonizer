package tfgirls.project.javarts.Model.Tech;

import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Resource.ResourceType;

public class TechEffect {
    private final EffectType type;
    private final Object target;
    private final double multiplier;
    private final int amount;

    public enum EffectType {
        UNLOCK_BUILDING,
        BOOST_PRODUCTION,
        REDUCE_CONSUMPTION,
        UNLOCK_RECIPE,
        INCREASE_CAPACITY,
        BOOST_ALL
    }

    // 开放新建筑
    public static TechEffect unlockBuilding(BuildingType building) {
        return new TechEffect(EffectType.UNLOCK_BUILDING, building, 1.0, 0);
    }

    // 提高产量
    public static TechEffect boostProduction(ResourceType resource, double multiplier) {
        return new TechEffect(EffectType.BOOST_PRODUCTION, resource, multiplier, 0);
    }

    // 少用点资源
    public static TechEffect reduceConsumption(ResourceType resource, double multiplier) {
        return new TechEffect(EffectType.REDUCE_CONSUMPTION, resource, multiplier, 0);
    }

    // 开放新配方
    public static TechEffect unlockRecipe(String recipeId) {
        return new TechEffect(EffectType.UNLOCK_RECIPE, recipeId, 1.0, 0);
    }

    // 多装点东西
    public static TechEffect increaseCapacity(int amount) {
        return new TechEffect(EffectType.INCREASE_CAPACITY, null, 1.0, amount);
    }

    // 所有东西都多产一点
    public static TechEffect boostAll(double multiplier) {
        return new TechEffect(EffectType.BOOST_ALL, null, multiplier, 0);
    }

    private TechEffect(EffectType type, Object target, double multiplier, int amount) {
        this.type = type;
        this.target = target;
        this.multiplier = multiplier;
        this.amount = amount;
    }

    public EffectType getType() { return type; }
    public Object getTarget() { return target; }
    public double getMultiplier() { return multiplier; }
    public int getAmount() { return amount; }
}