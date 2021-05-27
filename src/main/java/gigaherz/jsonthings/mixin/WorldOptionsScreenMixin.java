package gigaherz.jsonthings.mixin;

import gigaherz.jsonthings.parser.ThingResourceManager;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreateWorldScreen.class)
public class WorldOptionsScreenMixin
{
    @Redirect(method="lambda$func_239048_a_$7(Lnet/minecraft/client/gui/screen/CreateWorldScreen;Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/widget/button/Button;)V",
            at = @At(value="NEW", target="([Lnet/minecraft/resources/IPackFinder;)Lnet/minecraft/resources/ResourcePackList;")
    )
    public ResourcePackList redirectPackListCreation(IPackFinder... finders) {
        ResourcePackList list = new ResourcePackList(finders);
        list.addPackFinder(ThingResourceManager.INSTANCE.getFolderPackFinder());
        return list;
    }
}
