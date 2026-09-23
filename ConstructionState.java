package tfgirls.project.javarts.Model.Building.State;

/**
 * 建造中的状态（按情况切换状态里的一个状态）
 */
public class ConstructionState extends AbstractState{
    private final Automata automata;

    public ConstructionState(Automata automata) {
        super(States.CONSTRUCTION);
        this.automata = automata;
    }

    /**
     * 建造中状态下，可以切换到正常运转状态
     */
    @Override
    public void running() {
        this.automata.setCurrentState(new RunningState(automata));
    }
}
