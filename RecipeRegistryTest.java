package tfgirls.project.javarts.resource;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Resource.RecipeRegistry;
import tfgirls.project.javarts.Model.Resource.ResourceConverter;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配方表逻辑测试（全局只有一份；每个测试用不同的配方名，互不影响）。
 */
class RecipeRegistryTest {

    @Test
    void unknownRecipeIsNotUnlocked() {
        assertFalse(RecipeRegistry.getInstance().isRecipeUnlocked("NO_SUCH_RECIPE"));
    }

    @Test
    void unlockingUnknownRecipeDoesNothing() {
        RecipeRegistry registry = RecipeRegistry.getInstance();
        registry.unlockRecipe("NO_SUCH_RECIPE");
        assertFalse(registry.isRecipeUnlocked("NO_SUCH_RECIPE"));
    }

    @Test
    void unlockedRecipeConvertsOnProcessAll() {
        RecipeRegistry registry = RecipeRegistry.getInstance();
        registry.registerRecipe(new ResourceConverter("TEST_ALLOY",
                ResourceType.IRON, 2, ResourceType.COAL, 1, ResourceType.STEEL, 1));
        ResourceManager.setResourceAmount(ResourceType.IRON, 5);
        ResourceManager.setResourceAmount(ResourceType.COAL, 5);
        ResourceManager.setResourceAmount(ResourceType.STEEL, 0);
        registry.unlockRecipe("TEST_ALLOY");
        registry.processAll();
        assertEquals(1, ResourceManager.getResourceAmount(ResourceType.STEEL));
        assertEquals(3, ResourceManager.getResourceAmount(ResourceType.IRON));
    }

    @Test
    void lockedRecipeDoesNotConvert() {
        RecipeRegistry registry = RecipeRegistry.getInstance();
        registry.registerRecipe(new ResourceConverter("TEST_LOCKED",
                ResourceType.IRON, 2, ResourceType.COAL, 0, ResourceType.STEEL, 1));
        ResourceManager.setResourceAmount(ResourceType.IRON, 5);
        ResourceManager.setResourceAmount(ResourceType.COAL, 0);
        ResourceManager.setResourceAmount(ResourceType.STEEL, 0);
        registry.processAll();   // 没解锁，不算
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.STEEL));
    }
}
