package tfgirls.project.javarts.Model.Port;

import tfgirls.project.javarts.Model.Event.GameEvent;

/**
 * UI 桥接约定：游戏数据和逻辑那层通过它请求界面操作（比如让界面稍后做某事、弹窗），
 * 这样游戏数据和逻辑那层就不用直接依赖 JavaFX 和 View 类了（GameEvent 属于 Model 包，引用它没问题）。
 */
public interface GameUiBridge {
    /** 让一个动作在界面那边跑。 */
    void dispatch(Runnable action);

    /** 弹出警告提示框。 */
    void showWarning(String title, String header, String content);

    /** 弹出胜利弹窗。 */
    void showVictory();

    /** 弹出游戏失败弹窗（所有居民和工人都死亡时）。 */
    void showGameOver();

    /**
     * 弹出事件弹窗。
     *
     * @param event    要显示的 {@link GameEvent}
     * @param onClosed 关掉弹窗之后要做的事（用来重置事件弹窗标志）
     */
    void showEvent(GameEvent event, Runnable onClosed);
}
