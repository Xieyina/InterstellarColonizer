package tfgirls.project.javarts.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingBuilder;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.TestGame;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 游戏管理器（GameManager）一些还没测到的方法：
 * 设置/读取天数、默认干活效率、游戏是否结束、给猎人工会交猎物。
 */
class GameManagerExtraTest {

    private static GameManager gm;

    @BeforeAll
    static void bootstrap() {
        gm = TestGame.model();
    }

    @AfterEach
    void resetDay() {
        gm.setDay(0);
    }

    @Test
    void setDayAndGetDayRoundTrip() {
        gm.setDay(7);
        assertEquals(7, gm.getDay());
    }

    @Test
    void productionEfficiencyDefaultsToOne() {
        assertEquals(1.0, gm.getProductionEfficiency(), 1e-9);
    }

    @Test
    void deliverPreyZeroReturnsNull() {
        assertNull(gm.deliverPreyToGuild(0));
    }

    @Test
    void deliverPreyToBuiltGuildStoresStock() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 1000);
        ResourceManager.setResourceAmount(ResourceType.STONE, 1000);
        // 地图是 200x200，选一个靠边又不跟别的测试撞车的位置
        gm.addBuilding(BuildingType.HUNTERGUILD, new Position(150, 150));
        Building result = gm.deliverPreyToGuild(3);
        assertNotNull(result);
        assertEquals(3, result.getPreyStock());
    }
}
