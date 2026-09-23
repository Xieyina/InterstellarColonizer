package tfgirls.project.javarts.Model.Building.State;

/**
 * 用来管理状态切换的类
 */
public class Automata {
    private AbstractState currentState;

    /**
     *
     * @param currentState 要设置的当前状态，类型是 {@link AbstractState}
     */
    public void setCurrentState(AbstractState currentState) {
        this.currentState = currentState;
    }

    /**
     * @return 当前的状态，类型是 {@link AbstractState}
     */
    public AbstractState getCurrentState() {
        return currentState;
    }

    /**
     * @return 当前状态的名字
     */
    public States getCurrentStateName() {
        return currentState.getState();
    }

}
