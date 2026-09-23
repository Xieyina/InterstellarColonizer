package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.GameManager;

/**
 * 往某个建筑里加一个工人的命令。
 *
 * 属于命令模式，跟命令袋子一起用。
 * 把"加工人"这件事打包起来，
 * 这样可以稍后再做，或者排队等着做。
 *
 */
public class AddWorkerIntoCommand implements Command {
    private Building building;

    public AddWorkerIntoCommand(Building b) {
        this.building = b;
    }

    @Override
    public void execute(GameManager model, Controller controller) {

        model.assignWorkerTo(building);
    }
}
