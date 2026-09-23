package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.GameManager;

/**
 * 让某个建筑停止生产/消耗的命令。
 *
 * 属于命令模式，跟命令袋子一起用。
 * 把"停掉建筑"这件事打包起来，
 * 这样可以稍后再做，或者排队等着做。
 *
 */
public class BlockBuildingCommand implements Command {
    private final Building building;

    public BlockBuildingCommand(Building building) {
        this.building = building;
    }

    @Override
    public void execute(GameManager model, Controller controller) {
        model.blockBuilding(building);
    }
}
