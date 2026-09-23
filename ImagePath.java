package tfgirls.project.javarts.View;

import tfgirls.project.javarts.Exception.WrongBuildingType;
import tfgirls.project.javarts.Exception.WrongResourceType;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Resource.ResourceType;

public class ImagePath {



        // 私有的构造方法，不让外面 new 这个类
        private ImagePath() {
            throw new UnsupportedOperationException("Utility class");
        }

    /**
     * 根据资源类型返回对应的图片路径。
     * 用来做顶部资源菜单或底部卡片上的小图标。
     *
     * @param resource 一种资源类型
     * @return 这种资源对应的图片路径
     */
    public static String getResourceLogoPath(ResourceType resource) {
            switch (resource) {
                case COAL -> {
                    return "/tfgirls/project/javarts/resourcesIcons/coal.png";
                }
                case FOOD -> {
                    return "/tfgirls/project/javarts/resourcesIcons/food.png";
                }
                case IRON -> {
                    return "/tfgirls/project/javarts/resourcesIcons/iron.png";
                }
                case WOOD -> {
                    return "/tfgirls/project/javarts/resourcesIcons/wood.png";
                }
                case STEEL -> {
                    return "/tfgirls/project/javarts/resourcesIcons/steel.png";
                }
                case STONE -> {
                    return "/tfgirls/project/javarts/resourcesIcons/stone.png";
                }
                case TOOLS -> {
                    return "/tfgirls/project/javarts/resourcesIcons/tools.png";
                }
                case CEMENT -> {
                    return "/tfgirls/project/javarts/resourcesIcons/cement.png";
                }
                default -> throw new WrongResourceType("未知的资源类型");
            }
    }

    /**
     * 根据建筑类型返回对应的图片路径。
     * 用来做底部各种建筑卡片上的图片。
     *
     * @param buildingType 一种建筑类型
     * @return 这种建筑对应的图片路径
     */
    public static String getBuildingSpritePath(BuildingType buildingType) {
            switch (buildingType) {
                case WOODENCABIN -> {
                    return "/tfgirls/project/javarts/buildingSprites/woodenCabin.png";
                }
                case HOUSE -> {
                    return "/tfgirls/project/javarts/buildingSprites/house.png";
                }
                case APPARTMENTBUILDING -> {
                    return "/tfgirls/project/javarts/buildingSprites/apartmentbuilding.png";
                }
                case FARM -> {
                    return "/tfgirls/project/javarts/buildingSprites/farm.png";
                }
                case QUARRY -> {
                    return "/tfgirls/project/javarts/buildingSprites/quarry.png";
                }
                case CEMENTPLANT -> {
                    return "/tfgirls/project/javarts/buildingSprites/cementplant.png";
                }
                case STEELMILL -> {
                    return "/tfgirls/project/javarts/buildingSprites/steelmill.png";
                }
                case TOOLFACTORY -> {
                    return "/tfgirls/project/javarts/buildingSprites/factory.png";
                }
                // 猎人工会：暂时先借用农场的图片，以后再换专门的图
                case HUNTERGUILD -> {
                    return "/tfgirls/project/javarts/buildingSprites/farm.png";
                }
                default -> throw new WrongBuildingType("未知的建筑类型");

            }

    }
}
