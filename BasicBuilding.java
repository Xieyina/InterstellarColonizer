package tfgirls.project.javarts.Model.Building;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfgirls.project.javarts.Model.Building.State.*;
import tfgirls.project.javarts.Exception.WrongBuildingType;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Size;

import java.util.*;

public class BasicBuilding implements Building {
    private static final Logger LOG = LoggerFactory.getLogger(BasicBuilding.class);
    private final UUID id;
    private final Map<ResourceType, Integer> cost;
    private final Size size;
    private final Position position;
    private final BuildingType type;
    private final ArrayList<BuildingFunction> functions = new ArrayList<>();
    private final String name;
    private final int constructionTime;
    private final Automata buildingState = new Automata();
    private int stateCycleRemaining;
    private boolean stateChanged = false;
    private Random rand = new Random();

    // ===== 猎人工会攒了多少猎物（只有猎人工会用，别的建筑都是 0） =====
    private int preyStock = 0;

    public BasicBuilding(Position pos, Size s, String name, BuildingType type, 
                         Map<ResourceType, Integer> cost, int constructionTime) {
        this.id = UUID.randomUUID();
        this.position = pos;
        this.size = s;
        this.name = name;
        this.type = type;
        this.cost = cost;
        this.constructionTime = constructionTime;
        stateCycleRemaining = constructionTime;
        buildingState.setCurrentState(new ConstructionState(buildingState));
    }

    @Override
    public Size getSize() {
        return size;
    }

    @Override
    public Map<ResourceType, Integer> getCost() {
        return cost;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public int getConstructionTime() {
        return constructionTime;
    }

    @Override
    public int getRemainingTime() {
        return stateCycleRemaining;
    }

    @Override
    public BuildingType getType() {
        return type;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public ArrayList<BuildingFunction> getFunctions() {
        return functions;
    }

    @Override
    public void addFunction(ArrayList<BuildingFunction> functions) {
        this.functions.addAll(functions);
    }

    @Override
    public Map<ResourceType, Integer> getDailyConsumption() {
        throw new WrongBuildingType("该建筑不消耗资源");
    }

    @Override
    public List<People> getWorkers() {
        throw new WrongBuildingType("该建筑没有工人岗位");
    }

    @Override
    public int getMaxWorkers() {
        throw new WrongBuildingType("该建筑没有工人岗位");
    }

    @Override
    public int getNumberWorkers() {
        throw new WrongBuildingType("该建筑没有工人岗位");
    }

    @Override
    public void addWorker(People people) {
        throw new WrongBuildingType("该建筑没有工人岗位");
    }

    @Override
    public void removeWorker(People people) {
        throw new WrongBuildingType("该建筑没有工人岗位");
    }

    @Override
    public Map<ResourceType, Integer> getDailyProduction() {
        throw new WrongBuildingType("该建筑不生产资源");
    }

    @Override
    public List<People> getInhabitants() {
        throw new WrongBuildingType("该建筑不能住人");
    }

    @Override
    public int getMaxInhabitants() {
        throw new WrongBuildingType("该建筑不能住人");
    }

    @Override
    public int getNumberInhabitants() {
        throw new WrongBuildingType("该建筑不能住人");
    }

    @Override
    public void addInhabitant(People people) {
        throw new WrongBuildingType("该建筑不能住人");
    }

    @Override
    public void removeInhabitant(People people) {
        throw new WrongBuildingType("该建筑不能住人");
    }

    @Override
    public HashMap<ResourceType, Integer> handle() {
        handleState();
        return new HashMap<>();
    }

    private void handleState() {
        switch (buildingState.getCurrentStateName()) {
            case CONSTRUCTION:
                if (stateCycleRemaining == 0) {
                    buildingState.getCurrentState().running();
                    stateChanged = true;
                    LOG.info("{} switched to running state", name);
                    stateCycleRemaining = -1;
                } else {
                    stateChanged = false;
                    stateCycleRemaining--;
                }
                break;
            case RUNNING:
                if (rand.nextInt(1000) == 0) {
                    buildingState.getCurrentState().broken();
                    stateChanged = true;
                    LOG.warn("{} broken", name);
                    stateCycleRemaining = -1;
                } else {
                    stateChanged = false;
                }
                break;
            case BOOSTED:
                if (stateCycleRemaining == 0) {
                    if (rand.nextInt(4) == 0) {
                        buildingState.getCurrentState().broken();
                        LOG.warn("{} broken after boost", name);
                    } else {
                        buildingState.getCurrentState().running();
                        LOG.info("{} boost ended, returning to running", name);
                    }
                    stateChanged = true;
                    stateCycleRemaining = -1;
                } else {
                    stateChanged = false;
                    stateCycleRemaining--;
                }
                break;
            default:
                stateChanged = false;
                break;
        }
    }

    public void switchState(States state, int numberOfCycles) {
        switch (state) {
            case RUNNING:
                buildingState.getCurrentState().running();
                stateChanged = true;
                stateCycleRemaining = -1;
                break;
            case BOOSTED:
                buildingState.getCurrentState().boost();
                stateChanged = true;
                stateCycleRemaining = numberOfCycles;
                break;
            case BROKEN:
                buildingState.getCurrentState().broken();
                stateChanged = true;
                stateCycleRemaining = -1;
                break;
            case BLOCKED:
                buildingState.getCurrentState().blocked();
                stateChanged = true;
                stateCycleRemaining = -1;
                break;
            default:
                break;
        }
    }

    @Override
    public boolean needViewUpdate() {
        return stateChanged;
    }

    public States getState() {
        return buildingState.getCurrentStateName();
    }

    @Override
    public String getName() {
        return name;
    }

    // ===== 猎人工会：交了猎物再算钱 =====
    @Override
    public void deliverPrey(int preyCount) {
        if (preyCount > 0) {
            this.preyStock += preyCount;
        }
    }

    @Override
    public int getPreyStock() {
        return preyStock;
    }

    /** 每天算账时才调用：把攒的猎物拿出来，同时清空库存。 */
    public int consumePreyStock() {
        int stock = preyStock;
        preyStock = 0;
        return stock;
    }
}