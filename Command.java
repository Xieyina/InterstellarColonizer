package tfgirls.project.javarts.Controller;

import tfgirls.project.javarts.Model.GameManager;

/**
 * 命令模式（把操作打包成一个命令）
 * 知道控制器是谁，方便办事
 */
public interface Command {
    /**
     * 执行这个命令。
     *
     * @param model      处理游戏逻辑的游戏管理器
     * @param controller 控制器，需要的时候可以提供选中的建筑类型
     */
    void execute(GameManager model, Controller controller);
}
