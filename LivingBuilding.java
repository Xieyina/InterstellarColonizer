package tfgirls.project.javarts.Model.Building;

import tfgirls.project.javarts.Exception.NotEnoughInhabitants;
import tfgirls.project.javarts.Exception.TooManyInhabitants;
import tfgirls.project.javarts.Exception.WrongState;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.*;

public class LivingBuilding extends BuildingDecorator{
    private final List<People> inhabitants = new ArrayList<>();
    private final int maxInhabitants;
    Building b;

    public LivingBuilding(Building b, int maxInhabitants) {
        super(b);
        this.maxInhabitants = maxInhabitants;
        this.b = b;
    }

    @Override
    public List<People> getInhabitants() {
        return inhabitants;
    }

    @Override
    public int getMaxInhabitants() {
        return maxInhabitants;
    }
    @Override
    public int getNumberInhabitants(){
        return inhabitants.size();
    }
    @Override
    public void addInhabitant(People people){
        if (b.getState() == States.CONSTRUCTION) throw new WrongState("建造中的建筑不能入住居民。");
        if(getNumberInhabitants()<getMaxInhabitants()){
            inhabitants.add(people);
        }else{
            throw new TooManyInhabitants("该建筑的居民已经住满了");
        }
    }
    @Override
    public void removeInhabitant(People people){
        if (b.getState() == States.CONSTRUCTION) throw new WrongState("建造中的建筑不能移除居民。");
        if(getNumberInhabitants()>0){
            inhabitants.remove(people);
        }else{
            throw new NotEnoughInhabitants("该建筑里没有居民可以移除");
        }
    }

    @Override
    public void addFunction(ArrayList<BuildingFunction> functions){
        functions.add(BuildingFunction.LIVING);
        super.addFunction(functions);
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
    public List<People> getWorkers() {
        return b.getWorkers();
    }
    @Override
    public int getMaxWorkers() {
        return b.getMaxWorkers();
    }
    @Override
    public int getNumberWorkers(){
        return b.getNumberWorkers();
    }
    @Override
    public void addWorker(People people){
        b.addWorker(people);
    }
    @Override
    public void removeWorker(People people){
        b.removeWorker(people);
    }

    @Override
    public HashMap<ResourceType, Integer> handle(){
        return b.handle();
    }
}
