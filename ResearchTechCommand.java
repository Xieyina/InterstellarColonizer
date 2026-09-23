package tfgirls.project.javarts.Controller.Commands;

import tfgirls.project.javarts.Controller.Command;
import tfgirls.project.javarts.Controller.Controller;
import tfgirls.project.javarts.Model.GameManager;

public class ResearchTechCommand implements Command {
    private final String techId;

    public ResearchTechCommand(String techId) {
        this.techId = techId;
    }

    @Override
    public void execute(GameManager model, Controller controller) {
        model.researchTech(techId);
    }
}