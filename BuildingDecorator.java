package tfgirls.project.javarts.Model.Building;


import tfgirls.project.javarts.Exception.WrongBuildingType;
import tfgirls.project.javarts.Model.Building.State.States;
import tfgirls.project.javarts.Model.People;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Size;

import java.util.*;

public abstract class BuildingDecorator implements Building {
   private final Building decoratedBuilding;

    public BuildingDecorator(Building b) {
        this.decoratedBuilding = b;
    }

    @Override
    public Size getSize() {
        return decoratedBuilding.getSize();
    }

    @Override
    public Map<ResourceType, Integer> getCost() {
        return decoratedBuilding.getCost();
    }

    @Override
    public UUID getId() {
        return decoratedBuilding.getId();
    }

    @Override
    public int getConstructionTime() {
        return decoratedBuilding.getConstructionTime();
    }

    @Override
    public int getRemainingTime(){ return decoratedBuilding.getRemainingTime(); }

    @Override
    public BuildingType getType() {
        return decoratedBuilding.getType();
    }

    @Override
    public Position getPosition() {
        return decoratedBuilding.getPosition();
    }
    @Override
    public boolean equals(Object obj) {
        //TODO: 还没写好
        return super.equals(obj);
    }

    @Override
    public ArrayList<BuildingFunction> getFunctions() {
        return decoratedBuilding.getFunctions();
    }

    @Override
    public void addFunction(ArrayList<BuildingFunction> functions){
        decoratedBuilding.addFunction(functions);
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
    public String getName(){
        return decoratedBuilding.getName();
    }

    @Override
    public void removeInhabitant(People people) {
        throw new WrongBuildingType("该建筑不能住人");
    }

    @Override
    public States getState(){
        return decoratedBuilding.getState();
    }

    @Override
    public boolean needViewUpdate(){
        return decoratedBuilding.needViewUpdate();
    }

    @Override
    public void switchState(States state, int numberOfCycles){
        decoratedBuilding.switchState(state,numberOfCycles);
    }

    // ===== 猎人工会：交猎物再算钱，直接交给里面那层建筑去做 =====
    @Override
    public void deliverPrey(int preyCount) {
        decoratedBuilding.deliverPrey(preyCount);
    }

    @Override
    public int getPreyStock() {
        return decoratedBuilding.getPreyStock();
    }

    @Override
    public int consumePreyStock() {
        return decoratedBuilding.consumePreyStock();
    }

    @Override
    public HashMap<ResourceType, Integer> handle(){

        return null;
    }
}
