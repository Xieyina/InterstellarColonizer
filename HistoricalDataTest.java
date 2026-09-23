package tfgirls.project.javarts.Model;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 历史数据快照（HistoricalData）的测试：记下的天数、资源、建筑数、人口数对不对。
 */
class HistoricalDataTest {

    @Test
    void recordsSnapshotValues() {
        HistoricalData data = new HistoricalData(10,
                Map.of(ResourceType.FOOD, 5, ResourceType.WOOD, 8), 3, 7);
        assertEquals(10, data.getDay());
        assertEquals(3, data.getBuildingCount());
        assertEquals(7, data.getPopulation());
        assertEquals(5, data.getResources().get(ResourceType.FOOD));
        assertEquals(8, data.getResources().get(ResourceType.WOOD));
    }
}
