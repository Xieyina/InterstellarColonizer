package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tfgirls.project.javarts.Model.GameManager;

/**
 * 胜利弹窗：玩家研究出终极科技「星际航行」后，弹出这个窗口告诉玩家赢了。
 * 玩家可以选择继续玩或者退出游戏。
 */
public class VictoryDialog {
    private static final Logger LOG = LoggerFactory.getLogger(VictoryDialog.class);

    /**
     * 弹出胜利窗口。
     *
     * @param model 用来读取当前游戏天数等信息
     */
    public static void show(GameManager model) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("🏆 胜利");

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.getStyleClass().add("victory-dialog");

        Label titleLabel = new Label("🏆 星际航行研发成功！");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #ffd700;");

        Label messageLabel = new Label(
            "你的定居点已掌握星际航行技术，文明正式迈向星辰大海！\n"
            + "历时 " + model.getDay() + " 个游戏日，这是属于你的胜利！"
        );
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #ffffff;");

        Button continueButton = new Button("继续经营");
        continueButton.setOnAction(e -> stage.close());
        Button exitButton = new Button("退出游戏");
        exitButton.setOnAction(e -> {
            stage.close();
            Platform.exit();
        });

        HBox buttons = new HBox(15, continueButton, exitButton);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, messageLabel, buttons);

        Scene scene = new Scene(root, 500, 340);
        try {
            scene.getStylesheets().add(
                VictoryDialog.class.getResource("/tfgirls/project/javarts/css/dark-theme.css").toExternalForm()
            );
        } catch (Exception e) {
            LOG.warn("CSS 加载失败，使用默认样式", e);
        }
        stage.setScene(scene);
        stage.showAndWait();
    }
}
