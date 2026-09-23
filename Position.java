package tfgirls.project.javarts.Model;

/**
 * 用来表示地图上一个建筑的位置（x 和 y 坐标）
 */
public class Position {
    private final int x;
    private final int y;
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return x 坐标
     */
    public int getX() {
        return x;
    }

    /**
     * @return y 坐标
     */
    public int getY() {
        return y;
    }
}
