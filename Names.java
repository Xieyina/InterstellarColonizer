package tfgirls.project.javarts.View;

import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.Resource.ResourceType;

/**
 * 中文名称工具类：把游戏里的枚举（资源、建筑状态）换成中文名显示。
 * 游戏内所有给玩家看的文字都用中文，这里集中管映射。
 */
public class Names {

    // 私有的构造方法，不让外面 new 这个类
    private Names() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 资源类型的中文名。
     *
     * @param resource 一种资源类型
     * @return 这种资源的中文名（比如「食物」）
     */
    public static String resourceName(ResourceType resource) {
        return switch (resource) {
            case FOOD -> "食物";
            case WOOD -> "木材";
            case STONE -> "石头";
            case COAL -> "煤炭";
            case IRON -> "铁矿";
            case STEEL -> "钢铁";
            case CEMENT -> "水泥";
            case TOOLS -> "工具";
        };
    }

    /**
     * 建筑状态的中文名。
     *
     * @param state 一种建筑状态
     * @return 这种状态的中文名（比如「建造中」）
     */
    public static String stateName(States state) {
        return switch (state) {
            case CONSTRUCTION -> "建造中";
            case RUNNING -> "运行中";
            case BOOSTED -> "加速中";
            case BROKEN -> "已损坏";
            case BLOCKED -> "已停止";
        };
    }
}
