package tfgirls.project.javarts.Controller;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Controller.Commands.AddBuildingCommand;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Position;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 命令模式里的"放建筑命令"（AddBuildingCommand）测试：
 * 确认它把控制器里选中的建筑类型和执行位置，正确地转交给游戏管理器去建。
 */
class AddBuildingCommandTest {

    @Test
    void executeForwardsSelectedTypeAndPositionToModel() throws Exception {
        GameManager model = mock(GameManager.class);
        Controller controller = mock(Controller.class);
        // selectedBuilding 是控制器上的一个公开字段，用反射设上去（Mockito 不能直接 stub 字段）
        Field field = Controller.class.getField("selectedBuilding");
        field.set(controller, BuildingType.HUNTERGUILD);
        Position pos = new Position(5, 5);

        AddBuildingCommand cmd = new AddBuildingCommand(pos);
        cmd.execute(model, controller);

        verify(model).addBuilding(BuildingType.HUNTERGUILD, pos);
    }
}
