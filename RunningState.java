package tfgirls.project.javarts.Model.Building.State;

/**
 * 正常运转状态（按情况切换状态里的一个状态）
 */
public class RunningState extends AbstractState {
    private final Automata automata;

    public RunningState(Automata automata) {
        super(States.RUNNING);
        this.automata = automata;
    }

    /**
     * 正常运转状态下，可以切换到加速状态
     */
    @Override
    public void boost() {
        automata.setCurrentState(new BoostState(automata));
    }

    /**
     * 正常运转状态下，可以切换到坏掉的状态
     */
    @Override
    public void broken() {
        automata.setCurrentState(new BrokenState(automata));
    }

    /**
     * 正常运转状态下，可以切换到停摆状态
     */
    @Override
    public void blocked() {
        automata.setCurrentState(new BlockedState(automata));
    }
}
