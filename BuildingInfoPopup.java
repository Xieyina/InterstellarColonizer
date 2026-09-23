package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import tfgirls.project.javarts.Controller.BagOfCommands;
import tfgirls.project.javarts.Controller.Commands.*;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingFunction;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.GameManager;

public class BuildingInfoPopup {
    private static final Logger LOG = LoggerFactory.getLogger(BuildingInfoPopup.class);

    /**
     * 弹出一个窗口，显示某个建筑的详细信息和操作按钮。
     */
    public BuildingInfoPopup(Building building) {
        // ===== 改动：游戏失败后一切操作都不再继续，建筑详情窗口也不再打开 =====
        if (GameManager.getInstance().isGameOver()) {
            return;
        }
        Stage popup = new Stage();
        popup.setMinHeight(450);
        popup.setMinWidth(420);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("🏗️ " + building.getName() + " - 管理");

        Label nameLabel = new Label("名称: " + building.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // ===== 改动：状态显示成中文 =====
        Label stateLabel = new Label("状态: " + Names.stateName(building.getState()));
        stateLabel.setStyle("-fx-font-size: 14px;");

        Button repairButton = new Button("🔧 修复 (1 工具)");
        repairButton.setOnAction(event -> {
            BagOfCommands.getInstance().addCommand(new RepairBuildingCommand(building));
            LOG.info("Building repaired: {}", building.getName());
        });

        ProgressBar constructionProgressBar = new ProgressBar();
        if (building.getState() != States.CONSTRUCTION) {
            constructionProgressBar.setVisible(false);
        }
        constructionProgressBar.setMinWidth(200);

        HBox layout = new HBox(20);
        layout.setSpacing(20);
        layout.setStyle("-fx-padding: 20;");

        VBox buildingManagement = new VBox(10);
        buildingManagement.getChildren().addAll(nameLabel, stateLabel, repairButton, constructionProgressBar);

        VBox peopleManagement = new VBox(10);
        Label inhabitantsLabel = new Label();
        Label workersLabel = new Label();

        // 只有能住人的建筑才有下面的功能
        if (building.getFunctions().contains(BuildingFunction.LIVING)) {
            inhabitantsLabel.setText("🏠 居民: " + building.getNumberInhabitants() + "/" + building.getMaxInhabitants());
            // ===== 改动：居民标签前放男女小人图 =====
            HBox inhabitantsRow = createPeopleRow(true, inhabitantsLabel);
            Button addInhabitantsButton = new Button("➕ 添加居民");
            addInhabitantsButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
            addInhabitantsButton.setOnAction(event -> {
                BagOfCommands.getInstance().addCommand(new AddInhabitantIntoCommand(building));
            });
            Button removeInhabitantsButton = new Button("➖ 移除居民");
            removeInhabitantsButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
            removeInhabitantsButton.setOnAction(event -> {
                BagOfCommands.getInstance().addCommand(new RemoveInhabitantFromCommand(building));
            });
            peopleManagement.getChildren().addAll(inhabitantsRow, addInhabitantsButton, removeInhabitantsButton);
        }

        // 只有能干活的建筑才有下面的功能
        if (building.getFunctions().contains(BuildingFunction.WORKING)) {
            workersLabel.setText("👷 工人: " + building.getNumberWorkers() + "/" + building.getMaxWorkers());
            // ===== 改动：工人标签前放男女小人图 =====
            HBox workersRow = createPeopleRow(false, workersLabel);
            Button addWorkersButton = new Button("➕ 分配工人");
            addWorkersButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
            addWorkersButton.setOnAction(event -> {
                BagOfCommands.getInstance().addCommand(new AddWorkerIntoCommand(building));
            });
            Button removeWorkersButton = new Button("➖ 解雇工人");
            removeWorkersButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
            removeWorkersButton.setOnAction(event -> {
                BagOfCommands.getInstance().addCommand(new RemoveWorkerFromCommand(building));
            });
            Button boostBuildingButton = new Button("⚡ 加速 (1 工具)");
            boostBuildingButton.setOnAction(event -> {
                BagOfCommands.getInstance().addCommand(new BoostBuildingCommand(building));
            });
            peopleManagement.getChildren().addAll(workersRow, addWorkersButton, removeWorkersButton, boostBuildingButton);
        }

        // 生产/消耗建筑的控制
        if (building.getFunctions().contains(BuildingFunction.PRODUCING) || 
            building.getFunctions().contains(BuildingFunction.CONSUMING)) {
            if (building.getState() == States.BLOCKED) {
                Button running = new Button("▶️ 运行");
                running.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white;");
                running.setOnAction(event -> {
                    BagOfCommands.getInstance().addCommand(new RunBuildingCommand(building));
                });
                peopleManagement.getChildren().add(running);
            } else if (building.getState() == States.RUNNING || building.getState() == States.BOOSTED) {
                Button blockBuildingButton = new Button("⏸️ 停止");
                blockBuildingButton.setStyle("-fx-background-color: #e65100; -fx-text-fill: white;");
                blockBuildingButton.setOnAction(event -> {
                    BagOfCommands.getInstance().addCommand(new BlockBuildingCommand(building));
                });
                peopleManagement.getChildren().add(blockBuildingButton);
            }
        }

        Button removeButton = new Button("🗑️ 拆除");
        removeButton.setStyle("-fx-background-color: #b71c1c; -fx-text-fill: white;");
        removeButton.setOnAction(event -> {
            BagOfCommands.getInstance().addCommand(new RemoveBuildingCommand(building));
            LOG.info("Building removed: {}", building);
            popup.close();
        });
        buildingManagement.getChildren().add(removeButton);

        layout.getChildren().addAll(buildingManagement, peopleManagement);
        Scene scene = new Scene(layout);
        
        // ===== 加载 CSS =====
        try {
            scene.getStylesheets().add(
                getClass().getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS 加载失败，使用默认样式", e);
        }
        
        popup.setScene(scene);

        // 窗口打开期间，每隔一秒刷新一下显示的数字
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                if (building.getFunctions().contains(BuildingFunction.LIVING)) {
                    inhabitantsLabel.setText("🏠 居民: " + building.getNumberInhabitants() + "/" + building.getMaxInhabitants());
                }
                if (building.getFunctions().contains(BuildingFunction.WORKING)) {
                    workersLabel.setText("👷 工人: " + building.getNumberWorkers() + "/" + building.getMaxWorkers());
                }
                if (building.needViewUpdate()) {
                    stateLabel.setText("状态: " + Names.stateName(building.getState()));
                }
                if (building.getState() == States.CONSTRUCTION) {
                    constructionProgressBar.setVisible(true);
                    double progress = (double) (building.getConstructionTime() - building.getRemainingTime()) / building.getConstructionTime();
                    constructionProgressBar.setProgress(progress);
                } else {
                    constructionProgressBar.setVisible(false);
                }
            })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        popup.setOnCloseRequest(event -> timeline.stop());
        popup.showAndWait();
    }

    /**
     * 新建一行：两个小人图（男 + 女）+ 文字标签。
     *
     * @param residents true 表示居民小人，false 表示工人小人
     * @param label     要放在小人图右边的文字标签（比如「🏠 居民: 2/4」）
     * @return 小人图在左、文字在右的 {@link HBox}
     */
    private HBox createPeopleRow(boolean residents, Label label) {
        String malePath = residents
            ? "/tfgirls/project/javarts/icons/people/male_resident.png"
            : "/tfgirls/project/javarts/icons/people/male_worker.png";
        String femalePath = residents
            ? "/tfgirls/project/javarts/icons/people/female_resident.png"
            : "/tfgirls/project/javarts/icons/people/female_worker.png";

        javafx.scene.image.ImageView maleView =
            new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResource(malePath).toExternalForm()));
        maleView.setFitWidth(22);
        maleView.setFitHeight(22);
        javafx.scene.image.ImageView femaleView =
            new javafx.scene.image.ImageView(new javafx.scene.image.Image(getClass().getResource(femalePath).toExternalForm()));
        femaleView.setFitWidth(22);
        femaleView.setFitHeight(22);

        HBox row = new HBox(5);
        row.getChildren().addAll(maleView, femaleView, label);
        return row;
    }
}
