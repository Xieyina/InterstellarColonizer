package tfgirls.project.javarts.Controller;

import javafx.concurrent.Task;
import tfgirls.project.javarts.Model.GameManager;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 命令袋子模式（把一堆命令装起来）
 * 整个程序共用这同一个（外面不能直接新建）
 */
public class BagOfCommands {
    //private Queue<Command> commands;
    static BagOfCommands instance = null;
    private ConcurrentLinkedQueue<Command> commands = new ConcurrentLinkedQueue<>();
    GameManager model;
    Controller controller;
    boolean isRunning = false;
    private volatile boolean shutdown = false;
    private Thread workerThread;

    /**
     * 私有的构造方法，因为外面不能直接新建
     * 里面啥也没干，因为每个字段都在外面赋值了
     */
    private BagOfCommands() {
        /*commands = new LinkedList<>();*/
    }

    /**
     * 执行命令队列里的第一条命令
     */
    public void executeFirst() {
        Command command = commands.poll();
        if (command == null) {
            return;
        }
        command.execute(model, controller);
    }

    /**
     * 把命令加到命令队列里，如果命令还没开始跑，就调用
     * {@link processCommands()} 开始处理
     * @param command 要加进去的命令
     */
    public void addCommand(Command command){
        // ===== 改动：游戏失败后，一切操作都不再执行 =====
        if (model != null && model.isGameOver()) {
            return;
        }
        commands.add(command);
        if (!isRunning) {
            processCommands();
        }
       // executeAll();
    }

    /**
     * 把命令队列里的所有命令都执行一遍
     */
    public void executeAll(){
        for (Command command : commands) {
            command.execute(model, controller);
            commands.remove(command);
        }
    }

    /**
     * @param model 要设置的游戏管理器
     */
    public void setModel(GameManager model) {
        this.model = model;
    }

    /**
     * @param controller 要设置的控制器
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * 如果还没建过就新建一个，已经建过就直接返回那个
     * @return 命令袋子的唯一实例
     */
    public static BagOfCommands getInstance() {
        if (instance == null) {
            instance = new BagOfCommands();
        }
        return instance;
    }


    private void processCommands() {
        isRunning = true;
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                while (!commands.isEmpty() && !shutdown) {
                    Command command = commands.poll();
                    if (command != null) {
                        command.execute(model, controller);
                    }
                    Thread.sleep(1); // 命令之间的间隔
                }
                return null;
            }

            @Override
            protected void succeeded() {
                isRunning = false;
                // 做完这批命令后，看看还有没有新的
                if (!commands.isEmpty()) {
                    processCommands();
                }
                super.succeeded();
            }

            @Override
            protected void failed() {
                isRunning = false;
                super.failed();
            }
        };

        workerThread = new Thread(task);
        workerThread.setDaemon(true);   // 一直在后台跑的小任务，不会挡着程序退出
        workerThread.start();
    }

    /** 停止后台命令线程。 */
    public void shutdown() {
        shutdown = true;
        if (workerThread != null) workerThread.interrupt();
    }

}
