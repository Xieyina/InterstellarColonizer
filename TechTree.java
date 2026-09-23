package tfgirls.project.javarts.Model.Tech;

import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Resource.RecipeRegistry;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.*;

public class TechTree {
    private final Map<String, TechNode> nodes = new HashMap<>();
    private final List<String> researchedTechs = new ArrayList<>();
    private final List<BuildingType> unlockedBuildings = new ArrayList<>();
    private final Map<ResourceType, Double> productionMultipliers = new HashMap<>();
    private final Map<ResourceType, Double> consumptionMultipliers = new HashMap<>();
    private double globalProductionMultiplier = 1.0;

    public TechTree() {
        initializeTechTree();
        // ===== 改动：一级农业开局自动点亮，不用玩家手动研究 =====
        forceResearch("BASIC_FARMING");
    }

    private void initializeTechTree() {
        registerBasicTechs();
        registerAdvancedTechs();
        registerEndgameTechs();
    }

    /** 第一级科技（不需要先研究别的）。 */
    private void registerBasicTechs() {
        addNode(new TechNode("BASIC_MINING", "初级采矿", "解锁矿场",
            new ArrayList<>(),
            Map.of(ResourceType.FOOD, 50),
            TechEffect.unlockBuilding(BuildingType.QUARRY)));

        addNode(new TechNode("BASIC_FARMING", "初级农业", "解锁农场",
            new ArrayList<>(),
            Map.of(ResourceType.WOOD, 50),
            TechEffect.unlockBuilding(BuildingType.FARM)));

        addNode(new TechNode("BASIC_ENERGY", "初级能源", "解锁能源站",
            new ArrayList<>(),
            Map.of(ResourceType.STONE, 50),
            TechEffect.unlockBuilding(BuildingType.CEMENTPLANT)));
    }

    /** 第二级和第三级科技。 */
    private void registerAdvancedTechs() {
        addNode(new TechNode("ADVANCED_MINING", "高级采矿", "采矿效率+50%",
            List.of("BASIC_MINING"),
            Map.of(ResourceType.STEEL, 100, ResourceType.FOOD, 50),
            TechEffect.boostProduction(ResourceType.STEEL, 1.5)));

        addNode(new TechNode("METALLURGY", "冶金学", "解锁冶炼厂，效率+30%",
            List.of("BASIC_MINING", "BASIC_ENERGY"),
            Map.of(ResourceType.STEEL, 100, ResourceType.FOOD, 50),
            TechEffect.unlockBuilding(BuildingType.STEELMILL)));

        addNode(new TechNode("ADVANCED_FARMING", "高级农业", "食物产出+50%",
            List.of("BASIC_FARMING"),
            Map.of(ResourceType.STEEL, 100, ResourceType.WOOD, 50),
            TechEffect.boostProduction(ResourceType.FOOD, 1.5)));

        addNode(new TechNode("ADVANCED_ENERGY", "高级能源", "能源产出+50%",
            List.of("BASIC_ENERGY", "METALLURGY"),
            Map.of(ResourceType.STEEL, 200, ResourceType.CEMENT, 50),
            TechEffect.boostProduction(ResourceType.COAL, 1.5)));

        addNode(new TechNode("AI_OPTIMIZATION", "人工智能", "所有产能+20%",
            List.of("ADVANCED_MINING", "ADVANCED_FARMING"),
            Map.of(ResourceType.STEEL, 200, ResourceType.FOOD, 100),
            TechEffect.boostAll(1.2)));
    }

    /** 第四级科技（最厉害的）。 */
    private void registerEndgameTechs() {
        addNode(new TechNode("QUANTUM_COMPUTING", "量子计算", "解锁量子实验室",
            List.of("ADVANCED_ENERGY", "AI_OPTIMIZATION"),
            Map.of(ResourceType.STEEL, 500, ResourceType.CEMENT, 200),
            TechEffect.unlockBuilding(BuildingType.TOOLFACTORY)));

        addNode(new TechNode("INTERSTELLAR_TRAVEL", "星际航行", "胜利条件：研究此科技",
            List.of("METALLURGY", "ADVANCED_ENERGY", "AI_OPTIMIZATION"),
            Map.of(ResourceType.STEEL, 1000, ResourceType.CEMENT, 500),
            TechEffect.unlockBuilding(BuildingType.WOODENCABIN))); // 先占个位置
    }

    private void addNode(TechNode node) {
        nodes.put(node.getId(), node);
    }

    public boolean canResearch(String techId) {
        TechNode node = nodes.get(techId);
        if (node == null || node.isResearched()) return false;

        // 检查前面的科技研究完没有
        for (String prereq : node.getPrerequisites()) {
            if (!isResearched(prereq)) return false;
        }

        // 检查资源够不够
        return ResourceManager.areAvailable(node.getCost());
    }

    public boolean isResearched(String techId) {
        TechNode node = nodes.get(techId);
        return node != null && node.isResearched();
    }

    public void research(String techId) {
        if (!canResearch(techId)) return;

        TechNode node = nodes.get(techId);
        // 扣掉资源
        for (Map.Entry<ResourceType, Integer> entry : node.getCost().entrySet()) {
            ResourceManager.removeResource(entry.getKey(), entry.getValue());
        }

        forceResearch(techId);
    }

    /**
     * 不花资源就直接把科技研究完，效果也会生效。
     * 用来处理事件奖励（发现科技）和读档恢复已经研究过的科技。
     *
     * @param techId 要直接完成的科技 ID
     */
    public void forceResearch(String techId) {
        TechNode node = nodes.get(techId);
        if (node == null || node.isResearched()) return;

        node.setResearched(true);
        researchedTechs.add(techId);

        // 让效果生效
        applyEffect(node.getEffect());
    }

    private void applyEffect(TechEffect effect) {
        switch (effect.getType()) {
            case UNLOCK_BUILDING:
                if (effect.getTarget() instanceof BuildingType) {
                    unlockedBuildings.add((BuildingType) effect.getTarget());
                }
                break;
            case UNLOCK_RECIPE:
                RecipeRegistry.getInstance().unlockRecipe((String) effect.getTarget());
                break;
            case BOOST_PRODUCTION:
                if (effect.getTarget() instanceof ResourceType) {
                    productionMultipliers.put((ResourceType) effect.getTarget(),
                        productionMultipliers.getOrDefault((ResourceType) effect.getTarget(), 1.0) *
                        effect.getMultiplier());
                }
                break;
            case BOOST_ALL:
                globalProductionMultiplier *= effect.getMultiplier();
                break;
            case REDUCE_CONSUMPTION:
                if (effect.getTarget() instanceof ResourceType) {
                    consumptionMultipliers.put((ResourceType) effect.getTarget(),
                        consumptionMultipliers.getOrDefault((ResourceType) effect.getTarget(), 1.0) *
                        effect.getMultiplier());
                }
                break;
            default:
                break;
        }
    }

    public List<TechNode> getAvailableTechs() {
        List<TechNode> available = new ArrayList<>();
        for (TechNode node : nodes.values()) {
            if (canResearch(node.getId())) {
                available.add(node);
            }
        }
        return available;
    }

    public double getProductionMultiplier(ResourceType resource) {
        return productionMultipliers.getOrDefault(resource, 1.0) * globalProductionMultiplier;
    }

    public double getConsumptionMultiplier(ResourceType resource) {
        return consumptionMultipliers.getOrDefault(resource, 1.0);
    }

    public boolean isBuildingUnlocked(BuildingType type) {
        return unlockedBuildings.contains(type);
    }

    public Map<String, TechNode> getNodes() { return nodes; }
    public List<String> getResearchedTechs() { return researchedTechs; }
}