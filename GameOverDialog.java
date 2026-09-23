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
import tfgirls.project.javarts.Model.GameManager;

/**
 * 游戏失败弹窗：所有居民和工人都死亡后弹出。
 * 玩家点「确定」后回到开屏界面，从开屏可以重新开始新一局。
 */
public class GameOverDialog {
    private static final Logger LOG = LoggerFactory.getLogger(GameOverDialog.class);

    /**
     * 弹出游戏失败窗口。
     *
     * @param model    当前对局（用来读游戏天数）
     * @param onReturn 点「确定」后要做的事（回到开屏界面）
     */
    public static void show(GameManager model, Runnable onReturn) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("💀 游戏失败");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("victory-dialog");

        Label titleLabel = new Label("💀 殖民地毁灭");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ff5252;");

        Label messageLabel = new Label(
            "所有居民和工人都已死亡，殖民地无法继续运转…\n"
            + "你的定居点坚持了 " + model.getDay() + " 个游戏日。"
        );
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ffffff;");

        Button okButton = new Button("确定");
        okButton.setStyle("-fx-background-color: #c62828; -fx-text-fill: white;");
        okButton.setOnAction(e -> {
            stage.close();
            onReturn.run();
        });

        root.getChildren().addAll(titleLabel, messageLabel, okButton);

        Scene scene = new Scene(root, 420, 280);
        try {
            scene.getStylesheets().add(
                GameOverDialog.class.getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS 加载失败，使用默认样式", e);
        }
        stage.setScene(scene);
        stage.showAndWait();
    }
}
