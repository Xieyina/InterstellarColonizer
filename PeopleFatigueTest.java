package tfgirls.project.javarts.people;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingBuilder;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Position;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 居民累不累的测试：
 * 干活每天 +25 累度（最多 100）、休息每天 −25（最少 0）、
 * 累度到 50 以上，效率和移动速度都减半。
 */
class PeopleFatigueTest {

    private static Building jobBuilding() {
        return new BuildingBuilder().build(BuildingType.FARM, new Position(0, 0));
    }

    @Test
    void restingPeopleDoNotAccumulateFatigue() {
        People p = new People();
        p.advanceDay();
        assertEquals(0, p.getFatigue());
        assertEquals(0, p.getConsecutiveWorkDays());
        assertFalse(p.isFatigued());
    }

    @Test
    void workingDayAddsTwentyFiveFatigue() {
        People p = new People();
        p.affectJobPlace(jobBuilding());
        p.advanceDay();
        assertEquals(25, p.getFatigue());
        assertEquals(1, p.getConsecutiveWorkDays());
        assertFalse(p.isFatigued());
        assertEquals(1.0, p.getEfficiency(), 0.0001);
        assertEquals(1.0, p.getMovementSpeed(), 0.0001);
    }

    @Test
    void fatigueThresholdHalvesEfficiencyAndSpeed() {
        People p = new People();
        p.affectJobPlace(jobBuilding());
        p.advanceDay();
        p.advanceDay();   // 累度 50，到这个数就触发了
        assertEquals(50, p.getFatigue());
        assertTrue(p.isFatigued());
        assertEquals(0.5, p.getEfficiency(), 0.0001);
        assertEquals(0.5, p.getMovementSpeed(), 0.0001);
    }

    @Test
    void fatigueCapsAtHundred() {
        People p = new People();
        p.affectJobPlace(jobBuilding());
        for (int i = 0; i < 6; i++) {
            p.advanceDay();   // 6×25 = 150 → 但最多就是 100
        }
        assertEquals(100, p.getFatigue());
    }

    @Test
    void restingRecoversFatigueAndFloorsAtZero() {
        People p = new People();
        p.affectJobPlace(jobBuilding());
        p.advanceDay();          // 累度 25
        p.affectJobPlace(null);  // 开始休息
        p.advanceDay();          // 25 − 25 = 0
        assertEquals(0, p.getFatigue());
        p.advanceDay();          // 最少就是 0
        assertEquals(0, p.getFatigue());
        assertEquals(0, p.getConsecutiveWorkDays());
    }
}
