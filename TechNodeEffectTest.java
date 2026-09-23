package tfgirls.project.javarts.Model.Tech;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 科技节点（TechNode）和科技效果（TechEffect）的测试：
 * 各种效果工厂造出来的类型/目标/倍率对不对，节点能不能记住自己被研究了。
 */
class TechNodeEffectTest {

    @Test
    void unlockBuildingEffect() {
        TechEffect e = TechEffect.unlockBuilding(BuildingType.QUARRY);
        assertEquals(TechEffect.EffectType.UNLOCK_BUILDING, e.getType());
        assertEquals(BuildingType.QUARRY, e.getTarget());
    }

    @Test
    void boostProductionEffect() {
        TechEffect e = TechEffect.boostProduction(ResourceType.STEEL, 1.5);
        assertEquals(TechEffect.EffectType.BOOST_PRODUCTION, e.getType());
        assertEquals(ResourceType.STEEL, e.getTarget());
        assertEquals(1.5, e.getMultiplier(), 1e-9);
    }

    @Test
    void reduceConsumptionEffect() {
        TechEffect e = TechEffect.reduceConsumption(ResourceType.STONE, 0.5);
        assertEquals(TechEffect.EffectType.REDUCE_CONSUMPTION, e.getType());
        assertEquals(0.5, e.getMultiplier(), 1e-9);
    }

    @Test
    void unlockRecipeEffect() {
        TechEffect e = TechEffect.unlockRecipe("ALLOY_SMELTING");
        assertEquals(TechEffect.EffectType.UNLOCK_RECIPE, e.getType());
        assertEquals("ALLOY_SMELTING", e.getTarget());
    }

    @Test
    void increaseCapacityEffect() {
        TechEffect e = TechEffect.increaseCapacity(10);
        assertEquals(TechEffect.EffectType.INCREASE_CAPACITY, e.getType());
        assertEquals(10, e.getAmount());
    }

    @Test
    void boostAllEffect() {
        TechEffect e = TechEffect.boostAll(1.2);
        assertEquals(TechEffect.EffectType.BOOST_ALL, e.getType());
        assertEquals(1.2, e.getMultiplier(), 1e-9);
    }

    @Test
    void techNodeStoresFieldsAndResearchFlag() {
        TechEffect effect = TechEffect.unlockBuilding(BuildingType.QUARRY);
        TechNode node = new TechNode("BASIC_MINING", "初级采矿",
                "解锁矿场", List.of(), Map.of(ResourceType.FOOD, 50), effect);
        assertEquals("BASIC_MINING", node.getId());
        assertEquals("初级采矿", node.getName());
        assertEquals(List.of(), node.getPrerequisites());
        assertEquals(50, node.getCost().get(ResourceType.FOOD));
        assertEquals(effect, node.getEffect());
        assertFalse(node.isResearched());
        node.setResearched(true);
        assertTrue(node.isResearched());
    }
}
