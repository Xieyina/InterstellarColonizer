package tfgirls.project.javarts.Model.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tfgirls.project.javarts.Exception.NotEnoughResources;

public class ResourceConverter {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceConverter.class);
    private final ResourceType input1, input2;
    private final int input1Amount, input2Amount;
    private final ResourceType output;
    private final int outputAmount;
    private final String recipeId;

    public ResourceConverter(String recipeId, ResourceType input1, int input1Amount,
                             ResourceType input2, int input2Amount,
                             ResourceType output, int outputAmount) {
        this.recipeId = recipeId;
        this.input1 = input1;
        this.input2 = input2;
        this.input1Amount = input1Amount;
        this.input2Amount = input2Amount;
        this.output = output;
        this.outputAmount = outputAmount;
    }

    public boolean canConvert(ResourceManager rm) {
        return ResourceManager.getResourceAmount(input1) >= input1Amount &&
               ResourceManager.getResourceAmount(input2) >= input2Amount;
    }

    public void convert() {
        try {
            ResourceManager.removeResource(input1, input1Amount);
            ResourceManager.removeResource(input2, input2Amount);
            ResourceManager.addResource(output, outputAmount);
        } catch (NotEnoughResources e) {
            LOG.warn("资源转换时资源不足（之前已经检查过了，理论不可达）", e);
        }
    }

    public String getRecipeId() { return recipeId; }
    public ResourceType getOutput() { return output; }
    public int getOutputAmount() { return outputAmount; }
}