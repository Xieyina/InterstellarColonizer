package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.RecipeRegistry;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存档管理器（全局只有一个）。
 * 用 Jackson 把游戏状态保存成 JSON 文件，放在运行目录下的 saves/ 文件夹里。
 * v1.0 存档范围：游戏天数、资源库存、人口数量、已研究科技、已解锁配方；
 * 建筑布局暂不参与保存（以后再做）。
 */
public class SaveManager {
    private static final Logger LOG = LoggerFactory.getLogger(SaveManager.class);
    private static SaveManager instance;
    private static final String SAVE_DIR = "saves";
    private final ObjectMapper mapper = new ObjectMapper();

    // ===== 改动：存档日期格式（保存时显示对应存档的日期） =====
    public static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private SaveManager() {
    }

    public static SaveManager getInstance() {
        if (instance == null) {
            instance = new SaveManager();
        }
        return instance;
    }

    /**
     * 存档数据结构。字段都是公开的，方便 Jackson 直接读写。
     */
    public static class SaveData {
        public int day;
        public Map<String, Integer> resources = new HashMap<>();
        public int population;
        public int buildingCount;
        public List<String> researchedTechs = new ArrayList<>();
        public List<String> unlockedRecipes = new ArrayList<>();
        // ===== 改动：存档保存时的日期（显示给玩家看） =====
        public String saveDate;
    }

    /**
     * 把当前对局保存到 saves/&lt;slotName&gt;.json。
     *
     * @param model    {@link GameManager} 当前对局
     * @param slotName 存档名（不含扩展名）
     */
    public void save(GameManager model, String slotName) throws IOException {
        Files.createDirectories(Path.of(SAVE_DIR));
        SaveData data = new SaveData();
        // ===== 改动：记录存档的日期 =====
        data.saveDate = LocalDateTime.now().format(DATE_FORMAT);
        data.day = model.getDay();
        for (ResourceType rt : ResourceType.values()) {
            data.resources.put(rt.name(), ResourceManager.getResourceAmount(rt));
        }
        data.population = model.getPopulation();
        data.buildingCount = model.getBuildings().size();
        data.researchedTechs.addAll(model.getTechTree().getResearchedTechs());
        data.unlockedRecipes.addAll(RecipeRegistry.getInstance().getUnlockedRecipes());
        mapper.writerWithDefaultPrettyPrinter().writeValue(new File(SAVE_DIR, slotName + ".json"), data);
    }

    /**
     * @return saves/ 目录下全部存档名（不含扩展名）
     */
    public List<String> listSaves() {
        List<String> names = new ArrayList<>();
        File dir = new File(SAVE_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                names.add(f.getName().replace(".json", ""));
            }
        }
        return names;
    }

    /**
     * 从 saves/&lt;slotName&gt;.json 恢复对局状态。
     *
     * @param model    {@link GameManager} 当前对局
     * @param slotName 存档名（不含扩展名）
     */
    public void load(GameManager model, String slotName) throws IOException {
        SaveData data = mapper.readValue(new File(SAVE_DIR, slotName + ".json"), SaveData.class);
        model.setDay(data.day);
        for (Map.Entry<String, Integer> entry : data.resources.entrySet()) {
            try {
                ResourceManager.setResourceAmount(ResourceType.valueOf(entry.getKey()), entry.getValue());
            } catch (IllegalArgumentException e) {
                LOG.warn("忽略不认识的资源类型（为了兼容旧存档）: {}", entry.getKey(), e);
            }
        }
        int current = model.getPopulation();
        if (data.population < current) {
            model.killPeople(current - data.population);
        } else if (data.population > current) {
            model.addPopulation(data.population - current);
        }
        for (String techId : data.researchedTechs) {
            model.getTechTree().forceResearch(techId);
        }
        for (String recipeId : data.unlockedRecipes) {
            RecipeRegistry.getInstance().unlockRecipe(recipeId);
        }
        model.notifyListener();
    }

    /**
     * 弹出一个窗口，列出全部存档，可以加载或删除。
     *
     * @param model {@link GameManager} 当前对局
     */
    public void showLoadDialog(GameManager model) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("📂 加载存档");

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        Label feedback = new Label();
        feedback.setWrapText(true);

        VBox list = buildSaveList(model, feedback);
        if (list.getChildren().isEmpty()) {
            list.getChildren().add(new Label("暂无存档，请先在顶部点击「💾 保存」创建存档。"));
        }

        ScrollPane scroll = new ScrollPane(list);
        scroll.setFitToWidth(true);
        Button closeButton = new Button("关闭");
        closeButton.setOnAction(e -> stage.close());
        root.getChildren().addAll(scroll, feedback, closeButton);

        Scene scene = new Scene(root, 440, 380);
        try {
            scene.getStylesheets().add(
                getClass().getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS 加载失败，使用默认样式", e);
        }
        stage.setScene(scene);
        stage.showAndWait();
    }

    /** 构建存档列表：每个存档一行（名字 + 存档日期 + 加载按钮 + 删除按钮）。 */
    private VBox buildSaveList(GameManager model, Label feedback) {
        VBox list = new VBox(8);
        for (String name : listSaves()) {
            HBox row = new HBox(10);
            // ===== 改动：在存档列表里显示对应存档的日期 =====
            Label nameLabel = new Label("💾 " + name + "（" + readSaveDate(name) + "）");
            nameLabel.setMinWidth(200);
            Button loadButton = new Button("加载");
            bindLoadAction(loadButton, model, name, feedback);
            Button deleteButton = new Button("删除");
            bindDeleteAction(deleteButton, name, list, row);
            row.getChildren().addAll(nameLabel, loadButton, deleteButton);
            list.getChildren().add(row);
        }
        return list;
    }

    /**
     * 读出一个存档的保存日期。
     * 旧存档里没有日期字段时，返回「日期未知」。
     */
    private String readSaveDate(String name) {
        try {
            SaveData data = mapper.readValue(new File(SAVE_DIR, name + ".json"), SaveData.class);
            return (data.saveDate == null || data.saveDate.isBlank()) ? "日期未知" : data.saveDate;
        } catch (Exception e) {
            return "日期未知";
        }
    }

    /** 给「加载」按钮加上点击后的动作：读取存档，结果显示在窗口里。 */
    private void bindLoadAction(Button loadButton, GameManager model, String name, Label feedback) {
        loadButton.setOnAction(e -> {
            try {
                load(model, name);
                feedback.setText("✅ 已加载存档：" + name);
                feedback.setStyle("-fx-text-fill: #4caf50;");
            } catch (Exception ex) {
                feedback.setText("❌ 加载失败: " + ex.getMessage());
                feedback.setStyle("-fx-text-fill: #ff5252;");
            }
        });
    }

    /** 给「删除」按钮加上点击后的动作：删掉存档文件，把这行从列表里移走。 */
    private void bindDeleteAction(Button deleteButton, String name, VBox list, HBox row) {
        deleteButton.setOnAction(e -> {
            new File(SAVE_DIR, name + ".json").delete();
            list.getChildren().remove(row);
        });
    }
}
