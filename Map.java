package tfgirls.project.javarts.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfgirls.project.javarts.Exception.MapTileStateException;

/**
 * 整个程序共用这同一个：游戏里只能有一张地图
 */
public class Map {
    private static final Logger LOG = LoggerFactory.getLogger(Map.class);
    private static Map instance = null;
    private Size size;
    private MapTileStatus[][] tiles;

    private Map() {
        // ===== 改动：地图从 40x12 扩到 200x200 =====
        this.size = new Size(200, 200);
        this.tiles = new MapTileStatus[size.getWidth()][size.getHeight()];
        // 把所有格子设为 FREE
        for (int i = 0; i < size.getWidth(); i++) {
            for (int j = 0; j < size.getHeight(); j++) {
                tiles[i][j] = MapTileStatus.FREE;
            }
        }
        LOG.debug("Map initialized with size: {}x{}", size.getWidth(), size.getHeight());
    }

    /**
     * @param position 一个格子的位置
     * @return 那个格子当前的状态
     */
    public MapTileStatus getTileStatus(Position position) {
        if (tiles == null) {
            throw new MapTileStateException("Map has not been initialized yet");
        }
        if (position.getX() < 0 || position.getX() >= size.getWidth() ||
            position.getY() < 0 || position.getY() >= size.getHeight()) {
            return MapTileStatus.OCCUPIED; // 出界了就算被占了
        }
        return tiles[position.getX()][position.getY()];
    }

    /**
     * 看看指定位置和大小的区域是不是空的。
     *
     * @param position 起始位置
     * @param size 区域大小
     * @return 全空返回 true，有一个被占就返回 false
     */
    public boolean isAreaFree(Position position, Size size) {
        for (int x = 0; x < size.getWidth(); x++) {
            for (int y = 0; y < size.getHeight(); y++) {
                int checkX = position.getX() + x;
                int checkY = position.getY() + y;
                if (checkX >= this.size.getWidth() || checkY >= this.size.getHeight()) {
                    return false; // 超出地图范围了
                }
                if (tiles[checkX][checkY] == MapTileStatus.OCCUPIED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 把指定区域所有格子标记为被占用（OCCUPIED）。
     * 注意：不会检查区域是不是空的，用之前自己确认！
     *
     * @param position 区域起始位置
     * @param size 区域大小
     */
    public void construct(Position position, Size size) {
        for (int x = 0; x < size.getWidth(); x++) {
            for (int y = 0; y < size.getHeight(); y++) {
                int setX = position.getX() + x;
                int setY = position.getY() + y;
                if (setX < this.size.getWidth() && setY < this.size.getHeight()) {
                    tiles[setX][setY] = MapTileStatus.OCCUPIED;
                }
            }
        }
    }

    /**
     * 把指定区域所有格子标记为空的（FREE）。
     * 如果有格子本来就不是被占状态，会抛异常。
     *
     * @param position 区域起始位置
     * @param size 区域大小
     */
    public void destruct(Position position, Size size) {
        for (int x = 0; x < size.getWidth(); x++) {
            for (int y = 0; y < size.getHeight(); y++) {
                int setX = position.getX() + x;
                int setY = position.getY() + y;
                if (setX < this.size.getWidth() && setY < this.size.getHeight()) {
                    if (tiles[setX][setY] == MapTileStatus.FREE) {
                        throw new MapTileStateException("Trying to free an already free map");
                    }
                    tiles[setX][setY] = MapTileStatus.FREE;
                }
            }
        }
    }

    /**
     * 整个程序共用这同一个
     * 如果地图还没建就建一个，建过了就返回那个。
     *
     * @return 地图就这同一个
     */
    public static Map getInstance() {
        if (instance == null) {
            instance = new Map();
        }
        return instance;
    }

    /**
     * @return 地图的 {@link Size}
     */
    public Size getSize() {
        return size;
    }

    /**
     * @return 地图所有格子组成的数组
     */
    public MapTileStatus[][] getTiles() {
        return tiles;
    }

    /**
     * 把地图上所有格子重新标记为空地（FREE）。
     * 游戏失败后从开屏重新开始新一局时用。
     */
    public void resetTiles() {
        for (int i = 0; i < size.getWidth(); i++) {
            for (int j = 0; j < size.getHeight(); j++) {
                tiles[i][j] = MapTileStatus.FREE;
            }
        }
        LOG.debug("Map tiles reset");
    }
}