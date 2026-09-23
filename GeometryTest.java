package tfgirls.project.javarts.Model;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Size;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 坐标（Position）和大小（Size）这两个最基础的几何类的测试。
 */
class GeometryTest {

    @Test
    void positionStoresXAndY() {
        Position p = new Position(7, 13);
        assertEquals(7, p.getX());
        assertEquals(13, p.getY());
    }

    @Test
    void positionZeroValues() {
        Position p = new Position(0, 0);
        assertEquals(0, p.getX());
        assertEquals(0, p.getY());
    }

    @Test
    void sizeStoresWidthAndHeight() {
        Size s = new Size(3, 2);
        assertEquals(3, s.getWidth());
        assertEquals(2, s.getHeight());
    }

    @Test
    void sizeAsArrayHasWidthThenHeight() {
        Size s = new Size(4, 5);
        assertArrayEquals(new int[]{4, 5}, s.getSize());
    }
}
