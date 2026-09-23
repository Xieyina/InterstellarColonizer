package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.GameManager;

/**
 * 把地图上某个建筑拆掉的命令。
 *
 * 属于命令模式，跟命令袋子一起用。
 * 把"拆建筑"这件事打包起来，
 * 这样可以稍后再做，或者排队等着做。
 *
 */
public class RemoveBuildingCommand implements Command {
    private Building building;

    public RemoveBuildingCommand(Building building) {
        this.building = building;
    }

    @Override
    public void execute(GameManager model, Controller controller) {
        model.removeBuilding(building);
    }
}
