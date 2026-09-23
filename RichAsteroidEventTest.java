package tfgirls.project.javarts.Model.Event;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.GameManager;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;
import tfgirls.project.javarts.TestGame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 富饶小行星事件的测试：第 5 天之后才能触发、有 3 个选项，
 * 以及"开采/派遣探测器"在资源够和不够时分别会发生什么。
 */
class RichAsteroidEventTest {

    @BeforeAll
    static void bootstrap() {
        TestGame.model();
    }

    @Test
    void onlyTriggersFromDayFive() {
        RichAsteroidEvent event = new RichAsteroidEvent();
        GameManager early = mock(GameManager.class);
        when(early.getDay()).thenReturn(3);
        assertFalse(event.canTrigger(early));
        GameManager late = mock(GameManager.class);
        when(late.getDay()).thenReturn(5);
        assertTrue(event.canTrigger(late));
    }

    @Test
    void hasThreeOptions() {
        RichAsteroidEvent event = new RichAsteroidEvent();
        List<EventOption> options = event.getOptions();
        assertEquals(3, options.size());
    }

    @Test
    void miningConsumesToolsAndYieldsIronCoal() {
        ResourceManager.setResourceAmount(ResourceType.TOOLS, 20);
        ResourceManager.setResourceAmount(ResourceType.IRON, 0);
        ResourceManager.setResourceAmount(ResourceType.COAL, 0);
        RichAsteroidEvent event = new RichAsteroidEvent();
        event.getOptions().get(0).getAction().run();
        assertEquals(10, ResourceManager.getResourceAmount(ResourceType.TOOLS));
        assertEquals(50, ResourceManager.getResourceAmount(ResourceType.IRON));
        assertEquals(30, ResourceManager.getResourceAmount(ResourceType.COAL));
    }

    @Test
    void miningFailsWhenToolsInsufficient() {
        ResourceManager.setResourceAmount(ResourceType.TOOLS, 5);
        ResourceManager.setResourceAmount(ResourceType.IRON, 0);
        RichAsteroidEvent event = new RichAsteroidEvent();
        event.getOptions().get(0).getAction().run();
        assertEquals(5, ResourceManager.getResourceAmount(ResourceType.TOOLS));
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.IRON));
    }

    @Test
    void probeConsumesSteelAndYieldsSteelTools() {
        ResourceManager.setResourceAmount(ResourceType.STEEL, 20);
        ResourceManager.setResourceAmount(ResourceType.TOOLS, 0);
        RichAsteroidEvent event = new RichAsteroidEvent();
        event.getOptions().get(1).getAction().run();
        assertEquals(40, ResourceManager.getResourceAmount(ResourceType.STEEL));
        assertEquals(10, ResourceManager.getResourceAmount(ResourceType.TOOLS));
    }

    @Test
    void probeFailsWhenSteelInsufficient() {
        ResourceManager.setResourceAmount(ResourceType.STEEL, 5);
        ResourceManager.setResourceAmount(ResourceType.TOOLS, 0);
        RichAsteroidEvent event = new RichAsteroidEvent();
        event.getOptions().get(1).getAction().run();
        assertEquals(5, ResourceManager.getResourceAmount(ResourceType.STEEL));
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.TOOLS));
    }
}
