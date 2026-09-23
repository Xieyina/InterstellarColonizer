package tfgirls.project.javarts.Model.Port;

/**
 * 游戏时钟约定：按固定时间间隔推进游戏。
 * 游戏数据和逻辑那层不直接依赖 JavaFX 的定时器，具体怎么定时由界面层来安排。
 */
public interface GameClock {
    /**
     * 启动游戏循环。
     *
     * @param periodMs 每个游戏日隔多少毫秒
     * @param onTick   每到时间就跑一次（就是每天的结算）
     */
    void start(long periodMs, Runnable onTick);

    /** 停掉游戏循环。 */
    void stop();

    /** 暂停游戏循环（每天不再推进，随时可以继续）。 */
    void pause();

    /** 从暂停的地方继续游戏循环。 */
    void resume();
}
