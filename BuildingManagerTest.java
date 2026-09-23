package tfgirls.project.javarts.building;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Exception.NotEnoughResources;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingBuilder;
import tfgirls.project.javarts.Model.Building.BuildingManager;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.TestGame;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 建筑管理器（BuildingManager）的测试：建造成本、能不能造、加建筑扣资源、
 * 修/加速/暂停/运行/拆，以及猎人工会"交猎物换食物"的结算。
 * 注意它内部会去读 GameManager 的科技树和效率，所以先用 TestGame 启动一下。
 */
class BuildingManagerTest {

    private final BuildingBuilder builder = new BuildingBuilder();

    @BeforeAll
    static void bootstrap() {
        TestGame.model();
    }

    @Test
    void buildingCostMatchesDesign() {
        assertEquals(Map.of(ResourceType.WOOD, 1), BuildingManager.buildingCost(BuildingType.WOODENCABIN));
        assertEquals(Map.of(ResourceType.WOOD, 2, ResourceType.STONE, 2), BuildingManager.buildingCost(BuildingType.HOUSE));
        assertEquals(Map.of(ResourceType.WOOD, 50, ResourceType.STONE, 50), BuildingManager.buildingCost(BuildingType.APPARTMENTBUILDING));
        assertEquals(Map.of(ResourceType.WOOD, 5, ResourceType.STONE, 5), BuildingManager.buildingCost(BuildingType.FARM));
        assertEquals(Map.of(ResourceType.WOOD, 10, ResourceType.STONE, 10), BuildingManager.buildingCost(BuildingType.HUNTERGUILD));
    }

    @Test
    void starterBuildingIsBuildableWhenResourcesAvailable() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 100);
        ResourceManager.setResourceAmount(ResourceType.STONE, 100);
        assertTrue(BuildingManager.isBuildable(BuildingType.WOODENCABIN));
    }

    @Test
    void nonStarterBuildingNotBuildableWithoutTech() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 1000);
        ResourceManager.setResourceAmount(ResourceType.STONE, 1000);
        assertFalse(BuildingManager.isBuildable(BuildingType.APPARTMENTBUILDING));
    }

    @Test
    void addBuildingDeductsCost() {
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 1000);
        ResourceManager.setResourceAmount(ResourceType.STONE, 1000);
        Building farm = builder.build(BuildingType.FARM, new Position(100, 100));
        bm.addBuilding(farm);
        assertTrue(bm.exists(farm));
        assertEquals(995, ResourceManager.getResourceAmount(ResourceType.WOOD));
        assertEquals(995, ResourceManager.getResourceAmount(ResourceType.STONE));
    }

    @Test
    void addBuildingThrowsWhenResourcesMissing() {
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 0);
        ResourceManager.setResourceAmount(ResourceType.STONE, 0);
        Building farm = builder.build(BuildingType.FARM, new Position(101, 101));
        assertThrows(NotEnoughResources.class, () -> bm.addBuilding(farm));
    }

    @Test
    void boostConsumesToolAndSetsBoosted() {
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.TOOLS, 100);
        Building farm = builder.build(BuildingType.FARM, new Position(102, 102));
        farm.switchState(States.RUNNING, -1);
        bm.boostBuilding(farm);
        assertEquals(States.BOOSTED, farm.getState());
        assertEquals(99, ResourceManager.getResourceAmount(ResourceType.TOOLS));
    }

    @Test
    void blockSetsBlockedState() {
        BuildingManager bm = new BuildingManager();
        Building farm = builder.build(BuildingType.FARM, new Position(103, 103));
        farm.switchState(States.RUNNING, -1);
        bm.blockBuilding(farm);
        assertEquals(States.BLOCKED, farm.getState());
    }

    @Test
    void runSetsRunningState() {
        BuildingManager bm = new BuildingManager();
        Building farm = builder.build(BuildingType.FARM, new Position(104, 104));
        // 建筑刚建好是"建造中"，让运行建筑管理器直接把它跑起来 -> 正常运转
        bm.runBuilding(farm);
        assertEquals(States.RUNNING, farm.getState());
    }

    @Test
    void repairConsumesToolAndSetsRunning() {
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.TOOLS, 100);
        Building farm = builder.build(BuildingType.FARM, new Position(105, 105));
        farm.switchState(States.RUNNING, -1);   // 建造中 -> 正常运转
        farm.switchState(States.BROKEN, -1);    // 正常运转 -> 坏掉
        bm.repairBuilding(farm);                // 修好 -> 正常运转
        assertEquals(States.RUNNING, farm.getState());
        assertEquals(99, ResourceManager.getResourceAmount(ResourceType.TOOLS));
    }

    @Test
    void removeBuildingClearsIt() {
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 1000);
        ResourceManager.setResourceAmount(ResourceType.STONE, 1000);
        Building farm = builder.build(BuildingType.FARM, new Position(106, 106));
        bm.addBuilding(farm);
        assertTrue(bm.exists(farm));
        bm.removeBuilding(farm);
        assertFalse(bm.exists(farm));
    }

    @Test
    void hunterGuildConvertsPreyToFoodOnSettlement() {
        BuildingManager bm = new BuildingManager();
        ResourceManager.setResourceAmount(ResourceType.WOOD, 1000);
        ResourceManager.setResourceAmount(ResourceType.STONE, 1000);
        ResourceManager.setResourceAmount(ResourceType.FOOD, 0);
        Building guild = builder.build(BuildingType.HUNTERGUILD, new Position(107, 107));
        bm.addBuilding(guild);
        guild.deliverPrey(4);
        bm.handle();
        assertEquals(20, ResourceManager.getResourceAmount(ResourceType.FOOD));
    }
}
