package tfgirls.project.javarts.View;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import tfgirls.project.javarts.Model.Event.GameEvent;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Port.GameUiBridge;

/**
 * {@link GameUiBridge} 的 JavaFX 版本：把游戏里要显示的东西，转成 JavaFX 界面上的操作。
 */
public class FxGameUiBridge implements GameUiBridge {
    private static final Logger LOG = LoggerFactory.getLogger(FxGameUiBridge.class);

    @Override
    public void dispatch(Runnable action) {
        Platform.runLater(action);
    }

    @Override
    public void showWarning(String title, String header, String content) {
        Platform.runLater(() -> {
            try {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(title);
                alert.setHeaderText(header);
                alert.setContentText(content);
                alert.show();
            } catch (Exception ex) {
                LOG.warn("饥饿警告弹窗无法显示：{}", ex.getMessage());
            }
        });
    }

    @Override
    public void showVictory() {
        Platform.runLater(() -> VictoryDialog.show(GameManager.getInstance()));
    }

    @Override
    public void showGameOver() {
        Platform.runLater(() -> {
            // 弹出失败提示；玩家点「确定」后回到开屏界面
            GameOverDialog.show(GameManager.getInstance(),
                tfgirls.project.javarts.MainApp::backToSplash);
        });
    }

    @Override
    public void showEvent(GameEvent event, Runnable onClosed) {
        Platform.runLater(() -> {
            EventDialog dialog = new EventDialog(event, GameManager.getInstance());
            dialog.showAndWait();
            onClosed.run();
        });
    }
}
