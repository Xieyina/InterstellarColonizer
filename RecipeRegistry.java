package tfgirls.project.javarts.Model.Resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeRegistry {
    private static RecipeRegistry instance;
    private final Map<String, ResourceConverter> recipes = new HashMap<>();
    private final List<String> unlockedRecipes = new ArrayList<>();

    private RecipeRegistry() {
        // 一开始就有的配方
        registerRecipe(new ResourceConverter("ALLOY_SMELTING",
            ResourceType.IRON, 2, ResourceType.COAL, 1,
            ResourceType.STEEL, 1));
        registerRecipe(new ResourceConverter("ADVANCED_FOOD",
            ResourceType.FOOD, 3, ResourceType.STEEL, 0,
            ResourceType.FOOD, 4));
        registerRecipe(new ResourceConverter("CEMENT_MIXING",
            ResourceType.STONE, 3, ResourceType.COAL, 1,
            ResourceType.CEMENT, 2));
    }

    public static RecipeRegistry getInstance() {
        if (instance == null) instance = new RecipeRegistry();
        return instance;
    }

    public void registerRecipe(ResourceConverter converter) {
        recipes.put(converter.getRecipeId(), converter);
    }

    public void unlockRecipe(String recipeId) {
        if (!unlockedRecipes.contains(recipeId) && recipes.containsKey(recipeId)) {
            unlockedRecipes.add(recipeId);
        }
    }

    public void processAll() {
        for (String id : unlockedRecipes) {
            ResourceConverter converter = recipes.get(id);
            if (converter != null && converter.canConvert(ResourceManager.getInstance())) {
                converter.convert();
            }
        }
    }

    public boolean isRecipeUnlocked(String recipeId) {
        return unlockedRecipes.contains(recipeId);
    }

    public List<String> getUnlockedRecipes() { return unlockedRecipes; }

    /**
     * 清空已解锁的配方列表（保留配方定义本身）。
     * 游戏失败后从开屏重新开始新一局时用。
     */
    public void resetUnlockedRecipes() {
        unlockedRecipes.clear();
    }
}