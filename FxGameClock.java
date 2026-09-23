package tfgirls.project.javarts.View;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import tfgirls.project.javarts.Model.Port.GameClock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link GameClock} 的 JavaFX 版本：用 {@link Timeline} 不断循环来推进每天。
 *
 * 做法说明：Timeline 到时间后要执行的代码在界面那边跑，只负责"安排活儿"——
 * 每天的结算（onTick，也就是 GameManager 的每日计算）交给另一个单独在后台跑的小任务去做，
 * 这样就不会因为结算太慢而卡住界面；
 * settling 标志用来防止上一轮还没算完就开下一轮。
 * 结算里面只要要改界面，都通过 GameUiBridge.dispatch / showEvent / showWarning
 * 回到界面那边去执行（见 FxGameUiBridge）。
 */
public class FxGameClock implements GameClock {

    private Timeline timeline;
    private ExecutorService settlementExecutor = newSettlementExecutor();
    private final AtomicBoolean settling = new AtomicBoolean(false);

    /** 新建每天结算用的后台小帮手线程池（每次重新开始游戏时重建）。 */
    private static ExecutorService newSettlementExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "daily-settlement");
            t.setDaemon(true);   // 后台小帮手，程序关了它也跟着关
            return t;
        });
    }

    @Override
    public void start(long periodMs, Runnable onTick) {
        // 上一局 stop() 时线程池会被关掉，重新开始时再建一个新的
        if (settlementExecutor.isShutdown()) {
            settlementExecutor = newSettlementExecutor();
            settling.set(false);
        }
        timeline = new Timeline(new KeyFrame(Duration.millis(periodMs), e -> {
            if (!settling.compareAndSet(false, true)) return;   // 防止上一轮还没做完就开始下一轮
            settlementExecutor.submit(() -> {
                try {
                    onTick.run();
                } finally {
                    settling.set(false);
                }
            });
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @Override
    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
        settlementExecutor.shutdownNow();
    }

    @Override
    public void pause() {
        if (timeline != null) {
            timeline.pause();
        }
    }

    @Override
    public void resume() {
        if (timeline != null) {
            timeline.play();
        }
    }
}
