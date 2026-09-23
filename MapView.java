package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import tfgirls.project.javarts.Controller.*;
import tfgirls.project.javarts.Controller.Commands.AddBuildingCommand;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingFunction;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.Map;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Size;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapView extends ScrollPane {
    private static final Logger LOG = LoggerFactory.getLogger(MapView.class);
    // ===== 200x200 的地图：以前是 4 万个地格全建好，现在改成只建看得见的（懒加载）=====
    // 只为屏幕上能看到的地格新建图片，滚出去的就删掉，同一张地格图只建一次。
    // 说明：本来想用 Canvas 来画，但在本机上同时开好几块 Canvas 会报错，
    // 所以退一步用 ImageView 的方式来做。
    private static final int TILE = 50;
    // ===== 改动：每天多少现实秒（跟 GameManager 的 6 秒一天保持一致） =====
    private static final double SECONDS_PER_DAY = 6.0;
    // ===== 改动：小人图标放大的尺寸（原来 16px，现在 22px，更醒目又不会撑出建筑） =====
    private static final double PEOPLE_SIZE = 22;
    /** 每个建筑最多显示的小人个数（居民、工人各算一组）。 */
    private static final int MAX_PEOPLE_PER_BUILDING = 8;
    private final String imagePath = "/tfgirls/project/javarts/buildingSprites/";
    private final int mapWidth;
    private final int mapHeight;
    private final Pane mapPane = new Pane();
    private final Pane tileLayer = new Pane();       // 底层：看得见的地格
    private final Pane buildingLayer = new Pane();   // 上层：建筑的图片
    private final Pane peopleLayer = new Pane();     // 最上层：居民和工人的小人
    private Image tileImage;                         // 同一张地格图反复用，只建一次
    private final HashMap<Long, ImageView> tileNodes = new HashMap<>();
    private HashMap<Building, ArrayList<ArrayList<ImageView>>> buildingSprites = new HashMap<>();
    private Set<Building> currentBuildings = new HashSet<>();
    // ===== 改动：每个建筑「灰变彩」的动画，擦掉建筑时也把动画停掉 =====
    private final HashMap<Building, Timeline> constructionAnimations = new HashMap<>();

    // ===== 改动：四种小人的图片（男女居民、男女工人） =====
    private Image maleResident;
    private Image femaleResident;
    private Image maleWorker;
    private Image femaleWorker;

    /**
     * 新建地图视图。
     * 只有屏幕上看得见的地格才会创建图片（滚到才建，滚走就删）。
     * 鼠标操作保留：悬停时高亮地格，点击空地格会发一个 {@link AddBuildingCommand} 放建筑，
     * 点击已有建筑会弹出建筑详情窗口。
     * 自带滚动条，这样地图比窗口大时也能看到全部。
     */
    public MapView() {
        Size mapSize = Map.getInstance().getSize();
        mapWidth = mapSize.getWidth();
        mapHeight = mapSize.getHeight();
        tileImage = new Image(getClass()
                .getResource("/tfgirls/project/javarts/mapTiles/tile_0002.png").toExternalForm());

        // ===== 改动：加载小人图片 =====
        maleResident = new Image(getClass()
                .getResource("/tfgirls/project/javarts/icons/people/male_resident.png").toExternalForm());
        femaleResident = new Image(getClass()
                .getResource("/tfgirls/project/javarts/icons/people/female_resident.png").toExternalForm());
        maleWorker = new Image(getClass()
                .getResource("/tfgirls/project/javarts/icons/people/male_worker.png").toExternalForm());
        femaleWorker = new Image(getClass()
                .getResource("/tfgirls/project/javarts/icons/people/female_worker.png").toExternalForm());

        mapPane.setPrefSize(mapWidth * TILE, mapHeight * TILE);
        mapPane.getChildren().addAll(tileLayer, buildingLayer, peopleLayer);
        buildingLayer.setPickOnBounds(false);   // 建筑层的空白区域点不到，让点击传到地格层
        tileLayer.setPickOnBounds(false);
        peopleLayer.setPickOnBounds(false);     // 小人只是装饰，不挡鼠标
        this.setContent(mapPane);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 滚动或窗口大小变化时，刷新看得见的地格
        hvalueProperty().addListener((obs, oldV, newV) -> refreshVisibleTiles());
        vvalueProperty().addListener((obs, oldV, newV) -> refreshVisibleTiles());
        widthProperty().addListener((obs, oldV, newV) -> refreshVisibleTiles());
        heightProperty().addListener((obs, oldV, newV) -> refreshVisibleTiles());
        // ===== 改动：视口大小一确定就立刻刷新地格 =====
        // 刚进主界面时窗口还在布局，宽度/高度事件可能先到、视口尺寸还是 0，
        // 光听宽高会漏掉这一次，导致草坪要等滚动一下才出现。
        // 再听 viewportBounds，保证开屏点一下后草坪马上就能画出来。
        viewportBoundsProperty().addListener((obs, oldV, newV) -> refreshVisibleTiles());
        refreshVisibleTiles();
    }

    /**
     * 为当前看得见的范围创建地格图片，滚走的就删掉。
     */
    private void refreshVisibleTiles() {
        double vw = getViewportBounds().getWidth();
        double vh = getViewportBounds().getHeight();
        if (vw <= 0 || vh <= 0) {
            return;
        }
        double offX = getHvalue() * Math.max(0, mapWidth * TILE - vw);
        double offY = getVvalue() * Math.max(0, mapHeight * TILE - vh);
        int col0 = Math.max(0, (int) (offX / TILE));
        int col1 = Math.min(mapWidth - 1, (int) ((offX + vw) / TILE));
        int row0 = Math.max(0, (int) (offY / TILE));
        int row1 = Math.min(mapHeight - 1, (int) ((offY + vh) / TILE));

        Set<Long> wanted = new HashSet<>();
        for (int col = col0; col <= col1; col++) {
            for (int row = row0; row <= row1; row++) {
                long key = (long) col * mapHeight + row;
                wanted.add(key);
                if (!tileNodes.containsKey(key)) {
                    tileNodes.put(key, createTile(col, row));
                }
            }
        }
        for (Long key : new HashSet<>(tileNodes.keySet())) {
            if (!wanted.contains(key)) {
                tileLayer.getChildren().remove(tileNodes.remove(key));
            }
        }
    }

    /**
     * 新建一个地格：鼠标悬停时变亮，点击时触发放建筑。
     */
    private ImageView createTile(int col, int row) {
        ImageView tileImageView = new ImageView(tileImage);
        tileImageView.setFitWidth(TILE);
        tileImageView.setFitHeight(TILE);
        tileImageView.setPreserveRatio(true);
        tileImageView.setX(col * TILE);
        tileImageView.setY(row * TILE);
        tileImageView.setOnMouseEntered(t -> tileImageView.setOpacity(0.5));
        tileImageView.setOnMouseExited(t -> tileImageView.setOpacity(1));
        tileImageView.setOnMouseClicked(event -> {
            BagOfCommands.getInstance().addCommand(new AddBuildingCommand(new Position(col, row)));
            LOG.debug("Tile clicked at X={}, Y={}", col, row);
        });
        tileLayer.getChildren().add(tileImageView);
        return tileImageView;
    }

    /**
     * 把一组建筑画到地图上。
     *
     * 具体做法：
     * - 如果建筑已经在地图上且状态没变，就复用原来的图片。
     * - 如果建筑在地图上但状态变了，就删掉旧图片，用新状态重新画。
     * - 新出现的建筑画上去，图片存到 {@link HashMap} 里以后用。
     * - 已经不在地图上的建筑就擦掉。
     *
     * @param buildings 当前地图上所有 {@link Building}
     */
    public void drawBuildings(Set<Building> buildings) {
        currentBuildings = buildings;
        HashMap<Building, ArrayList<ArrayList<ImageView>>> newBuildings = computeBuildingSprites(buildings);
        removeStaleSprites(newBuildings);
        buildingSprites = newBuildings;
        drawPeople(buildings);
    }

    /**
     * 为所有建筑准备好图片，没变的就复用原来的。
     */
    private HashMap<Building, ArrayList<ArrayList<ImageView>>> computeBuildingSprites(Set<Building> buildings) {
        HashMap<Building, ArrayList<ArrayList<ImageView>>> newBuildings = new HashMap<>();
        for (Building building : buildings) {
            if (buildingSprites.containsKey(building)) {
                if (!building.needViewUpdate()) {
                    newBuildings.put(building, buildingSprites.get(building));
                    continue;
                } else {
                    eraseBuilding(building);
                }
            }
            int buildingHeight = building.getSize().getHeight();
            int buildingWidth = building.getSize().getWidth();
            LOG.debug("Building size: {}x{}", buildingHeight, buildingWidth);
            ArrayList<ArrayList<ImageView>> buildingView = new ArrayList<>();
            for (int col = 0; col < buildingWidth; col++) {
                ArrayList<ImageView> tileImageViews = new ArrayList<>();
                for (int row = 0; row < buildingHeight; row++) {
                    ImageView tileImageView;
                    String path;
                    if (building.getState() != States.RUNNING && row == buildingHeight - 1 && col == 0) {
                        LOG.debug("Building state: {}", building.getState());
                        path = imagePath + building.getType().toString().toLowerCase() + "/" + row + "_"
                                + col + "_" + building.getState().toString().toLowerCase() + ".png";
                        LOG.debug("Loading sprite: {}", path);
                    } else {
                        path = imagePath + building.getType().toString().toLowerCase() + "/" + row + "_"
                                + col + ".png";
                    }
                    tileImageView = new ImageView(getClass().getResource(path).toExternalForm());
                    tileImageView.setFitWidth(TILE);
                    tileImageView.setFitHeight(TILE);
                    tileImageView.setPreserveRatio(true);
                    tileImageView.setX((building.getPosition().getX() + col) * TILE);
                    tileImageView.setY((building.getPosition().getY() + row) * TILE);
                    tileImageView.setOnMouseClicked(event -> new BuildingInfoPopup(building));

                    // ===== 改动：名称悬浮窗（光标放到建筑上时显示半透明名称提示） =====
                    Tooltip nameTip = new Tooltip(building.getName());
                    nameTip.getStyleClass().add("name-tooltip");
                    Tooltip.install(tileImageView, nameTip);

                    buildingLayer.getChildren().add(tileImageView);
                    tileImageViews.add(tileImageView);
                }
                buildingView.add(tileImageViews);
            }
            newBuildings.put(building, buildingView);

            // ===== 改动：建造中的房屋由灰色渐变为彩色的动画 =====
            if (building.getState() == States.CONSTRUCTION) {
                startConstructionFade(building, buildingView);
            }
        }
        return newBuildings;
    }

    /**
     * 建造中的建筑：先整体变灰（饱和度 -1），
     * 然后在整个建造期里慢慢恢复成彩色（饱和度回到 0）。
     * 建造时长按「每天 6 秒」换算成现实时间。
     */
    private void startConstructionFade(Building building, ArrayList<ArrayList<ImageView>> buildingView) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setSaturation(-1.0);
        for (ArrayList<ImageView> row : buildingView) {
            for (ImageView sprite : row) {
                sprite.setEffect(colorAdjust);
            }
        }
        double totalSeconds = Math.max(2.0, building.getRemainingTime() * SECONDS_PER_DAY);
        Timeline fade = new Timeline(
            new KeyFrame(Duration.seconds(totalSeconds),
                new KeyValue(colorAdjust.saturationProperty(), 0.0)));
        fade.setOnFinished(e -> {
            constructionAnimations.remove(building);
            LOG.debug("建造完成，{} 恢复彩色", building.getName());
        });
        constructionAnimations.put(building, fade);
        fade.play();
    }

    /**
     * 把已经不在地图上的建筑的图片删掉。
     */
    private void removeStaleSprites(HashMap<Building, ArrayList<ArrayList<ImageView>>> newBuildings) {
        for (Building building : buildingSprites.keySet()) {
            if (!newBuildings.containsKey(building)) {
                eraseBuilding(building);
            }
        }
    }

    /**
     * 从地图上擦掉指定的建筑。
     *
     * @param building 要删掉的那个 {@link Building}
     */
    public void eraseBuilding(Building building) {
        Timeline anim = constructionAnimations.remove(building);
        if (anim != null) {
            anim.stop();
        }
        ArrayList<ArrayList<ImageView>> sprites = buildingSprites.get(building);
        if (sprites == null) {
            return;
        }
        int buildingHeight = building.getSize().getHeight();
        int buildingWidth = building.getSize().getWidth();
        for (int col = 0; col < buildingWidth; col++) {
            for (int row = 0; row < buildingHeight; row++) {
                buildingLayer.getChildren().remove(sprites.get(col).get(row));
            }
        }
    }

    // ===== 改动：在地图上画小人（男女居民、男女工人） =====
    /**
     * 把每个建筑里的居民和工人画成小人图标，放在建筑上：
     * - 居民用男/女居民小人（按每个人的性别选图）
     * - 工人用男/女工人小人
     * 居民排在上面几行，工人紧挨着排在居民下面（避免两组小人叠在一起）。
     * 居民、工人各自最多显示 {@link #MAX_PEOPLE_PER_BUILDING} 个，多余的不画。
     */
    private void drawPeople(Set<Building> buildings) {
        peopleLayer.getChildren().clear();
        for (Building building : buildings) {
            boolean living = building.getFunctions().contains(BuildingFunction.LIVING);
            boolean working = building.getFunctions().contains(BuildingFunction.WORKING);
            if (!living && !working) {
                continue;
            }
            // 只有真的有对应功能的建筑才能问它要居民/工人（否则会抛异常）
            List<People> inhabitants = living ? building.getInhabitants() : new ArrayList<>();
            List<People> workers = working ? building.getWorkers() : new ArrayList<>();
            drawPeopleOfBuilding(building, inhabitants, false, 0);
            // 工人从居民下面一行开始排，错开两组小人
            int residentRows = (int) Math.ceil(
                Math.min(inhabitants.size(), MAX_PEOPLE_PER_BUILDING) / (double) perRowOf(building));
            drawPeopleOfBuilding(building, workers, true, residentRows);
        }
    }

    /** 一个建筑一排最多能放下几个小人。 */
    private int perRowOf(Building building) {
        // 建筑宽度（像素）两边各留 2px 边距，再按小人尺寸算能放几个
        return Math.max(1, (int) ((building.getSize().getWidth() * TILE - 4) / PEOPLE_SIZE));
    }

    /**
     * 把一个建筑里的居民或工人画成一排排小人。
     *
     * @param building 小人所在的建筑
     * @param people   要画的居民或工人
     * @param workers  true 表示画工人，false 表示画居民
     * @param startRow 从第几行开始画（居民从 0 开始，工人接在居民下面）
     */
    private void drawPeopleOfBuilding(Building building, List<People> people, boolean workers, int startRow) {
        if (people == null || people.isEmpty()) {
            return;
        }
        int shown = Math.min(people.size(), MAX_PEOPLE_PER_BUILDING);
        double startX = building.getPosition().getX() * TILE + 2;
        double startY = building.getPosition().getY() * TILE + 2;
        int perRow = perRowOf(building);
        for (int i = 0; i < shown; i++) {
            People p = people.get(i);
            boolean male = p.getGender() == People.Gender.MALE;
            Image img = workers
                ? (male ? maleWorker : femaleWorker)
                : (male ? maleResident : femaleResident);
            ImageView view = new ImageView(img);
            view.setPreserveRatio(true);
            view.setFitHeight(PEOPLE_SIZE);
            int row = startRow + i / perRow;
            view.setX(startX + (i % perRow) * PEOPLE_SIZE);
            view.setY(startY + row * PEOPLE_SIZE);
            // 小人也有名称提示（半透明悬浮窗）
            Tooltip tip = new Tooltip(workers ? (male ? "男工人" : "女工人") : (male ? "男居民" : "女居民"));
            tip.getStyleClass().add("name-tooltip");
            Tooltip.install(view, tip);
            peopleLayer.getChildren().add(view);
        }
    }
}
