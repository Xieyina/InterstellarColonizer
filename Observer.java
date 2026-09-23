package tfgirls.project.javarts.View;

/**
 * 谁关心这个变化就通知谁的约定：用来让大家知道"有东西变了"。
 * 别人注册一下，变了就通知他们。
 */
public interface Observer {
    public void update();
}
