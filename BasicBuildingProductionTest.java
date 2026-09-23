package tfgirls.project.javarts.building;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingBuilder;
import tfgirls.project.javarts.Model.Building.BuildingManager;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.TestGame;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 产出怎么算、Boost 翻倍（产出 ×2、消耗不变）的测试。
 * 生产结算用单独的 BuildingManager 来跑（它里面会去读 GameManager 的
 * 科技树和生产效率，所以得先启动 TestGame）。
 */
class BasicBuildingProductionTest {

    private final BuildingBuilder builder = new BuildingBuilder();

    @BeforeAll
    static void bootstrapModel() {
        TestGame.model();
    }

    @Test
    void farmHasExpectedProductionAndCapacity() {
        Building farm = builder.build(BuildingType.FARM, new Position(10, 10));
        assertEquals(Map.of(ResourceType.FOOD, 10), farm.getDailyProduction());
        assertEquals(3, farm.getMaxWorkers());
        assertEquals(5, farm.getMaxInhabitants());
    }

    @Test
    void workerFillRatioScalesProductionInDailySettlement() {
        Building farm = builder.build(BuildingType.FARM, new Position(20, 20));
        farm.switchState(States.RUNNING, -1);
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 100);
        ResourceManager.setResourceAmount(ResourceType.STONE, 100);
        bm.addBuilding(farm);   // 扣建造花费：木头×5 + 石头×5

        ResourceManager.setResourceAmount(ResourceType.FOOD, 100);
        bm.handle();            // 0 个工人 → 产出 0（没有居民消耗）
        assertEquals(100, ResourceManager.getResourceAmount(ResourceType.FOOD));

        for (int i = 0; i < 3; i++) {
            farm.addWorker(new People());
        }
        ResourceManager.setResourceAmount(ResourceType.FOOD, 100);
        bm.handle();            // 3 个工人全满 → +10
        assertEquals(110, ResourceManager.getResourceAmount(ResourceType.FOOD));

        farm.removeWorker(farm.getWorkers().get(0));
        farm.removeWorker(farm.getWorkers().get(0));
        ResourceManager.setResourceAmount(ResourceType.FOOD, 100);
        bm.handle();            // 只有 1/3 的工人 → 10 × 1/3 = 3
        assertEquals(103, ResourceManager.getResourceAmount(ResourceType.FOOD));
    }

    @Test
    void boostDoublesProductionButNotConsumption() {
        // 水泥厂：消耗石头 4 + 煤炭 4，产出水泥 4（加速时产出 ×2、消耗不变）
        Building mill = builder.build(BuildingType.CEMENTPLANT, new Position(30, 30));
        mill.switchState(States.RUNNING, -1);
        for (int i = 0; i < 10; i++) {
            mill.addWorker(new People());
        }
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 200);
        ResourceManager.setResourceAmount(ResourceType.STONE, 200);
        bm.addBuilding(mill);   // 扣建造花费：木头×50 + 石头×50

        ResourceManager.setResourceAmount(ResourceType.STONE, 100);
        ResourceManager.setResourceAmount(ResourceType.COAL, 100);
        ResourceManager.setResourceAmount(ResourceType.CEMENT, 0);
        bm.handle();            // 正常运行：消耗石头 4、煤炭 4，产出水泥 4
        assertEquals(96, ResourceManager.getResourceAmount(ResourceType.STONE));
        assertEquals(96, ResourceManager.getResourceAmount(ResourceType.COAL));
        assertEquals(4, ResourceManager.getResourceAmount(ResourceType.CEMENT));

        mill.switchState(States.BOOSTED, 5);
        ResourceManager.setResourceAmount(ResourceType.STONE, 100);
        ResourceManager.setResourceAmount(ResourceType.COAL, 100);
        ResourceManager.setResourceAmount(ResourceType.CEMENT, 0);
        bm.handle();            // 加速状态：产出翻倍 +8，消耗还是 −4
        assertEquals(96, ResourceManager.getResourceAmount(ResourceType.STONE));
        assertEquals(96, ResourceManager.getResourceAmount(ResourceType.COAL));
        assertEquals(8, ResourceManager.getResourceAmount(ResourceType.CEMENT));
    }
}
