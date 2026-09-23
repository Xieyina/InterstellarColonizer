package tfgirls.project.javarts.Model.Building.State;

/**
 * 按情况自动切换状态的基类
 * 里面放着切换到各种状态需要用到的方法
 */
public class AbstractState implements State {
    private final States currentState;

    public AbstractState(States currentState) {
        this.currentState = currentState;
    }

    public void creation() {
        throw new IllegalStateException("Unexpected creation state");
    }

    public void running() {
        throw new IllegalStateException("Unexpected running state");
    }

    public void broken() {
        throw new IllegalStateException("Unexpected broken state");
    }

    public void boost() {
        throw new IllegalStateException("Unexpected boost state");
    }

    public void blocked(){
        throw new IllegalStateException("Unexpected blocked state");
    }

    /**
     * @return 当前的状态，类型是 {@link States}
     */
    public States getState() {
        return currentState;
    }

    @Override
    public void loop() {

    }
}
