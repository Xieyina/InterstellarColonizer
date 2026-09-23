package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;

import java.util.List;

public interface GameEvent {
    void trigger(GameManager model);
    String getTitle();
    String getDescription();
    List<EventOption> getOptions();
    boolean canTrigger(GameManager model);
}