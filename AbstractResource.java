package tfgirls.project.javarts.Model.Resource;

import tfgirls.project.javarts.Exception.NotEnoughResources;

import java.util.HashMap;

public abstract class AbstractResource implements Resource {
    private int quantity;
    public AbstractResource(int quantity) {
        this.quantity = quantity;
    }
    public int getQuantity() {
        return quantity;
    }
    public void addResources(int quantity){
        this.quantity += quantity;
    }
    public void removeResources(int quantity){
        if (this.quantity < quantity) {
            throw new NotEnoughResources("资源数量不能为负数");
        }
        this.quantity -= quantity;
    }

    public HashMap<ResourceType,Integer> handle(){
        return new HashMap<>();
    }

}
