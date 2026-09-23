package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tfgirls.project.javarts.Controller.BagOfCommands;
import tfgirls.project.javarts.Controller.Commands.ResearchTechCommand;
import tfgirls.project.javarts.Model.Tech.TechNode;
import tfgirls.project.javarts.Model.Tech.TechTree;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 科技树页面：一层一层地显示所有科技的名字、说明、状态和花费，
 * 需要先满足的条件已经满足了而且资源够的科技会显示「研究」按钮。
 */
public class TechView {
    private static final Logger LOG = LoggerFactory.getLogger(TechView.class);
    private final TechTree techTree;
    private final VBox root = new VBox(10);

    public TechView(TechTree techTree) {
        this.techTree = techTree;
        buildUI();
    }

    private void buildUI() {
        root.setPadding(new Insets(20));
        root.getChildren().clear();

        Label title = new Label("🔬 科技树");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #ffffff;");
        root.getChildren().add(title);

        // 根据前置关系算出每个科技在第几层，再按层分组
        Map<String, Integer> depthCache = new HashMap<>();
        Map<Integer, List<TechNode>> tiers = new TreeMap<>();
        for (TechNode node : techTree.getNodes().values()) {
            int depth = depthOf(node, depthCache);
            tiers.computeIfAbsent(depth, k -> new ArrayList<>()).add(node);
        }

        VBox treeContainer = new VBox(15);
        for (Map.Entry<Integer, List<TechNode>> entry : tiers.entrySet()) {
            treeContainer.getChildren().add(createTier(entry.getKey(), entry.getValue()));
        }

        ScrollPane scrollPane = new ScrollPane(treeContainer);
        scrollPane.setFitToWidth(true);
        root.getChildren().add(scrollPane);

        Button closeButton = new Button("关闭");
        closeButton.setOnAction(e -> root.getScene().getWindow().hide());
        root.getChildren().add(closeButton);
    }

    /**
     * 一层层算出科技在第几层：没有必须先完成的就是第 0 层，有必须先完成的就取最大层数再加 1。
     *
     * @param node       要算层级的科技
     * @param depthCache 算过的结果存下来，不用重复算
     * @return 科技在第几层
     */
    private int depthOf(TechNode node, Map<String, Integer> depthCache) {
        if (depthCache.containsKey(node.getId())) {
            return depthCache.get(node.getId());
        }
        int depth = 0;
        for (String prereq : node.getPrerequisites()) {
            TechNode p = techTree.getNodes().get(prereq);
            if (p != null) {
                depth = Math.max(depth, depthOf(p, depthCache) + 1);
            }
        }
        depthCache.put(node.getId(), depth);
        return depth;
    }

    private static final String[] TIER_NAMES = {
        "一级科技", "二级科技", "三级科技", "四级科技", "五级科技", "六级科技"
    };

    private String tierName(int depth) {
        return depth < TIER_NAMES.length ? TIER_NAMES[depth] : "深层科技";
    }

    private VBox createTier(int tier, List<TechNode> nodes) {
        VBox tierBox = new VBox(5);
        Label tierLabel = new Label(tierName(tier));
        tierLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ffd700;");
        tierBox.getChildren().add(tierLabel);

        HBox nodesBox = new HBox(15);
        for (TechNode node : nodes) {
            nodesBox.getChildren().add(createNodeCard(node));
        }
        tierBox.getChildren().add(nodesBox);
        return tierBox;
    }

    private VBox createNodeCard(TechNode node) {
        VBox card = buildHeader(node);
        buildPrerequisites(card, node);
        if (!node.isResearched()) {
            buildCostRow(card, node);
            if (techTree.canResearch(node.getId())) {
                bindResearchAction(card, node);
            }
        }
        return card;
    }

    /** 卡片本体：名字、说明和状态标签。 */
    private VBox buildHeader(TechNode node) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #2a2a4a; -fx-background-radius: 8; " +
                      "-fx-border-color: " + (node.isResearched() ? "#4CAF50" : "#666") + "; " +
                      "-fx-border-radius: 8; -fx-border-width: 2;");
        card.setPrefWidth(170);

        Label nameLabel = new Label(node.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
            (node.isResearched() ? "#4CAF50" : "#ffffff") + ";");

        Label statusLabel = new Label(node.isResearched() ? "✅ 已研究" : "🔒 未解锁");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e0e0e0;");

        card.getChildren().addAll(nameLabel, new Label(node.getDescription()), statusLabel);
        return card;
    }

    /** 必须先完成的科技显示。 */
    private void buildPrerequisites(VBox card, TechNode node) {
        if (!node.getPrerequisites().isEmpty()) {
            StringBuilder prereqText = new StringBuilder("前置: ");
            for (String prereq : node.getPrerequisites()) {
                TechNode p = techTree.getNodes().get(prereq);
                prereqText.append(p != null ? p.getName() : prereq).append(" ");
            }
            Label prereqLabel = new Label(prereqText.toString().trim());
            prereqLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9e9e9e;");
            prereqLabel.setWrapText(true);
            card.getChildren().add(prereqLabel);
        }
    }

    /** 研究花费显示。 */
    private void buildCostRow(VBox card, TechNode node) {
        VBox costBox = new VBox(2);
        costBox.getChildren().add(new Label("📦 研究消耗:"));
        for (Map.Entry<ResourceType, Integer> entry : node.getCost().entrySet()) {
            // ===== 改动：资源名显示成中文 =====
            costBox.getChildren().add(new Label(Names.resourceName(entry.getKey()) + ": " + entry.getValue()));
        }
        card.getChildren().add(costBox);
    }

    /** 研究按钮（可以研究的时候才显示）。 */
    private void bindResearchAction(VBox card, TechNode node) {
        Button researchBtn = new Button("🔬 研究");
        researchBtn.setOnAction(e -> {
            BagOfCommands.getInstance().addCommand(new ResearchTechCommand(node.getId()));
            buildUI();
        });
        card.getChildren().add(researchBtn);
    }

    /**
     * 弹出一个窗口来显示科技树。
     *
     * @param owner 主窗口，这个弹窗会挡住主窗口
     */
    public void show(Stage owner) {
        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(owner);
        stage.setTitle("🔬 科技树");
        stage.setScene(new Scene(root, 900, 640));
        try {
            stage.getScene().getStylesheets().add(
                getClass().getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS 加载失败，使用默认样式", e);
        }
        stage.showAndWait();
    }
}
