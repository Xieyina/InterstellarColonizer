package tfgirls.project.javarts.View;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * 开屏动画界面：
 * 全屏显示开屏图（opening screen model），带淡入动画效果；
 * 在画面任意位置点一下，就进入游戏主界面。
 */
public class SplashScreen {

    /**
     * 新建开屏场景。
     *
     * @param width    窗口宽度
     * @param height   窗口高度
     * @param onEnter  点击画面后要做的事（进入游戏主界面）
     * @return 开屏用的 {@link Scene}
     */
    public static Scene create(double width, double height, Runnable onEnter) {
        // 开屏图片铺满整个窗口
        ImageView splashView = new ImageView(new Image(
            SplashScreen.class.getResource("/tfgirls/project/javarts/splash/splash.png").toExternalForm()));
        splashView.setPreserveRatio(false);
        splashView.fitWidthProperty().set(width);
        splashView.fitHeightProperty().set(height);

        // 底部提示文字
        Label hint = new Label("🖱️ 点击任意位置开始游戏");
        hint.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #ffffff; "
                + "-fx-background-color: rgba(0,0,0,0.55); -fx-background-radius: 12; "
                + "-fx-padding: 10 28 10 28;");
        StackPane.setAlignment(hint, Pos.BOTTOM_CENTER);
        StackPane.setMargin(hint, new javafx.geometry.Insets(0, 0, 40, 0));

        StackPane root = new StackPane(splashView, hint);
        root.setStyle("-fx-background-color: black;");
        // ===== 改动：开屏动画期间随便点一下就直接进入游戏主界面（不用等动画放完，也不用再点第二次） =====
        // 点击时先把淡入动画停掉，立刻加载主画面的草坪。
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), root);
        Runnable enter = () -> {
            fade.stop();
            onEnter.run();
        };
        root.setOnMouseClicked(event -> enter.run());
        hint.setOnMouseClicked(event -> enter.run());

        Scene scene = new Scene(root, width, height);

        // 淡入动画：开屏图从透明慢慢显现
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();

        return scene;
    }
}
