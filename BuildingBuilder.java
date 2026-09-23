package tfgirls.project.javarts.Model.Building;

import tfgirls.project.javarts.Exception.WrongBuildingType;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

public class BuildingBuilder {

    public BuildingBuilder() {

    }

    public Building build(BuildingType buildingType, Position position) {
        return switch (buildingType) {
            case WOODENCABIN -> buildWoodenCabin(position);
            case HOUSE -> buildHouse(position);
            case APPARTMENTBUILDING -> buildApartmentBuilding(position);
            case FARM -> buildFarm(position);
            case QUARRY -> buildQuarry(position);
            case CEMENTPLANT -> buildCementPlant(position);
            case STEELMILL -> buildSteelMill(position);
            case TOOLFACTORY -> buildToolFactory(position);
            case HUNTERGUILD -> buildHunterGuild(position);
            default -> throw new WrongBuildingType("找不到建筑类型：" + buildingType);
        };
    }

    private Building buildWoodenCabin(Position position) {
        Building woodenCabin = new WorkingBuilding(new ProductionBuilding(new LivingBuilding(new BasicBuilding(position, new Size(1, 1), "木屋",BuildingType.WOODENCABIN, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 1);}},2), 2), new Hashtable<ResourceType, Integer>(){{put(ResourceType.WOOD, 2); put(ResourceType.FOOD, 2);}}), 2);
        woodenCabin.addFunction(new ArrayList<BuildingFunction>());
        return woodenCabin;
    }

    private Building buildHouse(Position position) {
        Building house = new LivingBuilding(new BasicBuilding(position, new Size(2, 2), "房屋",BuildingType.HOUSE, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 2);put(ResourceType.STONE,2);}},4), 4);
        house.addFunction(new ArrayList<BuildingFunction>());
        return house;
    }

    private Building buildApartmentBuilding(Position position) {
        Building apartment = new LivingBuilding(new BasicBuilding(position, new Size(3, 2), "公寓",BuildingType.APPARTMENTBUILDING, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 50);put(ResourceType.STONE,50);}},6), 60);
        apartment.addFunction(new ArrayList<BuildingFunction>());
        return apartment;
    }

    private Building buildFarm(Position position) {
        Building farm = new WorkingBuilding(new ProductionBuilding(new LivingBuilding(new BasicBuilding(position, new Size(3, 3), "农场",BuildingType.FARM, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 5);put(ResourceType.STONE,5);}},2), 5), new Hashtable<ResourceType, Integer>(){{put(ResourceType.FOOD, 10);}}), 3);
        farm.addFunction(new ArrayList<BuildingFunction>());
        return farm;
    }

    private Building buildQuarry(Position position) {
        Building quarry = new LivingBuilding( new ProductionBuilding( new WorkingBuilding( new BasicBuilding(position, new Size(2,2), "矿场",BuildingType.QUARRY, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 50);}},2), 30), new Hashtable<ResourceType, Integer>(){{put(ResourceType.STONE, 4); put(ResourceType.IRON, 4); put(ResourceType.COAL, 4);}}), 2);
        quarry.addFunction(new ArrayList<BuildingFunction>());
        return quarry;
    }

    private Building buildCementPlant(Position position) {
        Building cementplant = new ProductionBuilding(new ConsumptionBuilding(new WorkingBuilding(new BasicBuilding(position, new Size(4, 3), "水泥厂",BuildingType.CEMENTPLANT, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 50);put(ResourceType.STONE,50);}},4), 10), new Hashtable<ResourceType, Integer>(){{put(ResourceType.STONE, 4); put(ResourceType.COAL, 4);}}), new Hashtable<ResourceType, Integer>(){{put(ResourceType.CEMENT, 4);}});
        cementplant.addFunction(new ArrayList<BuildingFunction>());
        return cementplant;
    }

    private Building buildSteelMill(Position position) {
        Building steelmill = new ProductionBuilding(new ConsumptionBuilding(new WorkingBuilding(new BasicBuilding(position, new Size(4,3), "钢铁厂",BuildingType.STEELMILL, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 100);put(ResourceType.STONE,50);}},6), 40), new Hashtable<ResourceType, Integer>(){{put(ResourceType.IRON, 4); put(ResourceType.COAL, 2);}}), new Hashtable<ResourceType, Integer>(){{put(ResourceType.STEEL, 4);}});
        steelmill.addFunction(new ArrayList<BuildingFunction>());
        return steelmill;
    }

    private Building buildToolFactory(Position position) {
        Building toolfactory = new ConsumptionBuilding(new ProductionBuilding(new WorkingBuilding(new BasicBuilding(position, new Size(4, 3), "工具厂",BuildingType.TOOLFACTORY, new HashMap<ResourceType, Integer>(){{put(ResourceType.WOOD, 50);put(ResourceType.STONE,50);}},6), 12), new Hashtable<ResourceType, Integer>(){{put(ResourceType.TOOLS, 4);}}), new Hashtable<ResourceType, Integer>(){{put(ResourceType.STEEL, 4); put(ResourceType.COAL, 4);}});
        toolfactory.addFunction(new ArrayList<BuildingFunction>());
        return toolfactory;
    }

    private Building buildHunterGuild(Position position) {
        // 猎人工会。
        // 以前那种「自动发钱」没了——它自己不自动产出任何资源（每天产出一栏是空的）。
        // 现在的玩法：玩家把猎物交给猎人工会（deliverPrey），每天算账的时候
        // 再按猎物数量换成食物（具体看 BuildingManager.handle 里的算账逻辑）。
        Building guild = new WorkingBuilding(
                new ProductionBuilding(
                        new BasicBuilding(position, new Size(2, 2), "猎人工会", BuildingType.HUNTERGUILD,
                                new HashMap<ResourceType, Integer>() {{
                                    put(ResourceType.WOOD, 10);
                                    put(ResourceType.STONE, 10);
                                }}, 3),
                        new Hashtable<ResourceType, Integer>() {{}}), // 产出为空：不会自动赚东西
                3);
        guild.addFunction(new ArrayList<BuildingFunction>());
        return guild;
    }
}
