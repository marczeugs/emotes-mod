package marczeugs.emotes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Accessor public ResourceManager getResourceContainer();
}