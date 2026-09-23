package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tfgirls.project.javarts.Model.Event.EventOption;
import tfgirls.project.javarts.Model.Event.GameEvent;
import tfgirls.project.javarts.Model.GameManager;

/**
 * 事件弹窗：弹出一个窗口，显示随机事件的标题、说明和所有选项。
 * 玩家点了一个选项后执行对应的动作，然后在弹窗里显示结果。
 */
public class EventDialog {
    private static final Logger LOG = LoggerFactory.getLogger(EventDialog.class);
    private final Stage stage = new Stage();

    /**
     * @param event 要展示的 {@link GameEvent}
     * @param model 用来了解当前情况（暂时没直接用到，留着以后扩展）
     */
    public EventDialog(GameEvent event, GameManager model) {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("⚠️ 突发事件");

        VBox root = new VBox(12);
        root.setPadding(new Insets(14));
        root.getStyleClass().add("event-dialog");

        Label titleLabel = new Label("⚠️ " + event.getTitle());
        titleLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #ffd700;");

        Label descriptionLabel = new Label(event.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e0e0e0;");

        VBox optionsBox = new VBox(8);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        for (EventOption option : event.getOptions()) {
            Button optionButton = new Button(option.getLabel());
            optionButton.setMaxWidth(Double.MAX_VALUE);
            optionButton.setStyle("-fx-font-size: 12px; -fx-padding: 6 10 6 10;");
            optionButton.setOnAction(e -> {
                try {
                    option.getAction().run();
                } catch (Exception ex) {
                    LOG.warn("Event option failed: {}", ex.getMessage(), ex);
                }
                // 藏起选项，显示结果
                root.getChildren().remove(optionsBox);
                Label resultLabel = new Label(option.getResultDescription());
                resultLabel.setWrapText(true);
                resultLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #4caf50;");
                Button okButton = new Button("确定");
                okButton.setOnAction(e2 -> stage.close());
                root.getChildren().addAll(resultLabel, okButton);
            });
            optionsBox.getChildren().add(optionButton);
        }

        root.getChildren().addAll(titleLabel, descriptionLabel, optionsBox);

        // ===== 改动：突发事件弹窗调小（原来 500x420，现在 350x280） =====
        Scene scene = new Scene(root, 350, 280);
        try {
            scene.getStylesheets().add(
                getClass().getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS 加载失败，使用默认样式", e);
        }
        stage.setScene(scene);
    }

    /**
     * 弹出窗口，等玩家选完才关掉。
     */
    public void showAndWait() {
        stage.showAndWait();
    }
}
