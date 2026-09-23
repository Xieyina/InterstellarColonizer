package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Position;

/**
 * 在指定位置放一个建筑的命令。
 *
 * 属于命令模式，跟命令袋子一起用。
 * 把"放建筑"这件事打包起来，
 * 这样可以稍后再做，或者排队等着做。
 *
 */
public class AddBuildingCommand implements Command {
    private Position position;

    public AddBuildingCommand(Position position) {
        this.position = position;
    }

    @Override
    public void execute(GameManager model, Controller controller) {
        model.addBuilding(controller.selectedBuilding, position);
    }
}
