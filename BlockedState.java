package tfgirls.project.javarts.Model.Building.State;

/**
 * 停摆状态（按情况切换状态里的一个状态）
 */
public class BlockedState extends AbstractState {
    private final Automata automata;
    public BlockedState(Automata automata) {
        super(States.BLOCKED);
        this.automata = automata;
    }

    /**
     * 在停摆状态下，可以切换回正常运转状态
     */
    @Override
    public void running() {
        automata.setCurrentState(new RunningState(automata));
    }
}
