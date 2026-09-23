package tfgirls.project.javarts.Controller;

import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.View.MainView;

/**
 * 控制器（负责在模型和界面之间传话）
 */
public class Controller {
    private GameManager model;
    private MainView view;
    private BagOfCommands bagOfCommands;
    public BuildingType selectedBuilding;
    public Building selectedBuildingInfo;

    /**
     * 新建一个控制器，用来把界面发来的命令转给模型去执行
     *
     * @param model         游戏管理器，命令发给它去执行
     * @param view          主界面，命令执行完后更新它
     * @param bagOfCommands 命令袋子，把命令放进去
     */
    public Controller(GameManager model, MainView view, BagOfCommands bagOfCommands) {
        this.model = model;
        this.view = view;
        this.bagOfCommands = bagOfCommands;
        bagOfCommands.setModel(model);
    }

    public void ChangeSelectedBuilding(BuildingType buildingType) {
        selectedBuilding = buildingType;
        view.setSelectedBuilding(buildingType);
    }

    public void ChangeSelectedBuildingInfo(Building building) {
        selectedBuildingInfo = building;
        view.setSelectedBuildingInfo(building);
    }

}
