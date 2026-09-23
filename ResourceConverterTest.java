package tfgirls.project.javarts.resource;

import org.junit.jupiter.api.Test;
import tfgirls.project.javarts.Model.Resource.ResourceConverter;
import tfgirls.project.javarts.Model.Resource.ResourceManager;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 资源转换器逻辑测试（包括材料不够时不会报错往外抛的情况）。
 */
class ResourceConverterTest {

    private final ResourceConverter converter = new ResourceConverter("T_CONV",
            ResourceType.IRON, 2, ResourceType.COAL, 1, ResourceType.STEEL, 1);

    @Test
    void canConvertTrueWhenInputsSufficient() {
        ResourceManager.setResourceAmount(ResourceType.IRON, 2);
        ResourceManager.setResourceAmount(ResourceType.COAL, 1);
        assertTrue(converter.canConvert(ResourceManager.getInstance()));
    }

    @Test
    void canConvertFalseWhenInputInsufficient() {
        ResourceManager.setResourceAmount(ResourceType.IRON, 1);
        ResourceManager.setResourceAmount(ResourceType.COAL, 0);
        assertFalse(converter.canConvert(ResourceManager.getInstance()));
    }

    @Test
    void convertConsumesInputsAndProducesOutput() {
        ResourceManager.setResourceAmount(ResourceType.IRON, 5);
        ResourceManager.setResourceAmount(ResourceType.COAL, 5);
        ResourceManager.setResourceAmount(ResourceType.STEEL, 0);
        converter.convert();
        assertEquals(1, ResourceManager.getResourceAmount(ResourceType.STEEL));
        assertEquals(3, ResourceManager.getResourceAmount(ResourceType.IRON));
        assertEquals(4, ResourceManager.getResourceAmount(ResourceType.COAL));
    }

    @Test
    void convertWithInsufficientInputsDoesNotThrow() {
        ResourceManager.setResourceAmount(ResourceType.IRON, 1);
        ResourceManager.setResourceAmount(ResourceType.COAL, 0);
        ResourceManager.setResourceAmount(ResourceType.STEEL, 0);
        // 测一下 convert() 里"正常不会跑到"的那段：材料不够也不报错
        assertDoesNotThrow(converter::convert);
        assertEquals(0, ResourceManager.getResourceAmount(ResourceType.STEEL));
    }
}
