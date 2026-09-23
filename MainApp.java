package tfgirls.project.javarts;

import javafx.application.Application;
import javafx.stage.Stage;
import tfgirls.project.javarts.Controller.BagOfCommands;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.View.FxGameClock;
import tfgirls.project.javarts.View.FxGameUiBridge;
import tfgirls.project.javarts.View.MainView;
import tfgirls.project.javarts.View.SplashScreen;

/**
 * 应用入口：
 * 先显示开屏动画（点击任意位置进入游戏）；
 * 游戏失败后也会回到开屏，从开屏点击可以重新开始新一局。
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    private static MainView currentView;
    private static Runnable currentUpdateListener;
    private static final double WINDOW_WIDTH = 1280;
    private static final double WINDOW_HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // ===== 改动：先传进去 GameClock / GameUiBridge 的实现，再拿 GameManager（整个程序共用这同一个） =====
        GameManager.bootstrap(new FxGameClock(), new FxGameUiBridge());

        stage.setTitle("星际殖民者");
        showSplash();
    }

    /** 显示开屏动画：在画面上随便点一下就开始游戏。 */
    private static void showSplash() {
        primaryStage.setScene(SplashScreen.create(
            WINDOW_WIDTH, WINDOW_HEIGHT, MainApp::startGame));
        primaryStage.show();
    }

    /** 从开屏进入游戏主界面（第一次进入，或游戏失败后重新开始新一局）。 */
    private static void startGame() {
        GameManager model = GameManager.getInstance();

        // 旧界面的监听先摘掉，避免旧主界面还跟着刷新
        if (currentUpdateListener != null) {
            model.removeListener(currentUpdateListener);
            model.removeErrorListener();
        }

        if (model.isGameOver()) {
            // 上一局失败了：把整局重置回刚开局的样子再开始
            model.resetForNewGame();
        }

        MainView view = new MainView(primaryStage, model);
        currentView = view;
        currentUpdateListener = view::update;
        Controller controller = new Controller(model, view, BagOfCommands.getInstance());
        view.setController(controller);
        BagOfCommands.getInstance().setModel(model);
        BagOfCommands.getInstance().setController(controller);
    }

    /**
     * 游戏失败弹窗点「确定」后调用：回到开屏界面。
     * （由 FxGameUiBridge 在弹窗关掉后调用）
     */
    public static void backToSplash() {
        showSplash();
    }

    @Override
    public void stop() {
        GameManager.getInstance().shutdown();
        BagOfCommands.getInstance().shutdown();
    }

}
