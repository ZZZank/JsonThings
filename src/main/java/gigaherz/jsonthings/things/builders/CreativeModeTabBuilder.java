package gigaherz.jsonthings.things.builders;

import gigaherz.jsonthings.things.IFlexBlock;
import gigaherz.jsonthings.things.misc.FlexCreativeModeTab;
import gigaherz.jsonthings.util.Utils;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class CreativeModeTabBuilder implements Supplier<FlexCreativeModeTab>
{
    private FlexCreativeModeTab builtTab = null;

    private final ResourceLocation registryName;
    private ResourceLocation iconItem;

    private CreativeModeTabBuilder(ResourceLocation registryName)
    {
        this.registryName = registryName;
    }

    public static CreativeModeTabBuilder begin(ResourceLocation registryName)
    {
        return new CreativeModeTabBuilder(registryName);
    }

    public void setIcon(ResourceLocation iconItem)
    {
        this.iconItem = iconItem;
    }

    private FlexCreativeModeTab build()
    {
        return builtTab = new FlexCreativeModeTab(registryName.getNamespace() + "." + registryName.getPath().replace("/", "."), () -> Utils.getItemOrCrash(iconItem));
    }

    public FlexCreativeModeTab get()
    {
        if (builtTab == null)
            return build();
        return builtTab;
    }

    public ResourceLocation getRegistryName()
    {
        return registryName;
    }
}
