package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.Building.Building;

import tfgirls.project.javarts.Model.GameManager;

/**
 * 在控制器里设置选中的建筑信息的命令。
 *
 * 属于命令模式，跟命令袋子一起用。
 * 把"选中建筑信息"这件事打包起来，
 * 这样可以稍后再做，或者排队等着做。
 *
 */
public class SetSelectedBuildingInfoCommand implements Command {
    private Building b;

    public SetSelectedBuildingInfoCommand(Building b) {
        this.b = b;
    }

    @Override
    public void execute(GameManager model, Controller controller) {
        controller.ChangeSelectedBuildingInfo(b);
    }
}