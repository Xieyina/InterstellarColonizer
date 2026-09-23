package tfgirls.project.javarts.Model.Building;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Size;

public interface Building {
    Size getSize();
    Map<ResourceType, Integer> getCost();
    UUID getId();
    int getConstructionTime();
    int getRemainingTime();
    BuildingType getType();
    Position getPosition();
    ArrayList<BuildingFunction> getFunctions();
    void addFunction(ArrayList<BuildingFunction> functions);
    Map<ResourceType, Integer> getDailyConsumption();
    List<People> getWorkers();
    int getMaxWorkers();
    int getNumberWorkers();
    void addWorker(People people);
    void removeWorker(People people);
    Map<ResourceType, Integer> getDailyProduction();
    List<People> getInhabitants();
    int getMaxInhabitants();
    int getNumberInhabitants();
    void addInhabitant(People people);
    void removeInhabitant(People people);
    HashMap<ResourceType, Integer> handle();
    String getName();
    States getState();
    boolean needViewUpdate();
    void switchState(States state, int numberOfCycles);

    // ===== 猎人工会：交猎物再算钱相关方法 =====
    /** 把猎物交给建筑，攒起来（只有猎人工会真会用）。 */
    void deliverPrey(int preyCount);
    /** @return 目前攒了多少猎物（还没算成食物的部分）。 */
    int getPreyStock();
    /** 把攒的猎物全拿出来并清空，每天算账时 BuildingManager 才会调这个。 */
    int consumePreyStock();
}