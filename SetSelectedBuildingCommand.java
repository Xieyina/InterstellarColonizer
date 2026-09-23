package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.GameManager;

/**
 * 在控制器里设置选中的建筑类型的命令。
 *
 * 属于命令模式，跟命令袋子一起用。
 * 把"选建筑"这件事打包起来，
 * 这样可以稍后再做，或者排队等着做。
 *
 */
public class SetSelectedBuildingCommand implements Command {
    private BuildingType type;

    public SetSelectedBuildingCommand(BuildingType type) {
        this.type = type;
    }

    @Override
    public void execute(GameManager model, Controller controller) {
        controller.ChangeSelectedBuilding(type);
    }
}
