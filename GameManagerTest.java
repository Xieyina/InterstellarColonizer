package tfgirls.project.javarts;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

/**
 * GameManager 每天结算的测试（第 2 档：GameClock / GameUiBridge 用的是假的）。
 *
 * 怎么让每个测试互不影响：每次先 killPeople 把人都清掉（建筑里的人也一起清），
 * 建筑还留在游戏里不动；每个测试按固定位置找建筑或新建，保证不管谁先跑都行。
 * 不用 gm.removeBuilding 来清场——主代码的 removeBuilding 有两个 bug
 * （先从集合里移除再清人会死循环；对没有 WORKING 功能的建筑调 getWorkers() 会报错），
 * 已经写在测试报告「测试发现的可能 bug」里了，主代码没改。
 */
class GameManagerTest {

    /** 把游戏里的人都清掉（建筑里的居民和工人也一起清）。 */
    private void resetPopulation() {
        TestGame.model().killPeople(Integer.MAX_VALUE);
    }

    /** 找某种类型的建筑；找不到就在给定位置建一个（这种类型得是一开始就能造的）。 */
    private Building findOrAdd(BuildingType type, Position position) {
        GameManager gm = TestGame.model();
        for (Building b : gm.getBuildings()) {
            if (b.getType() == type) {
                return b;
            }
        }
        gm.addBuilding(type, position);
        for (Building b : gm.getBuildings()) {
            if (b.getType() == type) {
                return b;
            }
        }
        fail("无法创建建筑: " + type);
        return null;
    }

    @Test
    void dailySettlementTickIncrementsDay() {
        GameManager gm = TestGame.model();
        int before = gm.getDay();
        TestGame.tick().run();
        assertEquals(before + 1, gm.getDay());
    }

    @Test
    void dailySettlementChangesResourcesAsExpected() {
        resetPopulation();
        GameManager gm = TestGame.model();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 100);
        ResourceManager.setResourceAmount(ResourceType.STONE, 100);
        Building farm = findOrAdd(BuildingType.FARM, new Position(10, 10));
        farm.switchState(States.RUNNING, -1);
        for (int i = 0; i < 3; i++) {
            gm.createInhabitantInto(farm);
        }
        for (int i = 0; i < 3; i++) {
            gm.assignWorkerTo(farm);
        }
        ResourceManager.setResourceAmount(ResourceType.FOOD, 100);
        TestGame.tick().run();
        // 3 个工人全满了 → 产出 +10；3 个居民 → 吃掉 −3；总共 +7
        assertEquals(107, ResourceManager.getResourceAmount(ResourceType.FOOD));
    }

    @Test
    void hungerActivatesAtZeroFoodAndRecoversAfterResupply() {
        resetPopulation();
        GameManager gm = TestGame.model();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 100);
        ResourceManager.setResourceAmount(ResourceType.STONE, 100);
        Building house = findOrAdd(BuildingType.HOUSE, new Position(80, 80));
        house.switchState(States.RUNNING, -1);
        gm.createInhabitantInto(house);
        gm.createInhabitantInto(house);
        assertEquals(2, gm.getPopulation());

        ResourceManager.setResourceAmount(ResourceType.FOOD, 0);
        TestGame.tick().run();
        // 食物变零 → 饥饿状态：所有效率减半，每天走掉 1 个人，弹警告
        assertTrue(gm.isHungerActive());
        assertEquals(0.5, gm.getProductionEfficiency(), 0.0001);
        assertEquals(1, gm.getPopulation());
        verify(TestGame.ui(), atLeastOnce()).showWarning(anyString(), anyString(), anyString());

        ResourceManager.setResourceAmount(ResourceType.FOOD, 10);
        TestGame.tick().run();
        // 食物有了 → 饥饿解除，效率回到正常，不再走人
        assertFalse(gm.isHungerActive());
        assertEquals(1.0, gm.getProductionEfficiency(), 0.0001);
        assertEquals(1, gm.getPopulation());
    }

    @Test
    void researchingInterstellarTravelTriggersVictory() {
        resetPopulation();
        GameManager gm = TestGame.model();
        gm.getTechTree().forceResearch("BASIC_MINING");
        gm.getTechTree().forceResearch("BASIC_ENERGY");
        gm.getTechTree().forceResearch("BASIC_FARMING");
        gm.getTechTree().forceResearch("METALLURGY");
        gm.getTechTree().forceResearch("ADVANCED_MINING");
        gm.getTechTree().forceResearch("ADVANCED_FARMING");
        gm.getTechTree().forceResearch("ADVANCED_ENERGY");
        gm.getTechTree().forceResearch("AI_OPTIMIZATION");
        ResourceManager.setResourceAmount(ResourceType.STEEL, 2000);
        ResourceManager.setResourceAmount(ResourceType.CEMENT, 1000);
        gm.researchTech("INTERSTELLAR_TRAVEL");
        verify(TestGame.ui(), atLeastOnce()).showVictory();
    }
}
