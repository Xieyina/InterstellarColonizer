package tfgirls.project.javarts.Model;

/**
 * 用来表示地图上一个建筑的大小
 */
public class Size {
    private final int width;
    private final int height;

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }
    /**
     * @return 宽度
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return 高度
     */
    public int getHeight() {
        return height;
    }

    /**
     * @return 数组，第 0 个是宽度，第 1 个是高度
     */
    public int[] getSize() {
        return new int[]{width, height};
    }

}
