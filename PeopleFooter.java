package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import tfgirls.project.javarts.Controller.*;
import tfgirls.project.javarts.Controller.Commands.*;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingFunction;
import java.util.Set;

public class PeopleFooter extends VBox {
    private static final Logger LOG = LoggerFactory.getLogger(PeopleFooter.class);
    private Label inhabitantsLabel = new Label();
    private Label workerLabel = new Label();
    private HBox cardContainer;
    private ScrollPane cardRoot;
    private Building selectedBuilding;

    /**
     * 新建底部栏，里面显示地图上所有建筑。
     * 每张卡片上有按钮可以管理建筑里的人员。
     */
    public PeopleFooter() {
        cardRoot = new ScrollPane();
        // 新建一个容器来装卡片
        cardContainer = new HBox(10);
        this.setBackground(new Background(new BackgroundImage(
                new Image(getClass().getResource("/tfgirls/project/javarts/panel_blue.png").toExternalForm()),
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        this.setPadding(new Insets(5));
        this.setAlignment(Pos.CENTER_LEFT);

        generateButtons();

        cardRoot.setContent(cardContainer);
        cardRoot.setFitToHeight(true);
        cardRoot.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // 内容超宽时可以左右滚动
        cardRoot.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // 不允许上下滚动

        // 让 cardRoot 尽量占满空间，不要溢出
        HBox.setHgrow(cardRoot, Priority.ALWAYS);
        this.getChildren().add(cardRoot);

        this.getChildren().addAll();
    }

    /**
     * 生成管理建筑人员的按钮
     */
    public void generateButtons() {
        HBox buttons = new HBox();
        buttons.setFillHeight(true);
        buttons.setPadding(new Insets(10));
        buttons.setSpacing(15);
        Button addInhabitantButton = new Button("添加居民");
        addInhabitantButton.setOnAction(event -> {
            BagOfCommands.getInstance().addCommand(new AddInhabitantIntoCommand(selectedBuilding));
            LOG.debug("Inhabitant added to {}", selectedBuilding);
        });
        Button removeInhabitantButton = new Button("移除居民");
        removeInhabitantButton.setOnAction(event -> {
            BagOfCommands.getInstance().addCommand(new RemoveInhabitantFromCommand(selectedBuilding));
            LOG.debug("Inhabitant removed from {}", selectedBuilding);
        });
        Button assignWorkerButton = new Button("分配工人");
        assignWorkerButton.setOnAction(event -> {
            BagOfCommands.getInstance().addCommand(new AddWorkerIntoCommand(selectedBuilding));
            LOG.debug("Worker added to {}", selectedBuilding);
        });
        Button fireWorkerButton = new Button("解雇工人");
        fireWorkerButton.setOnAction(event -> {
            BagOfCommands.getInstance().addCommand(new RemoveWorkerFromCommand(selectedBuilding));
            LOG.debug("Worker removed from {}", selectedBuilding);
        });
        buttons.getChildren().addAll(addInhabitantButton, removeInhabitantButton, assignWorkerButton, fireWorkerButton);
        // ===== 改动：居民/工人的图标换成男女小人图片 =====
        ImageView inhabitants = new ImageView(
                new Image(getClass().getResource("/tfgirls/project/javarts/icons/people/male_resident.png").toExternalForm()));
        ImageView workers = new ImageView(
                new Image(getClass().getResource("/tfgirls/project/javarts/icons/people/male_worker.png").toExternalForm()));
        workers.fitHeightProperty().bind(workerLabel.heightProperty());
        workers.setPreserveRatio(true);
        inhabitants.fitHeightProperty().bind(inhabitantsLabel.heightProperty());
        inhabitants.setPreserveRatio(true);
        buttons.getChildren().add(inhabitants);
        buttons.getChildren().addAll(inhabitantsLabel, workers, workerLabel);
        buttons.setAlignment(Pos.CENTER);
        this.getChildren().addAll(buttons);
    }

    /**
     * 更新每张卡片上的居民和工人数量
     *
     * @param buildings 游戏数据更新后的建筑列表
     */
    public void updateBuildings(Set<Building> buildings) {
        int inhabitants = 0;
        int workers = 0;
        int maxInhabitants = 0;
        int maxWorkers = 0;
        this.getChildren().clear();
        generateButtons();
        cardContainer.getChildren().clear();
        VBox container2 = new VBox();
        container2.setAlignment(Pos.CENTER);
        HBox container3 = new HBox();
        container2.getChildren().add(container3);
        for (Building building : buildings) {
            if (building.getFunctions().contains(BuildingFunction.LIVING)) {
                inhabitants += building.getInhabitants().size();
                maxInhabitants += building.getMaxInhabitants();
            }
            if (building.getFunctions().contains(BuildingFunction.WORKING)) {
                workers += building.getWorkers().size();
                maxWorkers += building.getMaxWorkers();
            }

            BuildingInfoCard bc = new BuildingInfoCard(building, selectedBuilding);
            bc.setOnMouseClicked(event -> {
                BagOfCommands.getInstance().addCommand(new SetSelectedBuildingInfoCommand(building));
            });
            container3.getChildren().add(bc);
        }
        inhabitantsLabel.setText("居民: " + inhabitants + "/" + maxInhabitants);
        workerLabel.setText("工人: " + workers + "/" + maxWorkers);
        cardContainer.getChildren().addAll(container2);
        this.getChildren().add(cardRoot);

    }

    /**
     * 设置当前选中的建筑。
     *
     * @param building
     */
    public void setSelectedBuildingInfo(Building building) {
        this.selectedBuilding = building;
    }
}
