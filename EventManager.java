package tfgirls.project.javarts.Model.Event;

import tfgirls.project.javarts.Model.GameManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventManager {
    private final List<GameEvent> events = new ArrayList<>();
    private final Random random = new Random();
    private int turnCounter = 0;
    private int nextEventTurn = 5 + random.nextInt(10);

    public EventManager() {
        // 把所有事件加进来
        events.add(new MeteorStrikeEvent());
        events.add(new AlienTraderEvent());
        events.add(new SpacePlagueEvent());
        events.add(new SolarFlareEvent());
        events.add(new RefugeeWaveEvent());
        events.add(new AlienAttackEvent());
        events.add(new RichAsteroidEvent());
        events.add(new TechDiscoveryEvent());
        events.add(new ResourceShortageEvent());
        events.add(new PopulationGrowthEvent());
        events.add(new StellarStormEvent());
        events.add(new AncientRuinsEvent());
        events.add(new TradeEmbargoEvent());
        events.add(new DiplomaticMissionEvent());
        events.add(new CosmicBountyEvent());
    }

    public void checkAndTrigger(GameManager model) {
        turnCounter++;
        if (turnCounter >= nextEventTurn) {
            List<GameEvent> availableEvents = events.stream()
                .filter(e -> e.canTrigger(model))
                .toList();

            if (!availableEvents.isEmpty()) {
                GameEvent event = availableEvents.get(random.nextInt(availableEvents.size()));
                model.setPendingEvent(event);
            }

            // 重新开始倒计时
            nextEventTurn = turnCounter + 5 + random.nextInt(10);
        }
    }
}