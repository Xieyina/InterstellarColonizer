package tfgirls.project.javarts.Model.Event;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 事件选项（EventOption）的测试：固定说明和"会变的说明"（Supplier）分别怎么返回。
 */
class EventOptionTest {

    @Test
    void fixedResultDescriptionIsReturned() {
        EventOption opt = new EventOption("OK", () -> {
        }, "做完了");
        assertEquals("OK", opt.getLabel());
        assertEquals("做完了", opt.getResultDescription());
    }

    @Test
    void supplierResultDescriptionIsReturned() {
        AtomicBoolean ran = new AtomicBoolean(false);
        EventOption opt = new EventOption("挖", () -> ran.set(true),
                () -> ran.get() ? "成功" : "还没做");
        opt.getAction().run();
        assertTrue(ran.get());
        assertEquals("成功", opt.getResultDescription());
    }
}
