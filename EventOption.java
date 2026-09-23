package tfgirls.project.javarts.Model.Event;

import java.util.function.Supplier;

/**
 * 事件选项：就是弹窗里玩家能选的一个选项。
 * 里面有选项文字、选了之后会做什么，以及做完之后的结果说明。
 */
public class EventOption {
    private final String label;
    private final Runnable action;
    private final String resultDescription;
    private final Supplier<String> resultSupplier;

    /**
     * 结果是固定的选项。
     *
     * @param label             选项按钮上的文字
     * @param action            选了之后要做的事
     * @param resultDescription 做完之后显示的结果说明
     */
    public EventOption(String label, Runnable action, String resultDescription) {
        this(label, action, resultDescription, null);
    }

    /**
     * 结果会变的选项（比如资源不够时结局不一样）。
     *
     * @param label          选项按钮上的文字
     * @param action         选了之后要做的事
     * @param resultSupplier 做完之后用来随时生成结果说明的东西
     */
    public EventOption(String label, Runnable action, Supplier<String> resultSupplier) {
        this(label, action, null, resultSupplier);
    }

    private EventOption(String label, Runnable action, String resultDescription, Supplier<String> resultSupplier) {
        this.label = label;
        this.action = action;
        this.resultDescription = resultDescription;
        this.resultSupplier = resultSupplier;
    }

    public String getLabel() {
        return label;
    }

    public Runnable getAction() {
        return action;
    }

    /**
     * 返回选了这个选项之后的结果说明。
     * 如果结果是会变的（{@link Supplier}），就优先返回变出来的那个结果。
     *
     * @return 结果说明文字
     */
    public String getResultDescription() {
        return resultSupplier != null ? resultSupplier.get() : resultDescription;
    }
}
