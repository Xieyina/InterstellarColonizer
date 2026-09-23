package tfgirls.project.javarts;

import org.mockito.Mockito;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Port.GameClock;
import tfgirls.project.javarts.Model.Port.GameUiBridge;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * 测试用的 GameManager 启动工具（整个程序只建一次）：
 * - 用 Mockito 造假 GameClock / GameUiBridge（是大家都要遵守的约定，不需要 JavaFX）；
 * - 拿到 GameManager 每天结算的 tick()，测试可以直接跑一天；
 * - 事件弹窗"立刻关掉"，不会卡住每天的推进。
 */
public final class TestGame {

    private static GameManager gm;
    private static Runnable tick;
    private static GameUiBridge ui;

    private TestGame() {
    }

    /** 启动并返回唯一的 GameManager。 */
    public static synchronized GameManager model() {
        if (gm == null) {
            GameClock clock = mock(GameClock.class);
            ui = Mockito.mock(GameUiBridge.class);
            AtomicReference<Runnable> tickRef = new AtomicReference<>();
            doAnswer(inv -> {
                tickRef.set(inv.getArgument(1));
                return null;
            }).when(clock).start(anyLong(), any());
            doAnswer(inv -> {
                ((Runnable) inv.getArgument(1)).run();
                return null;
            }).when(ui).showEvent(any(), any());
            GameManager.bootstrap(clock, ui);
            gm = GameManager.getInstance();
            tick = tickRef.get();
        }
        return gm;
    }

    /** 每天结算时要执行的代码（跑一天游戏）。 */
    public static Runnable tick() {
        return tick;
    }

    /** 假的 UI 桥接（用来检查弹窗有没有被调用）。 */
    public static GameUiBridge ui() {
        return ui;
    }
}
