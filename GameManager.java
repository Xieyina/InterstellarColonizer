package tfgirls.project.javarts.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfgirls.project.javarts.Exception.*;
import tfgirls.project.javarts.Model.Building.*;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.Event.EventManager;
import tfgirls.project.javarts.Model.Event.GameEvent;
import tfgirls.project.javarts.Model.Port.GameClock;
import tfgirls.project.javarts.Model.Port.GameUiBridge;
import tfgirls.project.javarts.Model.Resource.RecipeRegistry;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Tech.TechTree;

import java.util.*;

/**
 * 负责游戏数据和逻辑的那一层。
 * 整个游戏只有一个（整个程序共用这同一个，外面不能直接新建）。
 */
public class GameManager implements Subject {
    private static final Logger LOG = LoggerFactory.getLogger(GameManager.class);

    // ===== 改动：核心循环时间缩放 =====
    // 新需求：每 6 现实秒推进一个游戏日（6 秒 = 1 游戏日，节奏比原来 10 秒一天更快）。
    // 所有生产/消耗都是「按天算」，把每天的间隔设为 6 秒即可。
    public static final int TIME_SCALE = 6;
    public static final long DAY_DURATION_MS = 1000L * TIME_SCALE; // 6_000ms = 6 秒

    // ===== 改动：饥饿惩罚相关常量 =====
    // 食物归零时触发：干活效率掉多少（这里是 -50%）。
    public static final double HUNGER_EFFICIENCY = 0.5;
    // 饥饿时每天少几个人（慢慢少，不是一下子全没）。
    public static final int HUNGER_POPULATION_LOSS_PER_DAY = 1;

    private static GameManager instance;
    private BuildingManager buildings = new BuildingManager();
    private List<People> worldInhabitants;
    private Map map;
    private ResourceManager resources;
    private Set<Runnable> listener;
    public Exception currentException;
    private Runnable errorListener;
    // ===== 改动：把 JavaFX 的依赖拆出去 =====
    // 时钟和 UI 桥接由界面层传进来（GameClock / GameUiBridge），
    // 游戏数据和逻辑那层不再直接用 Timeline / Platform / Alert 和 View 类。
    private static GameClock clock;
    private static GameUiBridge ui;
    private final BuildingBuilder b = new BuildingBuilder();

    // ===== 新加的字段 =====
    private TechTree techTree;
    private EventManager eventManager;
    private GameEvent pendingEvent;
    private Object pendingEventContext;
    private List<HistoricalData> history = new ArrayList<>();
    // ===== 改动：天数从第 1 天开始（开局就显示第 1 天，不再从第 0 天开始） =====
    private int dayCounter = 1;
    private boolean eventDialogShowing = false;
    private boolean gameOver = false;
    private boolean paused = false;
    // ===== 改动：有没有过人口 =====
    // 开局本来就没有人，不能一开局就判「全员死亡」；
    // 只有曾经有过人、后来死光了，才算游戏失败。
    private boolean hadPopulation = false;

    // ===== 改动：饥饿惩罚的运行状态 =====
    private boolean hungerActive = false;
    private double productionEfficiency = 1.0; // 全局干活效率倍率（饿的时候是 0.5）
    private int hungerWarningDay = -1;           // 避免每天重复弹窗，按天来控制

    /**
     * 私有构造方法（整个程序共用这同一个，外面不能直接新建，不能随便 new）
     */
    private GameManager() {
        if (clock == null) {
            throw new IllegalStateException("GameManager.bootstrap(...) must be called before getInstance()");
        }
        resources = ResourceManager.getInstance();
        worldInhabitants = new ArrayList<>();
        listener = new HashSet<>();
        map = Map.getInstance();
        techTree = new TechTree();
        eventManager = new EventManager();
        loop();
    }

    /**
     * 传入 {@link GameClock} 和 {@link GameUiBridge} 的具体实现。
     * 必须在第一次调用 {@link #getInstance()} 之前执行（由 View 层启动时做）。
     */
    public static synchronized void bootstrap(GameClock c, GameUiBridge u) {
        clock = c;
        ui = u;
    }

    /**
     * 游戏主循环
     * 弄一个定时器，每个周期去调建筑管理器的处理方法，让每个建筑干活。
     * 干完活通知界面更新。
     * 如果资源不够，就通知错误监听器，好让界面上显示错误。
     */
    private void loop() {
        // 改动：每天的间隔从 1000ms 改成 DAY_DURATION_MS（6_000ms = 6 秒）。
        clock.start(DAY_DURATION_MS, () -> {
            try {
                // 弹窗开着的时候暂停每天推进，免得弹窗套弹窗
                if (eventDialogShowing) return;
                LOG.debug("World inhabitants = {}", worldInhabitants.size());
                buildings.handle();

                // ===== 新加：跑配方转化 =====
                RecipeRegistry.getInstance().processAll();

                // ===== 新加：看看有没有事件 =====
                if (!eventDialogShowing && !gameOver) {
                    eventManager.checkAndTrigger(this);
                }

                // ===== 新加：每10天记一次历史 =====
                dayCounter++;
                if (dayCounter % 10 == 0) {
                    recordHistory();
                }

                // ===== 改动：先更新人物的疲劳度，再看食物够不够来算饥饿惩罚 =====
                updateFatigue();
                checkFoodAndApplyHunger();

                notifyListener();
            } catch (NotEnoughResources ex) {
                notifyErrorListener(ex);
            }
        });
    }

    /**
     * 整个程序共用这同一个：如果没有就新建一个，有了就返回那个。
     *
     * @return {@link GameManager} 共用的那一个，用来操作游戏数据
     */
    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * 在指定位置建一个指定类型的建筑。
     */
    public void addBuilding(BuildingType type, Position position) {
        if (type == null) {
            notifyErrorListener(new WrongBuildingType("尚未选择要建造的建筑类型。"));
            return;
        }
        // ===== 新加：检查科技是不是开放了这个建筑 =====
        if (!techTree.isBuildingUnlocked(type) && !isStarterBuilding(type)) {
            notifyErrorListener(new WrongBuildingType("该建筑需要先研究科技：" + type));
            return;
        }
        Building building = b.build(type, position);
        LOG.debug("Building type: {}", building.getType());
        if (map.isAreaFree(position, building.getSize())) {
            try {
                LOG.debug("Adding building {} to position {}", type, position);
                buildings.addBuilding(building);
                map.construct(building.getPosition(), building.getSize());
            } catch (NotEnoughResources e) {
                notifyErrorListener(e);
            }
        } else {
            notifyErrorListener(new NotEnoughSpace("该地块已经被占用"));
        }
        notifyListener();
    }

    // ===== 新加：判断是不是一开始就能造的建筑 =====
    private boolean isStarterBuilding(BuildingType type) {
        return type == BuildingType.WOODENCABIN || 
               type == BuildingType.HOUSE || 
               type == BuildingType.FARM ||
               type == BuildingType.HUNTERGUILD;
    }

    /**
     * 拆掉指定位置的建筑。
     */
    public void removeBuilding(Building building) {
        if (!buildings.exists(building)) {
            throw new RuntimeException("cannot remove building " + building);
        }
        map.destruct(building.getPosition(), building.getSize());
        buildings.removeBuilding(building);
        while (!building.getInhabitants().isEmpty()) {
            deleteInhabitantFrom(building);
        }
        while (!building.getWorkers().isEmpty()) {
            deleteWorkerFrom(building);
        }
        notifyListener();
    }

    /**
     * 先看建筑存不存在，不存在就报错。
     * 再看建筑是不是坏了的状态，没坏就不用修。
     * 都没问题的话，试着修，调 {@link BuildingManager} 来干。
     * 资源不够也会报错。
     */
    public void repairBuilding(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
            return;
        }
        if (building.getState() != States.BROKEN) {
            notifyErrorListener(new WrongState("该建筑不需要维修。"));
            return;
        }
        try {
            buildings.repairBuilding(building);
            notifyListener();
        } catch (NotEnoughResources e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 先看建筑存不存在，不存在就报错。
     * 再看建筑是不是在运行中，不是的话不能加速。
     * 都没问题的话，试着加速，调 {@link BuildingManager} 来干。
     * 资源不够也会报错。
     */
    public void boostBuilding(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
            return;
        }
        if (building.getState() != States.RUNNING) {
            notifyErrorListener(new WrongState("当前状态（" + building.getState() + "）的建筑不能加速"));
            return;
        }
        try {
            buildings.boostBuilding(building);
            notifyListener();
        } catch (NotEnoughResources e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 先看建筑存不存在，不存在就报错。
     * 再看建筑是不是在运行中或加速中，不是的话不能暂停。
     * 都没问题的话，试着暂停，调 {@link BuildingManager} 来干。
     * 资源不够也会报错。
     */
    public void blockBuilding(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
        }
        if (building.getState() != States.RUNNING && building.getState() != States.BOOSTED) {
            notifyErrorListener(new WrongState("当前状态（" + building.getState() + "）的建筑不能暂停"));
        }
        try {
            buildings.blockBuilding(building);
            notifyListener();
        } catch (NotEnoughResources e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 先看建筑存不存在，不存在就报错。
     * 都没问题的话，试着让建筑运行起来，调 {@link BuildingManager} 来干。
     * 资源不够也会报错。
     */
    public void runBuilding(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
        }
        try {
            buildings.runBuilding(building);
            notifyListener();
        } catch (NotEnoughResources e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 新建一个居民，安排到建筑里住。
     */
    public void createInhabitantInto(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
            return;
        }
        People people = new People();
        try {
            worldInhabitants.add(people);
            buildings.addInhabitantInto(building, people);
            hadPopulation = true;
            notifyListener();
        } catch (TooManyInhabitants | WrongState e) {
            worldInhabitants.remove(people);
            notifyErrorListener(e);
        }
    }

    /**
     * 找到建筑里的第一个居民，让他搬走。
     */
    public void deleteInhabitantFrom(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
            return;
        }
        try {
            if (building.getInhabitants().isEmpty()) {
                throw new NotEnoughInhabitants("该建筑里没有居民");
            }
            People people = building.getInhabitants().getFirst();
            if (people.getJobPlace() != null) {
                people.affectJobPlace(null);
                building.removeWorker(people);
            }
            building.removeInhabitant(people);
            people.affectHouse(null);
            worldInhabitants.remove(people);
            notifyListener();
        } catch (NotEnoughInhabitants | WrongBuildingType e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 在城里找一个没活干的人，安排到建筑里上班。
     */
    public void assignWorkerTo(Building building) {
        try {
            People worker = findUnemployed();
            if (building.getMaxWorkers() > building.getNumberWorkers()) {
                building.addWorker(worker);
                worker.affectJobPlace(building);
                notifyListener();
            }
        } catch (TooManyWorkers | NotEnoughInhabitants | WrongBuildingType e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 在所有居民里找一个没工作的人。
     * 如果人人都上班了，就抛 {@link NotEnoughInhabitants} 异常。
     * 
     * @return 还没上班的 {@link People}
     */
    public People findUnemployed() {
        for (People people : worldInhabitants) {
            if (people.getJobPlace() == null) {
                return people;
            }
        }
        throw new NotEnoughInhabitants("城里没有空闲的人了。");
    }

    /**
     * 找到建筑里的第一个工人，让他不干了。
     */
    public void deleteWorkerFrom(Building building) {
        if (!buildings.exists(building)) {
            notifyErrorListener(new WrongBuildingType("该建筑不存在"));
            return;
        }
        try {
            if (building.getWorkers().isEmpty()) {
                throw new NotEnoughWorkers("该建筑里没有工人");
            }
            People people = building.getWorkers().getFirst();
            people.getJobPlace().removeWorker(people);
            people.affectJobPlace(null);
            building.removeWorker(people);
            notifyListener();
        } catch (NotEnoughWorkers | WrongBuildingType e) {
            notifyErrorListener(e);
        }
    }

    /**
     * 减少一些人（食物不够养的时候用）
     *
     * @param count 要去掉的 {@link People} 数量
     */
    public void killPeople(int count) {
        if (worldInhabitants.size() < count) {
            count = worldInhabitants.size();
        }
        int killed = 0;
        while (killed < count) {
            boolean found = false;
            for (Building b : getBuildings()) {
                if (b.getFunctions().contains(BuildingFunction.LIVING) && b.getNumberInhabitants() > 0) {
                    deleteInhabitantFrom(b);
                    killed++;
                    found = true;
                    break;
                }
            }
            if (!found) break;
        }
        // ===== 改动：工人居民全部死亡 -> 游戏失败 =====
        // 开局本来没人不算失败，只有「曾经有过人、现在死光了」才算。
        if (worldInhabitants.isEmpty() && hadPopulation) {
            gameOver = true;
            LOG.warn("游戏失败：所有居民和工人都已死亡");
            clock.stop();          // 停掉时钟：游戏失败后一切操作都不再推进
            ui.showGameOver();     // 弹出失败提示，玩家确认后回到开屏界面
        }
        notifyListener();
    }

    // ===== 改动：人物疲劳度系统（数据层驱动）=====
    /**
     * 每过一天，更新所有人的疲劳状态。
     有活干的人：连续干活天数 +1、更累了；
     没活干（休息）的人：连续干活天数清零、疲劳恢复。
     太累之后，人的走路速度和干活效率（看 People.getEfficiency）会下降，
     这个效率会在建筑管理器算生产的时候按工人效率来算。
     */
    private void updateFatigue() {
        for (People people : worldInhabitants) {
            people.advanceDay();
        }
    }

    // ===== 改动：饥饿惩罚 =====
    /**
     * 每过一天，看看食物还够不够：
     * - 食物 <= 0：开启饥饿惩罚（全局干活效率降到 HUNGER_EFFICIENCY，
     *   而且每天少几个人），同时弹出警告；不会一下子全灭，也不会直接结束游戏。
     * - 食物 > 0：关掉饥饿惩罚，干活效率恢复 1.0。
     * 注意：食物归零的「清零和触发」是在 ResourceManager.addResource 里处理的，
     * 这里只管读食物数量然后开/关惩罚。
     */
    private void checkFoodAndApplyHunger() {
        int food = ResourceManager.getResourceAmount(ResourceType.FOOD);
        if (food <= 0) {
            if (!hungerActive) {
                hungerActive = true;
                productionEfficiency = HUNGER_EFFICIENCY;
                LOG.warn("食物耗尽 -> 饿肚子了（干活效率 -50%，人口慢慢减少）");
            } else {
                productionEfficiency = HUNGER_EFFICIENCY;
            }
            // 人口慢慢减少（不是一下全灭），直到食物恢复或人没了
            if (worldInhabitants.size() > 0) {
                killPeople(HUNGER_POPULATION_LOSS_PER_DAY);
            }
            // 警告按天来弹，免得每一下都弹
            if (dayCounter != hungerWarningDay) {
                hungerWarningDay = dayCounter;
                showHungerWarning();
            }
        } else {
            if (hungerActive) {
                hungerActive = false;
                productionEfficiency = 1.0;
                LOG.info("食物恢复了 -> 不再饿肚子了");
            }
        }
    }

    /**
     * 弹出饿肚子警告（食物归零时才弹，按天控制不重复弹）。
     * 说明：项目要求「弹出警告提示」，所以用 JavaFX Alert 做一个最简单的弹窗；
     * 界面其他部分这次没改。
     */
    private void showHungerWarning() {
        ui.showWarning("饥饿警告", "殖民地食物已耗尽！",
                "饿肚子了：干活效率下降 50%，居民开始减少。\n请尽快建农场或者给猎人工会交猎物，恢复食物供应。");
    }

    /** @return 现在是不是在饿肚子（给界面和测试用） */
    public boolean isHungerActive() { return hungerActive; }

    /**
     * @return 全局干活效率倍率。饿肚子时 0.5，正常时 1.0。
     *         跟每个工人的疲劳效率（People.getEfficiency）在算产出时乘在一起。
     */
    public double getProductionEfficiency() { return productionEfficiency; }

    // ===== 改动：猎人工会「按交猎物算」=====
    /**
     * 给城里第一座猎人工会交猎物。猎物存进猎物库存，
     * 每天结算时「按交了多少猎物」变成食物——取代以前自动发钱的机制。
     *
     * @param preyCount 这次交多少只猎物（&gt;0）
     * @return 收下猎物的猎人工会建筑，如果没有猎人工会就返回 null
     */
    public Building deliverPreyToGuild(int preyCount) {
        if (preyCount <= 0) return null;
        for (Building building : getBuildings()) {
            if (building.getType() == BuildingType.HUNTERGUILD) {
                building.deliverPrey(preyCount);
                LOG.info("向猎人工会交付猎物 x{}", preyCount);
                notifyListener();
                return building;
            }
        }
        LOG.warn("当前没有猎人工会，无法交付猎物");
        return null;
    }

    // ===== 新加：科技树相关方法 =====
    public void researchTech(String techId) {
        if (techTree.canResearch(techId)) {
            techTree.research(techId);
            notifyListener();
            // ===== 新加：判断胜利——研究出星际航行就赢了 =====
            if ("INTERSTELLAR_TRAVEL".equals(techId)) {
                ui.showVictory();
            }
        } else {
            notifyErrorListener(new NotEnoughResources("无法研究该科技：" + techId));
        }
    }

    public TechTree getTechTree() { return techTree; }

    // ===== 新加：事件相关方法 =====
    public void setPendingEvent(GameEvent event) {
        this.pendingEvent = event;
        if (event != null) {
            event.trigger(this); // 让事件准备好上下文（比如随机选一个目标建筑）
            if (!eventDialogShowing) {
                showEventDialog(event);
            }
        }
    }

    public void setPendingEventContext(Object context) {
        this.pendingEventContext = context;
    }

    public Object getPendingEventContext() { return pendingEventContext; }

    public void showEventDialog(GameEvent event) {
        if (eventDialogShowing) return;
        eventDialogShowing = true;
        ui.showEvent(event, () -> {
            eventDialogShowing = false;
            pendingEvent = null;
        });
    }

    // ===== 新加：历史数据记录 =====
    private void recordHistory() {
        // 注意：同包下有个游戏地图类也叫 Map，所以这里要写全 java.util.Map
        java.util.Map<ResourceType, Integer> snapshot = new HashMap<>();
        for (ResourceType rt : ResourceType.values()) {
            snapshot.put(rt, ResourceManager.getResourceAmount(rt));
        }
        history.add(new HistoricalData(dayCounter, snapshot, getBuildings().size(), worldInhabitants.size()));
        if (history.size() > 200) {
            history.remove(0);
        }
    }

    public List<HistoricalData> getHistory() { return history; }
    public int getDay() { return dayCounter; }
    public boolean isGameOver() { return gameOver; }

    // ===== 改动：暂停 / 继续 =====
    /**
     * 暂停游戏：每天的推进停下来，建筑也不再干活。
     */
    public void pause() {
        if (paused || gameOver) return;
        paused = true;
        clock.pause();
        LOG.info("游戏已暂停（第 {} 天）", dayCounter);
    }

    /**
     * 从暂停的地方继续游戏。
     */
    public void resume() {
        if (!paused || gameOver) return;
        paused = false;
        clock.resume();
        LOG.info("游戏继续（第 {} 天）", dayCounter);
    }

    /** @return 现在是不是暂停中 */
    public boolean isPaused() { return paused; }

    // ===== 改动：游戏失败后从开屏重新开始 =====
    /**
     * 把整局游戏重置回刚开局的样子（游戏失败后从开屏界面重新开始时用）。
     * 会清掉所有建筑、人口、资源、科技、事件和历史记录，然后重新启动时钟。
     */
    public void resetForNewGame() {
        clock.stop();
        buildings = new BuildingManager();
        worldInhabitants.clear();
        Map.getInstance().resetTiles();
        ResourceManager.resetToInitial();
        RecipeRegistry.getInstance().resetUnlockedRecipes();
        techTree = new TechTree();
        eventManager = new EventManager();
        history.clear();
        dayCounter = 1;
        gameOver = false;
        paused = false;
        hadPopulation = false;
        eventDialogShowing = false;
        pendingEvent = null;
        pendingEventContext = null;
        hungerActive = false;
        productionEfficiency = 1.0;
        hungerWarningDay = -1;
        loop();
        notifyListener();
        LOG.info("游戏已重置，准备重新开始");
    }

    /**
     * @return 当前总人口数
     */
    public int getPopulation() {
        return worldInhabitants.size();
    }

    /**
     * 直接设置当前是第几天（读档的时候用）。
     *
     * @param day 要恢复到第几天
     */
    public void setDay(int day) {
        this.dayCounter = day;
    }

    /**
     * 一下子加一些人（事件奖励或者读档的时候用）。
     * 新来的人会依次住进还有空位的居住类建筑。
     *
     * @param count 要加多少人
     * @return 实际加了多少人
     */
    public int addPopulation(int count) {
        int added = 0;
        for (Building b : buildings.getBuildings()) {
            if (!b.getFunctions().contains(BuildingFunction.LIVING)) continue;
            while (added < count && b.getNumberInhabitants() < b.getMaxInhabitants()) {
                createInhabitantInto(b);
                added++;
            }
            if (added >= count) break;
        }
        if (added > 0) {
            hadPopulation = true;
        }
        notifyListener();
        return added;
    }

    /**
     * 拿到当前所有建筑的集合。
     *
     * @return 所有 {@link Building} 的 {@link Set}
     */
    public Set<Building> getBuildings() {
        return buildings.getBuildings();
    }

    /**
     * 把监听器加到列表里
     *
     * @param o 要加的 {@link Runnable}
     */
    @Override
    public void addListener(Runnable o) {
        listener.add(o);
    }

    @Override
    public void removeListener(Runnable o) {
        listener.remove(o);
    }

    /**
     * 设置错误监听器
     *
     * @param o {@link Runnable}
     */
    @Override
    public void addErrorListener(Runnable o) {
        errorListener = o;
    }

    @Override
    public void removeErrorListener() {
        errorListener = null;
    }

    /** 停掉时钟和监听器，退出游戏的时候用。 */
    public void shutdown() {
        if (clock != null) {
            clock.stop();
        }
        listener.clear();
        errorListener = null;
    }

    /**
     * 通知所有监听器
     */
    @Override
    public void notifyListener() {
        for (Runnable o : listener) {
            ui.dispatch(o);
        }
    }

    /**
     * 用异常通知错误监听器。
     * 异常会存到 {@code currentException} 里，如果设了错误监听器，
     * 就在界面线程上跑它。
     *
     * @param e 要传给错误监听器的 {@link Exception}
     */
    @Override
    public void notifyErrorListener(Exception e) {
        currentException = e;
        if (errorListener != null) {
            ui.dispatch(errorListener);
        }
    }
}