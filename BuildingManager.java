package tfgirls.project.javarts.Model.Building;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import tfgirls.project.javarts.Exception.NotEnoughResources;
import tfgirls.project.javarts.Exception.WrongBuildingType;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Tech.TechTree;

public class BuildingManager {
    private static final Logger LOG = LoggerFactory.getLogger(BuildingManager.class);
    private final Set<Building> buildings;

    // ===== 猎人工会算账参数 =====
    // 交 1 份猎物，每天算账时换成多少食物。
    public static final int HUNTER_PREY_FOOD_YIELD = 5;

    public BuildingManager() {
        buildings = new HashSet<>();
    }

    public static HashMap<ResourceType, Integer> buildingCost(BuildingType buildingType) {
        HashMap<ResourceType, Integer> cost;
        switch (buildingType) {
            case WOODENCABIN -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 1); }};
            }
            case HOUSE -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 2); put(ResourceType.STONE, 2); }};
            }
            case APPARTMENTBUILDING -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 50); put(ResourceType.STONE, 50); }};
            }
            case FARM -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 5); put(ResourceType.STONE, 5); }};
            }
            case QUARRY -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 50); }};
            }
            case CEMENTPLANT -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 50); put(ResourceType.STONE, 50); }};
            }
            case STEELMILL -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 100); put(ResourceType.STONE, 50); }};
            }
            case TOOLFACTORY -> {
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 50); put(ResourceType.STONE, 50); }};
            }
            case HUNTERGUILD -> {
                // 猎人工会的建造成本（不会自动赚钱，靠交猎物算账）
                cost = new HashMap<>() {{ put(ResourceType.WOOD, 10); put(ResourceType.STONE, 10); }};
            }
            default -> {
                throw new WrongBuildingType("找不到建筑类型：" + buildingType);
            }
        }
        return cost;
    }

    public static boolean isBuildable(BuildingType building) {
        HashMap<ResourceType, Integer> cost = buildingCost(building);
        // ===== 检查科技有没有解锁这个建筑 =====
        TechTree techTree = GameManager.getInstance().getTechTree();
        boolean isStarter = building == BuildingType.WOODENCABIN
            || building == BuildingType.HOUSE
            || building == BuildingType.FARM
            || building == BuildingType.HUNTERGUILD; // 猎人工会一开始就能建（不用解锁科技）
        if (!techTree.isBuildingUnlocked(building) && !isStarter) {
            return false; // 科技没解锁的建筑不能建
        }
        return ResourceManager.areAvailable(cost);
    }

    public void addBuilding(Building building) {
        if (!exists(building)) {
            HashMap<ResourceType, Integer> cost = buildingCost(building.getType());
            if (ResourceManager.areAvailable(cost)) {
                for (ResourceType type : cost.keySet()) {
                    ResourceManager.removeResource(type, cost.get(type));
                }
                buildings.add(building);
                LOG.debug("Added {} at {}", building.getType(), building.getPosition());
            } else {
                throw new NotEnoughResources("资源不足，无法建造：" + building.getType());
            }
        }
    }

    public void repairBuilding(Building building) {
        ResourceManager.removeResource(ResourceType.TOOLS, 1);
        building.switchState(States.RUNNING, -1);
        LOG.info("Repaired {}", building.getName());
    }

    public void boostBuilding(Building building) {
        ResourceManager.removeResource(ResourceType.TOOLS, 1);
        building.switchState(States.BOOSTED, 5);
        LOG.info("Boosted {} for 5 days", building.getName());
    }

    public void blockBuilding(Building building) {
        building.switchState(States.BLOCKED, -1);
        LOG.info("Blocked {}", building.getName());
    }

    public void runBuilding(Building building) {
        building.switchState(States.RUNNING, -1);
        LOG.info("Running {}", building.getName());
    }

    public void removeBuilding(Building building) {
        if (!exists(building)) {
            return;
        }
        buildings.remove(building);
        LOG.info("Removed {}", building.getName());
    }

    public boolean exists(Building building) {
        return buildings.contains(building);
    }

    public void addInhabitantInto(Building building, People people) {
        if (buildings.contains(building)) {
            if (building.getFunctions().contains(BuildingFunction.LIVING)) {
                building.addInhabitant(people);
                people.affectHouse(building);
            } else {
                throw new WrongBuildingType("该建筑不能住人：" + building.getType());
            }
        }
    }

    public Set<Building> getBuildings() {
        return buildings;
    }

    // ===== handle() 方法里加上科技加成 =====
    public void handle() {
        HashMap<ResourceType, Integer> global = computeGlobalProduction();
        LOG.debug("Daily resources: {}", global);
        applyGlobalResources(global);
    }

    /**
     * 跑一遍所有建筑，算每天产出和消耗，把工人累不累、科技加成、
     * 人饿不饿都算进去，最后把猎人工会的猎物也结了。
     */
    private HashMap<ResourceType, Integer> computeGlobalProduction() {
        TechTree techTree = GameManager.getInstance().getTechTree();
        // 读取全局生产效率倍率（人饿的时候效率减半变成 0.5，不饿就是 1.0）
        double hungerMult = GameManager.getInstance().getProductionEfficiency();
        HashMap<ResourceType, Integer> global = new HashMap<>();

        for (Building building : buildings) {
            HashMap<ResourceType, Integer> resources = building.handle();

            if (building.getFunctions().contains(BuildingFunction.WORKING)) {
                // 按工人累不累来算（累的工人干活少），不是以前那种光看人数比
                double workerEff = 0.0;
                for (People w : building.getWorkers()) {
                    workerEff += w.getEfficiency();
                }
                double percentage = workerEff / building.getMaxWorkers();
                for (ResourceType rt : resources.keySet()) {
                    // 把科技加成和饿不饿的效率都乘上去
                    double multiplier = techTree.getProductionMultiplier(rt);
                    int amount = (int) (resources.get(rt) * percentage * multiplier * hungerMult);
                    global.put(rt, global.getOrDefault(rt, 0) + amount);
                }
            }

            if (building.getFunctions().contains(BuildingFunction.LIVING)) {
                global.put(ResourceType.FOOD,
                    global.getOrDefault(ResourceType.FOOD, 0) - building.getNumberInhabitants());
            }

            settleHunterGuild(building, global, hungerMult);
        }
        return global;
    }

    // ===== 猎人工会：交猎物再算钱 =====
    // 这建筑自己不自动产出东西；如果攒了猎物，每天算账时就换成食物。
    private void settleHunterGuild(Building building, HashMap<ResourceType, Integer> global,
                                   double hungerMult) {
        if (building.getType() != BuildingType.HUNTERGUILD) {
            return;
        }
        int prey = building.consumePreyStock();
        if (prey > 0) {
            int foodYield = (int) (prey * HUNTER_PREY_FOOD_YIELD * hungerMult);
            global.put(ResourceType.FOOD,
                global.getOrDefault(ResourceType.FOOD, 0) + foodYield);
            LOG.info("结算猎物 x{} -> 食物 +{}", prey, foodYield);
        }
    }

    /**
     * 把每天算好的产出和消耗加到资源库里；如果某种资源不够用，
     * 就把需要消耗它的建筑自动停掉。
     */
    private void applyGlobalResources(HashMap<ResourceType, Integer> global) {
        for (ResourceType rt : global.keySet()) {
            try {
                ResourceManager.addResource(rt, global.get(rt));
            } catch (NotEnoughResources e) {
                LOG.warn("Not enough resources: {}", rt);
                for (Building b : buildings) {
                    if (b.getFunctions().contains(BuildingFunction.CONSUMING) &&
                        b.getDailyConsumption().containsKey(rt)) {
                        if (b.getState() != States.BLOCKED && b.getState() != States.BROKEN) {
                            b.switchState(States.BLOCKED, -1);
                            LOG.warn("Auto-blocked {} due to resource shortage", b.getName());
                        }
                    }
                }
            }
        }
    }
}