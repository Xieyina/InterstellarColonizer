package tfgirls.project.javarts.Model.Building.State;

/**
 * 坏掉的状态（按情况切换状态里的一个状态）
 */
public class BrokenState extends AbstractState {
    private final Automata automata;

    public BrokenState(Automata automata) {
        super(States.BROKEN);
        this.automata = automata;
    }

    /**
     * 坏掉状态下，可以切换回正常运转状态
     */
    @Override
    public void running() {
        automata.setCurrentState(new RunningState(automata));
    }
}
