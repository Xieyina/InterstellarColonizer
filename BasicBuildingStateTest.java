package tfgirls.project.javarts.building;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.BasicBuilding;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Size;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 建筑五种状态（建造中/运行中/堵塞/加速/坏了）的逻辑测试。
 */
class BasicBuildingStateTest {

    private BasicBuilding newBuilding(int constructionTime) {
        return new BasicBuilding(new Position(0, 0), new Size(1, 1), "TestCabin",
                BuildingType.WOODENCABIN, new HashMap<>(), constructionTime);
    }

    @Test
    void newBuildingStartsInConstruction() {
        assertEquals(States.CONSTRUCTION, newBuilding(2).getState());
    }

    @Test
    void constructionCompletesAfterCountdown() {
        BasicBuilding b = newBuilding(1);
        b.handle();   // 倒计时 1 → 0
        b.handle();   // 0 → 变成运行中
        assertEquals(States.RUNNING, b.getState());
    }

    @Test
    void switchStateChangesState() {
        BasicBuilding b = newBuilding(1);
        b.switchState(States.RUNNING, -1);
        assertEquals(States.RUNNING, b.getState());
        b.switchState(States.BLOCKED, -1);
        assertEquals(States.BLOCKED, b.getState());
        b.switchState(States.RUNNING, -1);
        b.switchState(States.BOOSTED, 5);
        assertEquals(States.BOOSTED, b.getState());
        assertEquals(5, b.getRemainingTime());
        b.switchState(States.RUNNING, -1);
        b.switchState(States.BROKEN, -1);
        assertEquals(States.BROKEN, b.getState());
        b.switchState(States.RUNNING, -1);
        assertEquals(States.RUNNING, b.getState());
    }

    @Test
    void boostExpiresToRunningOrBroken() {
        BasicBuilding b = newBuilding(1);
        b.switchState(States.RUNNING, -1);
        b.switchState(States.BOOSTED, 2);
        b.handle();   // 2 → 1
        b.handle();   // 1 → 0
        b.handle();   // 加速到期：1/4 概率坏了，3/4 概率恢复正常（源码里的随机数没法替换）
        assertTrue(b.getState() == States.RUNNING || b.getState() == States.BROKEN,
                "Boost 到期后状态应为 RUNNING 或 BROKEN，实际: " + b.getState());
    }

    @Test
    void stateChangeMarksViewUpdate() {
        BasicBuilding b = newBuilding(1);
        b.switchState(States.RUNNING, -1);
        assertTrue(b.needViewUpdate());
    }
}
