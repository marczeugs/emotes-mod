package marczeugs.emotes.mixin;

import marczeugs.emotes.EmotesMod;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ResourceTexture.class)
public class ResourceTextureMixin {
    @Inject(
            method = "load",
            at = @At("RETURN"),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void handleNativeEmoteImage(ResourceManager manager, CallbackInfo ci, @Coerce Object textureData, boolean bl3, boolean bl4, NativeImage nativeImage) {
        if (EmotesMod.captureNextNativeImageAsEmote) {
            EmotesMod.lastEmoteImage = nativeImage;
            EmotesMod.captureNextNativeImageAsEmote = false;
        }
    }
}
