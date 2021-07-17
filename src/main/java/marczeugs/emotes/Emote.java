package marczeugs.emotes;

import net.minecraft.util.Identifier;

public record Emote(
		Identifier identifier,
		int width,
		int height,
		int textureWidth,
		int textureHeight,
		boolean animated,
		int frames,
		int rows,
		int delay
) { }
