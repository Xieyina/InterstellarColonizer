package tfgirls.project.javarts.Model.Building;

import tfgirls.project.javarts.Exception.NotEnoughWorkers;
import tfgirls.project.javarts.Exception.TooManyWorkers;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.*;

public class WorkingBuilding extends BuildingDecorator{
    private final List<People> workers;
    private final int maxWorkers;
    Building b;

    public WorkingBuilding(Building b, int maxWorkers) {
        super(b);
        workers = new ArrayList<>();
        this.maxWorkers = maxWorkers;
        this.b = b;
    }

    @Override
    public List<People> getWorkers() {
        return workers;
    }
    @Override
    public int getMaxWorkers() {
        return maxWorkers;
    }

    @Override
    public int getNumberWorkers(){
        return workers.size();
    }
    @Override
    public void addWorker(People people){
        if (b.getState() == States.CONSTRUCTION) return;
        if(getNumberWorkers()<maxWorkers){
            workers.add(people);
        }else{
            throw new TooManyWorkers("该建筑的工人已经满了。");
        }
    }
    @Override
    public void removeWorker(People people){
        if (b.getState() == States.CONSTRUCTION) return;
        if(getNumberWorkers()>0){
            workers.remove(people);
        }else{
            throw new NotEnoughWorkers("该建筑里没有工人：" + b.getName());
        }
    }

    @Override
    public void addFunction(ArrayList<BuildingFunction> functions){
        functions.add(BuildingFunction.WORKING);
        super.addFunction(functions);
    }

    @Override
    public List<People> getInhabitants() {
        return b.getInhabitants();
    }

    @Override
    public int getMaxInhabitants() {
        return b.getMaxInhabitants();
    }

    @Override
    public int getNumberInhabitants(){
        return b.getNumberInhabitants();
    }

    @Override
    public void addInhabitant(People people){
        b.addInhabitant(people);
    }
    @Override
    public void removeInhabitant(People people){
        b.removeInhabitant(people);
    }

    @Override
    public Map<ResourceType, Integer> getDailyConsumption() {
        return b.getDailyConsumption();
    }

    @Override
    public Map<ResourceType, Integer> getDailyProduction() {
        return b.getDailyProduction();
    }

    @Override
    public HashMap<ResourceType, Integer> handle(){
        return b.handle();
    }

}
