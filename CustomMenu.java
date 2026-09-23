package tfgirls.project.javarts.View;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.AbstractResource;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.HashMap;
import java.util.Map;

public class CustomMenu extends VBox {
    private HBox container;
    private HashMap<ResourceType, Label> resourcesLabels = new HashMap<>();
    private HBox errorBox;
    private Label errorLabel;
    private Label dayLabel;

    /**
     * 新建顶部菜单栏，上面显示各种资源的数量，
     * 还有一个地方用来显示报错信息。
     */
    public CustomMenu() {
        // 资源列表
        HBox resourceBox = new HBox();
        resourceBox.setSpacing(10);
        resourceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        Map<ResourceType, AbstractResource> resources = ResourceManager.getResources();
        for (ResourceType resource : resources.keySet()) {
            HBox resourceDisplay = new HBox();
            resourceDisplay.setSpacing(4);
            resourceDisplay.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 6; -fx-padding: 4 10 4 10;");

            ImageView logo = new ImageView(new Image(
                getClass().getResource(ImagePath.getResourceLogoPath(resource)).toExternalForm()
            ));
            logo.setFitWidth(20);
            logo.setFitHeight(20);

            Label quantityLabel = new Label(String.valueOf(resources.get(resource).getQuantity()));
            quantityLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            resourcesLabels.put(resource, quantityLabel);
            resourceDisplay.getChildren().addAll(logo, quantityLabel);

            // ===== 改动：物资名称悬浮窗（光标落到物资上时显示半透明名称提示） =====
            Tooltip tip = new Tooltip(Names.resourceName(resource));
            tip.getStyleClass().add("name-tooltip");
            Tooltip.install(resourceDisplay, tip);

            resourceBox.getChildren().add(resourceDisplay);
        }

        // ===== 天数显示 =====
        dayLabel = new Label("📅 第 " + GameManager.getInstance().getDay() + " 天");
        dayLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 14px;");
        resourceBox.getChildren().add(dayLabel);

        // ===== 保存/加载按钮 =====
        Button saveButton = new Button("💾 保存");
        saveButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-background-radius: 4;");
        saveButton.setOnAction(e -> {
            // ===== 改动：游戏失败后一切操作都不再继续 =====
            if (GameManager.getInstance().isGameOver()) {
                return;
            }
            try {
                // ===== 改动：存档时显示对应存档的日期 =====
                // 文件名不能带冒号（Windows 保留字符），所以文件名用 yyyyMMdd_HHmmss；
                // 保存成功的提示里显示完整日期时间（yyyy-MM-dd HH:mm:ss）。
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                String slotName = "save_" + now.format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String displayDate = now.format(SaveManager.DATE_FORMAT);
                SaveManager.getInstance().save(GameManager.getInstance(), slotName);
                showInfo("✅ 游戏已保存（" + displayDate + "）");
            } catch (Exception ex) {
                showError("❌ 保存失败: " + ex.getMessage());
            }
        });
        resourceBox.getChildren().add(saveButton);

        Button loadButton = new Button("📂 加载");
        loadButton.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-background-radius: 4;");
        loadButton.setOnAction(e -> {
            // ===== 改动：游戏失败后一切操作都不再继续 =====
            if (GameManager.getInstance().isGameOver()) {
                return;
            }
            try {
                SaveManager.getInstance().showLoadDialog(GameManager.getInstance());
            } catch (Exception ex) {
                showError("❌ 加载失败: " + ex.getMessage());
            }
        });
        resourceBox.getChildren().add(loadButton);

        resourceBox.setPadding(new Insets(5, 10, 5, 10));

        // 显示报错信息的地方
        errorBox = new HBox();
        errorBox.setStyle("-fx-alignment: center-right; -fx-padding: 0 10 0 0;");
        errorLabel = new Label();
        errorLabel.setWrapText(true);

        container = new HBox(resourceBox, errorBox);
        container.setSpacing(10);
        container.setPadding(new Insets(5));
        container.setStyle("-fx-alignment: center-left;");
        container.setBackground(new Background(new BackgroundImage(
            new Image(getClass().getResource("/tfgirls/project/javarts/menu_background.png").toExternalForm()),
            BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
            BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT
        )));
        this.getChildren().addAll(container);
    }

    /**
     * 更新菜单栏上各种资源的数字
     */
    public void actualiseResources() {
        Map<ResourceType, AbstractResource> resources = ResourceManager.getResources();
        for (ResourceType resource : resources.keySet()) {
            resourcesLabels.get(resource).setText(String.valueOf(resources.get(resource).getQuantity()));
        }
        // 更新天数
        int day = GameManager.getInstance().getDay();
        dayLabel.setText("📅 第 " + day + " 天");
    }

    /**
     * 显示一条报错信息。
     * 信息会在红色背景上加粗显示 3 秒，让人一眼就能看到。
     * @param message
     */
    public void showError(String message) {
        showMessage(message, "rgba(255, 0, 0, 0.8)");
    }

    /**
     * 显示一条成功/提示信息（绿色背景，3 秒后消失）。
     * @param message
     */
    public void showInfo(String message) {
        showMessage(message, "rgba(46, 125, 50, 0.85)");
    }

    /** 在顶部右侧显示一条消息，3 秒后自动消失。 */
    private void showMessage(String message, String background) {
        errorBox.getChildren().clear();
        errorLabel.setText(message);
        errorLabel.setStyle(
            "-fx-background-color: " + background + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-font-family: Arial; " +
            "-fx-padding: 5 15 5 15; " +
            "-fx-background-radius: 6;"
        );

        errorBox.getChildren().add(errorLabel);

        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(event -> {
            errorBox.getChildren().remove(errorLabel);
        });
        pause.play();
    }
}
