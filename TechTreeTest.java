package tfgirls.project.javarts.tech;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Tech.TechTree;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 科技树（10 个科技）逻辑测试。
 * 说明：INTERSTELLAR_TRAVEL 的效果是临时凑的（解锁木屋），
 * 测试按现在的实际行为来写；真正的胜利是在 GameManager.researchTech 里触发的。
 * BASIC_FARMING（一级农业）开局自动研究，不用玩家手动点。
 */
class TechTreeTest {

    @Test
    void techTreeHasTenNodes() {
        assertEquals(10, new TechTree().getNodes().size());
    }

    @Test
    void basicFarmingIsAutoResearchedAtStart() {
        TechTree tree = new TechTree();
        assertTrue(tree.isResearched("BASIC_FARMING"));
        assertTrue(tree.isBuildingUnlocked(BuildingType.FARM));
        assertFalse(tree.canResearch("BASIC_FARMING")); // 已经研究过，不能再研究
    }

    @Test
    void researchRejectedWhenPrerequisiteMissing() {
        TechTree tree = new TechTree();
        ResourceManager.setResourceAmount(ResourceType.STEEL, 1000);
        ResourceManager.setResourceAmount(ResourceType.FOOD, 1000);
        assertFalse(tree.canResearch("ADVANCED_MINING"));   // 前面的 BASIC_MINING 还没研究
        assertFalse(tree.isResearched("ADVANCED_MINING"));
    }

    @Test
    void researchRejectedWhenResourcesInsufficient() {
        TechTree tree = new TechTree();
        ResourceManager.setResourceAmount(ResourceType.FOOD, 0);
        assertFalse(tree.canResearch("BASIC_MINING"));
    }

    @Test
    void researchConsumesCostAndAppliesEffect() {
        TechTree tree = new TechTree();
        ResourceManager.setResourceAmount(ResourceType.FOOD, 100);
        assertTrue(tree.canResearch("BASIC_MINING"));
        tree.research("BASIC_MINING");
        assertTrue(tree.isResearched("BASIC_MINING"));
        assertEquals(50, ResourceManager.getResourceAmount(ResourceType.FOOD));
        assertTrue(tree.isBuildingUnlocked(BuildingType.QUARRY));
    }

    @Test
    void productionMultiplierAppliesAfterResearchedBoost() {
        TechTree tree = new TechTree();
        assertEquals(1.0, tree.getProductionMultiplier(ResourceType.STEEL), 0.0001);
        tree.forceResearch("BASIC_MINING");
        tree.forceResearch("ADVANCED_MINING");   // 钢铁产出 ×1.5
        assertEquals(1.5, tree.getProductionMultiplier(ResourceType.STEEL), 0.0001);
    }

    @Test
    void interstellarTravelResearchUsesPlaceholderEffect() {
        TechTree tree = new TechTree();
        // 前提：METALLURGY、ADVANCED_ENERGY、AI_OPTIMIZATION（forceResearch 不花资源）
        tree.forceResearch("BASIC_MINING");
        tree.forceResearch("BASIC_ENERGY");
        tree.forceResearch("BASIC_FARMING");
        tree.forceResearch("METALLURGY");
        tree.forceResearch("ADVANCED_MINING");
        tree.forceResearch("ADVANCED_FARMING");
        tree.forceResearch("ADVANCED_ENERGY");
        tree.forceResearch("AI_OPTIMIZATION");
        ResourceManager.setResourceAmount(ResourceType.STEEL, 2000);
        ResourceManager.setResourceAmount(ResourceType.CEMENT, 1000);
        assertTrue(tree.canResearch("INTERSTELLAR_TRAVEL"));
        tree.research("INTERSTELLAR_TRAVEL");
        assertTrue(tree.isResearched("INTERSTELLAR_TRAVEL"));
        assertEquals(1000, ResourceManager.getResourceAmount(ResourceType.STEEL));
        // 临时效果：解锁木屋，按现在的实际行为来写
        assertTrue(tree.isBuildingUnlocked(BuildingType.WOODENCABIN));
    }
}
