package tfgirls.project.javarts.Model.Building.State;

/**
 * 加速状态（按情况切换状态里的一个状态）
 */
public class BoostState extends AbstractState{
    private final Automata automata;

    public BoostState(Automata automata) {
        super(States.BOOSTED);
        this.automata = automata;
    }

    /**
     * 加速状态下，可以切换到坏掉的状态
     */
    @Override
    public void broken() {
        automata.setCurrentState(new BrokenState(automata));
    }

    /**
     * 加速状态下，可以切换回正常运转状态
     */
    @Override
    public void running() {
        automata.setCurrentState(new RunningState(automata));
    }

    /**
     * 加速状态下，可以切换到停摆状态
     */
    @Override
    public void blocked() {
        automata.setCurrentState(new BlockedState(automata));
    }
}
