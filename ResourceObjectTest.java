package tfgirls.project.javarts.resource;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Exception.NotEnoughResources;
import tfgirls.project.javarts.Model.Resource.AbstractResource;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.Model.Resource.Wood;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源对象（AbstractResource 及其子类，比如 Wood）的加减和"不够扣就报错"测试。
 */
class ResourceObjectTest {

    @Test
    void woodStoresAndReportsQuantity() {
        Wood wood = new Wood(10);
        assertEquals(10, wood.getQuantity());
        assertEquals(ResourceType.WOOD, wood.getType());
    }

    @Test
    void addResourcesIncreasesQuantity() {
        Wood wood = new Wood(10);
        wood.addResources(5);
        assertEquals(15, wood.getQuantity());
    }

    @Test
    void removeResourcesDecreasesQuantity() {
        Wood wood = new Wood(10);
        wood.removeResources(4);
        assertEquals(6, wood.getQuantity());
    }

    @Test
    void removeMoreThanOwnedThrows() {
        AbstractResource wood = new Wood(3);
        assertThrows(NotEnoughResources.class, () -> wood.removeResources(10));
    }
}
