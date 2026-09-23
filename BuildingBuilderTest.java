package tfgirls.project.javarts.building;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingBuilder;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Position;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 建筑工厂（BuildingBuilder）能不能把 9 种建筑都正常造出来，
 * 以及每种建筑的建造天数、占地大小、名字对不对。
 */
class BuildingBuilderTest {

    private final BuildingBuilder builder = new BuildingBuilder();

    @Test
    void allBuildingTypesBuildWithoutError() {
        for (BuildingType type : BuildingType.values()) {
            Building b = builder.build(type, new Position(0, 0));
            assertNotNull(b);
            assertEquals(type, b.getType());
        }
    }

    @Test
    void constructionTimesMatchDesign() {
        Map<BuildingType, Integer> expected = Map.of(
                BuildingType.WOODENCABIN, 2,
                BuildingType.HOUSE, 4,
                BuildingType.APPARTMENTBUILDING, 6,
                BuildingType.FARM, 2,
                BuildingType.QUARRY, 2,
                BuildingType.CEMENTPLANT, 4,
                BuildingType.STEELMILL, 6,
                BuildingType.TOOLFACTORY, 6,
                BuildingType.HUNTERGUILD, 3
        );
        for (Map.Entry<BuildingType, Integer> e : expected.entrySet()) {
            Building b = builder.build(e.getKey(), new Position(1, 1));
            assertEquals(e.getValue().intValue(), b.getConstructionTime(),
                    () -> "construction time of " + e.getKey());
        }
    }

    @Test
    void sizesMatchDesign() {
        assertEquals(1, builder.build(BuildingType.WOODENCABIN, new Position(0, 0)).getSize().getWidth());
        assertEquals(1, builder.build(BuildingType.WOODENCABIN, new Position(0, 0)).getSize().getHeight());
        assertEquals(2, builder.build(BuildingType.HOUSE, new Position(0, 0)).getSize().getWidth());
        assertEquals(2, builder.build(BuildingType.HOUSE, new Position(0, 0)).getSize().getHeight());
        assertEquals(2, builder.build(BuildingType.HUNTERGUILD, new Position(0, 0)).getSize().getWidth());
        assertEquals(2, builder.build(BuildingType.HUNTERGUILD, new Position(0, 0)).getSize().getHeight());
    }

    @Test
    void namesAreNotEmpty() {
        for (BuildingType type : BuildingType.values()) {
            Building b = builder.build(type, new Position(0, 0));
            assertNotNull(b.getName());
            assertFalse(b.getName().isEmpty());
        }
    }
}
