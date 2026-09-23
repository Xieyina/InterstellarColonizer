package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tfgirls.project.javarts.Controller.BagOfCommands;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Controller.Commands.SetSelectedBuildingCommand;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingManager;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Tech.TechTree;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 应用的主界面，把不同的功能分给其他类来管。
 */
public class MainView implements Observer {
    private static final Logger LOG = LoggerFactory.getLogger(MainView.class);

    private Controller controller;
    private final ArrayList<BuildingCard> cards = new ArrayList<>();
    private final CustomMenu topContainer;
    private final MapView map;
    private final GameManager model;
    private final VBox footer;
    private final BuildingFooter buildingFooter;
    private final PeopleFooter peopleFooter;
    private BorderPane root;
    private String footerState = "building";

    /**
     * 新建应用的主窗口
     *
     * @param stage 应用的窗口 {@link Stage}
     * @param model {@link GameManager}，主界面需要知道游戏数据，所以存一份
     */
    public MainView(Stage stage, GameManager model) {
        stage.setTitle("星际殖民者");
        this.model = model;
        topContainer = new CustomMenu();
        footer = new VBox();
        map = new MapView();
        root = new BorderPane();
        buildingFooter = new BuildingFooter();
        peopleFooter = new PeopleFooter();
        root.setTop(topContainer);
        root.setCenter(map);
        root.setBottom(footer);
        
        HBox modeSelection = new HBox();
        modeSelection.setSpacing(10);
        
        Button buildingModeButton = new Button("🏗️ 建筑");
        buildingModeButton.onMouseClickedProperty().setValue(event -> {
            switchEditionMode("building");
        });
        Button peopleModeButton = new Button("👥 人员");
        peopleModeButton.onMouseClickedProperty().setValue(event -> {
            switchEditionMode("people");
        });
        
        // ===== 科技树按钮 =====
        Button techButton = new Button("🔬 科技树");
        techButton.setOnAction(e -> {
            TechView techView = new TechView(model.getTechTree());
            techView.show(stage);
        });

        // ===== 暂停 / 继续按钮（点一下暂停，再点一下继续） =====
        Button pauseResumeButton = new Button();
        Runnable refreshPauseText = () -> {
            if (model.isPaused()) {
                pauseResumeButton.setText("▶️ 继续");
            } else {
                pauseResumeButton.setText("⏸️ 暂停");
            }
        };
        refreshPauseText.run();
        pauseResumeButton.setOnAction(e -> {
            if (model.isPaused()) {
                model.resume();
            } else {
                model.pause();
            }
            refreshPauseText.run();
        });
        model.addListener(refreshPauseText);

        modeSelection.getChildren().addAll(buildingModeButton, peopleModeButton, techButton, pauseResumeButton);
        footer.getChildren().add(modeSelection);
        footer.getChildren().add(buildingFooter);
        
        peopleFooter.prefHeightProperty().bind(stage.heightProperty().multiply(0.30));
        peopleFooter.setMinHeight(50);
        peopleFooter.setMaxHeight(Double.MAX_VALUE);
        buildingFooter.prefViewportHeightProperty().bind(stage.heightProperty().multiply(0.30));
        buildingFooter.setMinHeight(50);
        buildingFooter.setMaxHeight(Double.MAX_VALUE);
        
        // ===== 改动：底部建筑栏去掉公寓和猎人工会 =====
        java.util.Set<BuildingType> hiddenTypes = java.util.Set.of(
            BuildingType.APPARTMENTBUILDING, BuildingType.HUNTERGUILD);

        for (BuildingType buildingType : BuildingType.values()) {
            if (hiddenTypes.contains(buildingType)) {
                continue;
            }
            BuildingCard b = new BuildingCard(buildingType);
            cards.add(b);
            b.setOnMouseClicked(event -> {
                BagOfCommands.getInstance().addCommand(new SetSelectedBuildingCommand(buildingType));
            });
            buildingFooter.addWidget(b);
        }

        model.addListener(this::update);
        model.addErrorListener(this::updateError);

        setAvailability();
        Scene scene = new Scene(root, 1280, 720);
        // 按 ESC 键退出游戏
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            }
        });
        
        // ===== 加载 CSS =====
        try {
            scene.getStylesheets().add(
                getClass().getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS not found, using default styling");
        }
        
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 给主界面设置控制器
     * @param controller
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * 在建筑卡片里选中指定的建筑
     *
     * @param buildingType
     */
    public void setSelectedBuilding(BuildingType buildingType) {
        for (BuildingCard b : cards) {
            b.setSelected(buildingType);
        }
    }

    /**
     * 在人员底栏里选中指定的建筑。
     *
     * @param building 用户选中的建筑
     */
    public void setSelectedBuildingInfo(Building building) {
        peopleFooter.setSelectedBuildingInfo(building);
    }

    /**
     * 遍历底部栏里所有建筑卡片：
     * 如果资源不够建造这个建筑，就把卡片变暗（透明度 0.4）；
     * 够的话就正常显示（透明度 1）。
     */
    public void setAvailability() {
        for (BuildingCard b : cards) {
            // ===== 检查科技是否解锁了该建筑 =====
            TechTree techTree = model.getTechTree();
            BuildingType type = b.getBuildingType();
            boolean isUnlocked = techTree.isBuildingUnlocked(type) || 
                                 type == BuildingType.WOODENCABIN || 
                                 type == BuildingType.HOUSE || 
                                 type == BuildingType.FARM;
            if (!isUnlocked) {
                b.setOpacity(0.2);
                b.setDisable(true);
            } else if (!BuildingManager.isBuildable(b.getBuildingType())) {
                b.setOpacity(0.4);
                b.setDisable(false);
            } else {
                b.setOpacity(1);
                b.setDisable(false);
            }
        }
    }

    /**
     * 根据参数切换底栏：显示建筑底栏还是人员底栏。
     *
     * @param mode 要切换到的模式
     */
    public void switchEditionMode(String mode) {
        if (Objects.equals(mode, "building")) {
            if (!footerState.equals("building")) {
                footer.getChildren().remove(peopleFooter);
                footer.getChildren().add(buildingFooter);
                footerState = "building";
            }
        } else {
            if (!footerState.equals("people")) {
                footer.getChildren().remove(buildingFooter);
                footer.getChildren().add(peopleFooter);
                footerState = "people";
            }
        }
    }

    @Override
    public void update() {
        topContainer.actualiseResources();
        map.drawBuildings(model.getBuildings());
        peopleFooter.updateBuildings(model.getBuildings());
        setAvailability();
    }

    /**
     * 把报错信息显示在顶部菜单栏里
     */
    public void updateError() {
        topContainer.showError(model.currentException.getMessage());
    }
}
