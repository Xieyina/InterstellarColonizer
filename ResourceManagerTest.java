package tfgirls.project.javarts.resource;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Exception.NotEnoughResources;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源管理器逻辑测试（库存是全局的，每个测试先设好初始数量）。
 */
class ResourceManagerTest {

    @Test
    void addResourceIncreasesStock() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 100);
        ResourceManager.addResource(ResourceType.WOOD, 5);
        assertEquals(105, ResourceManager.getResourceAmount(ResourceType.WOOD));
    }

    @Test
    void removeResourceDecreasesStock() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 100);
        ResourceManager.removeResource(ResourceType.WOOD, 30);
        assertEquals(70, ResourceManager.getResourceAmount(ResourceType.WOOD));
    }

    @Test
    void removeMoreThanStockIsBlockedAndStockUnchanged() {
        ResourceManager.setResourceAmount(ResourceType.STONE, 10);
        assertThrows(NotEnoughResources.class,
                () -> ResourceManager.removeResource(ResourceType.STONE, 1000));
        assertEquals(10, ResourceManager.getResourceAmount(ResourceType.STONE));
    }

    @Test
    void addNegativeToNonFoodClampsToZeroAndThrows() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 10);
        assertThrows(NotEnoughResources.class,
                () -> ResourceManager.addResource(ResourceType.WOOD, -20));
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.WOOD));
    }

    @Test
    void addNegativeToFoodClampsToZeroWithoutThrowing() {
        ResourceManager.setResourceAmount(ResourceType.FOOD, 5);
        assertDoesNotThrow(() -> ResourceManager.addResource(ResourceType.FOOD, -10));
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.FOOD));
    }

    @Test
    void getResourceAmountOfUnknownTypeReturnsZero() {
        assertEquals(0, ResourceManager.getResourceAmount(null));
    }

    @Test
    void areAvailableChecksAllEntries() {
        ResourceManager.setResourceAmount(ResourceType.WOOD, 10);
        ResourceManager.setResourceAmount(ResourceType.STONE, 5);
        assertTrue(ResourceManager.areAvailable(
                Map.of(ResourceType.WOOD, 10, ResourceType.STONE, 5)));
        assertFalse(ResourceManager.areAvailable(Map.of(ResourceType.WOOD, 11)));
    }

    @Test
    void setResourceAmountClampsAtZero() {
        ResourceManager.setResourceAmount(ResourceType.IRON, -5);
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.IRON));
    }
}
