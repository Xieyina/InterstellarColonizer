package tfgirls.project.javarts.Model;

/**
 * 谁关心这个变化就通知谁——这是「被通知方」要遵守的约定
 */
public interface Subject {
    public void addListener(Runnable o);
    public void removeListener(Runnable o);
    public void addErrorListener(Runnable o);
    public void removeErrorListener();
    public void notifyListener();
    public void notifyErrorListener(Exception e);
}
