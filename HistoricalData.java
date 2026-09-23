package tfgirls.project.javarts.Model;

import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.Map;

/**
 * 历史数据快照：记下某一天的资源库存、建筑数量和人口数量。
 * {@link GameManager} 每 10 天记一次，给图表视图展示长期变化用。
 */
public class HistoricalData {
    private final int day;
    private final Map<ResourceType, Integer> resources;
    private final int buildingCount;
    private final int population;

    /**
     * @param day           是第几天
     * @param resources     当天各种资源有多少
     * @param buildingCount 当天有多少建筑
     * @param population    当天有多少人
     */
    public HistoricalData(int day, Map<ResourceType, Integer> resources, int buildingCount, int population) {
        this.day = day;
        this.resources = Map.copyOf(resources);
        this.buildingCount = buildingCount;
        this.population = population;
    }

    // 取值方法
    public int getDay() {
        return day;
    }

    public Map<ResourceType, Integer> getResources() {
        return resources;
    }

    public int getBuildingCount() {
        return buildingCount;
    }

    public int getPopulation() {
        return population;
    }
}
